package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.math.BigDecimal;  // 导入BigDecimal类，用于精确表示金额
import java.util.List;  // 导入List集合类
import java.util.Map;  // 导入Map集合类

/**
 * 数据大屏视图对象（VO）
 * 用于向前端返回管理后台数据大屏的统计信息
 * 包含用户总数、订单总数、销售额、销售趋势等
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class DashboardVO {  // 数据大屏视图对象类

    private Long totalUsers;  // 用户总数
    private Long totalOrders;  // 订单总数
    private Long totalProducts;  // 商品总数
    private BigDecimal totalRevenue;  // 累计总营收
    private BigDecimal todayRevenue;  // 今日营收
    private Long todayOrders;  // 今日订单数
    private List<Map<String, Object>> salesTrend; // 近7天销售趋势数据
    private List<Map<String, Object>> categorySales; // 各分类销售占比数据
    private List<ProductVO> hotProducts; // 热销商品列表
}
