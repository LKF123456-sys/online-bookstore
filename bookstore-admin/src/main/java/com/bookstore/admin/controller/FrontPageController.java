package com.bookstore.admin.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bookstore.admin.service.AdminLogService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 前台页面控制器 - 处理所有面向用户的前台页面视图请求
 *
 * 职责范围：
 * 1. 首页展示：分类、推荐商品、热销商品、商品列表（GET /）
 * 2. 商品搜索：关键词搜索（GET /search）
 * 3. 商品详情：单个商品完整信息展示（GET /product/detail）
 * 4. 购物车操作：查看、添加、更新数量、删除、清空（GET/POST /cart*）
 * 5. 订单管理：确认订单、提交订单、订单详情、订单历史（GET/POST /order*）
 * 6. 支付处理：支付页面、支付子页面（微信/支付宝/银行卡）（GET /payment*）
 * 7. 确认收货：AJAX接口（POST /order/confirmReceipt）
 *
 * 工作原理：
 * - 通过RestTemplate调用商品服务（bookstore-product）、订单服务（bookstore-order）获取数据
 * - 将数据放入Model中，传递给JSP视图模板进行渲染
 * - 购物车和订单操作需要用户登录，通过Session中的user属性判断
 */
@Slf4j
@Controller
public class FrontPageController extends BaseController {

    public FrontPageController(RestTemplate restTemplate, AdminLogService adminLogService) {
        super(restTemplate, adminLogService);
    }

    /** 浏览器favicon.ico请求，重定向到SVG格式图标 */
    @GetMapping("/favicon.ico")
    public String favicon() {
        return "redirect:/favicon.svg";
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
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String category) {
        // --- 获取商品分类列表 ---
        try {
            String catResp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class);
            Map<String, Object> catResult = parseJson(catResp);
            if (catResult.get("data") != null) {
                model.addAttribute("categories", catResult.get("data"));
            } else {
                model.addAttribute("categories", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("categories", new ArrayList<>());
        }
        // --- 获取推荐商品（最多8个） ---
        try {
            String recResp = restTemplate.getForObject("http://bookstore-product/api/product/recommend?limit=8", String.class);
            Map<String, Object> recResult = parseJson(recResp);
            if (recResult.get("data") != null) {
                model.addAttribute("recommended", recResult.get("data"));
            } else {
                model.addAttribute("recommended", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("recommended", new ArrayList<>());
        }
        // --- 获取热销商品（最多8个） ---
        try {
            String hotResp = restTemplate.getForObject("http://bookstore-product/api/product/hot?limit=8", String.class);
            Map<String, Object> hotResult = parseJson(hotResp);
            if (hotResult.get("data") != null) {
                model.addAttribute("bestsellers", hotResult.get("data"));
            } else {
                model.addAttribute("bestsellers", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("bestsellers", new ArrayList<>());
        }
        // --- 获取商品列表（分页，每页12个） ---
        try {
            String url = "http://bookstore-product/api/product/list?pageNum=1&pageSize=12";
            if (keyword != null && !keyword.isEmpty()) url += "&keyword=" + keyword;
            if (category != null && !category.isEmpty()) url += "&category=" + category;
            String prodResp = restTemplate.getForObject(url, String.class);
            Map<String, Object> prodResult = parseJson(prodResp);
            if (prodResult.get("data") != null) {
                Map<String, Object> pageData = (Map<String, Object>) prodResult.get("data");
                model.addAttribute("products", pageData.getOrDefault("records", new ArrayList<>()));
            } else {
                model.addAttribute("products", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("products", new ArrayList<>());
        }
        model.addAttribute("announcementList", new ArrayList<>());
        model.addAttribute("newArrivals", new ArrayList<>());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("cartSize", 0);
        return "index";
    }

    /** /index路径重定向到首页，兼容前端代码中使用/index访问首页的情况 */
    @GetMapping("/index")
    public String indexPage() {
        return "redirect:/";
    }

    /**
     * 搜索页面 - 根据关键词搜索商品
     *
     * @param keyword 搜索关键词（必填）
     * @param model Model对象
     * @return 首页视图（搜索结果复用首页模板）
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model) {
        try {
            String url = "http://bookstore-product/api/product/list?pageNum=1&pageSize=20&keyword=" + keyword;
            String prodResp = restTemplate.getForObject(url, String.class);
            Map<String, Object> prodResult = parseJson(prodResp);
            if (prodResult.get("data") != null) {
                Map<String, Object> pageData = (Map<String, Object>) prodResult.get("data");
                model.addAttribute("products", pageData.getOrDefault("records", new ArrayList<>()));
            } else {
                model.addAttribute("products", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("products", new ArrayList<>());
        }
        try {
            String catResp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class);
            Map<String, Object> catResult = parseJson(catResp);
            if (catResult.get("data") != null) {
                model.addAttribute("categories", catResult.get("data"));
            } else {
                model.addAttribute("categories", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("categories", new ArrayList<>());
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("announcementList", new ArrayList<>());
        model.addAttribute("bestsellers", new ArrayList<>());
        model.addAttribute("newArrivals", new ArrayList<>());
        model.addAttribute("recommended", new ArrayList<>());
        model.addAttribute("cartSize", 0);
        return "index";
    }

    /**
     * 商品详情页面 - 展示单个商品的详细信息、分类列表和推荐商品
     *
     * @param id 商品ID（必填）
     * @param model Model对象
     * @return 商品详情页视图
     */
    @GetMapping("/product/detail")
    public String productDetail(@RequestParam String id, Model model) {
        try {
            String resp = restTemplate.getForObject("http://bookstore-product/api/product/" + id, String.class);
            Map<String, Object> result = parseJson(resp);
            if (result.get("data") != null) {
                model.addAttribute("product", result.get("data"));
            } else {
                model.addAttribute("product", new HashMap<>());
            }
        } catch (Exception e) {
            model.addAttribute("product", new HashMap<>());
        }
        try {
            String catResp = restTemplate.getForObject("http://bookstore-product/api/category/list", String.class);
            Map<String, Object> catResult = parseJson(catResp);
            if (catResult.get("data") != null) {
                model.addAttribute("categories", catResult.get("data"));
            } else {
                model.addAttribute("categories", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("categories", new ArrayList<>());
        }
        try {
            String recResp = restTemplate.getForObject("http://bookstore-product/api/product/recommend?limit=4", String.class);
            Map<String, Object> recResult = parseJson(recResp);
            if (recResult.get("data") != null) {
                model.addAttribute("relatedBooks", recResult.get("data"));
            } else {
                model.addAttribute("relatedBooks", new ArrayList<>());
            }
        } catch (Exception e) {
            model.addAttribute("relatedBooks", new ArrayList<>());
        }
        model.addAttribute("guessYouLike", new ArrayList<>());
        model.addAttribute("cartSize", 0);
        return "product_detail";
    }

    // ======================== 购物车操作 ========================

    /**
     * 购物车页面 - 展示当前用户的购物车内容
     * 需要用户登录才能查看购物车，从订单服务获取购物车数据
     *
     * @param model Model对象
     * @param session HTTP会话，用于获取登录用户信息
     * @return 购物车页视图
     */
    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        model.addAttribute("cartSize", 0);
        model.addAttribute("cart", new ArrayList<>());
        model.addAttribute("recommendedBooks", new ArrayList<>());
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user != null) {
            String userId = (String) user.get("userid");
            if (userId != null) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("X-User-Id", userId);
                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    ResponseEntity<String> response = restTemplate.exchange(
                        "http://bookstore-order/api/cart", HttpMethod.GET, entity, String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        Map<String, Object> result = parseJson(response.getBody());
                        Map<String, Object> data = (Map<String, Object>) result.get("data");
                        if (data != null) {
                            java.util.List<?> items = (java.util.List<?>) data.get("items");
                            if (items != null) {
                                model.addAttribute("cart", items);
                                model.addAttribute("cartSize", items.size());
                            }
                        }
                    }
                } catch (Exception e) {
                    // 购物车加载失败，显示空购物车（不中断页面渲染）
                }
            }
        }
        try {
            String recResp = restTemplate.getForObject("http://bookstore-product/api/product/recommend?limit=4", String.class);
            Map<String, Object> recResult = parseJson(recResp);
            if (recResult.get("data") != null) {
                model.addAttribute("recommendedBooks", recResult.get("data"));
            }
        } catch (Exception e) {
            model.addAttribute("recommendedBooks", new ArrayList<>());
        }
        return "cart";
    }

    /**
     * 购物车商品数量更新（AJAX接口）
     *
     * @param productId 商品ID
     * @param quantity 新的数量值
     * @param session HTTP会话
     * @return JSON响应，包含success状态和message消息
     */
    @PostMapping("/cart/update")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> updateCartAjax(
            @RequestParam String productId,
            @RequestParam int quantity,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        String userId = user != null ? (String) user.get("userid") : null;
        if (userId == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("productId", productId);
            body.put("quantity", quantity);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", userId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.put("http://bookstore-order/api/cart/item", entity);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败");
        }
        return result;
    }

    /**
     * 删除购物车中的某个商品
     *
     * @param productId 要删除的商品ID
     * @param session HTTP会话
     * @return 重定向回购物车页面
     */
    @GetMapping("/cart/remove")
    public String removeCartItem(@RequestParam String productId, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        String userId = user != null ? (String) user.get("userid") : null;
        if (userId != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                restTemplate.exchange("http://bookstore-order/api/cart/item/" + productId,
                    HttpMethod.DELETE, entity, String.class);
            } catch (Exception ignored) {}
        }
        return "redirect:/cart";
    }

    /**
     * 清空购物车中的所有商品
     *
     * @param session HTTP会话
     * @return 重定向回购物车页面
     */
    @GetMapping("/cart/clear")
    public String clearCart(HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        String userId = user != null ? (String) user.get("userid") : null;
        if (userId != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                restTemplate.exchange("http://bookstore-order/api/cart/clear",
                    HttpMethod.DELETE, entity, String.class);
            } catch (Exception ignored) {}
        }
        return "redirect:/cart";
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
    @GetMapping("/cart/add/ajax")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> addToCartAjax(
            @RequestParam(value = "productId", required = false) String productId,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String idToUse = productId != null ? productId : id;
        if (idToUse == null) {
            result.put("success", false);
            result.put("message", "参数错误");
            return result;
        }
        try {
            Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
            String userId = user != null ? (String) user.get("userid") : null;
            if (userId == null) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("productId", idToUse);
            body.put("quantity", quantity);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", userId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                "http://bookstore-order/api/cart", entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                result.put("success", true);
                result.put("message", "已加入购物车");
            } else {
                result.put("success", false);
                result.put("message", "添加失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "请先登录");
        }
        return result;
    }

    // ======================== 订单操作 ========================

    /**
     * 订单确认页面 - 显示购物车内容并填写收货地址，需要登录
     *
     * @param model Model对象
     * @param session HTTP会话
     * @return 订单确认页或重定向到登录页
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/order")
    public String order(Model model, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        String userId = (String) user.get("userid");
        BigDecimal total = BigDecimal.ZERO;
        List<Map<String, Object>> cartItems = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                "http://bookstore-order/api/cart", HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = parseJson(response.getBody());
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null && data.get("items") != null) {
                    cartItems = (List<Map<String, Object>>) data.get("items");
                    for (Map<String, Object> item : cartItems) {
                        Object subtotal = item.get("subtotal");
                        if (subtotal != null) {
                            total = total.add(new BigDecimal(subtotal.toString()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 购物车加载失败，显示空购物车
        }
        model.addAttribute("cart", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("cartSize", cartItems.size());
        return "order";
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
    @SuppressWarnings("unchecked")
    @PostMapping("/order/submit")
    public String submitOrder(@RequestParam String addr1,
                              @RequestParam String city,
                              @RequestParam String state,
                              @RequestParam String zip,
                              @RequestParam(required = false) String couponCode,
                              HttpSession session) {
        Map<String, Object> userSession = (Map<String, Object>) session.getAttribute("user");
        if (userSession == null) {
            return "redirect:/login";
        }
        String userId = (String) userSession.get("userid");
        try {
            // 第1步：获取购物车信息
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> cartResp = restTemplate.exchange(
                "http://bookstore-order/api/cart", HttpMethod.GET, entity, String.class);
            Map<String, Object> cartResult = parseJson(cartResp.getBody());
            Map<String, Object> cartData = (Map<String, Object>) cartResult.get("data");
            List<Map<String, Object>> items = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (cartData != null && cartData.get("items") != null) {
                List<Map<String, Object>> cartItems = (List<Map<String, Object>>) cartData.get("items");
                for (Map<String, Object> item : cartItems) {
                    Map<String, Object> orderItem = new HashMap<>();
                    orderItem.put("productId", item.get("productId"));
                    Object qty = item.get("quantity");
                    orderItem.put("quantity", qty != null ? ((Number) qty).intValue() : 1);
                    items.add(orderItem);
                    Object subtotal = item.get("subtotal");
                    if (subtotal != null) {
                        totalAmount = totalAmount.add(new BigDecimal(subtotal.toString()));
                    }
                }
            }
            if (items.isEmpty()) {
                return "redirect:/cart";
            }
            // 第2步：构建订单DTO
            Map<String, Object> orderBody = new HashMap<>();
            orderBody.put("items", items);
            orderBody.put("shipAddr1", addr1);
            orderBody.put("shipCity", city);
            orderBody.put("shipState", state);
            orderBody.put("shipZip", zip);
            orderBody.put("shipToFirstName", userSession.getOrDefault("firstname", ""));
            orderBody.put("shipToLastName", userSession.getOrDefault("lastname", ""));
            orderBody.put("billToFirstName", userSession.getOrDefault("firstname", ""));
            orderBody.put("billToLastName", userSession.getOrDefault("lastname", ""));
            orderBody.put("billAddr1", addr1);
            orderBody.put("billCity", city);
            orderBody.put("billState", state);
            orderBody.put("billZip", zip);
            // 第3步：处理优惠券
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                orderBody.put("couponName", couponCode.trim());
            }
            // 第4步：提交订单
            HttpHeaders postHeaders = new HttpHeaders();
            postHeaders.setContentType(MediaType.APPLICATION_JSON);
            postHeaders.set("X-User-Id", userId);
            HttpEntity<Map<String, Object>> postEntity = new HttpEntity<>(orderBody, postHeaders);
            ResponseEntity<String> orderResp = restTemplate.postForEntity(
                "http://bookstore-order/api/order", postEntity, String.class);
            if (orderResp.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> orderResult = parseJson(orderResp.getBody());
                Map<String, Object> orderData = (Map<String, Object>) orderResult.get("data");
                if (orderData != null && orderData.get("orderid") != null) {
                    // 第5步：清空购物车
                    try {
                        restTemplate.exchange("http://bookstore-order/api/cart/clear",
                            HttpMethod.DELETE, entity, String.class);
                    } catch (Exception ignored) {}
                    return "redirect:/payment?orderId=" + orderData.get("orderid");
                }
            }
        } catch (Exception e) {
            return "redirect:/order?msg=error";
        }
        return "redirect:/order?msg=error";
    }

    /**
     * 订单详情页面
     * 加载指定订单的完整信息（订单基本信息 + 商品明细 + 评价状态）
     *
     * @param orderId 订单ID（query参数）
     * @param model Model对象
     * @param session HTTP会话
     * @return 订单详情页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/order/detail")
    public String orderDetail(@RequestParam(required = false) String orderId, Model model, HttpSession session) {
        if (orderId == null || orderId.isEmpty()) {
            return "redirect:/order/history";
        }
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String userId = (String) user.get("userid");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                "http://bookstore-order/api/order/" + orderId,
                HttpMethod.GET, entity, String.class);
            Map<String, Object> result = parseJson(resp.getBody());
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            if (data != null) {
                deepConvertDates(data);
                model.addAttribute("order", data);
                Object itemsObj = data.get("items");
                if (itemsObj instanceof List) {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;
                    for (Map<String, Object> it : items) {
                        Object pidObj = it.get("productId");
                        if (pidObj != null && !(pidObj instanceof String)) it.put("productId", String.valueOf(pidObj));
                    }
                    model.addAttribute("orderItems", items);
                    // 检查是否有商品已被当前用户评价过
                    Set<String> reviewedProductIds = new HashSet<>();
                    for (Object it : items) {
                        if (it instanceof Map) {
                            Object pidObj = ((Map<?, ?>) it).get("productId");
                            String pid = pidObj != null ? String.valueOf(pidObj) : null;
                            if (pid != null) {
                                try {
                                    HttpHeaders rh = new HttpHeaders();
                                    rh.set("X-User-Id", userId);
                                    HttpEntity<Void> re = new HttpEntity<>(rh);
                                    ResponseEntity<String> revResp = restTemplate.exchange(
                                        "http://bookstore-promotion/api/review/product/" + pid + "?pageNum=1&pageSize=50",
                                        HttpMethod.GET, re, String.class);
                                    Map<String, Object> revResult = parseJson(revResp.getBody());
                                    Map<String, Object> revData = (Map<String, Object>) revResult.get("data");
                                    if (revData != null && revData.get("records") instanceof List) {
                                        List<?> revs = (List<?>) revData.get("records");
                                        for (Object r : revs) {
                                            if (r instanceof Map && userId.equals(((Map<?, ?>) r).get("userId"))) {
                                                reviewedProductIds.add(pid);
                                                break;
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                    model.addAttribute("reviewedProducts", reviewedProductIds);
                    model.addAttribute("orderLevelReviewed", reviewedProductIds.size() >= items.size());
                }
            } else {
                return "redirect:/order/history";
            }
        } catch (Exception e) {
            return "redirect:/order/history";
        }
        model.addAttribute("cartSize", 0);
        return "order_detail";
    }

    /**
     * 订单历史页面 - 显示用户的所有历史订单
     *
     * @param model Model对象
     * @param session HTTP会话
     * @return 订单历史页视图
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/order/history")
    public String orderHistory(Model model, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user != null) {
            String userId = (String) user.get("userid");
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> resp = restTemplate.exchange(
                    "http://bookstore-order/api/order/list?pageNum=1&pageSize=50",
                    HttpMethod.GET, entity, String.class);
                Map<String, Object> result = parseJson(resp.getBody());
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                java.util.List<Map<String, Object>> orderList = new java.util.ArrayList<>();
                if (data != null && data.get("records") != null) {
                    orderList = (java.util.List<Map<String, Object>>) data.get("records");
                    for (Map<String, Object> o : orderList) {
                        deepConvertDates(o);
                        Object oid = o.get("orderid");
                        if (oid != null && !(oid instanceof String)) o.put("orderid", String.valueOf(oid));
                    }
                }
                model.addAttribute("orderList", orderList);

                // 统计各状态订单数量和已评价订单ID
                int cntAll = orderList.size();
                int cntPending = 0, cntPaid = 0, cntShipped = 0, cntCompleted = 0, cntCancelled = 0;
                java.util.Set<String> reviewedOrderIds = new java.util.HashSet<>();
                for (Map<String, Object> o : orderList) {
                    String status = (String) o.get("status");
                    if ("待支付".equals(status)) cntPending++;
                    else if ("已支付".equals(status)) cntPaid++;
                    else if ("已发货".equals(status)) cntShipped++;
                    else if ("已完成".equals(status)) cntCompleted++;
                    else if ("已取消".equals(status)) cntCancelled++;
                    if ("已完成".equals(status)) {
                        Object itemsObj = o.get("items");
                        if (itemsObj instanceof List && !((List<?>) itemsObj).isEmpty()) {
                            try {
                                Object pidObj = ((Map<?, ?>) ((List<?>) itemsObj).get(0)).get("productId");
                                String pid = pidObj != null ? String.valueOf(pidObj) : null;
                                if (pid != null) {
                                    HttpHeaders rh = new HttpHeaders(); rh.set("X-User-Id", userId);
                                    HttpEntity<Void> re = new HttpEntity<>(rh);
                                    ResponseEntity<String> revResp = restTemplate.exchange(
                                        "http://bookstore-promotion/api/review/product/" + pid + "?pageNum=1&pageSize=50",
                                        HttpMethod.GET, re, String.class);
                                    Map<String, Object> revResult = parseJson(revResp.getBody());
                                    Map<String, Object> revData = (Map<String, Object>) revResult.get("data");
                                    if (revData != null && revData.get("records") instanceof List) {
                                        List<?> revs = (List<?>) revData.get("records");
                                        for (Object r : revs) {
                                            if (r instanceof Map && userId.equals(((Map<?, ?>) r).get("userId"))) {
                                                reviewedOrderIds.add(String.valueOf(o.get("orderid")));
                                                break;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
                model.addAttribute("cntAll", cntAll);
                model.addAttribute("cntPending", cntPending);
                model.addAttribute("reviewedOrderIds", reviewedOrderIds);
            } catch (Exception e) {
                model.addAttribute("orderList", new ArrayList<>());
            }
        } else {
            model.addAttribute("orderList", new ArrayList<>());
        }
        model.addAttribute("cartSize", 0);
        return "order_history";
    }

    // ======================== 支付相关 ========================

    /**
     * 支付页面 - 显示订单支付信息，需要登录
     *
     * @param orderId 订单ID（可选）
     * @param model Model对象
     * @param session HTTP会话
     * @return 支付页视图或重定向到登录页
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/payment")
    public String payment(@RequestParam(required = false) String orderId, Model model, HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String userId = (String) user.get("userid");
        model.addAttribute("userId", userId);
        if (orderId != null && !orderId.isEmpty()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> resp = restTemplate.exchange(
                    "http://bookstore-order/api/order/" + orderId,
                    HttpMethod.GET, entity, String.class);
                Map<String, Object> result = parseJson(resp.getBody());
                Map<String, Object> orderData = (Map<String, Object>) result.get("data");
                if (orderData != null) {
                    model.addAttribute("order", orderData);
                    model.addAttribute("orderId", orderId);
                    model.addAttribute("orderAmount", orderData.get("totalprice"));
                }
            } catch (Exception e) {
                model.addAttribute("order", new HashMap<>());
            }
        }
        return "payment";
    }

    /** 支付成功页面 */
    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam(required = false) String orderId,
                                  Model model, HttpSession session) {
        loadPaymentResultData(orderId, model, session);
        return "payment/success";
    }

    /** 支付失败页面 */
    @GetMapping("/payment/fail")
    public String paymentFail(@RequestParam(required = false) String orderId,
                               Model model, HttpSession session) {
        loadPaymentResultData(orderId, model, session);
        return "payment/fail";
    }

    /** 支付结果页面公共数据加载 */
    @SuppressWarnings("unchecked")
    private void loadPaymentResultData(String orderId, Model model, HttpSession session) {
        if (orderId != null && !orderId.isEmpty()) {
            model.addAttribute("orderId", orderId);
            try {
                Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
                if (user != null) {
                    String userId = String.valueOf(user.get("userid"));
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("X-User-Id", userId);
                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    ResponseEntity<String> resp = restTemplate.exchange(
                        "http://bookstore-order/api/order/" + orderId,
                        HttpMethod.GET, entity, String.class);
                    Map<String, Object> result = parseJson(resp.getBody());
                    Map<String, Object> orderData = (Map<String, Object>) result.get("data");
                    if (orderData != null) {
                        model.addAttribute("orderAmount", orderData.get("totalprice"));
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    /** 微信支付页面 */
    @GetMapping("/payment/wechat")
    public String paymentWechat(@RequestParam(required = false) String orderId,
                                 @RequestParam(required = false) String userId,
                                 Model model, HttpSession session) {
        loadPaymentSubPageData(orderId, userId, model, session);
        return "payment/wechat";
    }

    /** 支付宝支付页面 */
    @GetMapping("/payment/alipay")
    public String paymentAlipay(@RequestParam(required = false) String orderId,
                                 @RequestParam(required = false) String userId,
                                 Model model, HttpSession session) {
        loadPaymentSubPageData(orderId, userId, model, session);
        return "payment/alipay";
    }

    /** 银行卡支付页面 */
    @GetMapping("/payment/card")
    public String paymentCard(@RequestParam(required = false) String orderId,
                               @RequestParam(required = false) String userId,
                               Model model, HttpSession session) {
        loadPaymentSubPageData(orderId, userId, model, session);
        return "payment/card";
    }

    /** 支付子页面公共数据加载：从API获取订单号和金额 */
    @SuppressWarnings("unchecked")
    private void loadPaymentSubPageData(String orderId, String userId, Model model, HttpSession session) {
        if (orderId == null || orderId.isEmpty()) return;
        model.addAttribute("orderId", orderId);
        if (userId == null || userId.isEmpty()) {
            Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
            if (user != null) userId = String.valueOf(user.get("userid"));
        }
        if (userId != null && !userId.isEmpty()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-User-Id", userId);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> resp = restTemplate.exchange(
                    "http://bookstore-order/api/order/" + orderId,
                    HttpMethod.GET, entity, String.class);
                Map<String, Object> result = parseJson(resp.getBody());
                Map<String, Object> orderData = (Map<String, Object>) result.get("data");
                if (orderData != null) {
                    model.addAttribute("order", orderData);
                    model.addAttribute("orderAmount", orderData.get("totalprice"));
                }
            } catch (Exception e) {
                model.addAttribute("orderAmount", "加载失败");
            }
        }
    }

    // ======================== 确认收货 ========================

    /**
     * 确认收货（AJAX接口）
     * 用户收到商品后确认收货，将订单状态从"已发货"更新为"已完成"
     *
     * @param orderId 订单ID
     * @param session HTTP会话
     * @return JSON格式的操作结果
     */
    @PostMapping("/order/confirmReceipt")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> confirmReceipt(@RequestParam String orderId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        String userId = (String) user.get("userid");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户信息异常");
            return result;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                "http://bookstore-order/api/order/" + orderId + "/confirm",
                HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                result.put("success", true);
                result.put("message", "确认收货成功");
            } else {
                result.put("success", false);
                result.put("message", "操作失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "确认收货失败：" + e.getMessage());
        }
        return result;
    }
}
