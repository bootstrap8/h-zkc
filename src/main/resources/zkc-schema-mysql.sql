CREATE TABLE `h_zkc_users`
(
    `id`         int AUTO_INCREMENT PRIMARY KEY comment '主键', -- 用户ID
    `username`   VARCHAR(255) NOT NULL UNIQUE comment '账号名', -- 账号名
    `password`   VARCHAR(255) NOT NULL comment '密码',          -- 密码
    `role_id`    NUMERIC(10) comment '角色ID',                  -- 角色ID (外键)
    `created_at` NUMERIC(20) comment '创建时间',                -- 创建时间
    `updated_at` NUMERIC(20) comment '更新时间'                 -- 更新时间
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE `h_zkc_roles`
(
    `id`          int AUTO_INCREMENT PRIMARY KEY comment '角色ID', -- 角色ID
    `name`        VARCHAR(255) NOT NULL comment '角色名称',        -- 角色名称
    `description` VARCHAR(500) comment '角色描述',                 -- 角色描述
    `created_at`  NUMERIC(20) comment '创建时间',                  -- 创建时间
    `updated_at`  NUMERIC(20) comment '更新时间'                   -- 更新时间
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `h_zkc_menus`
(
    `id`          int AUTO_INCREMENT PRIMARY KEY comment '菜单ID', -- 菜单ID
    `name`        VARCHAR(255) NOT NULL comment '菜单名称',        -- 菜单名称
    `url`         VARCHAR(255) comment '菜单链接',                 -- 菜单链接
    `parent_id`   NUMERIC(10) DEFAULT -1 comment '父菜单ID',       -- 父级菜单ID (用于树形结构，根菜单的parent_id为NULL)
    `order_index` NUMERIC(10) DEFAULT 0 comment '菜单顺序',        -- 菜单显示顺序
    `created_at`  NUMERIC(20) comment '创建时间',                  -- 创建时间
    `updated_at`  NUMERIC(20) comment '修改时间'                   -- 更新时间
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `h_zkc_role_menus`
(
    `id`      int AUTO_INCREMENT PRIMARY KEY comment '主键', -- 主键ID
    `role_id` NUMERIC(20) NOT NULL comment '角色ID',         -- 角色ID
    `menu_id` NUMERIC(20) NOT NULL comment '菜单ID'          -- 菜单ID
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;