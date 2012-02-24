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

import org.eclipse.jetty.http.HttpFields;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class IeCorsFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(IeCorsFilter.class.getName());
    private static final String __01Jan1970_COOKIE = HttpFields.formatDate(0);

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
            final List<Cookie> cookies = new LinkedList<>();
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            filterChain.doFilter(new HttpServletRequestWrapper(req) {
                    @Override
                    public String getHeader(String name) {
                        return name.equalsIgnoreCase("Accept-Encoding") ? null : super.getHeader(name);
                    }
                }, new HttpServletResponseWrapper(res) {
                @Override
                public void addCookie(Cookie cookie) {
                    super.addCookie(cookie);
                    cookies.add(cookie);
                }

                @Override
                public ServletOutputStream getOutputStream() throws IOException {
                    return new ServletOutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                            output.write(b);
                        }
                    };
                }

                @Override
                public PrintWriter getWriter() throws IOException {
                    return new PrintWriter(output, true);
                }
            }
            );
            HttpSession session = req.getSession(false);
            StringBuilder header = new StringBuilder(res.getHeader("Set-Cookie") == null ? "" : res.getHeader("Set-Cookie"));
            int p = header.indexOf(";HttpOnly");
            if (p != -1) {
                header.replace(p, p + 9, "");
            }
            for (Cookie cookie : cookies) {
                if (header.indexOf(cookie.getName() + "=") == -1) {
                    header.append(header.length() == 0 ? "" : ", ").append(cookie.getName()).append("=").append(cookie.getValue());
                    if (cookie.getPath() != null && cookie.getPath().length() > 0) {
                        header.append(";Path=").append(cookie.getPath());
                    }
                    if (cookie.getMaxAge() >= 0) {
                        header.append(";Expires=");
                        if (cookie.getMaxAge() == 0) {
                            header.append(__01Jan1970_COOKIE);
                        } else {
                            HttpFields.formatCookieDate(header, System.currentTimeMillis() + 1000L * cookie.getMaxAge());
                        }
                    }
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Appending cookies to request " + req.getRequestURI() + ", header=" + header + ", cookies=" + cookies + ", sessionId=" + (session == null ? null : session.getId()));
            }
            p = header.length();
            header.append("~").append(res.getStatus()).append("~").append(p).append("~");
            p = header.length();
            if ("gzip".equals(res.getHeader("Content-Encoding"))) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Uncompressing GZIP response...");
                }
                res.setHeader("Content-Encoding", null);
                ByteArrayOutputStream uncompressed = new ByteArrayOutputStream();
                GZIPInputStream gzipStream = new GZIPInputStream(new ByteArrayInputStream(output.toByteArray()));
                int c;
                byte[] buffer = new byte[8096];
                while ((c = gzipStream.read(buffer)) != -1) {
                    uncompressed.write(buffer, 0, c);
                }
                res.setContentLength(p + uncompressed.size());
                uncompressed.writeTo(res.getOutputStream());
            } else {
                res.setContentLength(p + output.size());
                output.writeTo(res.getOutputStream());
            }
            res.getOutputStream().write(header.toString().getBytes());
            res.flushBuffer();
        } else {
            filterChain.doFilter(req, res);
        }
    }

}
