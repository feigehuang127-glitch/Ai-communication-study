@echo off
chcp 65001 >nul
cd /d "%~dp0"

:: 编译（如有新文件）
if not exist "out\youxi\Main.class" (
    echo ⏳ 首次编译中...
    javac -encoding UTF-8 -cp "lib\*;src" -d out src\youxi\Main.java
    if %errorlevel% neq 0 (
        echo ❌ 编译失败，请检查错误信息
        pause
        exit /b %errorlevel%
    )
)

:: 启动
echo 🚀 启动知识竞答游戏...
java -cp "lib\*;out" youxi.Main
pause
