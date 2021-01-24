package com.andrulban.sandbox.stripe.config.filter;

import com.andrulban.sandbox.stripe.exception.ErrorInfo;
import com.andrulban.sandbox.stripe.utils.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private final String secret;
    private final String headerName;
    private final String tokenPrefix;

    public JWTAuthorizationFilter(AuthenticationManager authManager, String secret, String headerName, String tokenPrefix) {
        super(authManager);
        this.secret = secret;
        this.headerName = headerName;
        this.tokenPrefix = tokenPrefix;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(headerName);

        if (header == null || !header.startsWith(tokenPrefix)) {
            chain.doFilter(req, res);
            return;
        }

        try {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(req, res);
        } catch (ExpiredJwtException ex) {
            ErrorInfo errorInfo = new ErrorInfo(new Date(), HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getOutputStream().println(new ObjectMapper().writeValueAsString(errorInfo));
        }
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) throws IOException {
        String token = request.getHeader(headerName);
        if (token != null) {
            // parse the token.
            String userString = Jwts.parser()
                    .setSigningKey(secret.getBytes())
                    .parseClaimsJws(token.replace(tokenPrefix, ""))
                    .getBody()
                    .getSubject();

            if (userString != null) {
                CustomUserDetails customUserDetails = new ObjectMapper().readValue(userString, CustomUserDetails.class);
                return new UsernamePasswordAuthenticationToken(customUserDetails, null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + customUserDetails.getUserRole().toString())));
            }
            return null;
        }
        return null;
    }
}