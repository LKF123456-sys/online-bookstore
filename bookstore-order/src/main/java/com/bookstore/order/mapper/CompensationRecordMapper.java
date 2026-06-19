package com.bookstore.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bookstore.common.entity.CompensationRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 补偿记录数据访问层
 * 继承 MyBatis-Plus BaseMapper，自动获得 CRUD 能力
 */
@Mapper
public interface CompensationRecordMapper extends BaseMapper<CompensationRecord> {
}
