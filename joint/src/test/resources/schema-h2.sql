CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickName VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    status TINYINT DEFAULT 0,
    deptId VARCHAR(36),
    remark VARCHAR(500),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone)
);

CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    remark VARCHAR(500),
    status TINYINT DEFAULT 0,
    isBuiltin BOOLEAN DEFAULT FALSE,
    isSuper BOOLEAN DEFAULT FALSE,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS user_roles (
    userId VARCHAR(36) NOT NULL,
    roleId VARCHAR(36) NOT NULL,
    assignedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_roles PRIMARY KEY (userId, roleId)
);

CREATE TABLE IF NOT EXISTS depts (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    pid VARCHAR(36),
    status TINYINT DEFAULT 0,
    remark VARCHAR(500),
    treePath VARCHAR(1000),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

CREATE TABLE IF NOT EXISTS dict_types (
    id VARCHAR(36) PRIMARY KEY,
    dictName VARCHAR(100) NOT NULL,
    dictType VARCHAR(100) NOT NULL,
    status TINYINT DEFAULT 0,
    remark VARCHAR(500),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_dict_types_dict_type UNIQUE (dictType)
);

CREATE TABLE IF NOT EXISTS dict_data (
    id VARCHAR(36) PRIMARY KEY,
    typeId VARCHAR(36) NOT NULL,
    dictLabel VARCHAR(100) NOT NULL,
    dictValue VARCHAR(100) NOT NULL,
    dictSort INT DEFAULT 0,
    cssClass VARCHAR(100),
    listClass VARCHAR(100),
    isDefault BOOLEAN DEFAULT FALSE,
    status TINYINT DEFAULT 0,
    remark VARCHAR(500),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_dict_data_type_label UNIQUE (typeId, dictLabel)
);

CREATE TABLE IF NOT EXISTS menus (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    parentId VARCHAR(36),
    path VARCHAR(200),
    component VARCHAR(200),
    type VARCHAR(20) NOT NULL,
    authCode VARCHAR(100),
    "order" INT DEFAULT 0,
    status TINYINT DEFAULT 0,
    icon VARCHAR(100),
    activeIcon VARCHAR(100),
    keepAlive BOOLEAN DEFAULT FALSE,
    affixTab BOOLEAN DEFAULT FALSE,
    hideInMenu BOOLEAN DEFAULT FALSE,
    hideChildrenInMenu BOOLEAN DEFAULT FALSE,
    hideInBreadcrumb BOOLEAN DEFAULT FALSE,
    hideInTab BOOLEAN DEFAULT FALSE,
    iframeSrc VARCHAR(255),
    link VARCHAR(255),
    activePath VARCHAR(255),
    badge VARCHAR(50),
    badgeType VARCHAR(50),
    badgeVariants VARCHAR(50),
    treePath VARCHAR(1000),
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_menus (
    roleId VARCHAR(36) NOT NULL,
    menuId VARCHAR(36) NOT NULL,
    grantedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_role_menus PRIMARY KEY (roleId, menuId)
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
