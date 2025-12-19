package org.yearup.data.mysql;


import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {

    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Order createOrder(Order order) {

        String sql = """
            INSERT INTO orders (user_id, order_date, address, shipping_amount)
            VALUES (?, ?, ?, ?)
            """;


        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, order.getUserId());
            statement.setDate(2, Date.valueOf(order.getOrderDate()));
            statement.setString(3, order.getShippingAddress());
            statement.setBigDecimal(4, order.getShippingAmount());


            statement.executeUpdate();


            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    order.setOrderId(orderId);
                    return order;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating order", e);
        }

        return null;
    }

    @Override
    public void addLineItem(int orderId, int productId, BigDecimal salesPrice,
                            int quantity, BigDecimal discount) {

        String sql = """
            INSERT INTO order_line_items 
            (order_id, product_id, sales_price, quantity, discount)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);
            statement.setInt(2, productId);
            statement.setBigDecimal(3, salesPrice);
            statement.setInt(4, quantity);
            statement.setBigDecimal(5, discount);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error adding line item to order", e);
        }
    }
}