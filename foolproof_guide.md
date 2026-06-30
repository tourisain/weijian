# 微简应用服务器搭建傻瓜式指南

## 前言

这份指南是为没有技术背景的用户准备的，详细说明了如何从零开始搭建微简应用的服务器环境。按照本指南的步骤操作，即使您没有服务器管理经验，也能成功搭建起微简应用的服务器。

## 一、准备工作

### 1.1 购买服务器

首先，您需要购买一台云服务器。推荐使用以下平台：

- **阿里云**：https://www.aliyun.com
- **腾讯云**：https://cloud.tencent.com
- **华为云**：https://www.huaweicloud.com

**购买建议**：
- 操作系统选择：Ubuntu 20.04 LTS
- 配置选择：至少 2 核 CPU、4GB 内存、50GB 存储空间
- 带宽：至少 1Mbps

### 1.2 连接服务器

购买服务器后，您需要通过 SSH 工具连接到服务器。

**Windows 用户**：
1. 下载并安装 PuTTY：https://www.putty.org/
2. 打开 PuTTY，输入服务器的公网 IP 地址
3. 端口保持默认的 22
4. 点击 "Open" 按钮
5. 输入用户名（通常是 root）
6. 输入密码（购买服务器时设置的密码）

**Mac/Linux 用户**：
1. 打开终端
2. 输入命令：`ssh root@服务器公网IP`
3. 输入密码

## 二、服务器环境搭建

### 2.1 更新系统

连接到服务器后，首先更新系统：

```bash
# 更新系统软件包
sudo apt update && sudo apt upgrade -y
```

### 2.2 安装必要的软件

```bash
# 安装 Node.js 16
curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
sudo apt-get install -y nodejs

# 安装 MySQL 数据库
sudo apt-get install -y mysql-server

# 安装 Redis 缓存服务
sudo apt-get install -y redis-server

# 安装 Nginx  web 服务器
sudo apt-get install -y nginx

# 安装 Git 版本控制工具
sudo apt-get install -y git

# 安装 unzip 解压工具
sudo apt-get install -y unzip
```

### 2.3 配置 MySQL 数据库

1. **启动 MySQL 服务**：
   ```bash
   sudo systemctl start mysql
   sudo systemctl enable mysql
   ```

2. **配置 MySQL 安全设置**：
   ```bash
   sudo mysql_secure_installation
   ```
   按照提示操作：
   - 输入当前 root 密码（如果没有设置，直接按回车）
   - 选择是否设置 root 密码（输入 Y）
   - 输入新密码并确认
   - 选择是否移除匿名用户（输入 Y）
   - 选择是否禁止 root 远程登录（输入 Y）
   - 选择是否删除 test 数据库（输入 Y）
   - 选择是否重新加载权限表（输入 Y）

3. **创建数据库和用户**：
   ```bash
   sudo mysql -u root -p
   ```
   输入刚才设置的 root 密码，然后执行以下命令：
   ```sql
   CREATE DATABASE memo_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'memo_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON memo_app.* TO 'memo_user'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```
   **注意**：将 `your_password` 替换为您自己的密码

4. **创建数据库表**：
   ```bash
   sudo mysql -u memo_user -p memo_app
   ```
   输入刚才设置的密码，然后执行以下 SQL 语句：
   ```sql
   -- 创建 users 表
   CREATE TABLE users (
       id VARCHAR(36) PRIMARY KEY,
       username VARCHAR(50) UNIQUE NOT NULL,
       password_hash VARCHAR(255) NOT NULL,
       is_pro BOOLEAN DEFAULT FALSE,
       pro_expire_date BIGINT,
       membership_level INTEGER DEFAULT 0,
       device_id VARCHAR(255),
       avatar_uri VARCHAR(255),
       created_at BIGINT NOT NULL
   );

   -- 创建 diaries 表
   CREATE TABLE diaries (
       id VARCHAR(36) PRIMARY KEY,
       user_id VARCHAR(36) NOT NULL,
       content TEXT NOT NULL,
       mood INTEGER NOT NULL,
       weather INTEGER NOT NULL,
       images TEXT DEFAULT '[]',
       videos TEXT DEFAULT '[]',
       audios TEXT DEFAULT '[]',
       documents TEXT DEFAULT '[]',
       location VARCHAR(255),
       tags TEXT DEFAULT '[]',
       template_id VARCHAR(36),
       created_at BIGINT NOT NULL,
       reminder_time BIGINT,
       is_deleted BOOLEAN DEFAULT FALSE,
       deleted_at BIGINT,
       FOREIGN KEY (user_id) REFERENCES users(id)
   );

   -- 创建 notes 表
   CREATE TABLE notes (
       id VARCHAR(36) PRIMARY KEY,
       user_id VARCHAR(36) NOT NULL,
       content TEXT NOT NULL,
       color VARCHAR(20) NOT NULL,
       is_pinned BOOLEAN DEFAULT FALSE,
       skin_id VARCHAR(50) DEFAULT 'classic',
       created_at BIGINT NOT NULL,
       format VARCHAR(20) DEFAULT 'plain',
       tags VARCHAR(255),
       type VARCHAR(20) DEFAULT 'text',
       attachments VARCHAR(255),
       FOREIGN KEY (user_id) REFERENCES users(id)
   );

   EXIT;
   ```

### 2.4 部署后端应用

1. **创建应用目录**：
   ```bash
   sudo mkdir -p /var/www/memo-app/backend
   sudo chown -R $USER:$USER /var/www/memo-app
   ```

2. **下载后端代码**：
   ```bash
   cd /var/www/memo-app/backend
   # 假设您已经有后端代码，或者从代码仓库克隆
   git clone https://github.com/yourusername/memo-app-backend.git .
   ```

3. **安装依赖**：
   ```bash
   npm install
   ```

4. **配置环境变量**：
   ```bash
   cp .env.example .env
   nano .env
   ```
   在打开的文件中，修改以下配置：
   ```env
   # 数据库配置
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=memo_app
   DB_USER=memo_user
   DB_PASSWORD=your_password

   # JWT配置
   JWT_SECRET=your_jwt_secret_key
   JWT_EXPIRES_IN=7d
   ```
   **注意**：将 `your_password` 替换为您在步骤 2.3 中设置的密码，将 `your_jwt_secret_key` 替换为一个随机的字符串

5. **启动应用**：
   ```bash
   # 安装 PM2 进程管理器
   npm install -g pm2
   
   # 启动应用
   pm2 start index.js --name memo-app
   
   # 设置 PM2 开机自启
   pm2 startup
   pm2 save
   ```

### 2.5 配置 Nginx

1. **创建 Nginx 配置文件**：
   ```bash
   sudo nano /etc/nginx/sites-available/memo-app
   ```

2. **添加以下配置**：
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;

       location / {
           proxy_pass http://localhost:3000;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
       }
   }
   ```
   **注意**：将 `your-domain.com` 替换为您的域名，如果没有域名，可以使用服务器的公网 IP

3. **启用站点**：
   ```bash
   sudo ln -s /etc/nginx/sites-available/memo-app /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```

## 三、应用配置

### 3.1 配置前端应用

1. **下载前端代码**：
   ```bash
   cd /var/www/memo-app
   git clone https://github.com/yourusername/memo-app-frontend.git frontend
   ```

2. **配置 API 地址**：
   ```bash
   cd frontend
   nano src/config/api.js
   ```
   修改 API 地址为您的服务器地址：
   ```javascript
   export const API_BASE_URL = 'http://your-domain.com/api';
   ```

3. **构建前端应用**：
   ```bash
   npm install
   npm run build
   ```

4. **配置 Nginx 服务前端**：
   ```bash
   sudo nano /etc/nginx/sites-available/memo-app
   ```
   修改配置文件：
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;

       location / {
           root /var/www/memo-app/frontend/build;
           index index.html;
           try_files $uri $uri/ /index.html;
       }

       location /api {
           proxy_pass http://localhost:3000;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
       }
   }
   ```

5. **重启 Nginx**：
   ```bash
   sudo systemctl restart nginx
   ```

### 3.2 测试应用

1. **打开浏览器**，访问您的域名或服务器 IP
2. **注册一个新账号**
3. **登录应用**
4. **测试功能**：
   - 创建日记
   - 创建便签
   - 测试数据同步

## 四、维护和管理

### 4.1 查看应用状态

```bash
# 查看 PM2 进程状态
pm status

# 查看 Nginx 状态
sudo systemctl status nginx

# 查看 MySQL 状态
sudo systemctl status mysql
```

### 4.2 查看日志

```bash
# 查看应用日志
pm logs memo-app

# 查看 Nginx 日志
sudo tail -f /var/log/nginx/error.log

# 查看 MySQL 日志
sudo tail -f /var/log/mysql/error.log
```

### 4.3 备份数据库

```bash
# 创建数据库备份
sudo mysqldump -u memo_user -p memo_app > /var/www/memo-app/backup/memo_app_$(date +%Y%m%d).sql

# 输入密码
```

### 4.4 更新应用

```bash
# 更新后端代码
cd /var/www/memo-app/backend
git pull
npm install
pm restart

# 更新前端代码
cd /var/www/memo-app/frontend
git pull
npm install
npm run build
```

## 五、常见问题解决

### 5.1 无法连接到服务器

- **检查服务器状态**：登录云服务提供商的控制台，查看服务器是否正常运行
- **检查网络连接**：确保您的网络连接正常
- **检查防火墙**：确保服务器的 22 端口（SSH）是开放的

### 5.2 数据库连接失败

- **检查 MySQL 服务**：`sudo systemctl status mysql`
- **检查数据库配置**：确保 `.env` 文件中的数据库配置正确
- **检查数据库用户权限**：确保 `memo_user` 用户有正确的权限

### 5.3 应用无法启动

- **检查 Node.js 版本**：`node -v`
- **检查依赖**：确保所有依赖都已安装
- **检查端口**：确保 3000 端口没有被占用
- **查看应用日志**：`pm2 logs memo-app`

### 5.4 前端无法访问

- **检查 Nginx 服务**：`sudo systemctl status nginx`
- **检查 Nginx 配置**：`sudo nginx -t`
- **检查前端文件**：确保前端代码已正确构建
- **查看 Nginx 日志**：`sudo tail -f /var/log/nginx/error.log`

## 六、总结

按照本指南的步骤操作，您应该已经成功搭建了微简应用的服务器环境。如果您遇到任何问题，可以参考常见问题解决部分，或者联系技术支持。

祝您使用愉快！