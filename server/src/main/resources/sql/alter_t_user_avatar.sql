-- 已有库升级：为用户表增加头像字段（相对 uploads 根目录的路径，如 202605/xxx.jpg）
USE db_java1234_rag;
ALTER TABLE t_user ADD COLUMN avatar VARCHAR(512) DEFAULT NULL COMMENT '头像相对路径' AFTER real_name;
