#!/bin/bash
set -e

echo "============================================"
echo " 医院检查管理系统 - Docker部署"
echo "============================================"

cd "$(dirname "$0")"

echo "[1/3] 构建并启动所有容器..."
docker-compose build --no-cache

echo ""
echo "[2/3] 启动服务..."
docker-compose up -d

echo ""
echo "[3/3] 等待服务就绪..."
sleep 30

echo ""
echo "============================================"
echo " 部署完成！"
echo " 前端页面: http://localhost"
echo " 后端API:  http://localhost:8090"
echo " API文档:  http://localhost:8090/swagger-ui.html"
echo ""
echo " 常用命令:"
echo "   查看日志:  docker-compose logs -f"
echo "   重启服务:  docker-compose restart"
echo "   停止服务:  docker-compose down"
echo "   删除数据:  docker-compose down -v"
echo "============================================"