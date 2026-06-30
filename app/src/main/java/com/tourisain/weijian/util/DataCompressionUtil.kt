package com.tourisain.weijian.util

object DataCompressionUtil {
    fun compress(text: String): ByteArray = text.toByteArray(Charsets.UTF_8)
    fun decompress(bytes: ByteArray): String = bytes.toString(Charsets.UTF_8)
}
