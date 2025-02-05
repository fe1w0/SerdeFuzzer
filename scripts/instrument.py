import os
import sys
import subprocess
from rich.console import Console
from rich.text import Text
from pathlib import Path


# 初始化 rich 控制台
console = Console()

# 默认路径设置
CURRENT_DIR = Path(__file__).resolve().parent.parent
INSTRUMENT_DIR = os.path.abspath(os.path.join(CURRENT_DIR, "tools", "instrument"))

# 检查路径是否正确
if not os.path.exists(INSTRUMENT_DIR):
    console.print(Text(f"Error: Instrument directory not found at {INSTRUMENT_DIR}", style="bold red"))
    sys.exit(1)

# 命令行参数解析
if len(sys.argv) != 4:
    console.print(
        Text(
            "Usage: python instrument.py <input_jar> <sinks_csv> <output_jar>\n"
            "Example: python instrument.py DataSet/demo/jar/xyz-xzaslxr-1.0.jar DataSet/demo/conf/sinks.csv DataSet/demo/jar/i-xyz-xzaslxr-1.0.jar",
            style="bold red",
        )
    )
    sys.exit(1)

input_jar = sys.argv[1]
sinks_csv = sys.argv[2]
output_jar = sys.argv[3]

def run_command(command, description, cwd):
    """
    执行命令并打印结果
    """
    try:
        # command 换成一行
        console.print(Text(f"Running: {description}, Command: {" ".join(command)}, Path:{cwd}", style="bold cyan"))
        if sys.platform == "win32":
            result = subprocess.run(command, cwd=cwd, shell=True, capture_output=True, text=True, check=True, )
        else:
            result = subprocess.run(command, cwd=cwd, capture_output=True, text=True, check=True, )
        console.print(Text(f"Success: {description}", style="bold green"))
        if result.stdout:
            console.print(result.stdout)
    except subprocess.CalledProcessError as e:
        console.print(Text(f"Error: {e}", style="bold red"))
        console.print(e.stderr)
        sys.exit(1)

# 脚本主逻辑
try:
    # Maven 打包
    # run_command(["mvn", "clean", "package", "-DskipTests=true"], "Maven clean and package", INSTRUMENT_DIR)

    # Java 执行 instrument-1.0.jar
    jar_path = os.path.join(INSTRUMENT_DIR, "target", "instrument-1.0.jar")
    if not os.path.exists(jar_path):
        console.print(Text(f"Error: JAR not found at {jar_path}", style="bold red"))
        sys.exit(1)

    run_command(
        [
            "java",
            "-jar",
            jar_path,
            "-i", os.path.abspath(os.path.join(CURRENT_DIR, input_jar)),
            "-s", os.path.abspath(os.path.join(CURRENT_DIR, sinks_csv)),
            "-o", os.path.abspath(os.path.join(CURRENT_DIR, output_jar)),
            "-l", "log.log",
        ],
        "Java execution with instrument-1.0.jar",
        INSTRUMENT_DIR,
    )

    # 完成
    console.print(Text("All tasks completed successfully!", style="bold green"))

except Exception as e:
    console.print(Text(f"Unexpected error: {e.with_traceback()}", style="bold red"))
    sys.exit(1)
