@echo off
chcp 65001 >nul 2>&1

echo ================================
echo   Setting Proxy + Launch Claude
echo ================================
echo.

set http_proxy=http://127.0.0.1:7890
set https_proxy=http://127.0.0.1:7890

echo [OK] http_proxy  = %http_proxy%
echo [OK] https_proxy = %https_proxy%
echo.
echo Starting Claude Code ...
echo.

claude

pause
