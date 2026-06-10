# BookVerse User Frontend

> BookVerse 在线书店用户前台，基于 Vue 3 + TypeScript + Naive UI 构建

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 前端框架（Composition API） |
| TypeScript | 5.4+ | 类型安全 |
| Vite | 5+ | 构建工具 |
| Vue Router | 4.3+ | 路由管理（懒加载 + 路由守卫） |
| Pinia | 2.1+ | 状态管理 |
| Axios | 1.7+ | HTTP 客户端（请求/响应拦截器） |
| Naive UI | latest | UI 组件库 |

## 页面

| 页面 | 路由 | 说明 |
|------|------|------|
| 首页 | `/` | 公告轮播、分类导航、热门商品、推荐商品 |
| 商品列表 | `/products` | 搜索、分类筛选、排序、分页 |
| 商品详情 | `/product/:id` | 商品信息、评价、推荐、加入购物车 |
| 登录 | `/login` | 用户名密码登录 |
| 注册 | `/register` | 新用户注册 |
| 购物车 | `/cart` | 数量调整、删除、清空、合计 |
| 结算 | `/checkout` | 收货地址、优惠券、订单确认 |
| 订单列表 | `/orders` | 状态筛选、支付/取消/确认收货 |
| 订单详情 | `/order/:id` | 订单信息、商品列表、支付明细 |
| 个人中心 | `/profile` | 查看/编辑个人信息、修改密码 |
| 消息中心 | `/messages` | 消息列表、标记已读 |
| 优惠券 | `/coupons` | 可领优惠券、我的优惠券 |
| 我的评价 | `/reviews` | 评价列表、删除评价 |

## 开发

```bash
npm install
npm run dev
```

开发服务器启动在 `http://localhost:5175`，`/api` 请求通过 Vite 代理转发到后端网关 `http://localhost:8080`。

## 构建

```bash
npm run build
```

## 后端接口

所有 API 通过网关（:8080）统一暴露，返回格式：

```json
{ "code": 200, "message": "success", "data": "<actual data>" }
```

## 项目结构

```
src/
├── api/              # API 模块（auth、user、product、cart、order 等）
├── assets/           # 静态资源和样式
├── components/       # 公共组件（AppHeader、AppFooter、ProductCard）
├── layouts/          # 布局组件（DefaultLayout）
├── pages/            # 页面组件（13 个页面）
├── router/           # 路由配置（懒加载 + 路由守卫）
├── stores/           # Pinia 状态管理（auth、cart、message）
├── types/            # TypeScript 类型定义
├── App.vue           # 根组件
└── main.ts           # 入口文件
```
