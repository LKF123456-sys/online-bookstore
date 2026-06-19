package com.bookstore.promotion.service;

import com.bookstore.common.api.dto.ReviewSubmitDTO;
import com.bookstore.common.api.vo.PageResult;
import com.bookstore.common.api.vo.ReviewVO;

/**
 * Book review service interface.
 * Defines all book review-related business operations.
 */
public interface IBookReviewService {

    /**
     * Get reviews for a product (paginated, unblocked only).
     * @param productId the product ID
     * @param pageNum page number
     * @param pageSize page size
     * @return paginated review list
     */
    PageResult<ReviewVO> getProductReviews(String productId, Integer pageNum, Integer pageSize);

    /**
     * Submit a product review.
     * @param userId the user ID
     * @param dto review data
     */
    void submitReview(String userId, ReviewSubmitDTO dto);

    /**
     * Get reviews by a specific user (paginated).
     * @param userId the user ID
     * @param pageNum page number
     * @param pageSize page size
     * @return paginated review list
     */
    PageResult<ReviewVO> getUserReviews(String userId, Integer pageNum, Integer pageSize);

    /**
     * Get all reviews (admin, paginated).
     * @param pageNum page number
     * @param pageSize page size
     * @param blocked optional blocked-status filter
     * @return paginated review list
     */
    PageResult<ReviewVO> getAllReviews(Integer pageNum, Integer pageSize, Integer blocked);

    /**
     * Block a review (admin).
     * @param id the review ID
     */
    void blockReview(Long id);

    /**
     * Unblock a review (admin).
     * @param id the review ID
     */
    void unblockReview(Long id);

    /**
     * Pin a review to top (admin).
     * @param id the review ID
     */
    void topReview(Long id);

    /**
     * Unpin a review (admin).
     * @param id the review ID
     */
    void untopReview(Long id);

    /**
     * Reply to a review (admin).
     * @param id the review ID
     * @param reply reply content
     */
    void replyReview(Long id, String reply);

    /**
     * Delete a review (admin).
     * @param id the review ID
     */
    void deleteReview(Long id);
}
