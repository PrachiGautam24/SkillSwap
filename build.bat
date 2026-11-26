@echo off
cd /d "%~dp0"
if not exist bin mkdir bin
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/skillswap/Main.java src/com/skillswap/model/*.java src/com/skillswap/util/*.java src/com/skillswap/dao/*.java src/com/skillswap/view/*.java
if %errorlevel% neq 0 exit /b %errorlevel%
java -cp "bin;lib/*" com.skillswap.Main
