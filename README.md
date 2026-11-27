# 校园食堂线上订餐与错峰取餐系统

本项目是《Web 程序设计》课程考核**项目六**的实现，采用前后端分离架构：Spring Boot + MyBatis + MySQL 后端，Vue3 + Vite 前端。前端构建后由 Spring Boot 统一托管，单个 `8080` 端口即可运行。

## 一、设计目的与系统分工

### 设计目的
解决食堂高峰期排队拥挤、窗口信息不透明等问题，让学生可以提前浏览菜品、在线下单并预约取餐时段，到店后凭取餐码核验取餐，减少现场等待时间；同时帮助商家管理菜品与库存、核验订单，帮助管理员掌握整体营业情况。

### 系统分工
| 角色 | 主要职责 |
| --- | --- |
| 学生 | 浏览菜品、搜索筛选、加入购物车、下单支付（模拟）、预约取餐时段、查看订单与取餐码 |
| 食堂商家 | 管理自己的窗口菜品（新增/编辑/上下架/库存）、核验学生取餐码、查看本窗口订单 |
| 管理员 | 数据概览、菜品销量排行、营业统计、用户管理、窗口与分类管理、订单记录导出 |

## 二、系统分析

### 功能需求
1. 多角色登录：学生 / 食堂商家 / 管理员，按角色进入不同首页。
2. 食堂窗口、菜品分类、价格及库存管理。
3. 菜品浏览、购物车、在线下单与支付模拟。
4. 预约取餐时间、订单状态更新、取餐码核验。
5. 订单记录、菜品销量排行、营业数据统计与导出。

### 非功能需求
- 前后端分离，接口统一返回 Result 结构（code/message/data）。
- 下单、扣库存、累计销量在同一事务中完成，失败自动回滚。
- 库存校验：下单时再次校验库存，不足则整单失败。

## 三、系统设计

### 技术选型
- 后端：Java 17、Spring Boot 3、Spring MVC、MyBatis、MySQL 8、Maven。
- 前端：Vue 3、Vite、原生 CSS。
- 部署：前端 vite build 输出到 src/main/resources/static，由后端静态资源映射对外提供。

### 数据库设计
数据库名 canteen，主要表：

| 表名 | 说明 |
| --- | --- |
| user | 用户，含角色 student/merchant/admin |
| canteen_window | 食堂窗口（校区、楼层、位置），关联负责商家 |
| category | 菜品分类 |
| dish | 菜品，含价格、库存、状态、销量 |
| cart_item | 购物车 |
| order | 订单，含金额、状态、取餐码、预约取餐时段 |
| order_item | 订单明细 |

建表与初始数据脚本见 [docs/schema.sql](docs/schema.sql)。

### 接口设计（摘要）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /api/auth/login | 登录 |
| POST | /api/auth/register | 注册学生账号 |
| GET | /api/windows | 窗口列表 |
| GET | /api/categories | 分类列表 |
| GET | /api/dishes | 菜品列表（窗口/分类/关键字筛选） |
| POST | /api/dishes | 新增菜品 |
| PUT | /api/dishes/{id} | 修改菜品 |
| DELETE | /api/dishes/{id} | 删除菜品 |
| POST | /api/dishes/{id}/stock?delta= | 调整库存 |
| GET/POST | /api/cart | 购物车查询 / 加入 |
| PUT/DELETE | /api/cart/{id} | 修改数量 / 删除 |
| POST | /api/orders | 从购物车下单（按窗口拆单） |
| POST | /api/orders/{id}/pay | 模拟支付 |
| POST | /api/orders/{id}/cancel | 取消订单 |
| POST | /api/orders/{id}/verify | 取餐码核验 |
| GET | /api/orders/my?userId= | 学生订单 |
| GET | /api/orders?role=&userId= | 管理端订单 |
| GET | /api/stats/overview | 营业统计 |
| GET | /api/stats/export | 订单 CSV 导出 |

## 四、系统实现

### 项目结构

```
javawebxs/
├── docs/schema.sql              # 数据库脚本
├── src/main/java/com/example/canteen/
│   ├── CanteenApplication.java
│   ├── common/                  # 统一响应、全局异常
│   ├── config/                  # 跨域配置
│   ├── entity/                  # 实体类
│   ├── mapper/                  # MyBatis 接口
│   ├── service/                 # 业务层
│   └── controller/              # 控制层
├── src/main/resources/
│   ├── application.properties
│   ├── mapper/*.xml
│   └── static/                  # 前端构建产物
└── frontend/                    # Vue3 源码
```

### 关键实现
- 分层结构：Controller 只负责接收请求与组织响应；Service 处理业务规则与事务；Mapper 负责持久化，符合表示层 / 业务逻辑层 / 数据持久层的三层划分。
- 下单事务：OrderService.createFromCart 使用 @Transactional，按窗口拆分订单，逐个校验库存后扣减并累计销量，最后清空购物车；任何一步失败全部回滚。
- 取餐码核验：订单支付后状态为 PAID，商家输入学生提供的取餐码，匹配成功才置为 PICKED，避免误取。
- 库存安全：下单时以数据库当前库存为准二次校验，避免超卖。
- 统计导出：GET /api/stats/overview 汇总订单数、营业收入、各状态数量与菜品销量排行；GET /api/stats/export 返回 CSV。
- 会话与权限：登录成功后用户信息写入 HttpSession（JSESSIONID Cookie）；未登录统一返回 401，越权访问返回 403。窗口/分类维护限管理员，菜品维护限归属商家，订单操作限订单本人、归属商家或管理员，核销取餐码限商家/管理员；商家只能查看和操作自己窗口的数据。
- 部署友好：数据库地址、账号、密码和端口都支持环境变量覆盖（DB_URL、DB_USERNAME、DB_PASSWORD、SERVER_PORT），换机器无需改代码。

## 五、总结
系统完整覆盖了项目六要求的五类核心功能，结构清晰、可直接运行。通过本次设计，巩固了 Spring MVC 请求映射与参数接收、MyBatis 动态 SQL 与事务、Vue 响应式数据与组件化开发等知识点，也实践了前后端分离项目的接口约定与联调过程。

## 快速开始

1. 初始化数据库（需先安装 MySQL）：

```bash
mysql -uroot -p < docs/schema.sql
```

2. 修改 src/main/resources/application.properties 中的数据库连接。
3. 启动后端：

```bash
mvn spring-boot:run
```

4. 浏览器访问 http://localhost:8080。

也可以不改配置文件，直接用环境变量启动（推荐给他人部署时使用）：

```bash
export DB_URL="jdbc:mysql://localhost:3306/canteen?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
export DB_USERNAME="root"
export DB_PASSWORD="你的密码"
export SERVER_PORT=8080
mvn spring-boot:run
```

打包并安装到本地 Maven 仓库（供其他项目以依赖方式引用）：

```bash
mvn clean install -DskipTests
```

## 部署

### 方式一：可执行 JAR（推荐，内嵌 Tomcat）
```bash
mvn clean package -DskipTests
java -jar target/canteen.jar
# 或指定环境变量
DB_URL="jdbc:mysql://ip:3306/canteen?..." DB_USERNAME=root DB_PASSWORD=xxx SERVER_PORT=8080 java -jar target/canteen.jar
```
启动后访问 http://localhost:8080。

服务器后台运行示例：
```bash
nohup java -jar canteen.jar > canteen.log 2>&1 &
```

### 方式二：IDEA 中运行 / 打包
- 直接运行 `CanteenApplication.main()`；
- 或 Maven 面板执行 `package`，在 `target/canteen.jar` 得到可部署产物。

### 方式三：外部 Tomcat（WAR，可选）
本项目默认打成 JAR、使用内嵌 Tomcat。如课程要求部署到外部 Tomcat：
1. `pom.xml` 中把 `<packaging>jar</packaging>` 改为 `war`；
2. 启动类继承 `SpringBootServletInitializer` 并重写 `configure`；
3. `mvn clean package` 生成 WAR，放入 Tomcat 的 `webapps` 目录启动。

### 云服务器部署要点
1. 服务器安装 JDK 17 与 MySQL，导入 `docs/schema.sql`；
2. 用环境变量注入数据库连接与端口（不要把密码写进仓库）；
3. 安全组/防火墙放行项目端口（默认 8080）；
4. 建议前置 Nginx 做反向代理与静态资源缓存；
5. 用 `nohup` 或 systemd 保证进程常驻。

演示账号：

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 学生 | student1 | 123456 |
| 商家 | merchant1 | 123456 |
| 管理员 | admin | admin123 |

前端开发调试（可选）：

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器地址 http://localhost:5175，/api 会代理到 8080。
