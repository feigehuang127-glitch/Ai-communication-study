@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ⏳ 清理旧编译文件...
rmdir /s /q out 2>nul
mkdir out

echo 🔨 重新编译...
javac -encoding UTF-8 -cp "lib\*;src" -d out src\youxi\Main.java
if %errorlevel% neq 0 (
    echo ❌ 编译失败
    pause
    exit /b %errorlevel%
)

echo ✅ 编译成功，启动中...
java -cp "lib\*;out" youxi.Main
pause
