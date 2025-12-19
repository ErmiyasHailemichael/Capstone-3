package org.yearup.data;

import org.yearup.models.Order;
import org.yearup.models.ShoppingCart;

import java.math.BigDecimal;

public interface OrderDao {
    Order createOrder(Order order);
    void addLineItem(int orderId, int productId, BigDecimal salesPrice,
                     int quantity, BigDecimal discount);
}

