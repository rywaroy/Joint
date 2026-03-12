-- Joint 数据库初始化脚本
-- 对应 Phase 2: 数据库集成

CREATE DATABASE IF NOT EXISTS joint DEFAULT CHARACTER SET utf8mb4;

USE joint;

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
