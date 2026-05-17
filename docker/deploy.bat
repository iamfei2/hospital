@echo off
chcp 65001 >nul
echo ============================================
echo  医院检查管理系统 - Docker部署
echo ============================================

echo [1/3] 构建并启动所有容器...
cd /d "%~dp0"
docker-compose build --no-cache
if %ERRORLEVEL% NEQ 0 (
    echo 构建失败！
    exit /b 1
)

echo.
echo [2/3] 启动服务...
docker-compose up -d

echo.
echo [3/3] 等待服务就绪（约60秒）...
timeout /t 30 /nobreak >nul

echo.
echo ============================================
echo  部署完成！
echo  前端页面: http://localhost
echo  后端API:  http://localhost:8090
echo  API文档:  http://localhost:8090/swagger-ui.html
echo.
echo  常用命令:
echo    查看日志:  docker-compose logs -f
echo    重启服务:  docker-compose restart
echo    停止服务:  docker-compose down
echo    删除数据:  docker-compose down -v
echo ============================================
pause