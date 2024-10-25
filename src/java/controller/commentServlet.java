/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import dao.postDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Comment;
import model.Post;
import model.User;

/**
 *
 * @author bim26
 */
@MultipartConfig
public class commentServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet commentServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet commentServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            String postIdStr = request.getParameter("post_id");
            if (postIdStr != null) {
                try {
                    int postId = Integer.parseInt(postIdStr);
                    List<Comment> comments = postDAO.getComments(postId);
                    request.setAttribute("comments", comments);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            request.getRequestDispatcher("WEB-INF/home.jsp").forward(request, response);
        } else {
            response.sendRedirect("login");
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute("user") != null) {
        int post_id = Integer.parseInt(request.getParameter("post_id"));
        String commentContent = request.getParameter("commentContent");
        postDAO postDAO = new postDAO();
        User user = (User) session.getAttribute("user");
        Comment comment = new Comment();
        if (commentContent != null && !commentContent.trim().isEmpty()) {
            comment.setPost_id(post_id);
            comment.setUser_id(user.getUser_id());
            comment.setComment_text(commentContent);
            comment.setFirst_name(user.getFirst_name()); 
            comment.setLast_name(user.getLast_name());   
            comment.setProfile_pic(user.getProfile_pic());
        }
        try {
            postDAO.addComment(comment);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("success", true);
            jsonResponse.put("comment", comment);
            out.print(new Gson().toJson(jsonResponse));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("success", false);
            jsonResponse.put("error", e.getMessage());
            out.print(new Gson().toJson(jsonResponse));
            out.flush();
        }
    } else {
        response.sendRedirect("login");
    }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
