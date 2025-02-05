import os
import sys
import subprocess
from rich.console import Console
from rich.text import Text
from pathlib import Path

# 初始化 rich 控制台
console = Console()

# 默认路径设置 - SerdeFuzzer
CURRENT_DIR = Path(__file__).resolve().parent.parent
TOOLS_INSTRUMENT_DIR = os.path.join(CURRENT_DIR, "tools", "instrument")
SerdeFuzzer_JAR = os.path.join(CURRENT_DIR, "target", "SerdeFuzzer-1.0-modified.jar")

# 检查路径是否正确
if not os.path.exists(TOOLS_INSTRUMENT_DIR):
    console.print(Text(f"Error: Instrument directory not found at {TOOLS_INSTRUMENT_DIR}", style="bold red"))
    sys.exit(1)

if not os.path.exists(SerdeFuzzer_JAR):
    console.print(Text(f"Error: SerdeFuzzer JAR not found at {SerdeFuzzer_JAR}", style="bold red"))
    sys.exit(1)

# 默认的运行参数
DEFAULT_DATASET = os.path.join(CURRENT_DIR, "DataSet")
DEFAULT_TARGET_JAR = os.path.join(DEFAULT_DATASET, "demo", "jar", "i-xyz-xzaslxr-1.0.jar")
DEFAULT_OUTPUT_DIR = os.path.join(DEFAULT_DATASET, "demo", "fuzz")
DEFAULT_FAILURE_DIR = os.path.join(DEFAULT_DATASET, "demo", "failures")
DEFAULT_TIMEOUT = "1s"

# java -jar target/SerdeFuzzer-1.0-modified.jar -c DataSet -f DataSet/targets/xyz-xzaslxr-1.0.jar -m chains -r DataSet/failures -o DataSet/fuzz -t 1s
# # 命令行参数解析
if len(sys.argv) <= 1:
    console.print(
        Text(
            "Usage: python script.py <target_jar> <output_dir> <failure_dir> <timeout>\n"
            f"\tDefault target_jar: {DEFAULT_TARGET_JAR}\n"
            f"\tDefault output_dir: {DEFAULT_OUTPUT_DIR}\n"
            f"\tDefault failure_dir: {DEFAULT_FAILURE_DIR}\n"
            f"\tDefault timeout: {DEFAULT_TIMEOUT}\n"
            f"Example usage:\n"
            f"python run.py {DEFAULT_TARGET_JAR} {DEFAULT_OUTPUT_DIR} {DEFAULT_FAILURE_DIR} {DEFAULT_TIMEOUT}",
            style="bold red",
        )
    )
    sys.exit(1)

target_jar = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_TARGET_JAR
output_dir = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_OUTPUT_DIR
failure_dir = sys.argv[3] if len(sys.argv) > 3 else DEFAULT_FAILURE_DIR
timeout = sys.argv[4] if len(sys.argv) > 4 else DEFAULT_TIMEOUT

def run_command(command, description):
    """
    执行命令并打印结果
    """
    try:
        console.print(Text(f"Running: {description}", style="bold cyan"))
        if sys.platform == "win32":
            result = subprocess.run(command, cwd=cwd, shell=True, capture_output=True, text=True, check=True, )
        else:
            result = subprocess.run(command, cwd=cwd, capture_output=True, text=True, check=True, )
        console.print(Text(f"Success: {description}", style="bold green"))
        if result.stdout:
            console.print(result.stdout)
    except subprocess.CalledProcessError as e:
        console.print(Text(f"Error: {description}", style="bold red"))
        console.print(e.stderr)
        sys.exit(1)

# 主逻辑
try:
    # 清理旧文件
    if os.path.exists(DEFAULT_OUTPUT_DIR):
        console.print(Text(f"Cleaning up: {DEFAULT_OUTPUT_DIR}", style="bold yellow"))
        # 删除文件夹及其内容
        for root, dirs, files in os.walk(DEFAULT_OUTPUT_DIR, topdown=False):
            for name in files:
                os.remove(os.path.join(root, name))
            for name in dirs:
                os.rmdir(os.path.join(root, name))
        console.print(Text(f"Removed: {DEFAULT_OUTPUT_DIR}", style="bold yellow"))


    # 执行 Instrument 脚本
    # run_command(
    #     [
    #         "python",
    #         os.path.join(CURRENT_DIR, "scripts", "instrument.py"),
    #         os.path.join(CURRENT_DIR, "DataSet", "targets/xyz-xzaslxr-1.0.jar"),
    #         os.path.join(CURRENT_DIR, "DataSet", "sinks.csv"),
    #         os.path.join(CURRENT_DIR, "DataSet", "targets/i-xyz-xzaslxr-1.0.jar"),
    #          ],
    #     "Running instrumentation script",
    # )

    # 执行 SerdeFuzzer JAR
    run_command(
        [
            "java",
            "-jar",
            SerdeFuzzer_JAR,
            "-c", DEFAULT_DATASET,
            "-f", os.path.abspath(target_jar),
            "-m", "chains",
            "-r", os.path.abspath(failure_dir),
            "-o", os.path.abspath(output_dir),
            "-t", timeout,
        ],
        "Running SerdeFuzzer JAR",
    )

    # 完成
    console.print(Text("All tasks completed successfully!", style="bold green"))

except Exception as e:
    console.print(Text(f"Unexpected error: {e.with_traceback()}", style="bold red"))
    sys.exit(1)
