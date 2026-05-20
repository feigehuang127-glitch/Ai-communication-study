#!/bin/bash
cd "$(dirname "$0")"

# 编译
if [ ! -f "out/youxi/Main.class" ]; then
    echo "⏳ 首次编译中..."
    javac -encoding UTF-8 -cp "lib/*:src" -d out src/youxi/Main.java
    if [ $? -ne 0 ]; then
        echo "❌ 编译失败"
        exit 1
    fi
fi

# 启动
echo "🚀 启动知识竞答游戏..."
java -cp "lib/*:out" youxi.Main
