package medquery;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import medquery.comman.MedEntity;
import medquery.comman.getQuery;

public class QueryServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String medicine;
	final Logger log = Logger.getLogger(QueryServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		getQuery getmed = new getQuery();
		medicine = new String(req.getParameter("querystring").getBytes(
				"ISO-8859-1"), "UTF-8");
		log.info("querystring:" + medicine);
		List<MedEntity> meds = getmed.getMedicine(medicine);
		log.info(meds.toString());
		req.setAttribute("meds", meds);
		RequestDispatcher view = req.getRequestDispatcher("resault.jsp");
		view.forward(req, resp);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
