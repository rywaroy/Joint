-- Joint 数据库初始化脚本
-- 对应 Phase 2: 数据库集成
-- 说明：本脚本作用于当前数据源已连接的数据库，不在这里切换库名

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nick_name VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    dept_id VARCHAR(32) COMMENT '部门ID',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_status (status)
) COMMENT '用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    is_super TINYINT DEFAULT 0 COMMENT '是否超级管理员 0-否 1-是',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    parent_id VARCHAR(32) COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(100) COMMENT '图标',
    type TINYINT DEFAULT 1 COMMENT '类型 0-目录 1-菜单 2-按钮',
    auth_code VARCHAR(100) COMMENT '权限标识',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    hidden TINYINT DEFAULT 0 COMMENT '是否隐藏 0-显示 1-隐藏',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '菜单表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联表';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    role_id VARCHAR(32) NOT NULL COMMENT '角色ID',
    menu_id VARCHAR(32) NOT NULL COMMENT '菜单ID',
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) COMMENT '角色菜单关联表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    parent_id VARCHAR(32) COMMENT '父部门ID',
    name VARCHAR(50) NOT NULL COMMENT '部门名称',
    sort INT DEFAULT 0 COMMENT '排序',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '部门表';

-- 岗位表
CREATE TABLE IF NOT EXISTS sys_post (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    post_code VARCHAR(50) NOT NULL UNIQUE COMMENT '岗位编码',
    post_name VARCHAR(50) NOT NULL COMMENT '岗位名称',
    post_sort INT DEFAULT 0 COMMENT '岗位排序',
    status TINYINT DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '岗位表';

-- 用户-岗位关联表
CREATE TABLE IF NOT EXISTS sys_user_post (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(32) NOT NULL COMMENT '用户ID',
    post_id VARCHAR(32) NOT NULL COMMENT '岗位ID',
    UNIQUE KEY uk_user_post (user_id, post_id)
) COMMENT '用户岗位关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    module VARCHAR(100) COMMENT '模块名称',
    business_type VARCHAR(50) COMMENT '业务类型',
    description VARCHAR(255) COMMENT '操作描述',
    method VARCHAR(255) COMMENT '方法签名',
    request_method VARCHAR(20) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求地址',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    status TINYINT DEFAULT 0 COMMENT '状态 0-成功 1-失败',
    error_msg TEXT COMMENT '错误信息',
    operator_id VARCHAR(32) COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人名称',
    operator_ip VARCHAR(64) COMMENT '操作IP',
    cost_time BIGINT COMMENT '耗时(毫秒)',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_operate_time (operate_time),
    INDEX idx_module (module),
    INDEX idx_status (status)
) COMMENT '操作日志表';

-- 默认基础数据
-- 对齐 Nexus/scripts/init-admin.ts
-- 说明：
-- 1. Java 侧 schema 没有 title、affix_tab、is_builtin、dept.remark 字段，这些配置不落库
-- 2. 角色 code 使用 Joint 当前实现要求的 admin / user

-- 内置角色：admin、user
INSERT INTO sys_role (id, name, code, sort, status, is_super, remark, deleted)
SELECT REPLACE(UUID(), '-', ''), 'admin', 'admin', 0, 0, 1, '系统内置管理员角色', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE code = 'admin'
);

INSERT INTO sys_role (id, name, code, sort, status, is_super, remark, deleted)
SELECT REPLACE(UUID(), '-', ''), 'user', 'user', 0, 0, 0, '默认用户角色', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role WHERE code = 'user'
);

-- 默认部门：总公司
INSERT INTO sys_dept (id, parent_id, name, sort, status, deleted)
SELECT REPLACE(UUID(), '-', ''), NULL, '总公司', 0, 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_dept
    WHERE name = '总公司'
      AND (parent_id IS NULL OR parent_id = '' OR parent_id = '0')
);

-- Dashboard 菜单
INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), NULL, 'Dashboard', '/dashboard', NULL, 'lucide:layout-dashboard', 0, NULL, -1, 0, 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'Dashboard'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'Dashboard' LIMIT 1), 'Analytics', '/analytics', 'dashboard/analytics/index', 'lucide:area-chart', 1, NULL, 1, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'Dashboard'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'Analytics'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'Dashboard' LIMIT 1), 'Workspace', '/workspace', 'dashboard/workspace/index', 'carbon:workspace', 1, NULL, 2, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'Dashboard'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'Workspace'
);

-- 系统管理菜单
INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), NULL, 'System', '/system', NULL, 'ion:settings-outline', 0, NULL, 9997, 0, 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'System'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'System' LIMIT 1), 'SystemMenu', 'menu', 'system/menu/list', 'mdi:menu', 1, NULL, 1, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'System'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenu'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemMenu' LIMIT 1), 'SystemMenuList', '#', NULL, NULL, 2, 'system:menu:list', 1, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenu'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenuList'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemMenu' LIMIT 1), 'SystemMenuQuery', '#', NULL, NULL, 2, 'system:menu:query', 2, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenu'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenuQuery'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemMenu' LIMIT 1), 'SystemMenuCreate', '#', NULL, NULL, 2, 'system:menu:create', 3, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenu'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenuCreate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemMenu' LIMIT 1), 'SystemMenuUpdate', '#', NULL, NULL, 2, 'system:menu:update', 4, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenu'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenuUpdate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemMenu' LIMIT 1), 'SystemMenuDelete', '#', NULL, NULL, 2, 'system:menu:delete', 5, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenu'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemMenuDelete'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'System' LIMIT 1), 'SystemDept', 'dept', 'system/dept/list', 'mdi:file-tree-outline', 1, NULL, 2, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'System'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemDept'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemDept' LIMIT 1), 'SystemDeptList', '#', NULL, NULL, 2, 'system:dept:list', 1, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemDept'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemDeptList'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemDept' LIMIT 1), 'SystemDeptCreate', '#', NULL, NULL, 2, 'system:dept:create', 2, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemDept'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemDeptCreate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemDept' LIMIT 1), 'SystemDeptUpdate', '#', NULL, NULL, 2, 'system:dept:update', 3, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemDept'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemDeptUpdate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemDept' LIMIT 1), 'SystemDeptDelete', '#', NULL, NULL, 2, 'system:dept:delete', 4, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemDept'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemDeptDelete'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'System' LIMIT 1), 'SystemPost', 'post', 'system/post/list', 'mdi:briefcase-outline', 1, NULL, 3, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'System'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPost'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemPost' LIMIT 1), 'SystemPostList', '#', NULL, NULL, 2, 'system:post:list', 1, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPost'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPostList'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemPost' LIMIT 1), 'SystemPostQuery', '#', NULL, NULL, 2, 'system:post:query', 2, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPost'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPostQuery'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemPost' LIMIT 1), 'SystemPostCreate', '#', NULL, NULL, 2, 'system:post:create', 3, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPost'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPostCreate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemPost' LIMIT 1), 'SystemPostUpdate', '#', NULL, NULL, 2, 'system:post:update', 4, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPost'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPostUpdate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemPost' LIMIT 1), 'SystemPostDelete', '#', NULL, NULL, 2, 'system:post:delete', 5, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPost'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemPostDelete'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'System' LIMIT 1), 'SystemRole', 'role', 'system/role/list', 'mdi:account-group', 1, NULL, 4, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'System'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRole'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemRole' LIMIT 1), 'SystemRoleList', '#', NULL, NULL, 2, 'system:role:list', 1, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRole'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRoleList'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemRole' LIMIT 1), 'SystemRoleQuery', '#', NULL, NULL, 2, 'system:role:query', 2, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRole'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRoleQuery'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemRole' LIMIT 1), 'SystemRoleCreate', '#', NULL, NULL, 2, 'system:role:create', 3, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRole'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRoleCreate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemRole' LIMIT 1), 'SystemRoleUpdate', '#', NULL, NULL, 2, 'system:role:update', 4, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRole'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRoleUpdate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemRole' LIMIT 1), 'SystemRoleDelete', '#', NULL, NULL, 2, 'system:role:delete', 5, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRole'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemRoleDelete'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'System' LIMIT 1), 'SystemUser', 'user', 'system/user/list', 'mdi:account-outline', 1, NULL, 5, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'System'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUser'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemUser' LIMIT 1), 'SystemUserList', '#', NULL, NULL, 2, 'system:user:list', 1, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUser'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUserList'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemUser' LIMIT 1), 'SystemUserQuery', '#', NULL, NULL, 2, 'system:user:query', 2, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUser'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUserQuery'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemUser' LIMIT 1), 'SystemUserCreate', '#', NULL, NULL, 2, 'system:user:create', 3, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUser'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUserCreate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemUser' LIMIT 1), 'SystemUserUpdate', '#', NULL, NULL, 2, 'system:user:update', 4, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUser'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUserUpdate'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemUser' LIMIT 1), 'SystemUserDelete', '#', NULL, NULL, 2, 'system:user:delete', 5, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUser'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUserDelete'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemUser' LIMIT 1), 'SystemUserResetPassword', '#', NULL, NULL, 2, 'system:user:reset-password', 6, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUser'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemUserResetPassword'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'System' LIMIT 1), 'SystemLog', 'log', 'system/log/list', 'mdi:clipboard-text-clock-outline', 1, NULL, 6, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'System'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemLog'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemLog' LIMIT 1), 'SystemLogList', '#', NULL, NULL, 2, 'system:log:list', 1, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemLog'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemLogList'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemLog' LIMIT 1), 'SystemLogQuery', '#', NULL, NULL, 2, 'system:log:query', 2, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemLog'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemLogQuery'
);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, type, auth_code, sort, status, hidden, deleted)
SELECT REPLACE(UUID(), '-', ''), (SELECT id FROM sys_menu WHERE name = 'SystemLog' LIMIT 1), 'SystemLogDelete', '#', NULL, NULL, 2, 'system:log:delete', 3, 0, 0, 0
FROM DUAL
WHERE EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemLog'
)
  AND NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE name = 'SystemLogDelete'
);

-- 管理员用户
INSERT INTO sys_user (id, username, password, nick_name, status, dept_id, remark, deleted)
SELECT REPLACE(UUID(), '-', ''), 'admin', '$2a$10$1I2ZrJ.fpnv2XKiSqS0loeM6rcBtODeBdUMmfvxp242cXtLUv8iO2', '超级管理员', 0,
       (SELECT id FROM sys_dept WHERE name = '总公司' AND (parent_id IS NULL OR parent_id = '' OR parent_id = '0') LIMIT 1),
       '系统内置管理员账户', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'admin'
);

UPDATE sys_user
SET dept_id = (
    SELECT id FROM sys_dept WHERE name = '总公司' AND (parent_id IS NULL OR parent_id = '' OR parent_id = '0') LIMIT 1
)
WHERE username = 'admin'
  AND (dept_id IS NULL OR dept_id = '');

-- 关联 admin 用户与 admin 角色
INSERT INTO sys_user_role (id, user_id, role_id)
SELECT REPLACE(UUID(), '-', ''), u.id, r.id
FROM sys_user u
INNER JOIN sys_role r ON r.code = 'admin'
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1
    FROM sys_user_role ur
    WHERE ur.user_id = u.id
      AND ur.role_id = r.id
);

-- admin 角色拥有当前全部菜单权限
DELETE rm
FROM sys_role_menu rm
INNER JOIN sys_role r ON rm.role_id = r.id
WHERE r.code = 'admin';

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT REPLACE(UUID(), '-', ''), r.id, m.id
FROM sys_role r
INNER JOIN sys_menu m
WHERE r.code = 'admin';
