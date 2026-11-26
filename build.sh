#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p bin
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/skillswap/Main.java src/com/skillswap/model/*.java src/com/skillswap/util/*.java src/com/skillswap/dao/*.java src/com/skillswap/view/*.java
java -cp "bin:lib/*" com.skillswap.Main
