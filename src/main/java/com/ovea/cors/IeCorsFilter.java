package com.ovea.cors;

import org.eclipse.jetty.server.Response;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpCookie;

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
        String ua = req.getHeader("User-Agent");
        if (ua != null && ua.contains("MSIE")) {
            Response response = (Response) res;


            filterChain.doFilter(req, res);
        } else {
            filterChain.doFilter(req, res);
        }
    }

    public static void main(String[] args) {
        String header = "Set-Cookie:id=izpw3ordoglb189uf5xjlsuke;Path=/;Domain=jaxspot.net;Expires=Tue, 21-Feb-2012 19:26:53 GMT;HttpOnly, rmb=deleteMe; Domain=.jaxspot.net; Path=/; Max-Age=0; Expires=Mon, 20-Feb-2012 18:56:53 GMT; Version=1, rmb=6ySnDQOMmbdgTU8WVgSCfu37ysbXHEDRuLIyZ0s5HB6ovmpcSTCJEkFCEOOy4T/8GA1c4AMi6hY+AFBuc33Fpg==; Domain=.jaxspot.net; Path=/; Version=1; HttpOnly";
        StringBuilder sb = new StringBuilder();
        for (int start = 0, next = 0; next != -1 && (next = header.indexOf("Expires=", start)) >= -1; start = next == -1 ? start : (header.indexOf(';', next) + 1)) {
            sb.append(header.substring(start, next == -1 ? header.length() : next));
        }
        System.out.println(sb);
        for (HttpCookie cookie : HttpCookie.parse(sb.toString())) {
            System.out.println(cookie);
            System.out.println(cookie.isHttpOnly());
            System.out.println(cookie.getMaxAge());
            System.out.println(cookie.getVersion());
        }
    }
}
