import sys
import os
import subprocess
from rich.console import Console
from rich.text import Text
from difflib import unified_diff

# 初始化 rich 控制台
console = Console()

# 默认的 SerDump.jar 路径（相对于脚本运行的当前目录）
DEFAULT_SERDUMP_JAR = os.path.abspath(os.path.join("..", "tools", "SerializationDumper", "SerializationDumper.jar"))

# 检查参数数量是否正确
if len(sys.argv) != 2:
    console.print(
        Text(f"Usage: python script.py <DataSetPath>\nDefault SerDumpJar: {DEFAULT_SERDUMP_JAR}", style="bold red")
    )
    sys.exit(1)

# 从命令行参数中获取 DataSetPath
DataSetPath = sys.argv[1]

def run_java_jar(jar_path, input_file):
    """
    执行 Java JAR 文件并获取输出
    """
    try:
        result = subprocess.run(
            ["java", "-jar", jar_path, "-r", input_file],
            capture_output=True,
            text=True,
            check=True
        )
        return result.stdout
    except subprocess.CalledProcessError as e:
        console.print(Text(f"Error running {jar_path} on {input_file}: {e}", style="bold red"))
        sys.exit(1)

def compare_texts(text1, text2):
    """
    比较两个文本内容并生成差异
    """
    diff = unified_diff(
        text1.splitlines(),
        text2.splitlines(),
        fromfile="poc_result",
        tofile="no_poc_result",
        lineterm=""
    )
    return "\n".join(diff)

# 主逻辑
try:
    # 确保 SerDump.jar 存在
    if not os.path.exists(DEFAULT_SERDUMP_JAR):
        console.print(Text(f"Error: Default SerDumpJar not found at {DEFAULT_SERDUMP_JAR}", style="bold red"))
        sys.exit(1)

    # 获取 poc 和 no-poc 的结果
    poc_result = run_java_jar(DEFAULT_SERDUMP_JAR, f"{DataSetPath}/poc.ser")
    no_poc_result = run_java_jar(DEFAULT_SERDUMP_JAR, f"{DataSetPath}/no-poc.ser")

    # 计算差异
    comparison_result = compare_texts(poc_result, no_poc_result)

    # 美化输出
    console.rule(Text("POC Result", style="bold green"))
    console.print(poc_result)

    console.rule(Text("No POC Result", style="bold green"))
    console.print(no_poc_result)

    console.rule(Text("Comparison Result (Unified Diff)", style="bold magenta"))
    if comparison_result:
        console.print(comparison_result)
    else:
        console.print(Text("No differences found.", style="bold cyan"))

except Exception as e:
    console.print(Text(f"Error: {e}", style="bold red"))
    sys.exit(1)
