package com.tumsanitorium.servlets;

import com.tumsanitorium.utils.DatabaseConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/doctors")
public class DoctorsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id, name, specialty FROM doctors");
            ResultSet rs = stmt.executeQuery();

            JSONArray doctors = new JSONArray();
            while (rs.next()) {
                JSONObject doc = new JSONObject();
                doc.put("id", rs.getInt("id"));
                doc.put("name", rs.getString("name"));
                doc.put("specialty", rs.getString("specialty"));
                doctors.put(doc);
            }

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("doctors", doctors);
            out.print(json.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("message", "Database error: " + e.getMessage());
            out.print(json.toString());
        }
    }
}
