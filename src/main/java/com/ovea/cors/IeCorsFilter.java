package com.ovea.cors;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class IeCorsFilter implements Filter {
    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        String ua;
        if (Boolean.valueOf(req.getParameter("cors")) && (ua = req.getHeader("User-Agent")) != null && ua.contains("MSIE")) {
            filterChain.doFilter(req, res);
            String header = res.getHeader("Set-Cookie");
            if (header != null) {
                StringBuilder cookies = new StringBuilder(header).append("~").append(res.getStatus()).append("~").append(header.length()).append("~");
                res.getOutputStream().print(cookies.toString());
            }
        } else {
            filterChain.doFilter(req, res);
        }
    }

}
