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
    remark VARCHAR(500),
    deleted TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS posts (
    id VARCHAR(36) PRIMARY KEY,
    postCode VARCHAR(50) NOT NULL,
    postName VARCHAR(100) NOT NULL,
    postSort INT DEFAULT 0,
    status TINYINT DEFAULT 0,
    remark VARCHAR(500),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_posts_post_code UNIQUE (postCode)
);

CREATE TABLE IF NOT EXISTS user_posts (
    userId VARCHAR(36) NOT NULL,
    postId VARCHAR(36) NOT NULL,
    assignedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_posts PRIMARY KEY (userId, postId)
);

CREATE TABLE IF NOT EXISTS oper_logs (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(100),
    businessType TINYINT DEFAULT 0,
    method VARCHAR(255),
    requestMethod VARCHAR(20),
    operName VARCHAR(50),
    deptName VARCHAR(100),
    operUrl VARCHAR(255),
    operIp VARCHAR(64),
    operLocation VARCHAR(200),
    operParam CLOB,
    jsonResult CLOB,
    status TINYINT DEFAULT 0,
    errorMsg CLOB,
    costTime BIGINT,
    operTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
