package com.bookstore.common.api.vo;  // 声明当前类所属的包路径

import lombok.Data;  // 导入Lombok的@Data注解
import java.util.List;  // 导入List集合类

/**
 * 分页结果包装类（泛型）
 * 用于封装分页查询的结果，支持任意类型的列表数据
 * @param <T> 列表中元素的类型
 */
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class PageResult<T> {  // 分页结果包装类，T为泛型参数

    private List<T> records;  // 当前页的数据列表
    private Long total;  // 总记录数
    private Integer pageNum;  // 当前页码
    private Integer pageSize;  // 每页显示数量
    private Integer totalPages;  // 总页数

    /**
     * 无参构造方法
     */
    public PageResult() {}  // 默认构造方法

    /**
     * 有参构造方法
     * @param records 当前页的数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页显示数量
     */
    public PageResult(List<T> records, Long total, Integer pageNum, Integer pageSize) {  // 带参数的构造方法
        this.records = records;  // 设置数据列表
        this.total = total;  // 设置总记录数
        this.pageNum = pageNum;  // 设置当前页码
        this.pageSize = pageSize;  // 设置每页数量
        this.totalPages = (int) Math.ceil((double) total / pageSize);  // 计算总页数，向上取整
    }
}
