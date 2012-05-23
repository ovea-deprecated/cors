import org.eclipse.jetty.http.MimeTypes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ResourceServlet extends HttpServlet {
    private static final MimeTypes MIME_TYPES = new MimeTypes();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String res = req.getPathInfo().startsWith("/") ? req.getPathInfo().substring(1) : req.getPathInfo();
        File file = new File(res);
        if (!file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, req.getPathInfo());
        } else {
            OutputStream out = resp.getOutputStream();
            try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                byte[] data = new byte[8096];
                int c;
                int len = 0;
                while ((c = in.read(data)) != -1) {
                    len += c;
                    out.write(data, 0, c);
                }
                resp.setContentLength(len);
                resp.setContentType(MIME_TYPES.getMimeByExtension(req.getPathInfo()).toString());
            } finally {
                out.flush();
            }
        }
    }
}
