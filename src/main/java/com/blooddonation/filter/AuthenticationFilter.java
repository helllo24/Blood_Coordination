package com.blooddonation.filter;

import com.blooddonation.model.Role;
import com.blooddonation.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Filter securing protected dashboard pages and enforcing role-based access control.
 * Prevents unauthorized access, cross-role tampering, and post-logout browser cache retrieval.
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {
        "/admin-dashboard.jsp",
        "/admin-reports.jsp",
        "/reports",
        "/donor-dashboard.jsp",
        "/donor-profile.jsp",
        "/requester-dashboard.jsp",
        "/notifications",
        "/notifications.jsp"
})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. Prevent browser caching for all protected dashboard pages
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0
        httpResponse.setDateHeader("Expires", 0); // Proxies

        String uri = httpRequest.getRequestURI();
        HttpSession session = httpRequest.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        // 2. Unauthenticated check: Redirect if no active session exists
        if (currentUser == null) {
            String msg = URLEncoder.encode("Please sign in to access this page.", StandardCharsets.UTF_8);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?error=" + msg);
            return;
        }

        Role role = currentUser.getRole();

        // 3. Role-Based Access Control Checks
        if (uri.contains("admin-dashboard.jsp") || uri.contains("admin-reports.jsp") || uri.contains("/reports")) {
            if (role != Role.ADMIN) {
                String msg = URLEncoder.encode("Access denied. Administrator privileges are required to access this portal.", StandardCharsets.UTF_8);
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?error=" + msg);
                return;
            }
        } else if (uri.contains("donor-dashboard.jsp") || uri.contains("donor-profile.jsp")) {
            if (role != Role.DONOR && role != Role.ADMIN) {
                String msg = URLEncoder.encode("Access denied. The donor portal is restricted to registered blood donors.", StandardCharsets.UTF_8);
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?error=" + msg);
                return;
            }
        } else if (uri.contains("requester-dashboard.jsp")) {
            if (role != Role.REQUESTER && role != Role.ADMIN) {
                String msg = URLEncoder.encode("Access denied. The requester dashboard is restricted to blood requesters.", StandardCharsets.UTF_8);
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?error=" + msg);
                return;
            }
        }

        // 4. Authorized: proceed to destination
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup
    }
}
