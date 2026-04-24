package com.bookstore.entity;

import java.math.BigDecimal;

public class Book {
    private Integer id;
    private String name;
    private String author;
    private BigDecimal price;
    private Integer stock;
    private String img;
    private String description;
    private Integer categoryId;
    private String categoryName; // 分类名称（页面展示用）

    // 无参构造
    public Book() {}

    // Getter和Setter方法（IDEA自动生成：Alt+Insert → Getter and Setter）
    public Integer getId() {return id;}
    public void setId(Integer id) {this.id = id;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getAuthor() {return author;}
    public void setAuthor(String author) {this.author = author;}
    public BigDecimal getPrice() {return price;}
    public void setPrice(BigDecimal price) {this.price = price;}
    public Integer getStock() {return stock;}
    public void setStock(Integer stock) {this.stock = stock;}
    public String getImg() {return img;}
    public void setImg(String img) {this.img = img;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public Integer getCategoryId() {return categoryId;}
    public void setCategoryId(Integer categoryId) {this.categoryId = categoryId;}
    public String getCategoryName() {return categoryName;}
    public void setCategoryName(String categoryName) {this.categoryName = categoryName;}
}