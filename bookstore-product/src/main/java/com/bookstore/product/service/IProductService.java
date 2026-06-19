package com.bookstore.product.service;

import com.bookstore.common.api.dto.ProductQueryDTO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.ProductVO;

import java.util.List;

/**
 * 商品业务服务接口
 * 定义所有商品相关的业务操作，包括查询、增删改、库存管理
 */
public interface IProductService {

    /**
     * 获取商品列表（支持分页、筛选、排序）
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<ProductVO> getProductList(ProductQueryDTO query);

    /**
     * 根据商品ID获取商品详情（含三重缓存保护）
     * @param id 商品ID
     * @return 商品详情视图对象
     */
    ProductVO getProductById(String id);

    /**
     * 获取推荐商品列表
     * @param limit 返回数量
     * @return 推荐商品列表
     */
    List<ProductVO> getRecommendProducts(Integer limit);

    /**
     * 获取热门商品列表
     * @param limit 返回数量
     * @return 热门商品列表
     */
    List<ProductVO> getHotProducts(Integer limit);

    /**
     * 批量获取商品信息（解决 N+1 调用问题）
     * @param ids 商品ID列表
     * @return 商品VO列表
     */
    List<ProductVO> batchGetProducts(List<String> ids);

    /**
     * 新增商品
     * @param vo 商品信息
     */
    void addProduct(ProductVO vo);

    /**
     * 修改商品信息
     * @param vo 商品信息
     */
    void updateProduct(ProductVO vo);

    /**
     * 删除商品
     * @param id 商品ID
     */
    void deleteProduct(String id);

    /**
     * 更新商品状态（上架/下架）
     * @param id 商品ID
     * @param status 目标状态
     */
    void updateProductStatus(String id, Integer status);

    /**
     * 扣减/恢复商品库存（带分布式锁保护）
     * @param id 商品ID
     * @param quantity 正数扣减，负数恢复
     */
    void updateStock(String id, Integer quantity);
}
