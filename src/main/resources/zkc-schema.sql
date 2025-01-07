CREATE TABLE h_zkc_users
(
    id         INT AUTO_INCREMENT PRIMARY KEY, -- 用户ID
    username   VARCHAR(255) NOT NULL UNIQUE,   -- 账号名
    password   VARCHAR(255) NOT NULL,          -- 密码
    role_id    NUMERIC(10),                    -- 角色ID (外键)
    created_at NUMERIC(20),                    -- 创建时间
    updated_at NUMERIC(20)                     -- 更新时间
);


CREATE TABLE h_zkc_roles
(
    id          INT AUTO_INCREMENT PRIMARY KEY, -- 角色ID
    name        VARCHAR(255) NOT NULL,          -- 角色名称
    description VARCHAR(500),                   -- 角色描述
    created_at  NUMERIC(20),                    -- 创建时间
    updated_at  NUMERIC(20)                     -- 更新时间
);

CREATE TABLE h_zkc_menus
(
    id          INT AUTO_INCREMENT PRIMARY KEY, -- 菜单ID
    name        VARCHAR(255) NOT NULL,          -- 菜单名称
    url         VARCHAR(255),                   -- 菜单链接
    parent_id   NUMERIC(10) DEFAULT -1,         -- 父级菜单ID (用于树形结构，根菜单的parent_id为NULL)
    order_index NUMERIC(10) DEFAULT 0,          -- 菜单显示顺序
    created_at  NUMERIC(20),                    -- 创建时间
    updated_at  NUMERIC(20)                     -- 更新时间
);

CREATE TABLE h_zkc_role_menus
(
    id      INT AUTO_INCREMENT PRIMARY KEY, -- 主键ID
    role_id INT NOT NULL,                   -- 角色ID
    menu_id INT NOT NULL                    -- 菜单ID
);

