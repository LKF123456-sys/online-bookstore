package com.bookstore.entity;

public class Inventory {
    private String itemid;
    private int qty;

    public Inventory() {}

    public String getItemid() { return itemid; }
    public void setItemid(String itemid) { this.itemid = itemid; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}