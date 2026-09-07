-- ============================================================
-- 财务管理模块建表 SQL
-- 数据库：db_java1234_rag
-- 内容：部门表 t_department + 进项发票表 t_invoice_in
-- 说明：
--   1. 部门表用于区分发票所属部门，统计时按部门汇总；
--   2. 进项发票表通过 department_id 关联部门（单表外键字段，不做多表结构）；
--   3. 不含税金额 amount 与税额 tax_amount 分开存储，
--      价税合计 = amount + tax_amount（由前后端计算展示）。
-- ============================================================

USE `db_java1234_rag`;

/* ---------------- 部门表 ---------------- */

DROP TABLE IF EXISTS `t_department`;

CREATE TABLE `t_department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

/* 初始部门数据（可按需增删） */
INSERT INTO `t_department` (`name`, `remark`) VALUES
('技术部', '研发与技术支撑'),
('销售部', '市场销售'),
('财务部', '财务核算'),
('人事部', '人力资源'),
('行政部', '综合行政');

/* ---------------- 进项发票表 ---------------- */

DROP TABLE IF EXISTS `t_invoice_in`;

CREATE TABLE `t_invoice_in` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `invoice_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发票号码',
  `invoice_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '增值税专用发票' COMMENT '发票类型：增值税专用发票/增值税普通发票/电子发票/其他',
  `supplier` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售方（供应商）',
  `department_id` bigint NOT NULL COMMENT '所属部门ID（t_department.id）',
  `amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '不含税金额',
  `tax_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '税额',
  `invoice_date` date NOT NULL COMMENT '开票日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '入账状态：0未入账 1已入账',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_department` (`department_id`),
  KEY `idx_invoice_no` (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='进项发票入账明细';
