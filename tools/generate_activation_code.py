#!/usr/bin/env python3
import argparse
import base64
import csv
import hashlib
import hmac
import json
import re
import secrets
import sys
from dataclasses import asdict, dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Iterable, Optional

PREFIX_V5 = "WJ5"
NEVER_EXPIRES = "NEVER"
V5_NONCE_BYTES = 12
V5_TAG_BYTES = 16
V5_SECRET_PARTS = ("wj5", ".tourisain", ".local", ".activation", ".secure", ".v2", ".2026")


@dataclass(frozen=True)
class ActivationCodeRecord:
    protocol: str
    level: str
    level_code: int
    device_code: str
    expires: str
    issued_at: str
    nonce: str
    activation_code: str
    code_sha256: str


def sha256(value: str) -> str:
    return hashlib.sha256(value.encode("utf-8")).hexdigest().upper()


def v5_secret() -> bytes:
    return "".join(V5_SECRET_PARTS).encode("utf-8")


def hmac_bytes(secret: bytes, value: bytes) -> bytes:
    return hmac.new(secret, value, hashlib.sha256).digest()


def v5_key(label: str) -> bytes:
    return hmac_bytes(v5_secret(), label.encode("utf-8"))


def normalize_device_code(value: str) -> str:
    upper = value.strip().upper()
    if "WJDC" in upper:
        upper = upper[upper.index("WJDC") + 4 :]
    token = re.sub(r"[^A-F0-9]", "", upper)
    if len(token) != 24:
        raise SystemExit("device-code must contain the WJDC code shown in the app, for example WJDC-ABCD-1234-5678-90AB-CDEF.")
    return token


def normalize_nonce(value: Optional[str]) -> str:
    nonce = (value or secrets.token_hex(5)).upper()
    if not re.fullmatch(r"[A-Z0-9]{6,24}", nonce):
        raise SystemExit("nonce must be 6-24 A-Z/0-9 characters.")
    return nonce


def resolve_expiry() -> str:
    return NEVER_EXPIRES


def issued_at_utc() -> str:
    return datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")


def xor_with_keystream(data: bytes, nonce: bytes) -> bytes:
    key = v5_key("enc")
    output = bytearray(len(data))
    offset = 0
    counter = 0
    while offset < len(data):
        block = hmac_bytes(key, nonce + counter.to_bytes(4, "big"))
        for item in block:
            if offset >= len(data):
                break
            output[offset] = data[offset] ^ item
            offset += 1
        counter += 1
    return bytes(output)


def b32_encode(data: bytes) -> str:
    return base64.b32encode(data).decode("ascii").rstrip("=")


def b32_decode(value: str) -> bytes:
    padding = "=" * ((8 - len(value) % 8) % 8)
    return base64.b32decode(value + padding, casefold=False)


def chunk(value: str, size: int = 5) -> str:
    return "-".join(value[index : index + size] for index in range(0, len(value), size))


def build_code_v5(level: int, device_code: str, expires: str, nonce_label: str, issued_at: str) -> str:
    payload = "|".join(["5", f"L{level}", expires, device_code, issued_at, nonce_label]).encode("utf-8")
    nonce = secrets.token_bytes(V5_NONCE_BYTES)
    cipher_text = xor_with_keystream(payload, nonce)
    tag = hmac_bytes(v5_key("tag"), PREFIX_V5.encode("utf-8") + nonce + cipher_text)[:V5_TAG_BYTES]
    return f"{PREFIX_V5}-{chunk(b32_encode(nonce + cipher_text + tag))}"


def decode_code_v5(code: str) -> dict:
    token = "".join(code.strip().upper().split("-")[1:])
    raw = b32_decode(token)
    if len(raw) <= V5_NONCE_BYTES + V5_TAG_BYTES:
        raise ValueError("WJ5 code is too short.")
    nonce = raw[:V5_NONCE_BYTES]
    tag = raw[-V5_TAG_BYTES:]
    cipher_text = raw[V5_NONCE_BYTES:-V5_TAG_BYTES]
    expected_tag = hmac_bytes(v5_key("tag"), PREFIX_V5.encode("utf-8") + nonce + cipher_text)[:V5_TAG_BYTES]
    if not hmac.compare_digest(tag, expected_tag):
        raise ValueError("WJ5 tag check failed.")
    fields = xor_with_keystream(cipher_text, nonce).decode("utf-8").split("|")
    if len(fields) != 6 or fields[0] != "5":
        raise ValueError("WJ5 payload is invalid.")
    return {
        "version": fields[0],
        "level": fields[1],
        "expires": fields[2],
        "device_code": fields[3],
        "issued_at": fields[4],
        "nonce": fields[5],
    }


def build_record(device_code: str, expires: str, nonce: Optional[str]) -> ActivationCodeRecord:
    level = 2
    issued_at = issued_at_utc()
    nonce_label = normalize_nonce(nonce)
    code = build_code_v5(level, device_code, expires, nonce_label, issued_at)
    decoded = decode_code_v5(code)
    if decoded["device_code"] != device_code or decoded["expires"] != expires:
        raise RuntimeError("WJ5 self verification failed.")
    return ActivationCodeRecord(
        protocol=PREFIX_V5,
        level="lifetime",
        level_code=level,
        device_code=device_code,
        expires=expires,
        issued_at=issued_at,
        nonce=nonce_label,
        activation_code=code,
        code_sha256=sha256(code),
    )


def write_records(records: list[ActivationCodeRecord], output: Optional[str], output_format: str) -> None:
    if output_format == "json":
        content = json.dumps([asdict(record) for record in records], ensure_ascii=False, indent=2)
        if output:
            Path(output).write_text(content + "\n", encoding="utf-8")
        else:
            print(content)
        return

    if output_format == "csv":
        fieldnames = list(asdict(records[0]).keys())
        if output:
            with Path(output).open("w", newline="", encoding="utf-8") as handle:
                writer = csv.DictWriter(handle, fieldnames=fieldnames)
                writer.writeheader()
                writer.writerows(asdict(record) for record in records)
        else:
            writer = csv.DictWriter(sys.stdout, fieldnames=fieldnames)
            writer.writeheader()
            writer.writerows(asdict(record) for record in records)
        return

    lines = [record.activation_code for record in records]
    if output:
        Path(output).write_text("\n".join(lines) + "\n", encoding="utf-8")
    else:
        print("\n".join(lines))


def print_details(records: Iterable[ActivationCodeRecord]) -> None:
    for index, record in enumerate(records, start=1):
        if index > 1:
            print()
        print(f"Protocol: {record.protocol}")
        print(f"Level: {record.level}")
        print(f"Device: {record.device_code}")
        print(f"Expires: {record.expires}")
        print(f"Issued at: {record.issued_at}")
        print(f"Nonce: {record.nonce}")
        print(f"Code SHA-256: {record.code_sha256}")
        print(f"Activation code: {record.activation_code}")


def run_self_test() -> None:
    device_code = "ABCDEF1234567890ABCDEF12"
    expires = resolve_expiry()
    record = build_record(device_code, expires, "TEST01")
    decoded = decode_code_v5(record.activation_code)
    assert decoded["device_code"] == device_code
    assert decoded["expires"] == expires
    assert decoded["level"] == "L2"
    print("Self-test passed.")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Generate encrypted local Weijian activation codes.",
        epilog=(
            "Example: python tools/generate_activation_code.py "
            "--device-code WJDC-ABCD-EF12-3456-7890-ABCD-EF12 --level lifetime --details"
        ),
    )
    parser.add_argument("--device-code", help="WJDC device code generated by the app after the user enters username and email.")
    parser.add_argument("--level", choices=["lifetime"], default="lifetime", help="Only lifetime membership is supported.")
    parser.add_argument("--nonce", help="Optional 6-24 character A-Z/0-9 label. Random by default.")
    parser.add_argument("--count", type=int, default=1, help="Generate multiple codes for the same device and plan.")
    parser.add_argument("--output", help="Write activation code records to a file.")
    parser.add_argument("--format", choices=["text", "json", "csv"], default="text", help="Output format.")
    parser.add_argument("--details", action="store_true", help="Print stable details with each activation code.")
    parser.add_argument("--self-test", action="store_true", help="Run generator encryption/decryption self-test and exit.")
    args = parser.parse_args()

    if args.self_test:
        run_self_test()
        return

    raw_device_code = args.device_code or input("Paste WJDC device code: ").strip()
    device_code = normalize_device_code(raw_device_code)
    expires = resolve_expiry()
    count = max(args.count, 1)

    records = [
        build_record(
            device_code=device_code,
            expires=expires,
            nonce=args.nonce if count == 1 else None,
        )
        for _ in range(count)
    ]

    if args.details:
        print_details(records)
    else:
        write_records(records, args.output, args.format)
        if args.output:
            print(f"Wrote {len(records)} activation code(s) to {args.output}")


if __name__ == "__main__":
    main()
