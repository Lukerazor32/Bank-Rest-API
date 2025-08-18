package com.example.bankcards.security;

import com.example.bankcards.util.JwtUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
public class JwtFilter implements Filter {

    @Autowired
    private ApplicationContext applicationContext;

    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Инициализация фильтра
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            // Ленивая инициализация зависимостей
            if (jwtUtil == null) {
                jwtUtil = applicationContext.getBean(JwtUtil.class);
                System.out.println("JWT фильтр: JwtUtil инициализирован");
            }
            if (userDetailsService == null) {
                userDetailsService = applicationContext.getBean(UserDetailsService.class);
                System.out.println("JWT фильтр: UserDetailsService инициализирован");
            }

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String token = extractTokenFromRequest(httpRequest);
            
            System.out.println("JWT фильтр: Запрос к " + httpRequest.getRequestURI());
            System.out.println("JWT фильтр: Токен найден: " + (token != null ? "Да" : "Нет"));

            if (StringUtils.hasText(token) && jwtUtil.validateAccessToken(token)) {
                System.out.println("JWT фильтр: Токен валиден");
                String username = jwtUtil.extractUsername(token);
                System.out.println("JWT фильтр: Username из токена: " + username);

                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    System.out.println("JWT фильтр: UserDetails загружен: " + (userDetails != null ? "Да" : "Нет"));

                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("JWT фильтр: Аутентификация установлена для пользователя: " + username);
                        System.out.println("JWT фильтр: Роли: " + userDetails.getAuthorities());
                    }
                } else {
                    System.out.println("JWT фильтр: Аутентификация уже установлена или username пустой");
                }
            } else {
                System.out.println("JWT фильтр: Токен не валиден или отсутствует");
            }
        } catch (Exception e) {
            // Логируем ошибку, но не прерываем цепочку фильтров
            System.err.println("Ошибка в JWT фильтре: " + e.getMessage());
            e.printStackTrace();
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Освобождение ресурсов
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
