package com.bookstore.promotion.service;

import com.bookstore.common.api.dto.CouponCreateDTO;
import com.bookstore.common.api.vo.CouponVO;
import com.bookstore.common.api.vo.PageResult;

import java.util.List;

/**
 * Coupon service interface.
 * Defines all coupon-related business operations.
 */
public interface ICouponService {

    /**
     * Get available coupons for users (paginated).
     * @param pageNum page number
     * @param pageSize page size
     * @return paginated coupon list
     */
    PageResult<CouponVO> getCouponList(Integer pageNum, Integer pageSize);

    /**
     * Claim a coupon.
     * @param userId the user ID
     * @param couponId the coupon ID
     */
    void claimCoupon(String userId, Long couponId);

    /**
     * Get user's unused coupons.
     * @param userId the user ID
     * @return list of user's coupons
     */
    List<CouponVO> getUserCoupons(String userId);

    /**
     * Use a coupon.
     * @param userId the user ID
     * @param couponId the coupon ID
     */
    void useCoupon(String userId, Long couponId);

    /**
     * Get all coupons (admin, paginated).
     * @param pageNum page number
     * @param pageSize page size
     * @return paginated coupon list
     */
    PageResult<CouponVO> getAllCoupons(Integer pageNum, Integer pageSize);

    /**
     * Create a coupon (admin).
     * @param dto coupon creation data
     */
    void createCoupon(CouponCreateDTO dto);

    /**
     * Update a coupon (admin).
     * @param id the coupon ID
     * @param dto coupon update data
     */
    void updateCoupon(Long id, CouponCreateDTO dto);

    /**
     * Delete a coupon (admin).
     * @param id the coupon ID
     */
    void deleteCoupon(Long id);

    /**
     * Update coupon status (admin).
     * @param id the coupon ID
     * @param status target status (1=enabled, 0=disabled)
     */
    void updateCouponStatus(Long id, Integer status);
}
