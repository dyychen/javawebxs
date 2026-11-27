CREATE DATABASE IF NOT EXISTS canteen DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE canteen;

-- 用户表：学生 / 商家 / 管理员
CREATE TABLE IF NOT EXISTS `user` (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL COMMENT 'student/merchant/admin',
    real_name   VARCHAR(50),
    phone       VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 食堂窗口（含校区与楼层）
CREATE TABLE IF NOT EXISTS canteen_window (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(100) NOT NULL,
    campus         VARCHAR(50)  COMMENT '校区',
    floor          VARCHAR(20)  COMMENT '楼层',
    location       VARCHAR(200),
    merchant_id    INT,
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1营业 0休息',
    business_hours VARCHAR(100),
    FOREIGN KEY (merchant_id) REFERENCES `user` (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 菜品分类
CREATE TABLE IF NOT EXISTS category (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 菜品
CREATE TABLE IF NOT EXISTS dish (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    window_id   INT            NOT NULL,
    category_id INT,
    name        VARCHAR(100)   NOT NULL,
    price       DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    image       VARCHAR(255),
    status      TINYINT        NOT NULL DEFAULT 1 COMMENT '1在售 0下架',
    sales       INT            NOT NULL DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (window_id) REFERENCES canteen_window (id),
    FOREIGN KEY (category_id) REFERENCES category (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 购物车
CREATE TABLE IF NOT EXISTS cart_item (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    user_id  INT NOT NULL,
    dish_id  INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES `user` (id),
    FOREIGN KEY (dish_id) REFERENCES dish (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 订单
CREATE TABLE IF NOT EXISTS `order` (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    order_no    VARCHAR(32)    NOT NULL UNIQUE,
    user_id     INT            NOT NULL,
    window_id   INT            NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING_PAY'
        COMMENT 'PENDING_PAY待支付/PAID待取餐/PICKED已完成/CANCELLED已取消',
    pickup_code VARCHAR(20),
    pickup_time VARCHAR(20)    COMMENT '预约取餐时段，如 12:00-12:15',
    pay_time    DATETIME,
    remark      VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user` (id),
    FOREIGN KEY (window_id) REFERENCES canteen_window (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 订单明细
CREATE TABLE IF NOT EXISTS order_item (
    id        INT PRIMARY KEY AUTO_INCREMENT,
    order_id  INT            NOT NULL,
    dish_id   INT            NOT NULL,
    dish_name VARCHAR(100),
    price     DECIMAL(10, 2),
    quantity  INT,
    FOREIGN KEY (order_id) REFERENCES `order` (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ============ 初始数据 ============

INSERT INTO `user` (username, password, role, real_name, phone) VALUES
('admin',    'admin123', 'admin',    '系统管理员', '13800000001'),
('merchant1', '123456',   'merchant', '张老板',     '13800000002'),
('merchant2', '123456',   'merchant', '李老板',     '13800000003'),
('merchant3', '123456',   'merchant', '王老板',     '13800000004'),
('merchant4', '123456',   'merchant', '赵老板',     '13800000005'),
('merchant5', '123456',   'merchant', '陈老板',     '13800000006'),
('merchant6', '123456',   'merchant', '刘老板',     '13800000007'),
('student1', '123456',   'student',  '王小明',     '13800000008'),
('student2', '123456',   'student',  '赵小红',     '13800000009'),
('student3', '123456',   'student',  '李小刚',     '13800000010');

INSERT INTO canteen_window (name, campus, floor, location, merchant_id, status, business_hours) VALUES
('风味窗口',   '校本部', '一楼', '校本部学生食堂一楼A区', 2, 1, '10:30-13:30'),
('面食窗口',   '校本部', '二楼', '校本部学生食堂二楼B区', 3, 1, '10:30-14:00'),
('综合窗口',   '东校区', '一楼', '东校区食堂一楼C区',     4, 1, '10:00-13:30'),
('麻辣烫窗口', '东校区', '二楼', '东校区食堂二楼D区',     5, 1, '10:30-15:00'),
('清真窗口',   '南校区', '一楼', '南校区食堂一楼E区',     6, 1, '10:30-13:00'),
('轻食窗口',   '南校区', '二楼', '南校区食堂二楼F区',     7, 0, '11:00-14:00');

INSERT INTO category (name) VALUES
('盖浇饭'), ('面食'), ('小吃'), ('饮品'), ('麻辣烫'), ('轻食');

INSERT INTO dish (window_id, category_id, name, price, stock, sales, image) VALUES
-- 校本部一楼 风味窗口
(1, 1, '红烧肉盖浇饭',   15.00, 100, 120, ''),
(1, 1, '鱼香肉丝盖浇饭', 13.50, 80,  90,  ''),
(1, 1, '宫保鸡丁盖浇饭', 14.00, 90,  110, ''),
(1, 3, '香酥鸡腿',        8.00,  60,  200, ''),
(1, 3, '炸鸡翅',          6.00,  70,  150, ''),
(1, 4, '冰镇酸梅汤',      4.00,  120, 80,  ''),
-- 校本部二楼 面食窗口
(2, 2, '牛肉拉面',        14.00, 90,  150, ''),
(2, 2, '炸酱面',          11.00, 70,  60,  ''),
(2, 2, '番茄鸡蛋面',      10.00, 85,  75,  ''),
(2, 3, '卤蛋',            2.00,  200, 300, ''),
(2, 4, '豆浆',            3.00,  150, 95,  ''),
-- 东校区一楼 综合窗口
(3, 1, '咖喱鸡肉饭',      14.50, 80,  130, ''),
(3, 1, '黑椒牛柳饭',      16.00, 75,  100, ''),
(3, 3, '骨肉相连',        5.00,  100, 160, ''),
(3, 4, '柠檬水',          4.50,  110, 60,  ''),
-- 东校区二楼 麻辣烫窗口
(4, 5, '招牌麻辣烫（大）', 18.00, 60,  140, ''),
(4, 5, '招牌麻辣烫（小）', 12.00, 80,  170, ''),
(4, 5, '麻辣拌',          13.50, 65,  90,  ''),
-- 南校区一楼 清真窗口
(5, 1, '大盘鸡盖饭',      17.00, 50,  85,  ''),
(5, 2, '羊肉拉面',        15.00, 60,  70,  ''),
(5, 3, '烤馕',            5.00,  90,  120, ''),
-- 南校区二楼 轻食窗口
(6, 6, '鸡胸肉沙拉',      16.00, 40,  65,  ''),
(6, 6, '全麦三明治',      9.00,  55,  45,  ''),
(6, 4, '鲜榨橙汁',        8.00,  70,  50,  '');
