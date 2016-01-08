package medquery;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class MedicalQueryServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		req.getRequestDispatcher("/index.jsp").forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.getRequestDispatcher("/index.jsp").forward(req, resp);
	}
}
