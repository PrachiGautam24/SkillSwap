@echo off
cd /d "%~dp0"
echo.
echo ========================================
echo   SkillSwap Database Viewer
echo ========================================
echo.
echo Starting H2 Database Console...
echo.
echo Once the browser opens:
echo   JDBC URL: jdbc:h2:~/skillswapdb
echo   User: sa
echo   Password: (leave empty)
echo.
echo Press Ctrl+C to stop the server when done
echo.
java -cp "lib/h2-1.4.200.jar" org.h2.tools.Server -web -webAllowOthers -tcp -tcpAllowOthers -baseDir ~
