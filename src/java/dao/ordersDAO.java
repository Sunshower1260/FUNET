/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.lang.System.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.OrderDetailDTO;
import model.Orders;
import util.sqlConnect;
/**
 *
 * @author ADMIN
 */
public class ordersDAO {
    private Connection connection;

    // Lấy tất cả các đơn hàng
    public List<Orders> getAllOrders() throws Exception {
        List<Orders> ordersList = new ArrayList<>();
        String sql = "SELECT * FROM Orders";

        try {
            // Kết nối đến database
            connection = sqlConnect.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Lấy dữ liệu từ kết quả query và đưa vào danh sách orders
            while (resultSet.next()) {
                int order_id = resultSet.getInt("order_id");
                int user_id = resultSet.getInt("user_id");
                int cart_id = resultSet.getInt("cart_id");
                double total_amount = resultSet.getDouble("total_amount");
                String order_status = resultSet.getString("order_status");
                Timestamp order_date = resultSet.getTimestamp("order_date");
                String order_note = resultSet.getString("order_note");
                String shipping_address = resultSet.getString("shipping_address");

                Orders order = new Orders(order_id, user_id, cart_id, total_amount, order_status, order_date, order_note, shipping_address);
                ordersList.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ordersList;
    }

    public List<Orders> getOrdersByUserId(int userId) throws Exception {
        List<Orders> orders = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE user_id = ? ORDER BY order_date DESC";

        try (Connection conn = sqlConnect.getInstance().getConnection(); 
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Orders order = new Orders(
                    rs.getInt("order_id"),
                    rs.getInt("user_id"),
                    rs.getInt("cart_id"),
                    rs.getDouble("total_amount"),
                    rs.getString("order_status"),
                    rs.getTimestamp("order_date"),
                    rs.getString("order_note"),
                    rs.getString("shipping_address")
                );

                    orders.add(order);
                }
            }
        } catch (SQLException e) {
        }

        return orders;
    }

    // Phương thức lưu đơn hàng
    public boolean saveOrder(Orders order) throws Exception {
        // Thêm order_id vào câu SQL
        String sql = "INSERT INTO Orders (order_id, user_id, cart_id, total_amount, order_status, order_date, order_note, shipping_address) "
                + "VALUES (?, ?, ?, ?, ?, GETDATE(), ?, ?)";
        try {
            connection = sqlConnect.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            
            // Set order_id là parameter đầu tiên
            preparedStatement.setInt(1, order.getOrder_id());     // order_id từ Config.getRandomNumber
            preparedStatement.setInt(2, order.getUser_id());      // user_id
            preparedStatement.setInt(3, order.getCart_id());      // cart_id
            preparedStatement.setDouble(4, order.getTotal_amount()); // total_amount
            preparedStatement.setString(5, order.getOrder_status()); // order_status
            preparedStatement.setString(6, order.getOrder_note());   // order_note
            preparedStatement.setString(7, order.getShipping_address()); // shipping_address
            
            // Thêm log để debug
            System.out.println("Executing SQL: " + sql);
            System.out.println("Parameters: " + 
                "order_id=" + order.getOrder_id() +
                ", user_id=" + order.getUser_id() + 
                ", cart_id=" + order.getCart_id() + 
             ", total_amount=" + order.getTotal_amount()
                    + ", order_status=" + order.getOrder_status()
                    + ", order_note=" + order.getOrder_note()
                    + ", shipping_address=" + order.getShipping_address());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println("Error saving order: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    public boolean deleteOrder(int orderId) throws Exception {
        String sql = "DELETE FROM Orders WHERE order_id = ?";

        try {
            connection = sqlConnect.getInstance().getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, orderId);

            System.out.println("Executing SQL: " + sql);
            System.out.println("Parameter orderId: " + orderId);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            System.out.println("Error deleting order: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }
    public List<Orders> getOrdersBySellerProducts(int sellerId) {
        List<Orders> sellerOrders = new ArrayList<>();
        String sql = "SELECT o.* FROM Orders o " +
                    "JOIN ShoppingCartItem sci ON o.cart_id = sci.cart_id " +
                    "JOIN Product p ON sci.product_id = p.product_id " +
                    "WHERE p.user_id = ?";
                    
        try (Connection con = sqlConnect.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                Orders order = new Orders();
                order.setOrder_id(rs.getInt("order_id"));
                order.setUser_id(rs.getInt("user_id")); 
                order.setCart_id(rs.getInt("cart_id"));
                order.setTotal_amount(rs.getDouble("total_amount"));
                order.setOrder_status(rs.getString("order_status"));
                order.setOrder_date(rs.getTimestamp("order_date"));
                order.setOrder_note(rs.getString("order_note"));
                order.setShipping_address(rs.getString("shipping_address"));
                sellerOrders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sellerOrders;
    }
    
    public void updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE Orders SET order_status = ? WHERE order_id = ?";
        try (Connection con = sqlConnect.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<OrderDetailDTO> getOrderDetailsForUser(int userId) {
        List<OrderDetailDTO> orderDetails = new ArrayList<>();
        String sql = "SELECT o.order_id, p.product_name, sci.quantity, "
                + "p.price * sci.quantity AS total_price, o.order_status, "
                + "o.order_date, CONCAT(u.first_name, ' ', u.last_name) AS seller_name "
                + "FROM Orders o "
                + "JOIN ShoppingCartItem sci ON o.cart_id = sci.cart_id "
                + "JOIN Product p ON sci.product_id = p.product_id "
                + "JOIN userAccount u ON p.user_id = u.user_id "
                + "WHERE o.user_id = ? "
                + "ORDER BY o.order_date DESC;";

        try (Connection conn = sqlConnect.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OrderDetailDTO detail = new OrderDetailDTO(
                        rs.getInt("order_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("total_price"),
                        rs.getString("order_status"),
                        rs.getTimestamp("order_date"),
                        rs.getString("seller_name")
                );
                orderDetails.add(detail);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderDetails;
    }

    public List<OrderDetailDTO> getOrdersForSeller(int sellerId) throws Exception {
        List<OrderDetailDTO> orderDetails = new ArrayList<>();
        String sql = "SELECT o.order_id, o.order_date, p.product_name, sci.quantity, "
                + "p.price, CONCAT(u.first_name, ' ', u.last_name) AS buyer_name "
                + "FROM Orders o "
                + "JOIN ShoppingCartItem sci ON o.cart_id = sci.cart_id "
                + "JOIN Product p ON sci.product_id = p.product_id "
                + "JOIN userAccount u ON o.user_id = u.user_id " // Ensure this matches your user table structure
                + "WHERE p.user_id = ?"; // Filter by sellerId

        try (Connection conn = sqlConnect.getInstance().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sellerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Sử dụng constructor với tất cả các tham số
                OrderDetailDTO detail = new OrderDetailDTO(
                        rs.getInt("order_id"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        "Pending", // Hoặc thay bằng trạng thái đơn hàng thực tế nếu có trong truy vấn
                        rs.getTimestamp("order_date"), // Chuyển đổi thành Date
                        rs.getString("buyer_name") // Tên người mua
                );
                orderDetails.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderDetails;
    }

    public static void main(String[] args) throws Exception {
        ordersDAO o = new ordersDAO();
        System.out.println(o.getOrdersForSeller(4));
    }
}