package com.tumsanitorium.servlets;

import com.tumsanitorium.utils.DatabaseConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import org.json.JSONObject;

@WebServlet("/user")
public class UserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if ("register".equalsIgnoreCase(action)) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                String fullName = request.getParameter("fullName");
                String phone = request.getParameter("phone");

                // Check if username exists
                PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    JSONObject json = new JSONObject();
                    json.put("success", false);
                    json.put("message", "Username already exists");
                    out.print(json.toString());
                    return;
                }

                // Insert new user
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO users (username, password_hash, full_name, phone) VALUES (?, ?, ?, ?)");
                insertStmt.setString(1, username);
                insertStmt.setString(2, password); // In production, hash the password
                insertStmt.setString(3, fullName);
                insertStmt.setString(4, phone);
                int rows = insertStmt.executeUpdate();

                JSONObject json = new JSONObject();
                if (rows > 0) {
                    json.put("success", true);
                    json.put("message", "Registration successful");
                } else {
                    json.put("success", false);
                    json.put("message", "Registration failed");
                }
                out.print(json.toString());

            } else if ("login".equalsIgnoreCase(action)) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");

                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT id, full_name FROM users WHERE username = ? AND password_hash = ?");
                stmt.setString(1, username);
                stmt.setString(2, password); // In production, hash the password
                ResultSet rs = stmt.executeQuery();

                JSONObject json = new JSONObject();
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String fullName = rs.getString("full_name");
                    HttpSession session = request.getSession();
                    session.setAttribute("userId", userId);
                    session.setAttribute("username", username);
                    session.setAttribute("fullName", fullName);

                    json.put("success", true);
                    json.put("message", "Login successful");
                    json.put("fullName", fullName);
                } else {
                    json.put("success", false);
                    json.put("message", "Invalid username or password");
                }
                out.print(json.toString());
            } else {
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("message", "Invalid action");
                out.print(json.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("message", "Database error: " + e.getMessage());
            out.print(json.toString());
        }
    }
}
