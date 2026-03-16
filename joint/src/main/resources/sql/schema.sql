-- Joint 数据库初始化脚本
-- 对齐 Nexus/prisma/schema.mysql.prisma 与 Nexus/scripts/init-admin.ts
-- 说明：本脚本作用于当前数据源已连接的数据库，不在这里切换库名

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickName VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    deptId VARCHAR(36) COMMENT '部门ID',
    remark VARCHAR(500) COMMENT '备注',
    createdAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updatedAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX idx_users_username (username),
    INDEX idx_users_status (status),
    INDEX idx_users_dept_id (deptId)
) COMMENT '用户表';

CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    remark VARCHAR(500) COMMENT '备注',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    isBuiltin TINYINT DEFAULT 0 COMMENT '是否内置角色 0-否 1-是',
    isSuper TINYINT DEFAULT 0 COMMENT '是否超级管理员 0-否 1-是',
    createdAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updatedAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX idx_roles_status (status)
) COMMENT '角色表';

CREATE TABLE IF NOT EXISTS menus (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '菜单名称',
    title VARCHAR(100) NOT NULL COMMENT '菜单标题',
    parentId VARCHAR(36) COMMENT '父菜单ID',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    type ENUM('CATALOG', 'MENU', 'BUTTON', 'EMBEDDED', 'LINK') NOT NULL COMMENT '菜单类型',
    authCode VARCHAR(100) COMMENT '权限标识',
    `order` INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    icon VARCHAR(100) COMMENT '图标',
    activeIcon VARCHAR(100) COMMENT '激活图标',
    keepAlive TINYINT DEFAULT 0 COMMENT '是否缓存',
    affixTab TINYINT DEFAULT 0 COMMENT '是否固定标签',
    hideInMenu TINYINT DEFAULT 0 COMMENT '是否隐藏菜单',
    hideChildrenInMenu TINYINT DEFAULT 0 COMMENT '是否隐藏子菜单',
    hideInBreadcrumb TINYINT DEFAULT 0 COMMENT '是否隐藏面包屑',
    hideInTab TINYINT DEFAULT 0 COMMENT '是否隐藏标签页',
    iframeSrc VARCHAR(255) COMMENT 'iframe 地址',
    link VARCHAR(255) COMMENT '外链地址',
    activePath VARCHAR(255) COMMENT '激活路径',
    badge VARCHAR(50) COMMENT '徽标内容',
    badgeType VARCHAR(50) COMMENT '徽标类型',
    badgeVariants VARCHAR(50) COMMENT '徽标变体',
    treePath VARCHAR(1000) COMMENT '树路径',
    createdAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updatedAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX idx_menus_parent_id (parentId),
    INDEX idx_menus_status (status),
    INDEX idx_menus_auth_code (authCode)
) COMMENT '菜单表';

CREATE TABLE IF NOT EXISTS user_roles (
    userId VARCHAR(36) NOT NULL COMMENT '用户ID',
    roleId VARCHAR(36) NOT NULL COMMENT '角色ID',
    assignedAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '分配时间',
    PRIMARY KEY (userId, roleId)
) COMMENT '用户角色关联表';

CREATE TABLE IF NOT EXISTS role_menus (
    roleId VARCHAR(36) NOT NULL COMMENT '角色ID',
    menuId VARCHAR(36) NOT NULL COMMENT '菜单ID',
    grantedAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '授权时间',
    PRIMARY KEY (roleId, menuId)
) COMMENT '角色菜单关联表';

CREATE TABLE IF NOT EXISTS depts (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '部门名称',
    pid VARCHAR(36) COMMENT '父部门ID',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    remark VARCHAR(500) COMMENT '备注',
    treePath VARCHAR(1000) COMMENT '树路径',
    createdAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updatedAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX idx_depts_pid (pid),
    INDEX idx_depts_status (status)
) COMMENT '部门表';

SET @joint_has_dept_remark = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'depts'
      AND COLUMN_NAME = 'remark'
);
SET @joint_dept_remark_ddl = IF(
    @joint_has_dept_remark = 0,
    'ALTER TABLE depts ADD COLUMN remark VARCHAR(500) COMMENT ''备注''',
    'SELECT 1'
);
PREPARE joint_dept_remark_stmt FROM @joint_dept_remark_ddl;
EXECUTE joint_dept_remark_stmt;
DEALLOCATE PREPARE joint_dept_remark_stmt;

CREATE TABLE IF NOT EXISTS posts (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    postCode VARCHAR(50) NOT NULL UNIQUE COMMENT '岗位编码',
    postName VARCHAR(100) NOT NULL COMMENT '岗位名称',
    postSort INT DEFAULT 0 COMMENT '岗位排序',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    remark VARCHAR(500) COMMENT '备注',
    createdAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updatedAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间'
) COMMENT '岗位表';

CREATE TABLE IF NOT EXISTS user_posts (
    userId VARCHAR(36) NOT NULL COMMENT '用户ID',
    postId VARCHAR(36) NOT NULL COMMENT '岗位ID',
    assignedAt DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '分配时间',
    PRIMARY KEY (userId, postId)
) COMMENT '用户岗位关联表';

CREATE TABLE IF NOT EXISTS oper_logs (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(100) COMMENT '日志标题',
    businessType TINYINT DEFAULT 0 COMMENT '业务类型',
    method VARCHAR(255) COMMENT '方法签名',
    requestMethod VARCHAR(20) COMMENT '请求方法',
    operName VARCHAR(50) COMMENT '操作人名称',
    deptName VARCHAR(100) COMMENT '部门名称',
    operUrl VARCHAR(255) COMMENT '请求地址',
    operIp VARCHAR(64) COMMENT '操作IP',
    operLocation VARCHAR(200) COMMENT '操作地点',
    operParam TEXT COMMENT '请求参数',
    jsonResult TEXT COMMENT '响应结果',
    status TINYINT DEFAULT 0 COMMENT '状态 0-成功 1-失败',
    errorMsg TEXT COMMENT '错误信息',
    costTime BIGINT COMMENT '耗时(毫秒)',
    operTime DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
    INDEX idx_oper_logs_time (operTime),
    INDEX idx_oper_logs_title (title),
    INDEX idx_oper_logs_oper_name (operName),
    INDEX idx_oper_logs_business_type (businessType),
    INDEX idx_oper_logs_status (status)
) COMMENT '操作日志表';

-- 内置角色：admin、user
INSERT INTO roles (id, name, remark, status, isBuiltin, isSuper)
SELECT REPLACE(UUID(), '-', ''), 'admin', '系统内置管理员角色', 0, 1, 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'admin'
);

INSERT INTO roles (id, name, remark, status, isBuiltin, isSuper)
SELECT REPLACE(UUID(), '-', ''), 'user', '默认用户角色', 0, 1, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'user'
);

-- 默认部门：总公司
INSERT INTO depts (id, name, pid, status, remark, treePath)
SELECT REPLACE(UUID(), '-', ''), '总公司', NULL, 0, '系统默认一级部门', NULL
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM depts WHERE name = '总公司' AND pid IS NULL
);

-- Dashboard 菜单
INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon, affixTab)
SELECT REPLACE(UUID(), '-', ''), 'Dashboard', 'page.dashboard.title', NULL, '/dashboard', NULL, 'CATALOG', NULL, -1, 0, 'lucide:layout-dashboard', 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE name = 'Dashboard');

INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon, affixTab)
SELECT REPLACE(UUID(), '-', ''), 'Analytics', 'page.dashboard.analytics',
       (SELECT id FROM menus WHERE name = 'Dashboard' LIMIT 1),
       '/analytics', 'dashboard/analytics/index', 'MENU', NULL, 1, 0, 'lucide:area-chart', 1
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'Dashboard')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'Analytics');

INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon, affixTab)
SELECT REPLACE(UUID(), '-', ''), 'Workspace', 'page.dashboard.workspace',
       (SELECT id FROM menus WHERE name = 'Dashboard' LIMIT 1),
       '/workspace', 'dashboard/workspace/index', 'MENU', NULL, 2, 0, 'carbon:workspace', 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'Dashboard')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'Workspace');

-- 系统管理菜单
INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon)
SELECT REPLACE(UUID(), '-', ''), 'System', 'system.title', NULL, '/system', NULL, 'CATALOG', NULL, 9997, 0, 'ion:settings-outline'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menus WHERE name = 'System');

INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon)
SELECT REPLACE(UUID(), '-', ''), 'SystemMenu', 'system.menu.title',
       (SELECT id FROM menus WHERE name = 'System' LIMIT 1),
       'menu', 'system/menu/list', 'MENU', NULL, 1, 0, 'mdi:menu'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'System')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenu');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemMenuList', 'system.menu.list',
       (SELECT id FROM menus WHERE name = 'SystemMenu' LIMIT 1),
       '#', 'BUTTON', 'system:menu:list', 1, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenu')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenuList');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemMenuQuery', 'system.menu.query',
       (SELECT id FROM menus WHERE name = 'SystemMenu' LIMIT 1),
       '#', 'BUTTON', 'system:menu:query', 2, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenu')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenuQuery');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemMenuCreate', 'system.menu.create',
       (SELECT id FROM menus WHERE name = 'SystemMenu' LIMIT 1),
       '#', 'BUTTON', 'system:menu:create', 3, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenu')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenuCreate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemMenuUpdate', 'system.menu.update',
       (SELECT id FROM menus WHERE name = 'SystemMenu' LIMIT 1),
       '#', 'BUTTON', 'system:menu:update', 4, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenu')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenuUpdate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemMenuDelete', 'system.menu.delete',
       (SELECT id FROM menus WHERE name = 'SystemMenu' LIMIT 1),
       '#', 'BUTTON', 'system:menu:delete', 5, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenu')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemMenuDelete');

INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon)
SELECT REPLACE(UUID(), '-', ''), 'SystemDept', 'system.dept.title',
       (SELECT id FROM menus WHERE name = 'System' LIMIT 1),
       'dept', 'system/dept/list', 'MENU', NULL, 2, 0, 'mdi:file-tree-outline'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'System')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemDept');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemDeptList', 'system.dept.list',
       (SELECT id FROM menus WHERE name = 'SystemDept' LIMIT 1),
       '#', 'BUTTON', 'system:dept:list', 1, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemDept')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemDeptList');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemDeptCreate', 'system.dept.create',
       (SELECT id FROM menus WHERE name = 'SystemDept' LIMIT 1),
       '#', 'BUTTON', 'system:dept:create', 2, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemDept')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemDeptCreate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemDeptUpdate', 'system.dept.update',
       (SELECT id FROM menus WHERE name = 'SystemDept' LIMIT 1),
       '#', 'BUTTON', 'system:dept:update', 3, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemDept')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemDeptUpdate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemDeptDelete', 'system.dept.delete',
       (SELECT id FROM menus WHERE name = 'SystemDept' LIMIT 1),
       '#', 'BUTTON', 'system:dept:delete', 4, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemDept')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemDeptDelete');

INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon)
SELECT REPLACE(UUID(), '-', ''), 'SystemPost', 'system.post.title',
       (SELECT id FROM menus WHERE name = 'System' LIMIT 1),
       'post', 'system/post/list', 'MENU', NULL, 3, 0, 'mdi:briefcase-outline'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'System')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPost');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemPostList', 'system.post.list',
       (SELECT id FROM menus WHERE name = 'SystemPost' LIMIT 1),
       '#', 'BUTTON', 'system:post:list', 1, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPost')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPostList');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemPostQuery', 'system.post.query',
       (SELECT id FROM menus WHERE name = 'SystemPost' LIMIT 1),
       '#', 'BUTTON', 'system:post:query', 2, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPost')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPostQuery');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemPostCreate', 'system.post.create',
       (SELECT id FROM menus WHERE name = 'SystemPost' LIMIT 1),
       '#', 'BUTTON', 'system:post:create', 3, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPost')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPostCreate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemPostUpdate', 'system.post.update',
       (SELECT id FROM menus WHERE name = 'SystemPost' LIMIT 1),
       '#', 'BUTTON', 'system:post:update', 4, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPost')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPostUpdate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemPostDelete', 'system.post.delete',
       (SELECT id FROM menus WHERE name = 'SystemPost' LIMIT 1),
       '#', 'BUTTON', 'system:post:delete', 5, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPost')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemPostDelete');

INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon)
SELECT REPLACE(UUID(), '-', ''), 'SystemRole', 'system.role.title',
       (SELECT id FROM menus WHERE name = 'System' LIMIT 1),
       'role', 'system/role/list', 'MENU', NULL, 4, 0, 'mdi:account-group'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'System')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRole');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemRoleList', 'system.role.list',
       (SELECT id FROM menus WHERE name = 'SystemRole' LIMIT 1),
       '#', 'BUTTON', 'system:role:list', 1, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRole')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRoleList');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemRoleQuery', 'system.role.query',
       (SELECT id FROM menus WHERE name = 'SystemRole' LIMIT 1),
       '#', 'BUTTON', 'system:role:query', 2, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRole')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRoleQuery');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemRoleCreate', 'system.role.create',
       (SELECT id FROM menus WHERE name = 'SystemRole' LIMIT 1),
       '#', 'BUTTON', 'system:role:create', 3, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRole')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRoleCreate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemRoleUpdate', 'system.role.update',
       (SELECT id FROM menus WHERE name = 'SystemRole' LIMIT 1),
       '#', 'BUTTON', 'system:role:update', 4, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRole')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRoleUpdate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemRoleDelete', 'system.role.delete',
       (SELECT id FROM menus WHERE name = 'SystemRole' LIMIT 1),
       '#', 'BUTTON', 'system:role:delete', 5, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRole')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemRoleDelete');

INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon)
SELECT REPLACE(UUID(), '-', ''), 'SystemUser', 'system.user.title',
       (SELECT id FROM menus WHERE name = 'System' LIMIT 1),
       'user', 'system/user/list', 'MENU', NULL, 5, 0, 'mdi:account-outline'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'System')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUser');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemUserList', 'system.user.list',
       (SELECT id FROM menus WHERE name = 'SystemUser' LIMIT 1),
       '#', 'BUTTON', 'system:user:list', 1, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUser')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUserList');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemUserQuery', 'system.user.query',
       (SELECT id FROM menus WHERE name = 'SystemUser' LIMIT 1),
       '#', 'BUTTON', 'system:user:query', 2, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUser')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUserQuery');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemUserCreate', 'system.user.create',
       (SELECT id FROM menus WHERE name = 'SystemUser' LIMIT 1),
       '#', 'BUTTON', 'system:user:create', 3, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUser')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUserCreate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemUserUpdate', 'system.user.update',
       (SELECT id FROM menus WHERE name = 'SystemUser' LIMIT 1),
       '#', 'BUTTON', 'system:user:update', 4, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUser')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUserUpdate');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemUserDelete', 'system.user.delete',
       (SELECT id FROM menus WHERE name = 'SystemUser' LIMIT 1),
       '#', 'BUTTON', 'system:user:delete', 5, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUser')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUserDelete');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemUserResetPassword', 'system.user.resetPassword',
       (SELECT id FROM menus WHERE name = 'SystemUser' LIMIT 1),
       '#', 'BUTTON', 'system:user:reset-password', 6, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUser')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemUserResetPassword');

INSERT INTO menus (id, name, title, parentId, path, component, type, authCode, `order`, status, icon)
SELECT REPLACE(UUID(), '-', ''), 'SystemLog', 'system.log.title',
       (SELECT id FROM menus WHERE name = 'System' LIMIT 1),
       'log', 'system/log/list', 'MENU', NULL, 6, 0, 'mdi:clipboard-text-clock-outline'
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'System')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemLog');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemLogList', 'system.log.list',
       (SELECT id FROM menus WHERE name = 'SystemLog' LIMIT 1),
       '#', 'BUTTON', 'system:log:list', 1, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemLog')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemLogList');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemLogQuery', 'system.log.query',
       (SELECT id FROM menus WHERE name = 'SystemLog' LIMIT 1),
       '#', 'BUTTON', 'system:log:query', 2, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemLog')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemLogQuery');

INSERT INTO menus (id, name, title, parentId, path, type, authCode, `order`, status)
SELECT REPLACE(UUID(), '-', ''), 'SystemLogDelete', 'system.log.delete',
       (SELECT id FROM menus WHERE name = 'SystemLog' LIMIT 1),
       '#', 'BUTTON', 'system:log:delete', 3, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM menus WHERE name = 'SystemLog')
  AND NOT EXISTS (SELECT 1 FROM menus WHERE name = 'SystemLogDelete');

-- admin 角色同步所有菜单权限
DELETE FROM role_menus
WHERE roleId = (SELECT id FROM roles WHERE name = 'admin' LIMIT 1);

INSERT INTO role_menus (roleId, menuId)
SELECT (SELECT id FROM roles WHERE name = 'admin' LIMIT 1), m.id
FROM menus m
WHERE EXISTS (SELECT 1 FROM roles WHERE name = 'admin');

-- 创建 admin 用户
INSERT INTO users (id, username, password, nickName, status, deptId, remark)
SELECT REPLACE(UUID(), '-', ''), 'admin',
       '$2a$10$1I2ZrJ.fpnv2XKiSqS0loeM6rcBtODeBdUMmfvxp242cXtLUv8iO2',
       '超级管理员', 0,
       (SELECT id FROM depts WHERE name = '总公司' AND pid IS NULL LIMIT 1),
       '系统内置管理员账户'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

UPDATE users
SET deptId = (SELECT id FROM depts WHERE name = '总公司' AND pid IS NULL LIMIT 1)
WHERE username = 'admin'
  AND (deptId IS NULL OR deptId = '');

INSERT INTO user_roles (userId, roleId)
SELECT u.id, r.id
FROM users u
INNER JOIN roles r ON r.name = 'admin'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.userId = u.id
        AND ur.roleId = r.id
  );
