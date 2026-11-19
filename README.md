# library

图书借阅管理系统（D1 骨架）

## 环境要求

- JDK 17
- Maven 3.6+
- MySQL 8

## 启动前

1. 建库：`CREATE DATABASE library DEFAULT CHARACTER SET utf8mb4;`
2. 建表：见 `docs/schema.sql`（或手工执行 DDL）
3. 改 `src/main/resources/application.yml` 里的 `password`

## 启动

用 IDEA 打开本文件夹，运行 `LibraryApplication`。

或者命令行：

```bash
mvn spring-boot:run
```

## 验证

浏览器访问 http://localhost:8080/hello

控制台应出现：

```
Tomcat started on port 8080
HikariPool-1 - Start completed.
```

## 技术栈

Spring Boot 3.3.5 / Java 17 / MySQL 8 / Maven
