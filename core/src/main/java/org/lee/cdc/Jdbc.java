package org.lee.cdc;

import java.sql.*;

public class Jdbc {

    public static void main(String[] args) {
        // 使用try-with-resources自动关闭连接
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://192.168.5.4:33060/cdc?useSSL=false&serverTimezone=UTC",
                "cdc",
                "leeqian");
             Statement stmt = conn.createStatement()) {

            // 执行查询
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
