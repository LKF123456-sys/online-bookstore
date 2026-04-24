package com.bookstore.entity;

public class Category {
    private String categoryid;
    private String name;
    private String descn;

    public Category() {}

    public String getCategoryid() { return categoryid; }
    public void setCategoryid(String categoryid) { this.categoryid = categoryid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescn() { return descn; }
    public void setDescn(String descn) { this.descn = descn; }
}