package com.elephant.safetybackend.filter;

import com.elephant.safetybackend.service.UserService;
import com.elephant.safetybackend.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        System.out.println("=== JWT REQUEST FILTER ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Authorization header: " + (authorizationHeader != null ? "Present" : "Missing"));

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("JWT token found");
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("Username extracted from token: '" + username + "'");
            } catch (Exception e) {
                System.out.println("Failed to extract username from token: " + e.getMessage());
            }
        } else {
            System.out.println("No Bearer token found in request");
        }

        // IMPORTANT: Always try to set authentication if we have a username
        if (username != null) {
            System.out.println("Loading UserDetails for username: '" + username + "'");
            UserDetails userDetails = this.userService.loadUserByUsername(username);
            System.out.println("UserDetails loaded: " + (userDetails != null ? "Success" : "Failed"));

            if (jwtUtil.validateToken(jwt, userDetails)) {
                System.out.println("Token validated successfully for user: '" + username + "'");

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                System.out.println("Authentication set in SecurityContext for user: '" + username + "'");
            } else {
                System.out.println("Token validation FAILED for user: '" + username + "'");
            }
        } else {
            System.out.println("No username extracted, clearing authentication");
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}