-- =====================================================================
-- RPFSA 数据库升级脚本 v2（功能扩展）
-- 在 rpfsa.sql / rpfsa_upgrade.sql 之后执行；全部语句幂等，可重复执行。
--
-- 新增能力：
--   1. favorite     收藏（用户 × 宠物）
--   2. pet_photo    宠物相册（多图）
--   3. recharge     宠宠币充值记录
--   4. sign_in      每日签到
--   5. post_like    帖子点赞
--   6. verify_code  找回密码验证码
--   7. post.like_count 帖子点赞数
-- =====================================================================

USE rpfsa;

SET NAMES utf8mb4;

-- ----------------------------
-- 1. 收藏表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `favorite`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表id',
  `uid` int NOT NULL COMMENT '用户id',
  `pid` int NOT NULL COMMENT '宠物id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_uid_pid` (`uid`, `pid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 2. 宠物相册表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `pet_photo`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表id',
  `pid` int NOT NULL COMMENT '宠物id',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片文件名',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_pid` (`pid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 充值记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `recharge`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表id',
  `uid` int NOT NULL COMMENT '用户id',
  `amount` decimal(10, 2) NOT NULL COMMENT '充值金额（元）',
  `coin` int NOT NULL COMMENT '到账宠宠币',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_uid` (`uid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 4. 每日签到表（uid + sign_date 唯一约束保证一天只能签一次）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sign_in`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表id',
  `uid` int NOT NULL COMMENT '用户id',
  `sign_date` date NOT NULL COMMENT '签到日期',
  `coin` int NOT NULL COMMENT '本次奖励宠宠币',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_uid_date` (`uid`, `sign_date`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5. 帖子点赞表（uid + poid 唯一约束防止重复点赞）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `post_like`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表id',
  `uid` int NOT NULL COMMENT '点赞用户id',
  `poid` int NOT NULL COMMENT '帖子id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_uid_poid` (`uid`, `poid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 6. 找回密码验证码表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `verify_code`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '表id',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `used` int NOT NULL DEFAULT 0 COMMENT '是否已使用 0未使用 1已使用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- 7. post 表新增点赞数（information_schema 判断，保证幂等）
-- ----------------------------
SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'rpfsa' AND TABLE_NAME = 'post' AND COLUMN_NAME = 'like_count'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `post` ADD COLUMN `like_count` int NOT NULL DEFAULT 0 COMMENT ''点赞数''',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
