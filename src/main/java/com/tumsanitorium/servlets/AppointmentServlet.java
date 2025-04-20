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

@WebServlet("/appointment")
public class AppointmentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Book an appointment
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("message", "User not logged in");
            out.print(json.toString());
            return;
        }

        int userId = (int) session.getAttribute("userId");
        int doctorId = Integer.parseInt(request.getParameter("doctorId"));
        String appointmentDate = request.getParameter("appointmentDate");

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO appointments (user_id, doctor_id, appointment_date) VALUES (?, ?, ?)");
            stmt.setInt(1, userId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, appointmentDate);

            int rows = stmt.executeUpdate();
            JSONObject json = new JSONObject();
            if (rows > 0) {
                json.put("success", true);
                json.put("message", "Appointment booked successfully");
            } else {
                json.put("success", false);
                json.put("message", "Failed to book appointment");
            }
            out.print(json.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("message", "Database error: " + e.getMessage());
            out.print(json.toString());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // List appointments for logged-in user
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("message", "User not logged in");
            out.print(json.toString());
            return;
        }

        int userId = (int) session.getAttribute("userId");

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT a.id, a.appointment_date, a.status, d.name AS doctor_name, d.whatsapp_number " +
                    "FROM appointments a JOIN doctors d ON a.doctor_id = d.id WHERE a.user_id = ? ORDER BY a.appointment_date DESC");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            JSONArray appointments = new JSONArray();
            while (rs.next()) {
                JSONObject appt = new JSONObject();
                appt.put("id", rs.getInt("id"));
                appt.put("appointmentDate", rs.getString("appointment_date"));
                appt.put("status", rs.getString("status"));
                appt.put("doctorName", rs.getString("doctor_name"));
                appt.put("whatsappNumber", rs.getString("whatsapp_number"));
                appointments.put(appt);
            }

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("appointments", appointments);
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
