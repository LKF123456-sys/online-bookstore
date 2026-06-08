package com.bookstore.admin.controller; // 声明当前类所在的包路径：控制器层

// 导入BigDecimal类，用于精确处理金额计算（避免浮点数精度丢失）
import java.math.BigDecimal;
// 导入SimpleDateFormat类，用于日期字符串的格式化和解析
import java.text.SimpleDateFormat;
// 导入ArrayList类，用于创建动态数组列表
import java.util.ArrayList;
// 导入Date类，表示日期和时间
import java.util.Date;
// 导入HashMap类，用于创建键值对映射（字典/哈希表）
import java.util.HashMap;
// 导入List接口，表示有序集合
import java.util.List;
// 导入Map接口，表示键值对映射接口
import java.util.Map;

// 导入Spring的HTTP实体类，封装请求头和请求体
import org.springframework.http.HttpEntity;
// 导入Spring的HTTP请求头类，用于设置请求头信息
import org.springframework.http.HttpHeaders;
// 导入Spring的HTTP方法枚举（GET、POST、PUT、DELETE等）
import org.springframework.http.HttpMethod;
// 导入Spring的媒体类型类，用于设置Content-Type
import org.springframework.http.MediaType;
// 导入Spring的HTTP响应实体类，封装响应状态码和响应体
import org.springframework.http.ResponseEntity;
// 导入Spring MVC的@Controller注解，标记这是一个控制器类，返回视图名称
import org.springframework.stereotype.Controller;
// 导入Spring MVC的Model接口，用于向视图传递数据
import org.springframework.ui.Model;
// 导入@GetMapping注解，处理HTTP GET请求
import org.springframework.web.bind.annotation.GetMapping;
// 导入@PostMapping注解，处理HTTP POST请求
import org.springframework.web.bind.annotation.PostMapping;
// 导入@RequestParam注解，用于绑定请求参数到方法参数
import org.springframework.web.bind.annotation.RequestParam;
// 导入RestTemplate类，用于发送HTTP请求调用其他微服务
import org.springframework.web.client.RestTemplate;
// 导入RedirectAttributes类，用于在重定向时传递Flash属性（一次性数据）
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 导入操作日志服务类，用于记录管理员操作日志
import com.bookstore.admin.service.AdminLogService;
// 导入分页结果类，用于封装分页查询的结果
import com.bookstore.common.api.vo.PageResult;
// 导入Jackson的ObjectMapper类，用于JSON字符串与Java对象之间的转换
import com.fasterxml.jackson.databind.ObjectMapper;

// 导入Jakarta Servlet的HttpSession接口，用于管理用户会话
import jakarta.servlet.http.HttpSession;
// 导入Lombok的@RequiredArgsConstructor注解，自动生成包含final字段的构造方法
import lombok.RequiredArgsConstructor;

/**
 * 前端页面控制器 - 处理所有前端页面的视图请求
 *
 * 这个控制器是整个书店系统的"页面路由中心"，负责：
 * 1. 用户认证：登录、注册、登出
 * 2. 前台页面：首页、商品详情、购物车、订单、支付
 * 3. 用户中心：个人资料、优惠券、消息
 * 4. 管理后台：商品管理、订单管理、用户管理、优惠券管理等
 * 5. 静态页面：关于我们、帮助中心、退换货政策等
 *
 * 工作原理：
 * - 通过RestTemplate调用其他微服务（商品服务、订单服务、用户服务等）获取数据
 * - 将数据放入Model中，传递给JSP视图模板进行渲染
 * - 返回JSP视图名称，由Spring MVC的视图解析器找到对应的JSP文件
 */
@Controller // 标记这是一个Spring MVC控制器，返回值会被当作视图名称解析
@RequiredArgsConstructor // 使用Lombok自动生成包含final字段的构造方法（依赖注入）
public class PageController {

    // RestTemplate用于向其他微服务发送HTTP请求（通过服务名进行负载均衡调用）
    private final RestTemplate restTemplate;
    // 操作日志服务，用于记录管理员的操作记录
    private final AdminLogService adminLogService;
    // Jackson的ObjectMapper实例，用于将JSON字符串转换为Java对象
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 递归将Map中的日期字符串转换为java.util.Date对象
     * 解决JSP中使用JSTL的fmt:formatDate标签时类型不匹配的问题
     *
     * 为什么需要这个方法？
     * 从微服务API获取的数据是JSON格式，日期会被序列化为字符串（如"2024-01-15 10:30:00"）。
     * 但JSP的fmt:formatDate标签需要java.util.Date类型的对象才能正确格式化显示。
     * 所以需要在Controller层把日期字符串预先转换为Date对象。
     *
     * @param data 要处理的数据对象，可以是Map、List或其他类型
     */
    @SuppressWarnings("unchecked") // 抑制泛型转换的编译警告
    private void deepConvertDates(Object data) {
        if (data == null) return; // 如果数据为空，直接返回，不做任何处理
        if (data instanceof Map) { // 如果数据是Map类型，遍历所有键值对
            Map<String, Object> map = (Map<String, Object>) data; // 将Object强制转换为Map
            for (Map.Entry<String, Object> entry : map.entrySet()) { // 遍历Map中的每一个键值对
                Object value = entry.getValue(); // 获取当前键对应的值
                if (value instanceof String) { // 如果值是字符串类型
                    String str = (String) value; // 将值转换为字符串
                    // 检查字符串是否匹配日期时间格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd HH:mm
                    if (str.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}") ||
                        str.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                        try {
                            // 根据字符串长度选择对应的日期格式
                            SimpleDateFormat sdf = new SimpleDateFormat(
                                str.length() == 19 ? "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd HH:mm");
                            entry.setValue(sdf.parse(str)); // 将日期字符串解析为Date对象并替换原值
                        } catch (Exception ignored) {} // 解析失败时忽略，保持原值
                    }
                } else if (value instanceof Map) { // 如果值是Map类型，递归处理
                    deepConvertDates(value);
                } else if (value instanceof List) { // 如果值是List类型，遍历每个元素并递归处理
                    for (Object item : (List<?>) value) {
                        deepConvertDates(item);
                    }
                }
            }
        } else if (data instanceof List) { // 如果数据本身是List类型，遍历并递归处理
            for (Object item : (List<?>) data) {
                deepConvertDates(item);
            }
        }
    }

    /**
     * 将JSON字符串解析为Map对象，并自动转换其中的日期字符串
     *
     * @param json JSON格式的字符串
     * @return 解析后的Map对象，日期字段已被转换为java.util.Date类型
     * @throws Exception JSON解析失败时抛出异常
     */
    private Map<String, Object> parseJson(String json) throws Exception {
        Map<String, Object> result = objectMapper.readValue(json, Map.class); // JSON字符串解析为Map
        deepConvertDates(result); // 递归转换Map中所有日期字符串为Date对象
        return result; // 返回处理后的Map对象
    }

    // ======================== 认证处理（登录/注册） ========================

    /**
     * 处理前台用户登录请求
     * 将用户名和密码发送到用户服务进行验证，验证成功后将token和用户信息存入Session
     *
     * @param userid 用户输入的用户名/账号
     * @param password 用户输入的密码
     * @param session 当前HTTP会话，用于存储登录状态
     * @param redirectAttributes 重定向属性，用于在重定向时传递错误消息
     * @return 登录成功跳转首页，失败跳转登录页
     */
    @PostMapping("/login") // 处理POST /login请求（前台用户登录）
    public String doLogin(@RequestParam String userid, @RequestParam String password,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> body = new HashMap<>(); // 创建请求体Map
            body.put("username", userid); // 设置用户名
            body.put("password", password); // 设置密码
            HttpHeaders headers = new HttpHeaders(); // 创建HTTP请求头
            headers.setContentType(MediaType.APPLICATION_JSON); // 告诉服务端请求体是JSON格式
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers); // 封装请求实体
            // 通过RestTemplate向用户服务发送登录请求（http://bookstore-user是服务名，通过Nacos自动解析）
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://bookstore-user/api/auth/login", entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) { // 检查响应状态码是否为2xx（成功）
                Map<String, Object> result = parseJson(response.getBody()); // 将响应体JSON解析为Map
                Map<String, Object> data = (Map<String, Object>) result.get("data"); // 取出data字段
                session.setAttribute("token", data.get("token")); // 将JWT token存入Session
                session.setAttribute("user", data.get("user")); // 将用户信息存入Session
                return "redirect:/"; // 登录成功，重定向到首页
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "用户名或密码错误"); // 添加Flash错误消息
            return "redirect:/login"; // 重定向回登录页面
        }
        redirectAttributes.addFlashAttribute("error", "用户名或密码错误"); // 登录失败的错误消息
        return "redirect:/login"; // 重定向回登录页面
    }

    /**
     * 处理前台用户注册请求，将注册信息发送到用户服务创建新账号
     *
     * @param userid 用户名/账号
     * @param password 密码
     * @param email 邮箱地址
     * @param phone 手机号码（可选）
     * @param redirectAttributes 重定向属性，用于传递成功或失败消息
     * @return 注册成功跳转登录页，失败跳转注册页
     */
    @PostMapping("/register") // 处理POST /register请求（用户注册）
    public String doRegister(@RequestParam String userid, @RequestParam String password,
                             @RequestParam String email, @RequestParam(required = false) String phone,
                             RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> body = new HashMap<>(); // 创建请求体Map
            body.put("username", userid); // 设置用户名
            body.put("password", password); // 设置密码
            body.put("email", email); // 设置邮箱
            body.put("phone", phone != null ? phone : ""); // 设置手机号，null时设为空字符串
            HttpHeaders headers = new HttpHeaders(); // 创建HTTP请求头
            headers.setContentType(MediaType.APPLICATION_JSON); // 设置内容类型为JSON
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers); // 封装请求实体
            // 向用户服务发送注册请求
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://bookstore-user/api/auth/register", entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) { // 检查注册是否成功
                redirectAttributes.addFlashAttribute("msg", "注册成功，请登录"); // 添加成功消息
                return "redirect:/login"; // 重定向到登录页面
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "注册失败: " + e.getMessage()); // 添加错误消息
            return "redirect:/register"; // 重定向回注册页面
        }
        redirectAttributes.addFlashAttribute("error", "注册失败"); // 注册失败的错误消息
        return "redirect:/register"; // 重定向回注册页面
    }

    // ======================== 前台页面（首页、商品详情、购物车等） ========================

    /** 浏览器favicon.ico请求，重定向到SVG格式图标 */
    @GetMapping("/favicon.ico") // 处理GET /favicon.ico请求
    public String favicon() {
        return "redirect:/favicon.svg"; // 重定向到SVG格式的图标文件
    }

    /**
     * 首页 - 书店主页，展示分类、推荐商品、热销商品和商品列表
     * 支持按关键词搜索和按分类筛选
     *
     * @param model 用于向JSP视图传递数据的Model对象
     * @param keyword 搜索关键词（可选）
     * @param category 分类ID（可选）
     * @return 首页视图名称"index"
     */
    @GetMapping("/") // 处理GET /请求（网站首页）
    public String index(Model model,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String category) {
        // --- 获取商品分类列表 ---
        try {
            String catResp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class); // 请求分类数据
            Map<String, Object> catResult = parseJson(catResp); // 解析JSON
            if (catResult.get("data") != null) {
                model.addAttribute("categories", catResult.get("data")); // 将分类列表存入Model
            } else {
                model.addAttribute("categories", new ArrayList<>()); // 无数据时存入空列表
            }
        } catch (Exception e) {
            model.addAttribute("categories", new ArrayList<>()); // 请求失败时存入空列表
        }
        // --- 获取推荐商品（最多8个） ---
        try {
            String recResp = restTemplate.getForObject("http://bookstore-product/api/product/recommend?limit=8", String.class); // 请求推荐商品
            Map<String, Object> recResult = parseJson(recResp); // 解析JSON
            if (recResult.get("data") != null) {
                model.addAttribute("recommended", recResult.get("data")); // 存入推荐商品
            } else {
                model.addAttribute("recommended", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("recommended", new ArrayList<>());
        }
        // --- 获取热销商品（最多8个） ---
        try {
            String hotResp = restTemplate.getForObject("http://bookstore-product/api/product/hot?limit=8", String.class); // 请求热销商品
            Map<String, Object> hotResult = parseJson(hotResp); // 解析JSON
            if (hotResult.get("data") != null) {
                model.addAttribute("bestsellers", hotResult.get("data")); // 存入热销商品
            } else {
                model.addAttribute("bestsellers", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("bestsellers", new ArrayList<>());
        }
        // --- 获取商品列表（分页，每页12个） ---
        try {
            String url = "http://bookstore-product/api/product/list?pageNum=1&pageSize=12"; // 构建请求URL
            if (keyword != null && !keyword.isEmpty()) url += "&keyword=" + keyword; // 追加搜索关键词
            if (category != null && !category.isEmpty()) url += "&category=" + category; // 追加分类筛选
            String prodResp = restTemplate.getForObject(url, String.class); // 发送请求获取商品列表
            Map<String, Object> prodResult = parseJson(prodResp); // 解析JSON
            if (prodResult.get("data") != null) {
                Map<String, Object> pageData = (Map<String, Object>) prodResult.get("data"); // 取分页数据
                model.addAttribute("products", pageData.getOrDefault("records", new ArrayList<>())); // 取商品记录列表
            } else {
                model.addAttribute("products", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("products", new ArrayList<>());
        }
        model.addAttribute("announcementList", new ArrayList<>()); // 公告列表（暂为空）
        model.addAttribute("newArrivals", new ArrayList<>()); // 新品上架列表（暂为空）
        model.addAttribute("keyword", keyword); // 回显搜索关键词到页面
        model.addAttribute("selectedCategory", category); // 回显当前选中的分类
        model.addAttribute("cartSize", 0); // 购物车数量（暂设为0）
        return "index"; // 返回视图名，对应/WEB-INF/views/index.jsp
    }

    /** /index路径重定向到首页，兼容前端代码中使用/index访问首页的情况 */
    @GetMapping("/index") // 处理GET /index请求
    public String indexPage() {
        return "redirect:/"; // 重定向到首页根路径
    }

    /**
     * 搜索页面 - 根据关键词搜索商品
     *
     * @param keyword 搜索关键词（必填）
     * @param model Model对象
     * @return 首页视图（搜索结果复用首页模板）
     */
    @GetMapping("/search") // 处理GET /search请求（商品搜索）
    public String search(@RequestParam String keyword, Model model) {
        try {
            String url = "http://bookstore-product/api/product/list?pageNum=1&pageSize=20&keyword=" + keyword; // 搜索URL
            String prodResp = restTemplate.getForObject(url, String.class); // 发送搜索请求
            Map<String, Object> prodResult = parseJson(prodResp); // 解析JSON
            if (prodResult.get("data") != null) {
                Map<String, Object> pageData = (Map<String, Object>) prodResult.get("data");
                model.addAttribute("products", pageData.getOrDefault("records", new ArrayList<>())); // 搜索结果
            } else {
                model.addAttribute("products", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("products", new ArrayList<>());
        }
        try {
            String catResp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class); // 获取分类
            Map<String, Object> catResult = parseJson(catResp);
            if (catResult.get("data") != null) {
                model.addAttribute("categories", catResult.get("data")); // 分类数据
            } else {
                model.addAttribute("categories", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("categories", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword); // 回显搜索关键词
        model.addAttribute("announcementList", new ArrayList<>()); // 公告列表
        model.addAttribute("bestsellers", new ArrayList<>()); // 热销商品
        model.addAttribute("newArrivals", new ArrayList<>()); // 新品
        model.addAttribute("recommended", new ArrayList<>()); // 推荐商品
        model.addAttribute("cartSize", 0); // 购物车数量
        return "index"; // 复用首页模板
    }

    /** 登录页面 */
    @GetMapping("/login") // 处理GET /login请求
    public String loginPage() {
        return "login"; // 返回/WEB-INF/views/login.jsp
    }

    /** 注册页面 */
    @GetMapping("/register") // 处理GET /register请求
    public String registerPage() {
        return "register"; // 返回/WEB-INF/views/register.jsp
    }

    /**
     * 商品详情页面 - 展示单个商品的详细信息、分类列表和推荐商品
     *
     * @param id 商品ID（必填）
     * @param model Model对象
     * @return 商品详情页视图
     */
    @GetMapping("/product/detail") // 处理GET /product/detail请求
    public String productDetail(@RequestParam String id, Model model) {
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/product/" + id, String.class); // 获取商品详情
            Map<String, Object> result = parseJson(resp);
            if (result.get("data") != null) {
                model.addAttribute("product", result.get("data")); // 存入商品详情
            } else {
                model.addAttribute("product", new HashMap<>()); // 商品不存在
            }
        } catch (Exception e) {
            model.addAttribute("product", new HashMap<>()); // 请求失败
        }
        try {
            String catResp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class); // 获取分类
            Map<String, Object> catResult = parseJson(catResp);
            if (catResult.get("data") != null) {
                model.addAttribute("categories", catResult.get("data")); // 存入分类数据
            } else {
                model.addAttribute("categories", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("categories", new ArrayList<>());
        }
        try {
            String recResp = restTemplate.getForObject("http://bookstore-product/api/product/recommend?limit=4", String.class); // 推荐商品
            Map<String, Object> recResult = parseJson(recResp);
            if (recResult.get("data") != null) {
                model.addAttribute("relatedBooks", recResult.get("data")); // 存入相关书籍
            } else {
                model.addAttribute("relatedBooks", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("relatedBooks", new ArrayList<>());
        }
        model.addAttribute("guessYouLike", new ArrayList<>()); // "猜你喜欢"列表
        model.addAttribute("cartSize", 0); // 购物车数量
        return "product_detail"; // 返回商品详情页视图
    }

    /**
     * 购物车页面 - 展示当前用户的购物车内容
     * 需要用户登录才能查看购物车，从订单服务获取购物车数据
     *
     * @param model Model对象
     * @param session HTTP会话，用于获取登录用户信息
     * @return 购物车页视图
     */
    @GetMapping("/cart") // 处理GET /cart请求
    public String cart(Model model, HttpSession session) {
        model.addAttribute("cartSize", 0); // 初始化购物车数量为0
        model.addAttribute("cart", new ArrayList<>()); // 初始化购物车列表为空
        model.addAttribute("recommendedBooks", new ArrayList<>()); // 初始化推荐书籍为空
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user"); // 从Session获取用户信息
        if (user != null) { // 如果用户已登录
            String userId = (String) user.get("userid"); // 获取用户ID
            if (userId != null) {
                try {
                    HttpHeaders headers = new HttpHeaders(); // 创建请求头
                    headers.set("X-User-Id", userId); // 携带用户ID（订单服务需要此ID获取该用户的购物车）
                    HttpEntity<Void> entity = new HttpEntity<>(headers); // 封装请求实体
                    ResponseEntity<String> response = restTemplate.exchange(
                        "http://bookstore-order/api/cart", HttpMethod.GET, entity, String.class); // 获取购物车数据
                    if (response.getStatusCode().is2xxSuccessful()) { // 请求成功
                        Map<String, Object> result = parseJson(response.getBody()); // 解析JSON
                        Map<String, Object> data = (Map<String, Object>) result.get("data"); // 取data字段
                        if (data != null) {
                            java.util.List<?> items = (java.util.List<?>) data.get("items"); // 获取商品项列表
                            if (items != null) {
                                model.addAttribute("cart", items); // 存入购物车商品
                                model.addAttribute("cartSize", items.size()); // 设置商品数量
                            }
                        }
                    }
                } catch (Exception e) {
                    // 购物车加载失败，显示空购物车（不中断页面渲染）
                }
            }
        }
        try { // 获取推荐书籍（不论是否登录都展示）
            String recResp = restTemplate.getForObject("http://bookstore-product/api/product/recommend?limit=4", String.class);
            Map<String, Object> recResult = parseJson(recResp);
            if (recResult.get("data") != null) {
                model.addAttribute("recommendedBooks", recResult.get("data")); // 存入推荐书籍
            }
        } catch (Exception e) {
            model.addAttribute("recommendedBooks", new ArrayList<>());
        }
        return "cart"; // 返回购物车页视图
    }

    /**
     * 购物车商品数量更新（AJAX接口）
     *
     * @param productId 商品ID
     * @param quantity 新的数量值
     * @param session HTTP会话
     * @return JSON响应，包含success状态和message消息
     */
    @PostMapping("/cart/update") // 处理POST /cart/update请求
    @org.springframework.web.bind.annotation.ResponseBody // 返回值直接作为HTTP响应体（JSON）
    public Map<String, Object> updateCartAjax(
            @RequestParam String productId,
            @RequestParam int quantity,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>(); // 创建响应结果Map
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user"); // 获取用户信息
        String userId = user != null ? (String) user.get("userid") : null; // 获取用户ID
        if (userId == null) { // 未登录
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        try {
            Map<String, Object> body = new HashMap<>(); // 请求体
            body.put("productId", productId); // 商品ID
            body.put("quantity", quantity); // 新数量
            HttpHeaders headers = new HttpHeaders(); // 请求头
            headers.setContentType(MediaType.APPLICATION_JSON); // JSON格式
            headers.set("X-User-Id", userId); // 携带用户ID
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers); // 封装实体
            restTemplate.put("http://bookstore-order/api/cart/item", entity); // PUT请求更新数量
            result.put("success", true); // 更新成功
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败");
        }
        return result; // 返回JSON
    }

    /**
     * 删除购物车中的某个商品
     *
     * @param productId 要删除的商品ID
     * @param session HTTP会话
     * @return 重定向回购物车页面
     */
    @GetMapping("/cart/remove") // 处理GET /cart/remove请求
    public String removeCartItem(@RequestParam String productId, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user"); // 获取用户信息
        String userId = user != null ? (String) user.get("userid") : null; // 获取用户ID
        if (userId != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId); // 携带用户ID
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                restTemplate.exchange("http://bookstore-order/api/cart/item/" + productId,
                    HttpMethod.DELETE, entity, String.class); // DELETE请求删除商品
            } catch (Exception ignored) {} // 删除失败时静默处理
        }
        return "redirect:/cart"; // 重定向回购物车
    }

    /**
     * 清空购物车中的所有商品
     *
     * @param session HTTP会话
     * @return 重定向回购物车页面
     */
    @GetMapping("/cart/clear") // 处理GET /cart/clear请求
    public String clearCart(HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user"); // 获取用户信息
        String userId = user != null ? (String) user.get("userid") : null; // 获取用户ID
        if (userId != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                restTemplate.exchange("http://bookstore-order/api/cart/clear",
                    HttpMethod.DELETE, entity, String.class); // DELETE请求清空购物车
            } catch (Exception ignored) {}
        }
        return "redirect:/cart"; // 重定向回购物车
    }

    /**
     * 添加商品到购物车（AJAX接口）
     * 支持通过productId或id参数指定商品，兼容不同前端调用方式
     *
     * @param productId 商品ID（方式1，可选）
     * @param id 商品ID（方式2，可选，与productId二选一）
     * @param quantity 添加数量，默认为1
     * @param session HTTP会话
     * @return JSON响应
     */
    @GetMapping("/cart/add/ajax") // 处理GET /cart/add/ajax请求
    @org.springframework.web.bind.annotation.ResponseBody // 返回JSON
    public Map<String, Object> addToCartAjax(
            @RequestParam(value = "productId", required = false) String productId,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>(); // 响应结果
        String idToUse = productId != null ? productId : id; // 确定使用哪个ID（优先productId）
        if (idToUse == null) { // 两个ID都没传
            result.put("success", false);
            result.put("message", "参数错误");
            return result;
        }
        try {
            Map<String, Object> user = (Map<String, Object>) session.getAttribute("user"); // 获取用户信息
            String userId = user != null ? (String) user.get("userid") : null; // 获取用户ID
            if (userId == null) { // 未登录
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            Map<String, Object> body = new HashMap<>(); // 请求体
            body.put("productId", idToUse); // 商品ID
            body.put("quantity", quantity); // 数量
            HttpHeaders headers = new HttpHeaders(); // 请求头
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", userId); // 携带用户ID
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://bookstore-order/api/cart", entity, String.class); // POST请求添加到购物车
            if (response.getStatusCode().is2xxSuccessful()) {
                result.put("success", true); // 添加成功
                result.put("message", "已加入购物车");
            } else {
                result.put("success", false); // 添加失败
                result.put("message", "添加失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "请先登录");
        }
        return result;
    }

    /**
     * 订单确认页面 - 显示购物车内容并填写收货地址，需要登录
     *
     * @param model Model对象
     * @param session HTTP会话
     * @return 订单确认页或重定向到登录页
     */
    @GetMapping("/order") // 处理GET /order请求
    public String order(Model model, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user"); // 获取用户信息
        if (user == null) {
            return "redirect:/login"; // 未登录，重定向到登录页
        }
        model.addAttribute("user", user); // 将用户信息存入Model
        String userId = (String) user.get("userid"); // 获取用户ID
        BigDecimal total = BigDecimal.ZERO; // 初始化总金额
        List<Map<String, Object>> cartItems = new ArrayList<>(); // 初始化购物车列表
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId); // 携带用户ID
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                "http://bookstore-order/api/cart", HttpMethod.GET, entity, String.class); // 获取购物车
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = parseJson(response.getBody());
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null && data.get("items") != null) {
                    cartItems = (List<Map<String, Object>>) data.get("items"); // 购物车商品列表
                    for (Map<String, Object> item : cartItems) { // 遍历计算总金额
                        Object subtotal = item.get("subtotal"); // 每个商品的小计
                        if (subtotal != null) {
                            total = total.add(new BigDecimal(subtotal.toString())); // 累加到总金额
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 购物车加载失败，显示空购物车
        }
        model.addAttribute("cart", cartItems); // 购物车商品
        model.addAttribute("total", total); // 总金额
        model.addAttribute("cartSize", cartItems.size()); // 商品数量
        return "order"; // 返回订单确认页
    }

    /**
     * 提交订单 - 将购物车内容生成正式订单
     *
     * @param addr1 收货地址
     * @param city 城市
     * @param state 省份/州
     * @param zip 邮政编码
     * @param couponCode 优惠券代码（可选）
     * @param session HTTP会话
     * @return 成功跳转支付页，失败重定向回订单页
     */
    @PostMapping("/order/submit") // 处理POST /order/submit请求
    public String submitOrder(@RequestParam String addr1,
                              @RequestParam String city,
                              @RequestParam String state,
                              @RequestParam String zip,
                              @RequestParam(required = false) String couponCode,
                              HttpSession session) {
        Map<String, Object> userSession = (Map<String, Object>) session.getAttribute("user"); // 获取用户信息
        if (userSession == null) {
            return "redirect:/login"; // 未登录
        }
        String userId = (String) userSession.get("userid"); // 用户ID
        try {
            // 第1步：获取购物车信息
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId); // 携带用户ID
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> cartResp = restTemplate.exchange(
                "http://bookstore-order/api/cart", HttpMethod.GET, entity, String.class); // 获取购物车
            Map<String, Object> cartResult = parseJson(cartResp.getBody());
            Map<String, Object> cartData = (Map<String, Object>) cartResult.get("data");
            List<Map<String, Object>> items = new ArrayList<>(); // 订单项列表
            BigDecimal totalAmount = BigDecimal.ZERO; // 总金额
            if (cartData != null && cartData.get("items") != null) {
                List<Map<String, Object>> cartItems = (List<Map<String, Object>>) cartData.get("items");
                for (Map<String, Object> item : cartItems) { // 遍历购物车商品构建订单项
                    Map<String, Object> orderItem = new HashMap<>(); // 订单项DTO
                    orderItem.put("productId", item.get("productId")); // 商品ID
                    Object qty = item.get("quantity"); // 数量
                    orderItem.put("quantity", qty != null ? ((Number) qty).intValue() : 1); // 设置数量
                    items.add(orderItem); // 加入订单项列表
                    Object subtotal = item.get("subtotal"); // 小计
                    if (subtotal != null) {
                        totalAmount = totalAmount.add(new BigDecimal(subtotal.toString())); // 累加总金额
                    }
                }
            }
            if (items.isEmpty()) {
                return "redirect:/cart"; // 购物车为空，重定向回购物车
            }
            // 第2步：构建订单DTO
            Map<String, Object> orderBody = new HashMap<>();
            orderBody.put("items", items); // 订单商品列表
            orderBody.put("shipAddr1", addr1); // 收货地址
            orderBody.put("shipCity", city); // 城市
            orderBody.put("shipState", state); // 省份
            orderBody.put("shipZip", zip); // 邮编
            orderBody.put("shipToFirstName", userSession.getOrDefault("firstname", "")); // 收货人名
            orderBody.put("shipToLastName", userSession.getOrDefault("lastname", "")); // 收货人姓
            orderBody.put("billToFirstName", userSession.getOrDefault("firstname", "")); // 账单人名
            orderBody.put("billToLastName", userSession.getOrDefault("lastname", "")); // 账单人姓
            orderBody.put("billAddr1", addr1); // 账单地址
            orderBody.put("billCity", city); // 账单城市
            orderBody.put("billState", state); // 账单省份
            orderBody.put("billZip", zip); // 账单邮编
            // 第3步：处理优惠券
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                orderBody.put("couponName", couponCode.trim()); // 加入优惠券代码
            }
            // 第4步：提交订单
            HttpHeaders postHeaders = new HttpHeaders();
            postHeaders.setContentType(MediaType.APPLICATION_JSON); // JSON格式
            postHeaders.set("X-User-Id", userId); // 用户ID
            HttpEntity<Map<String, Object>> postEntity = new HttpEntity<>(orderBody, postHeaders);
            ResponseEntity<String> orderResp = restTemplate.postForEntity(
                "http://bookstore-order/api/order", postEntity, String.class); // 创建订单
            if (orderResp.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> orderResult = parseJson(orderResp.getBody());
                Map<String, Object> orderData = (Map<String, Object>) orderResult.get("data");
                if (orderData != null && orderData.get("orderid") != null) {
                    // 第5步：清空购物车
                    try {
                        restTemplate.exchange("http://bookstore-order/api/cart/clear",
                            HttpMethod.DELETE, entity, String.class); // 清空购物车
                    } catch (Exception ignored) {}
                    return "redirect:/payment?orderId=" + orderData.get("orderid"); // 跳转支付页
                }
            }
        } catch (Exception e) {
            return "redirect:/order?msg=error"; // 提交失败
        }
        return "redirect:/order?msg=error"; // 订单创建失败
    }

    /** 订单详情页面 */
    @GetMapping("/order/detail") // 处理GET /order/detail请求
    public String orderDetail() {
        return "order_detail"; // 返回订单详情页
    }

    /**
     * 订单历史页面 - 显示用户的所有历史订单
     *
     * @param model Model对象
     * @param session HTTP会话
     * @return 订单历史页视图
     */
    @GetMapping("/order/history") // 处理GET /order/history请求
    public String orderHistory(Model model, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user"); // 获取用户信息
        if (user != null) {
            String userId = (String) user.get("userid"); // 用户ID
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> resp = restTemplate.exchange(
                    "http://bookstore-order/api/order/list?pageNum=1&pageSize=50",
                    HttpMethod.GET, entity, String.class); // 获取订单列表（最多50条）
                Map<String, Object> result = parseJson(resp.getBody());
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null && data.get("records") != null) {
                    model.addAttribute("orderList", data.get("records")); // 存入订单列表
                } else {
                    model.addAttribute("orderList", new ArrayList<>());
                }
            } catch (Exception e) {
                model.addAttribute("orderList", new ArrayList<>());
            }
        } else {
            model.addAttribute("orderList", new ArrayList<>()); // 未登录
        }
        model.addAttribute("cartSize", 0);
        return "order_history"; // 返回订单历史页
    }

    /**
     * 支付页面 - 显示订单支付信息，需要登录
     *
     * @param orderId 订单ID（可选）
     * @param model Model对象
     * @param session HTTP会话
     * @return 支付页视图或重定向到登录页
     */
    @GetMapping("/payment") // 处理GET /payment请求
    public String payment(@RequestParam(required = false) String orderId, Model model, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user"); // 获取用户信息
        if (user == null) {
            return "redirect:/login"; // 未登录
        }
        if (orderId != null && !orderId.isEmpty()) { // 有订单ID
            String userId = (String) user.get("userid");
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> resp = restTemplate.exchange(
                    "http://bookstore-order/api/order/" + orderId,
                    HttpMethod.GET, entity, String.class); // 获取订单详情
                Map<String, Object> result = parseJson(resp.getBody());
                Map<String, Object> orderData = (Map<String, Object>) result.get("data");
                if (orderData != null) {
                    model.addAttribute("order", orderData); // 订单详情
                    model.addAttribute("orderId", orderId); // 订单ID
                    model.addAttribute("orderAmount", orderData.get("totalprice")); // 订单金额
                }
            } catch (Exception e) {
                model.addAttribute("order", new HashMap<>());
            }
        }
        return "payment"; // 返回支付页
    }

    /** 支付成功页面 */
    @GetMapping("/payment/success")
    public String paymentSuccess() { return "payment/success"; }

    /** 支付失败页面 */
    @GetMapping("/payment/fail")
    public String paymentFail() { return "payment/fail"; }

    /** 微信支付页面 */
    @GetMapping("/payment/wechat")
    public String paymentWechat() { return "payment/wechat"; }

    /** 支付宝支付页面 */
    @GetMapping("/payment/alipay")
    public String paymentAlipay() { return "payment/alipay"; }

    /** 银行卡支付页面 */
    @GetMapping("/payment/card")
    public String paymentCard() { return "payment/card"; }

    // ======================== 用户中心页面 ========================

    /** 用户个人资料页面 */
    @GetMapping("/user/profile")
    public String userProfile() { return "user/profile"; }

    /** 用户优惠券列表页面 */
    @GetMapping("/user/coupon")
    public String userCoupon() { return "user/coupon/list"; }

    /** 用户消息列表页面 */
    @GetMapping("/user/message")
    public String userMessage() { return "user/message/list"; }

    /** 商品评价页面 */
    @GetMapping("/review")
    public String review() { return "review"; }

    /** 评价管理页面 */
    @GetMapping("/review/manage")
    public String reviewManage() { return "review_manage"; }

    /** 配送说明页面 */
    @GetMapping("/shipping")
    public String shipping() { return "shipping"; }

    /** 退换货政策页面 */
    @GetMapping("/return-policy")
    public String returnPolicy() { return "return_policy"; }

    /** 帮助中心页面 */
    @GetMapping("/help")
    public String help() { return "help"; }

    /** 关于我们页面 */
    @GetMapping("/about")
    public String about() { return "about"; }

    /** 招聘信息页面 */
    @GetMapping("/careers")
    public String careers() { return "careers"; }

    /** 合作伙伴页面 */
    @GetMapping("/partners")
    public String partners() { return "partners"; }

    /** 投诉建议页面 */
    @GetMapping("/complaint")
    public String complaint() { return "complaint"; }

    /** 浏览历史页面 */
    @GetMapping("/history")
    public String history() { return "history"; }

    // ======================== 路径兼容重定向 ========================

    /** 我的优惠券路径兼容 -> /user/coupon */
    @GetMapping("/coupon/my")
    public String couponMy() { return "redirect:/user/coupon"; }

    /** 消息中心路径兼容 -> /user/message */
    @GetMapping("/message")
    public String message() { return "redirect:/user/message"; }

    /** 订单列表路径兼容 -> /order/history */
    @GetMapping("/orders")
    public String orders() { return "redirect:/order/history"; }

    /**
     * 支付回调处理 - 第三方支付平台完成支付后回调此接口
     *
     * @param orderId 订单ID
     * @param status 支付状态（如"success"）
     * @param paymentMethod 支付方式（可选）
     * @return 根据支付状态跳转到成功或失败页面
     */
    @GetMapping("/paymentCallback") // 处理GET /paymentCallback请求
    public String paymentCallback(@RequestParam String orderId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String paymentMethod) {
        if ("success".equals(status)) { // 支付成功
            return "redirect:/payment/success?orderId=" + orderId;
        }
        return "redirect:/payment/fail?orderId=" + orderId; // 支付失败
    }

    /** 特价商品路径兼容 -> 首页 */
    @GetMapping("/products/affordable")
    public String affordableProducts() { return "redirect:/"; }

    // ======================== 登出处理 ========================

    /**
     * 前台用户登出（POST），销毁Session清除登录状态
     *
     * @param session HTTP会话
     * @return 重定向到登录页
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 销毁Session，清除所有属性（token和用户信息）
        return "redirect:/login";
    }

    /** 前台用户登出（GET），兼容直接链接访问 */
    @GetMapping("/logout")
    public String logoutGet(HttpSession session) {
        session.invalidate(); // 销毁Session
        return "redirect:/login";
    }

    // ======================== 管理后台路径兼容重定向 ========================

    /** 管理后台根路径 -> /admin/index */
    @GetMapping("/admin/")
    public String adminRoot() { return "redirect:/admin/index"; }

    /** 管理后台商品列表路径兼容 -> /admin/product */
    @GetMapping("/admin/product/list")
    public String adminProductListAlias() { return "redirect:/admin/product"; }

    /** 管理后台订单列表路径兼容 -> /admin/order */
    @GetMapping("/admin/order/list")
    public String adminOrderListAlias() { return "redirect:/admin/order"; }

    /** 管理后台用户列表路径兼容 -> /admin/user */
    @GetMapping("/admin/user/list")
    public String adminUserListAlias() { return "redirect:/admin/user"; }

    /** 管理后台优惠券列表路径兼容 -> /admin/coupon */
    @GetMapping("/admin/coupon/list")
    public String adminCouponListAlias() { return "redirect:/admin/coupon"; }

    /** 管理后台公告列表路径兼容 -> /admin/announcement */
    @GetMapping("/admin/announcement/list")
    public String adminAnnouncementListAlias() { return "redirect:/admin/announcement"; }

    /** 管理后台评价列表路径兼容 -> /admin/review */
    @GetMapping("/admin/review/list")
    public String adminReviewListAlias() { return "redirect:/admin/review"; }

    /** 管理后台消息列表路径兼容 -> /admin/message */
    @GetMapping("/admin/message/list")
    public String adminMessageListAlias() { return "redirect:/admin/message"; }

    /**
     * 管理后台分类列表页面，从商品服务获取分类数据
     *
     * @param model Model对象
     * @return 分类列表管理页视图
     */
    @GetMapping("/admin/categories")
    public String adminCategoryList(Model model) {
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class); // 获取分类
            Map<String, Object> result = parseJson(resp);
            model.addAttribute("categoryList", result.get("data")); // 存入分类数据
        } catch (Exception e) {
            model.addAttribute("categoryList", new ArrayList<>()); // 失败时存空列表
        }
        return "admin/category/list"; // 返回分类管理页
    }

    // ======================== 管理后台认证 ========================

    /**
     * 管理员登录处理，验证用户名密码并检查管理员权限
     *
     * @param username 管理员用户名
     * @param password 管理员密码
     * @param session HTTP会话
     * @param redirectAttributes 重定向属性
     * @return 成功跳转管理后台首页，失败跳转管理员登录页
     */
    @PostMapping("/admin/login")
    public String doAdminLogin(@RequestParam String username, @RequestParam String password,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> body = new HashMap<>(); // 请求体
            body.put("username", username); // 用户名
            body.put("password", password); // 密码
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // JSON格式
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://bookstore-user/api/auth/login", entity, String.class); // 登录请求
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = parseJson(response.getBody());
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                Map<String, Object> user = (Map<String, Object>) data.get("user"); // 用户信息
                if (user != null && "admin".equals(user.get("role"))) { // 检查是否为管理员角色
                    session.setAttribute("token", data.get("token")); // 存入token
                    session.setAttribute("admin", user); // 存入管理员信息（用"admin"键区分普通用户）
                    return "redirect:/admin/index"; // 跳转管理后台首页
                }
                redirectAttributes.addFlashAttribute("error", "无管理员权限"); // 非管理员
                return "redirect:/admin/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
            return "redirect:/admin/login";
        }
        redirectAttributes.addFlashAttribute("error", "用户名或密码错误");
        return "redirect:/admin/login";
    }

    /** 管理员登出（POST），销毁Session */
    @PostMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate(); // 销毁Session
        return "redirect:/admin/login";
    }

    /** 管理员登出（GET），兼容链接访问 */
    @GetMapping("/admin/logout")
    public String adminLogoutGet(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // ======================== 管理后台页面 ========================

    /** 管理后台登录页面 */
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login"; // 返回管理员登录页
    }

    /**
     * 管理后台首页/仪表盘，展示统计数据和最近公告
     *
     * @param model Model对象
     * @param session HTTP会话
     * @return 管理后台首页视图
     */
    @GetMapping("/admin/index")
    public String adminIndex(Model model, HttpSession session) {
        loadDashboardData(model); // 加载核心统计数据（商品数、订单数、用户数、销售额等）
        loadDashboardExtras(model); // 加载辅助数据（低库存、日志、未读消息）
        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-promotion/admin/announcement/list?pageNum=1&pageSize=5", String.class); // 获取最近5条公告
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) model.addAttribute("announcementList", data.getOrDefault("records", new ArrayList<>()));
            else model.addAttribute("announcementList", new ArrayList<>());
        } catch (Exception e) { model.addAttribute("announcementList", new ArrayList<>()); }
        return "admin/index"; // 返回管理后台首页
    }

    /**
     * 管理后台数据大屏页面，展示详细的数据统计和可视化图表
     *
     * @param model Model对象
     * @return 数据大屏视图
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        loadDashboardData(model); // 加载核心统计数据
        loadDashboardExtras(model); // 加载辅助数据
        model.addAttribute("pageTitle", "数据大屏"); // 设置页面标题
        return "admin/dashboard"; // 返回数据大屏视图
    }

    /**
     * 加载仪表盘辅助数据：低库存商品、最近操作日志、未读消息数
     * adminIndex和adminDashboard共用的私有方法
     *
     * @param model Model对象
     */
    private void loadDashboardExtras(Model model) {
        // 低库存商品（按库存升序排列，取前5个）
        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-product/api/product/list?pageNum=1&pageSize=5&sortBy=stock&sortOrder=asc", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) model.addAttribute("lowStockItems", data.getOrDefault("records", new ArrayList<>()));
            else model.addAttribute("lowStockItems", new ArrayList<>());
        } catch (Exception e) { model.addAttribute("lowStockItems", new ArrayList<>()); }
        // 最近操作日志（前5条）
        try {
            PageResult<Map<String, Object>> logResult = adminLogService.getLogList(1, 5, null); // 调用日志服务
            model.addAttribute("recentLogs", logResult.getRecords()); // 存入日志列表
        } catch (Exception e) { model.addAttribute("recentLogs", new ArrayList<>()); }
        // 未读消息数
        try {
            String resp = restTemplate.getForObject(
                "http://bookstore-message/api/message/unread-count", String.class); // 获取未读消息数
            Map<String, Object> result = parseJson(resp);
            Object count = result.get("data");
            long unreadCount = count != null ? ((Number) count).longValue() : 0; // 转为long
            model.addAttribute("adminUnreadMsg", unreadCount); // 未读消息数（变量1）
            model.addAttribute("adminUnreadMsgCount", unreadCount); // 未读消息数（变量2，供不同模板使用）
        } catch (Exception e) {
            model.addAttribute("adminUnreadMsg", 0); // 默认为0
            model.addAttribute("adminUnreadMsgCount", 0);
        }
    }

    /**
     * 加载仪表盘核心统计数据
     * 包括：商品总数、分类数、用户总数、订单总数、总销售额、待处理订单数、各状态订单数、热销商品、优惠券总数
     *
     * @param model Model对象
     */
    private void loadDashboardData(Model model) {
        long totalProducts = 0, totalOrders = 0, totalUsers = 0, totalCoupons = 0; // 初始化统计变量
        long pendingOrders = 0; // 待处理订单数
        double totalRevenue = 0; // 总销售额
        java.util.List<?> bestsellers = new ArrayList<>(); // 热销商品列表

        try { // 获取商品总数
            String resp = restTemplate.getForObject(
                "http://bookstore-product/api/product/list?pageNum=1&pageSize=1", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) totalProducts = ((Number) data.get("total")).longValue(); // 取总数
        } catch (Exception ignored) {}

        try { // 获取商品分类数
            String resp = restTemplate.getForObject(
                "http://bookstore-product/api/category/list", String.class);
            Map<String, Object> result = parseJson(resp);
            java.util.List<?> catList = (java.util.List<?>) result.get("data");
            model.addAttribute("activeProducts", catList != null ? catList.size() : 0); // 分类数量
        } catch (Exception ignored) { model.addAttribute("activeProducts", 0); }

        try { // 获取用户总数
            String resp = restTemplate.getForObject(
                "http://bookstore-user/admin/user/list?pageNum=1&pageSize=1", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) totalUsers = ((Number) data.get("total")).longValue();
        } catch (Exception ignored) {}

        try { // 获取总订单数和总销售额
            String resp = restTemplate.getForObject(
                "http://bookstore-order/admin/order/list?pageNum=1&pageSize=10000", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                totalOrders = ((Number) data.get("total")).longValue(); // 订单总数
                java.util.List<?> orderList = (java.util.List<?>) data.get("records");
                if (orderList != null) {
                    for (Object obj : orderList) { // 遍历订单累加总销售额
                        Map<String, Object> order = (Map<String, Object>) obj;
                        totalRevenue += ((Number) order.getOrDefault("totalprice", 0)).doubleValue(); // 累加
                    }
                }
            }
        } catch (Exception ignored) {}

        try { // 获取待处理订单数（状态为"待支付"）
            String resp = restTemplate.getForObject(
                "http://bookstore-order/admin/order/list?pageNum=1&pageSize=1&status=待支付", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) pendingOrders = ((Number) data.get("total")).longValue();
        } catch (Exception ignored) {}

        // 获取各状态订单数（用于dashboard环形图）
        String[] statuses = {"待支付", "待发货", "已发货", "已完成", "已取消"}; // 所有订单状态
        String[] countKeys = {"pendingCount", "paidCount", "shippingCount", "completedCount", "cancelledCount"}; // 对应Model属性名
        for (int i = 0; i < statuses.length; i++) {
            try {
                String resp = restTemplate.getForObject(
                    "http://bookstore-order/admin/order/list?pageNum=1&pageSize=1&status=" + statuses[i], String.class);
                Map<String, Object> result = parseJson(resp);
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                long count = data != null ? ((Number) data.get("total")).longValue() : 0; // 该状态订单数
                model.addAttribute(countKeys[i], count); // 存入Model
            } catch (Exception e) { model.addAttribute(countKeys[i], 0); }
        }

        try { // 获取热销商品（前5名）
            String resp = restTemplate.getForObject(
                "http://bookstore-product/api/product/hot?limit=5", String.class);
            Map<String, Object> result = parseJson(resp);
            bestsellers = (java.util.List<?>) result.get("data"); // 热销商品列表
            if (bestsellers == null) bestsellers = new ArrayList<>();
        } catch (Exception ignored) {}

        try { // 获取优惠券总数
            String resp = restTemplate.getForObject(
                "http://bookstore-promotion/admin/coupon/list?pageNum=1&pageSize=1", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) totalCoupons = ((Number) data.get("total")).longValue();
        } catch (Exception ignored) {}

        // 将所有统计数据存入Model
        model.addAttribute("totalProducts", totalProducts); // 商品总数
        model.addAttribute("totalOrders", totalOrders); // 订单总数
        model.addAttribute("totalUsers", totalUsers); // 用户总数
        model.addAttribute("totalRevenue", totalRevenue); // 总销售额
        model.addAttribute("totalCoupons", totalCoupons); // 优惠券总数
        model.addAttribute("pendingOrders", pendingOrders); // 待处理订单数
        model.addAttribute("bestsellers", bestsellers); // 热销商品列表
    }

    /**
     * 管理后台商品列表页面，支持搜索和分页
     *
     * @param model Model对象
     * @param keyword 搜索关键词（可选）
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 商品列表管理页视图
     */
    @GetMapping("/admin/product")
    public String adminProductList(Model model,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            String url = "http://bookstore-product/api/product/list?pageNum=" + pageNum + "&pageSize=" + pageSize; // 构建URL
            if (keyword != null && !keyword.isEmpty()) url += "&keyword=" + keyword; // 追加搜索关键词
            String resp = restTemplate.getForObject(url, String.class); // 获取商品列表
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("productList", data.get("records")); // 商品列表
                model.addAttribute("total", data.get("total")); // 总数
                model.addAttribute("pageNum", data.get("pageNum")); // 当前页码
                model.addAttribute("pageSize", data.get("pageSize")); // 每页条数
                model.addAttribute("pages", data.get("totalPages")); // 总页数
            }
        } catch (Exception e) {
            model.addAttribute("productList", new ArrayList<>());
        }
        try {
            String catResp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class); // 获取分类
            Map<String, Object> catResult = parseJson(catResp);
            model.addAttribute("categories", catResult.get("data")); // 分类数据
        } catch (Exception e) {
            model.addAttribute("categories", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword); // 回显搜索关键词
        return "admin/product/list"; // 返回商品列表页
    }

    /**
     * 管理后台添加商品页面，加载分类列表供选择
     *
     * @param model Model对象
     * @return 添加商品页视图
     */
    @GetMapping("/admin/product/add")
    public String adminProductAdd(Model model) {
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class); // 获取分类
            Map<String, Object> result = parseJson(resp);
            model.addAttribute("categories", result.get("data")); // 分类数据
        } catch (Exception e) {
            model.addAttribute("categories", new ArrayList<>());
        }
        return "admin/product/add"; // 返回添加商品页
    }

    /**
     * 管理后台编辑商品页面，加载商品详情和分类列表
     *
     * @param id 商品ID（可选）
     * @param model Model对象
     * @return 编辑商品页视图
     */
    @GetMapping("/admin/product/edit")
    public String adminProductEdit(@RequestParam(required = false) String id, Model model) {
        if (id != null && !id.isEmpty()) {
            try {
                String resp = restTemplate.getForObject("http://bookstore-product/api/product/" + id, String.class); // 获取商品详情
                Map<String, Object> result = parseJson(resp);
                model.addAttribute("product", result.get("data")); // 商品详情
            } catch (Exception e) {
                model.addAttribute("product", new HashMap<>());
            }
        }
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class); // 获取分类
            Map<String, Object> result = parseJson(resp);
            model.addAttribute("categories", result.get("data")); // 分类数据
        } catch (Exception e) {
            model.addAttribute("categories", new ArrayList<>());
        }
        return "admin/product/edit"; // 返回编辑商品页
    }

    /**
     * 管理后台库存管理页面，展示所有商品的库存信息
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 库存管理页视图
     */
    @GetMapping("/admin/product/stock")
    public String adminProductStock(Model model,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            String url = "http://bookstore-product/api/product/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            String resp = restTemplate.getForObject(url, String.class); // 获取商品列表
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("productList", data.get("records")); // 商品列表
                model.addAttribute("total", data.get("total")); // 总数
                model.addAttribute("pageNum", data.get("pageNum")); // 页码
                model.addAttribute("pageSize", data.get("pageSize")); // 每页条数
            }
        } catch (Exception e) {
            model.addAttribute("productList", new ArrayList<>());
        }
        return "admin/product/stock"; // 返回库存管理页
    }

    /**
     * 管理后台热销排行页面，展示热销商品
     *
     * @param model Model对象
     * @return 热销排行页视图
     */
    @GetMapping("/admin/product/bestseller")
    public String adminProductBestseller(Model model) {
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/product/hot?limit=20", String.class); // 获取热销前20
            Map<String, Object> result = parseJson(resp);
            model.addAttribute("productList", result.get("data")); // 热销商品列表
        } catch (Exception e) {
            model.addAttribute("productList", new ArrayList<>());
        }
        return "admin/product/bestseller"; // 返回热销排行页
    }

    /**
     * 管理后台用户列表页面，支持搜索和分页
     *
     * @param model Model对象
     * @param keyword 搜索关键词（可选）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户列表管理页视图
     */
    @GetMapping("/admin/user")
    public String adminUserList(Model model,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            String url = "http://bookstore-user/admin/user/list?pageNum=" + pageNum + "&pageSize=" + pageSize; // 构建URL
            if (keyword != null && !keyword.isEmpty()) url += "&keyword=" + keyword; // 追加搜索
            String resp = restTemplate.getForObject(url, String.class); // 获取用户列表
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("userList", data.get("records")); // 用户列表
                model.addAttribute("total", data.get("total")); // 总数
                model.addAttribute("pageNum", data.get("pageNum")); // 页码
                model.addAttribute("pageSize", data.get("pageSize")); // 每页条数
                model.addAttribute("pages", data.get("totalPages")); // 总页数
            }
        } catch (Exception e) {
            model.addAttribute("userList", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword); // 回显搜索关键词
        return "admin/user/list"; // 返回用户列表页
    }

    /** 管理后台添加用户页面 */
    @GetMapping("/admin/user/add")
    public String adminUserAdd() {
        return "admin/user/add"; // 返回添加用户页
    }

    /**
     * 管理后台编辑用户页面，加载用户详情
     *
     * @param id 用户ID（可选）
     * @param model Model对象
     * @return 编辑用户页视图
     */
    @GetMapping("/admin/user/edit")
    public String adminUserEdit(@RequestParam(required = false) String id, Model model) {
        if (id != null && !id.isEmpty()) {
            try {
                String resp = restTemplate.getForObject("http://bookstore-user/api/user/" + id, String.class); // 获取用户详情
                Map<String, Object> result = parseJson(resp);
                model.addAttribute("user", result.get("data")); // 用户详情
            } catch (Exception e) {
                model.addAttribute("user", new HashMap<>());
            }
        }
        return "admin/user/edit"; // 返回编辑用户页
    }

    /**
     * 管理后台订单列表页面，支持按状态筛选和分页，展示各状态订单统计
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param status 订单状态筛选（可选）
     * @return 订单列表管理页视图
     */
    @GetMapping("/admin/order")
    public String adminOrderList(Model model,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String status) {
        // 加载各状态订单统计数据
        long totalOrders = 0, pendingCount = 0, paidCount = 0, shippingCount = 0, completedCount = 0, cancelledCount = 0;
        double totalRevenue = 0;
        try { // 获取总订单数
            String resp = restTemplate.getForObject(
                "http://bookstore-order/admin/order/list?pageNum=1&pageSize=1", String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) totalOrders = ((Number) data.get("total")).longValue();
        } catch (Exception e) {}
        String[] statuses = {"待支付", "已支付", "已发货", "已完成", "已取消"}; // 所有订单状态
        long[] counts = new long[5]; // 各状态计数数组
        for (int i = 0; i < statuses.length; i++) { // 遍历查询各状态订单数
            try {
                String resp = restTemplate.getForObject(
                    "http://bookstore-order/admin/order/list?pageNum=1&pageSize=1&status=" + statuses[i], String.class);
                Map<String, Object> result = parseJson(resp);
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                counts[i] = data != null ? ((Number) data.get("total")).longValue() : 0;
            } catch (Exception e) {}
        }
        pendingCount = counts[0]; paidCount = counts[1]; shippingCount = counts[2]; // 赋值各状态数量
        completedCount = counts[3]; cancelledCount = counts[4];

        try { // 获取订单列表（分页，支持状态筛选）
            String url = "http://bookstore-order/admin/order/list?pageNum=" + pageNum + "&pageSize=" + pageSize;
            if (status != null && !status.isEmpty()) url += "&status=" + status; // 追加状态筛选
            String resp = restTemplate.getForObject(url, String.class);
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("orderList", data.get("records")); // 订单列表
                model.addAttribute("total", data.get("total")); // 总数
                model.addAttribute("pageNum", data.get("pageNum")); // 页码
                model.addAttribute("pageSize", data.get("pageSize")); // 每页条数
                model.addAttribute("pages", data.get("totalPages")); // 总页数
                // 构建pageInfo对象供JSP分页标签使用
                Map<String, Object> pageInfo = new HashMap<>();
                pageInfo.put("total", data.get("total"));
                pageInfo.put("pageNum", data.get("pageNum"));
                pageInfo.put("pageSize", data.get("pageSize"));
                pageInfo.put("pages", data.get("totalPages"));
                pageInfo.put("hasPreviousPage", ((Number) data.get("pageNum")).intValue() > 1); // 是否有上一页
                pageInfo.put("hasNextPage", data.get("totalPages") != null && ((Number) data.get("pageNum")).intValue() < ((Number) data.get("totalPages")).intValue()); // 是否有下一页
                model.addAttribute("pageInfo", pageInfo);
            }
        } catch (Exception e) {
            model.addAttribute("orderList", new ArrayList<>());
        }
        model.addAttribute("selectedStatus", status); // 当前选中的状态筛选
        model.addAttribute("totalOrders", totalOrders); // 总订单数
        model.addAttribute("pendingCount", pendingCount); // 待支付数
        model.addAttribute("paidCount", paidCount); // 已支付数
        model.addAttribute("shippingCount", shippingCount); // 已发货数
        model.addAttribute("completedCount", completedCount); // 已完成数
        model.addAttribute("cancelledCount", cancelledCount); // 已取消数
        model.addAttribute("totalRevenue", totalRevenue); // 总销售额
        return "admin/order/list"; // 返回订单列表页
    }

    /**
     * 管理后台订单详情页面
     *
     * @param id 订单ID（可选）
     * @param model Model对象
     * @return 订单详情页视图
     */
    @GetMapping("/admin/order/detail")
    public String adminOrderDetail(@RequestParam(required = false) String id, Model model) {
        if (id != null && !id.isEmpty()) {
            try {
                String resp = restTemplate.getForObject("http://bookstore-order/admin/order/" + id, String.class); // 获取订单详情
                Map<String, Object> result = parseJson(resp);
                model.addAttribute("order", result.get("data")); // 订单详情
            } catch (Exception e) {
                model.addAttribute("order", new HashMap<>());
            }
        }
        return "admin/order/detail"; // 返回订单详情页
    }

    /**
     * 管理后台优惠券列表页面，支持分页
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 优惠券列表管理页视图
     */
    @GetMapping("/admin/coupon")
    public String adminCouponList(Model model,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        long totalCoupons = 0;
        try {
            String url = "http://bookstore-promotion/admin/coupon/list?pageNum=" + pageNum + "&pageSize=" + pageSize; // 构建URL
            String resp = restTemplate.getForObject(url, String.class); // 获取优惠券列表
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("couponList", data.get("records")); // 优惠券列表
                model.addAttribute("total", data.get("total")); // 总数
                model.addAttribute("pageNum", data.get("pageNum")); // 页码
                model.addAttribute("pageSize", data.get("pageSize")); // 每页条数
                if (data.get("total") != null) totalCoupons = ((Number) data.get("total")).longValue();
            }
        } catch (Exception e) {
            model.addAttribute("couponList", new ArrayList<>());
        }
        model.addAttribute("totalCoupons", totalCoupons); // 优惠券总数
        model.addAttribute("activeCoupons", 0); // 生效中优惠券数（暂设为0）
        model.addAttribute("totalIssued", 0); // 已发放总数（暂设为0）
        return "admin/coupon/list"; // 返回优惠券列表页
    }

    /**
     * 管理后台评价列表页面，支持分页和搜索
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 搜索关键词（可选）
     * @param status 状态筛选（可选）
     * @return 评价列表管理页视图
     */
    @GetMapping("/admin/review")
    public String adminReviewList(Model model,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String status) {
        try {
            String url = "http://bookstore-promotion/admin/review/list?pageNum=" + pageNum + "&pageSize=" + pageSize; // 构建URL
            String resp = restTemplate.getForObject(url, String.class); // 获取评价列表
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("reviewList", data.get("records")); // 评价列表
                model.addAttribute("total", data.get("total")); // 总数
                model.addAttribute("pageNum", data.get("pageNum")); // 页码
                model.addAttribute("pageSize", data.get("pageSize")); // 每页条数
            }
        } catch (Exception e) {
            model.addAttribute("reviewList", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword != null ? keyword : ""); // 搜索关键词
        model.addAttribute("statusFilter", status != null ? status : ""); // 状态筛选
        return "admin/review/list"; // 返回评价列表页
    }

    /**
     * 管理后台消息列表页面，支持分页
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 消息列表管理页视图
     */
    @GetMapping("/admin/message")
    public String adminMessageList(Model model,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            String url = "http://bookstore-message/admin/message/list?pageNum=" + pageNum + "&pageSize=" + pageSize; // 构建URL
            String resp = restTemplate.getForObject(url, String.class); // 获取消息列表
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("messageList", data.get("records")); // 消息列表
                model.addAttribute("total", data.get("total")); // 总数
                model.addAttribute("pageNum", data.get("pageNum")); // 页码
                model.addAttribute("pageSize", data.get("pageSize")); // 每页条数
            }
        } catch (Exception e) {
            model.addAttribute("messageList", new ArrayList<>());
        }
        model.addAttribute("unreadCount", 0); // 未读消息数（暂设为0）
        model.addAttribute("sentMessages", new ArrayList<>()); // 已发送消息（暂为空）
        return "admin/message/list"; // 返回消息列表页
    }

    /**
     * 管理后台公告列表页面，支持分页
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 公告列表管理页视图
     */
    @GetMapping("/admin/announcement")
    public String adminAnnouncementList(Model model,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            String url = "http://bookstore-promotion/admin/announcement/list?pageNum=" + pageNum + "&pageSize=" + pageSize; // 构建URL
            String resp = restTemplate.getForObject(url, String.class); // 获取公告列表
            Map<String, Object> result = parseJson(resp);
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                model.addAttribute("announcementList", data.get("records")); // 公告列表
                model.addAttribute("total", data.get("total")); // 总数
                model.addAttribute("pageNum", data.get("pageNum")); // 页码
                model.addAttribute("pageSize", data.get("pageSize")); // 每页条数
            }
        } catch (Exception e) {
            model.addAttribute("announcementList", new ArrayList<>());
        }
        return "admin/announcement/list"; // 返回公告列表页
    }

    /**
     * 管理后台操作日志列表页面，支持分页和关键词搜索
     *
     * @param model Model对象
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 搜索关键词（可选）
     * @return 操作日志列表页视图
     */
    @GetMapping("/admin/log")
    public String adminLogList(Model model,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) String keyword) {
        try {
            // 调用日志服务获取日志列表（支持分页和关键词搜索）
            PageResult<Map<String, Object>> logResult = adminLogService.getLogList(pageNum, pageSize, keyword);
            model.addAttribute("logList", logResult.getRecords()); // 日志列表
            model.addAttribute("total", logResult.getTotal()); // 总数
            model.addAttribute("pageNum", logResult.getPageNum()); // 页码
            model.addAttribute("pageSize", logResult.getPageSize()); // 每页条数
        } catch (Exception e) {
            model.addAttribute("logList", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword); // 回显搜索关键词
        return "admin/log/list"; // 返回操作日志列表页
    }

    /** 管理后台日志列表路径兼容 -> /admin/log */
    @GetMapping("/admin/log/list")
    public String adminLogListAlias() {
        return "redirect:/admin/log"; // 重定向到日志列表页
    }
}