package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    public MySqlShoppingCartDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId)
    {
        // Build a complete shopping cart from database
        ShoppingCart cart = new ShoppingCart();

        // SELECT: We need data from both the 'shopping_cart' table AND the 'products' table.
        // JOIN: We match the product_id in the cart with the product_id in the products table.
        // WHERE: We only want items for this specific user.

        String cartSql = "SELECT sc.user_id, sc.product_id, sc.quantity, " +
                "       p.product_id, p.name, p.price, p.category_id, " +
                "       p.description, p.subcategory, p.stock, p.featured, p.image_url " +
                "FROM shopping_cart sc " +
                "JOIN products p ON sc.product_id = p.product_id " +
                "WHERE sc.user_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(cartSql);
            statement.setInt(1, userId);

            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                // Get the product info or object
                Product product = MySqlProductDao.mapRow(row);

                // Get the cart item info
                int quantity = row.getInt("quantity");

                // Create a shopping cart item
                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(product);
                item.setQuantity(quantity);

                // Add item to cart
                cart.add(item);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return cart;
    }

    @Override
    public void addItemToCart(int userId, int productId)
    {
        // INSERT: add a new row with the user_id, product_id, and quantity of 1.
        // ON DUPLICATE KEY UPDATE: If that specific user/product combo already exists,
        // just add 1 to the existing quantity.

        String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) " +
                "VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + 1";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateItemQuantity(int userId, int productId, int quantity)
    {
        // Update the quantity of an item in the cart

        if (quantity == 0)
        {
            deleteItem(userId, productId);
            return;
        }

        String sql = "UPDATE shopping_cart " +
                "SET quantity = ? " +
                "WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCart(int userId)
    {
        // Remove ALL items from a user's cart

        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void deleteItem(int userId, int productId)
    {
        // Delete a specific item from cart

        String sql = "DELETE FROM shopping_cart " +
                "WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}