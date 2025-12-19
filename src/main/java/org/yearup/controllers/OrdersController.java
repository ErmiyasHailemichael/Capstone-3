package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/orders")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class OrdersController {

    private OrderDao orderDao;
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;

    @Autowired
    public OrdersController(OrderDao orderDao,
                            ShoppingCartDao shoppingCartDao,
                            UserDao userDao) {
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
    }

    @PostMapping
    public Order checkout(Principal principal) {
        try {

            String username = principal.getName();
            User user = userDao.getByUserName(username);
            int userId = user.getId();


            ShoppingCart cart = shoppingCartDao.getByUserId(userId);


            Order order = new Order();
            order.setUserId(userId);
            order.setOrderDate(LocalDate.now());
            order.setShippingAddress("");
            order.setShippingAmount(null);

            Order createdOrder = orderDao.createOrder(order);


            for (ShoppingCartItem cartItem : cart.getItems().values()) {
                orderDao.addLineItem(
                        createdOrder.getOrderId(),
                        cartItem.getProductId(),
                        cartItem.getProduct().getPrice(),
                        cartItem.getQuantity(),
                        cartItem.getDiscountPercent()
                );
            }


            shoppingCartDao.clearCart(userId);


            return createdOrder;

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error during checkout");
        }
    }
}