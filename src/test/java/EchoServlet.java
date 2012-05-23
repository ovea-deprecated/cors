import org.eclipse.jetty.util.IO;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.UUID;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class EchoServlet extends HttpServlet {

    private static final Random RANDOM = new Random();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        write(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        write(req, resp);
    }

    private void write(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long time = System.currentTimeMillis();
        String body = IO.toString(req.getInputStream());
        Cookie[] cookies = req.getCookies();
        String domain = System.getProperty("domain", "mycila.intra");

        System.out.println(time + " === REQUEST ===");
        System.out.println(" * type=" + req.getMethod());
        System.out.println(" * uri=" + req.getRequestURI());
        System.out.println(" * query=" + req.getQueryString());
        System.out.println(" * locale=" + req.getLocale());
        System.out.println(" * msg=" + req.getParameter("msg"));
        System.out.println(" * body=" + body);
        for (Cookie cookie : cookies) {
            System.out.println(" * cookie [" + cookie.getName() + "]=" + cookie.getValue());
        }

        String rmbrVal = UUID.randomUUID().toString();
        body = "{\"val\":\"" + new BigInteger(1024, RANDOM).toString() + "\"}";

        System.out.println(time + " === SESSION ===");
        req.getSession().setMaxInactiveInterval(60);
        System.out.println(" * id=" + req.getSession().getId());
        System.out.println(" * new=" + req.getSession().isNew());
        System.out.println(" * rmbr=" + req.getSession().getAttribute("rmbr"));
        req.getSession().setAttribute("rmbr", rmbrVal);

        System.out.println(time + " === RESPONSE ===");
        System.out.println(" * rmbr=" + rmbrVal);
        System.out.println(" * length=" + body.length());
        System.out.println("");

        Cookie rmbr = new Cookie("rmbr", rmbrVal);
        rmbr.setDomain("." + domain);
        rmbr.setPath("/");
        rmbr.setMaxAge(60);

        Cookie locale = new Cookie("locale", req.getLocale().toString());
        locale.setDomain("." + domain);
        locale.setPath("/");
        locale.setMaxAge(60);

        resp.addCookie(rmbr);
        resp.addCookie(locale);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(body.length());
        resp.getWriter().write(body);
    }
}
