package org.yearup.models;


import java.math.BigDecimal;
import java.time.LocalDate;

public class Order {
    private int orderId;
    private int userId;
    private LocalDate orderDate;
    private String shippingAddress;
    private BigDecimal shippingAmount;

    public Order() {
    }

    public Order(int orderId, int userId, LocalDate orderDate,
                 String shippingAddress, BigDecimal shippingAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.shippingAddress = shippingAddress;
        this.shippingAmount = shippingAmount;
    }


    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }
}