CREATE TABLE h_zkc_users
(
    id NUMBER (20) AUTO_INCREMENT PRIMARY KEY, -- 用户ID
    username VARCHAR2 (255) NOT NULL UNIQUE,   -- 账号名
    password VARCHAR2 (255) NOT NULL,          -- 密码
    role_id NUMBER (10),                       -- 角色ID (外键)
    created_at NUMBER (20),                    -- 创建时间
    updated_at NUMBER (20)                     -- 更新时间
);


CREATE TABLE h_zkc_roles
(
    id NUMBER (20) AUTO_INCREMENT PRIMARY KEY, -- 角色ID
    name VARCHAR2 (255) NOT NULL,              -- 角色名称
    description VARCHAR2 (500),                -- 角色描述
    created_at NUMBER (20),                    -- 创建时间
    updated_at NUMBER (20)                     -- 更新时间
);

CREATE TABLE h_zkc_menus
(
    id NUMBER (20) AUTO_INCREMENT PRIMARY KEY, -- 菜单ID
    name VARCHAR2 (255) NOT NULL,              -- 菜单名称
    url VARCHAR2 (255),                        -- 菜单链接
    parent_id   NUMERIC(10) DEFAULT -1,        -- 父级菜单ID (用于树形结构，根菜单的parent_id为NULL)
    order_index NUMERIC(10) DEFAULT 0,         -- 菜单显示顺序
    created_at NUMBER (20),                    -- 创建时间
    updated_at NUMBER (20)                     -- 更新时间
);

CREATE TABLE h_zkc_role_menus
(
    id NUMBER (20) AUTO_INCREMENT PRIMARY KEY, -- 主键ID
    role_id NUMBER (20) NOT NULL,              -- 角色ID
    menu_id NUMBER (20) NOT NULL               -- 菜单ID
);
