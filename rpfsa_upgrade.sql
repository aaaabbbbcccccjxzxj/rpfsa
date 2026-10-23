-- ============================================================================
-- RPFSA 改造升级脚本
-- 适用场景：已经导入过旧版 rpfsa.sql 的数据库。
-- 全新部署直接执行 rpfsa.sql 即可（其中已包含下列改动，无需再跑本脚本）。
-- 本脚本可重复执行：ALTER 语句在 MySQL 8 中对已存在的列/索引会给出提示但不影响数据。
-- ============================================================================

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- 1. 密码列扩容
-- BCrypt 密文固定 60 字符，原 varchar(20) 装不下。
-- 扩容后无需重置任何人的密码：历史明文会在该账号首次登录成功时
-- 由 PasswordUtil 自动改写为 BCrypt 密文（无感迁移）。
-- ----------------------------------------------------------------------------
ALTER TABLE `user`  MODIFY COLUMN `password` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;
ALTER TABLE `admin` MODIFY COLUMN `password` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL;

-- ----------------------------------------------------------------------------
-- 2. 补二级索引
-- 改造前 10 张表除主键外无任何索引，下列高频条件查询均为全表扫描。
-- 若某个索引已存在，先执行对应 DROP INDEX 或忽略该行报错即可。
-- ----------------------------------------------------------------------------
ALTER TABLE `comment` ADD INDEX `idx_poid_level` (`poid`, `level`);
ALTER TABLE `comment` ADD INDEX `idx_pid` (`pid`);

ALTER TABLE `adopt`   ADD INDEX `idx_uid_isread` (`userid`, `is_read`);
ALTER TABLE `adopt`   ADD INDEX `idx_petid` (`petid`);

ALTER TABLE `donate`  ADD INDEX `idx_uid` (`uid`);
ALTER TABLE `donate`  ADD INDEX `idx_create_time` (`create_time`);

ALTER TABLE `message` ADD INDEX `idx_uid_isread` (`uid`, `is_read`);

ALTER TABLE `pet`     ADD INDEX `idx_type_classic` (`type`, `classic`);
ALTER TABLE `pet`     ADD INDEX `idx_uid` (`uid`);

ALTER TABLE `post`    ADD INDEX `idx_type_classic` (`type`, `classic`);
ALTER TABLE `post`    ADD INDEX `idx_uid` (`uid`);

ALTER TABLE `journal` ADD INDEX `idx_pid` (`pid`);

-- ----------------------------------------------------------------------------
-- 3.（可选）校验
-- 改造后确认一下改动是否生效：
--   SHOW INDEX FROM `comment`;
--   SHOW COLUMNS FROM `user` LIKE 'password';
-- ----------------------------------------------------------------------------
