package com.bookstore.admin.controller;

/**
 * 原始页面控制器（已废弃） - 保留此类仅用于向后兼容引用
 *
 * 本控制器原有的所有功能已拆分到以下独立控制器中：
 *
 * - {@link FrontAuthController} - 用户认证（登录/注册/登出）
 * - {@link FrontPageController} - 前台页面（首页/搜索/购物车/订单/支付）
 * - {@link UserCenterController} - 用户中心（个人资料/评价/消息/静态页面）
 * - {@link AdminPageController} - 管理后台（仪表盘/商品/用户/订单/优惠券/日志等）
 * - {@link RedirectController} - 路径兼容重定向和支付回调
 * - {@link BaseController} - 控制器基类，提供共用的工具方法（JSON解析/日期转换/管理员校验）
 *
 * 拆分原因：
 * 原始文件约2284行/119KB，职责过多，违反单一职责原则。
 * 拆分后每个控制器职责清晰，便于维护和团队协作。
 *
 * @deprecated 此类已废弃，所有路由已迁移到上述独立控制器。保留此类仅为避免外部硬编码引用报错。
 *             后续版本将完全删除此文件。
 */
@Deprecated
public class PageController {
    // 所有功能已迁移至独立控制器，此文件保留为空壳
    // 注意：不要在此类上添加 @Controller 注解，否则会与新控制器产生路由冲突
}
