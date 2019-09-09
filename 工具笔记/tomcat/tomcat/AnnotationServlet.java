import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/my-annotation-servlet")
public class AnnotationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("正在处理 GET 请求。。。。。。");
        String name = req.getParameter("name");
		if (null == name || "".equals(name.trim())) {
            name = "World";
        }
        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/html;charset=utf-8");
        writer.println("<h1>Hello " + name + ", My Annotation Servlet</h1>");
        writer.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("正在处理 POST 请求。。。。。。");
        String name = req.getParameter("name");
		if (null == name || "".equals(name.trim())) {
            name = "World";
        }
        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/html;charset=utf-8");
        writer.println("<h1>Hello " + name + ", My Annotation Servlet</h1>");
        writer.flush();
    }

}
