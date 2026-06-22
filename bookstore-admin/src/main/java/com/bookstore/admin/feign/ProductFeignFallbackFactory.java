package com.bookstore.admin.feign; // 声明当前类所在的包路径：Feign降级工厂层

// 导入统一响应结果封装类
import com.bookstore.common.api.Result;
// 导入商品视图对象
import com.bookstore.common.api.vo.ProductVO;
// 导入Lombok的Slf4j日志注解，自动生成log日志对象
import lombok.extern.slf4j.Slf4j;
// 导入Spring Cloud的FallbackFactory接口，用于创建Feign降级处理对象
import org.springframework.cloud.openfeign.FallbackFactory;
// 导入Spring的Component注解
import org.springframework.stereotype.Component;

import java.util.List; // 导入Java集合框架的List接口
import java.util.Map; // 导入Java集合框架的Map接口

/**
 * 商品服务Feign降级工厂
 * 当商品服务不可用（网络异常、服务宕机、超时等）时，会执行这里的降级逻辑。
 *
 * 降级的作用：
 * - 防止因为一个服务的故障导致整个系统雪崩（服务雪崩效应）
 * - 给用户一个友好的错误提示，而不是直接报错
 * - 记录错误日志，方便排查问题
 *
 * 本类的降级策略：
 * - 查询类方法：返回友好的错误提示Result（503状态码）
 * - 写入/变更类方法：抛出RuntimeException，阻止业务继续执行
 */
@Slf4j // Lombok注解：自动生成名为log的SLF4J日志对象
@Component // 标记为Spring组件，注册到Spring容器中
public class ProductFeignFallbackFactory implements FallbackFactory<ProductFeignClient> { // 实现Feign降级工厂接口，泛型指定要降级的Feign客户端

    /**
     * 创建降级处理对象
     * 当Feign远程调用失败时，Feign框架会自动调用此方法获取降级处理对象
     * @param cause 导致降级的异常原因（如网络超时、连接拒绝等）
     * @return 降级处理对象（ProductFeignClient的匿名实现）
     */
    @Override
    public ProductFeignClient create(Throwable cause) { // 重写create方法，参数cause是导致降级的原始异常
        log.error("商品服务调用失败", cause); // 记录错误日志，包含异常堆栈信息
        return new ProductFeignClient() { // 返回ProductFeignClient的匿名内部类实现（即降级逻辑）

            // ======================== 查询类方法（返回友好错误提示） ========================

            /**
             * 获取商品分页列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> list(int pageNum, int pageSize, String keyword, Long categoryId, String sort) {
                log.warn("获取商品列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "商品服务暂时不可用");
            }

            /**
             * 获取商品详情 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> detail(String id) {
                log.warn("获取商品详情降级处理: id={}, 原因: {}", id, cause.getMessage());
                return Result.error(503, "商品服务暂时不可用");
            }

            /**
             * 获取推荐商品列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<List<ProductVO>> recommend(int limit) {
                log.warn("获取推荐商品降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "商品服务暂时不可用");
            }

            /**
             * 获取热销商品列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<List<ProductVO>> hot(int limit) {
                log.warn("获取热销商品降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "商品服务暂时不可用");
            }

            /**
             * 商品搜索 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> search(String keyword, int pageNum, int pageSize) {
                log.warn("商品搜索降级处理: keyword={}, 原因: {}", keyword, cause.getMessage());
                return Result.error(503, "商品服务暂时不可用");
            }

            /**
             * 获取商品分类列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<List<Map<String, Object>>> categoryList() {
                log.warn("获取商品分类列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "商品服务暂时不可用");
            }

            /**
             * 管理后台 — 获取商品分页列表 — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<Map<String, Object>> adminProductList(int pageNum, int pageSize, String keyword, Long categoryId) {
                log.warn("管理后台获取商品列表降级处理: 原因: {}", cause.getMessage());
                return Result.error(503, "商品服务暂时不可用");
            }

            /**
             * 管理后台 — 获取商品的所有SKU — 降级实现
             * 查询类方法，返回503错误提示
             */
            @Override
            public Result<List<Map<String, Object>>> getProductSkus(String id) {
                log.warn("获取商品SKU降级处理: id={}, 原因: {}", id, cause.getMessage());
                return Result.error(503, "商品服务暂时不可用");
            }

            // ======================== 写入/变更类方法（抛出异常） ========================

            /**
             * 管理后台 — 创建新商品 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> createProduct(Map<String, Object> productData) {
                log.error("创建商品失败: 原因: {}", cause.getMessage());
                throw new RuntimeException("商品服务暂时不可用，请稍后重试");
            }

            /**
             * 管理后台 — 更新商品信息 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> updateProduct(String id, Map<String, Object> productData) {
                log.error("更新商品失败: id={}, 原因: {}", id, cause.getMessage());
                throw new RuntimeException("商品服务暂时不可用，请稍后重试");
            }

            /**
             * 管理后台 — 删除商品 — 降级实现
             * 写入操作，不能静默失败，抛出运行时异常
             */
            @Override
            public Result<Void> deleteProduct(String id) {
                log.error("删除商品失败: id={}, 原因: {}", id, cause.getMessage());
                throw new RuntimeException("商品服务暂时不可用，请稍后重试");
            }
        };
    }
}
