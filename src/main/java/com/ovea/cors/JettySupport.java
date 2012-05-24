package com.ovea.cors;

import org.eclipse.jetty.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class JettySupport {
    public static void fixContentType(HttpServletRequest req) {
        String query = req.getQueryString();
        if (query != null) {
            query = query.toLowerCase();
            int pos = query.indexOf("content-type");
            if (pos != -1) {
                pos = query.indexOf('=', pos);
                int end = query.indexOf('&', pos);
                if (end == -1) {
                    end = query.length();
                }
                String dec;
                try {
                    dec = URLDecoder.decode(query.substring(pos + 1, end), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // should never occur
                    throw new RuntimeException(e.getMessage(), e);
                }
                pos = dec.indexOf(";"); // charset ?
                if (pos != -1) {
                    dec = dec.substring(0, pos).trim();
                }
                org.eclipse.jetty.server.AbstractHttpConnection.getCurrentConnection().getRequestFields().add(HttpHeaders.CONTENT_TYPE, dec);
            }
        }
    }
}
