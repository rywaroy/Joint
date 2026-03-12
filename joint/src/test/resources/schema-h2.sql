CREATE TABLE IF NOT EXISTS sys_user (
    id VARCHAR(32) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nick_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status TINYINT DEFAULT 0,
    dept_id VARCHAR(32),
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_user_username UNIQUE (username),
    CONSTRAINT uk_sys_user_email UNIQUE (email),
    CONSTRAINT uk_sys_user_phone UNIQUE (phone)
);

CREATE TABLE IF NOT EXISTS sys_role (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 0,
    is_super TINYINT DEFAULT 0,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_role_name UNIQUE (name),
    CONSTRAINT uk_sys_role_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS sys_menu (
    id VARCHAR(32) PRIMARY KEY,
    parent_id VARCHAR(32),
    name VARCHAR(50) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(100),
    type TINYINT DEFAULT 1,
    auth_code VARCHAR(100),
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 0,
    hidden TINYINT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    role_id VARCHAR(32) NOT NULL,
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id VARCHAR(32) PRIMARY KEY,
    role_id VARCHAR(32) NOT NULL,
    menu_id VARCHAR(32) NOT NULL,
    CONSTRAINT uk_sys_role_menu UNIQUE (role_id, menu_id)
);

CREATE TABLE IF NOT EXISTS sys_dept (
    id VARCHAR(32) PRIMARY KEY,
    parent_id VARCHAR(32),
    name VARCHAR(50) NOT NULL,
    sort INT DEFAULT 0,
    leader VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 0,
    deleted TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_post (
    id VARCHAR(32) PRIMARY KEY,
    post_code VARCHAR(50) NOT NULL,
    post_name VARCHAR(50) NOT NULL,
    post_sort INT DEFAULT 0,
    status TINYINT DEFAULT 0,
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_post_code UNIQUE (post_code)
);

CREATE TABLE IF NOT EXISTS sys_user_post (
    id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    post_id VARCHAR(32) NOT NULL,
    CONSTRAINT uk_sys_user_post UNIQUE (user_id, post_id)
);

CREATE TABLE IF NOT EXISTS sys_oper_log (
    id VARCHAR(32) PRIMARY KEY,
    module VARCHAR(100),
    business_type VARCHAR(50),
    description VARCHAR(255),
    method VARCHAR(255),
    request_method VARCHAR(20),
    request_url VARCHAR(255),
    request_params CLOB,
    response_result CLOB,
    status TINYINT DEFAULT 0,
    error_msg CLOB,
    operator_id VARCHAR(32),
    operator_name VARCHAR(50),
    operator_ip VARCHAR(64),
    cost_time BIGINT,
    operate_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
