/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.cors;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class IeCorsFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(IeCorsFilter.class.getName());

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
        if (Boolean.valueOf(req.getParameter("_xd")) && (ua = req.getHeader("User-Agent")) != null && ua.contains("MSIE")) {
            filterChain.doFilter(new HttpServletRequestWrapper(req) {
                @Override
                public String getHeader(String name) {
                    return name.equalsIgnoreCase("Accept-Encoding") ? null : super.getHeader(name);
                }
            }, res);
            String header = res.getHeader("Set-Cookie");
            if (header != null) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Appending cookies to request: " + req.getRequestURI());
                }
                StringBuilder cookies = new StringBuilder(header).append("~").append(res.getStatus()).append("~").append(header.length()).append("~");
                res.getOutputStream().print(cookies.toString());
            }
        } else {
            filterChain.doFilter(req, res);
        }
    }

}
