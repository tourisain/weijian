# 微简激活码工具

推荐使用 PowerShell 入口：

```powershell
.\tools\generate_activation_code.ps1
```

该脚本只生成新版加密 `WJ5` 激活码。App 端不接受旧版 `WJ3/WJ4` 激活码。

## 常用命令

生成终身会员激活码并显示完整信息：

```powershell
.\tools\generate_activation_code.ps1 -DeviceCode WJDC-ABCD-EF12-3456-7890-ABCD-EF12 -Level lifetime -Details
```

从剪贴板读取设备码，生成后复制激活码：

```powershell
.\tools\generate_activation_code.ps1 -FromClipboard -Level lifetime -Copy
```

批量读取设备码文件并保存为 CSV：

```powershell
.\tools\generate_activation_code.ps1 -DeviceCodeFile .\devices.txt -Level lifetime -Count 2 -Save -Format csv
```

自动保存为 JSON 并打开输出文件夹：

```powershell
.\tools\generate_activation_code.ps1 -DeviceCode WJDC-ABCD-EF12-3456-7890-ABCD-EF12 -Level lifetime -Save -Format json -OpenOutput
```

交互式生成：

```powershell
.\tools\generate_activation_code.ps1 -Interactive
```

工具自检：

```powershell
.\tools\generate_activation_code.ps1 -SelfTest
```

查看示例：

```powershell
.\tools\generate_activation_code.ps1 -Examples
```

自动化脚本中不暂停退出：

```powershell
.\tools\generate_activation_code.ps1 -DeviceCode WJDC-ABCD-EF12-3456-7890-ABCD-EF12 -Level lifetime -NoPause
```

## 参数说明

- `-DeviceCode`：会员中心生成的 `WJDC-...` 设备码。
- `-DeviceCodeFile`：批量设备码文件，一行一个设备码，空行和 `#` 开头的行会被忽略。
- `-FromClipboard`：从剪贴板读取设备码。
- `-Level lifetime`：终身会员；该参数可省略，工具只生成终身会员激活码。
- `-Count`：每个设备码生成的激活码数量。
- `-Format text|json|csv`：保存格式，默认 `text`。
- `-Save`：自动保存到 `tools/activation_exports`。
- `-Output`：指定输出文件路径。
- `-Copy`：将生成的激活码复制到剪贴板。
- `-OpenOutput`：保存后打开输出文件夹。
- `-Details`：在终端显示协议、等级、设备码、到期时间、摘要和激活码。
- `-Quiet`：减少终端输出，适合脚本调用。
- `-NoPause`：生成后不等待回车，适合自动化脚本调用。

底层加密实现仍在 `tools/generate_activation_code.py` 中，PowerShell 脚本负责更方便的终端交互、批量、保存和剪贴板流程。
