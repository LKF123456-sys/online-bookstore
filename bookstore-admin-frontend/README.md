# BookVerse 管理后台

> BookVerse 在线书店管理后台，基于 Vue 3 + TypeScript + Naive UI + ECharts 构建

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 前端框架（Composition API） |
| TypeScript | 5.4+ | 类型安全 |
| Vite | 5+ | 构建工具 |
| Vue Router | 4.3+ | 路由管理（管理员权限守卫） |
| Pinia | 2.1+ | 状态管理 |
| Axios | 1.7+ | HTTP 客户端 |
| Naive UI | latest | UI 组件库 |
| ECharts | 5+ | 数据可视化图表 |

## 页面

| 页面 | 路由 | 说明 |
|------|------|------|
| 管理员登录 | `/admin/login` | 管理员身份验证（role=admin） |
| 数据大屏 | `/admin/dashboard` | 统计卡片、订单状态饼图、热销TOP10柱状图、低库存预警、操作日志 |
| 商品管理 | `/admin/products` | 数据表格、搜索、新增/编辑弹窗、上下架、删除 |
| 订单管理 | `/admin/orders` | 状态筛选、详情抽屉、发货操作 |
| 用户管理 | `/admin/users` | 搜索、启用/禁用、删除 |
| 分类管理 | `/admin/categories` | 分类列表 |
| 优惠券管理 | `/admin/coupons` | 新增/编辑、启用/禁用、删除 |
| 评价管理 | `/admin/reviews` | 屏蔽/取消屏蔽、置顶、回复、删除 |
| 公告管理 | `/admin/announcements` | 新增/编辑/删除 |
| 消息管理 | `/admin/messages` | 消息列表、系统广播 |
| 操作日志 | `/admin/logs` | 只读日志表格、搜索、详情 |

## 开发

```bash
npm install
npm run dev
```

开发服务器启动在 `http://localhost:5174`，`/api` 和 `/admin` 请求通过 Vite 代理转发到后端网关 `http://localhost:8080`。

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
├── api/              # API 模块（10个模块）
├── assets/           # 静态资源和样式
├── router/           # 路由配置（管理员权限守卫）
├── stores/           # Pinia 状态管理
├── types/            # TypeScript 类型定义
├── views/            # 页面组件（12个页面）
│   ├── Dashboard.vue
│   ├── Login.vue
│   ├── Layout.vue
│   └── ...
├── App.vue           # 根组件
└── main.ts           # 入口文件
```
