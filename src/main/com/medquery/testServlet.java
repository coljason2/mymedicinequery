package medquery;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import medquery.comman.LoginFormData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;

@SuppressWarnings("serial")
public class testServlet extends HttpServlet {
	static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		Map<String, String> cookies = null;
		LoginFormData form = new LoginFormData();
		try {
			Connection.Response res = Jsoup
					.connect("http://www.chahwa.com.tw/user.php")
					.data("username", form.getUsername(), "password",
							form.getPassword(), "wsrc", form.getWsrc(), "act",
							form.getAct(), "back_act", form.getBack_act())
					.method(Method.POST).execute();
			cookies = res.cookies();
			String getURL = "http://www.chahwa.com.tw/order.php?act=query&&drug="
					+ URLEncoder.encode("®õ§J", "utf-8");
			String doc = Jsoup.connect(getURL).timeout(10000).cookies(cookies)
					.get().select("script").toString();
			String parseString = decode1(doc.replace("\\//", ""))
					.replace("\\", "").replace("}", "").replace("{", "")
					.replace("rn", "");

			Document jsoup = Jsoup.parse(parseString);
			String test = jsoup.toString();
			req.setAttribute("test", test);
			RequestDispatcher view = req.getRequestDispatcher("test.jsp");
			view.forward(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String decode1(String s) {
		Matcher m = reUnicode.matcher(s);
		StringBuffer sb = new StringBuffer(s.length());
		while (m.find()) {
			m.appendReplacement(sb,
					Character.toString((char) Integer.parseInt(m.group(1), 16)));
		}
		m.appendTail(sb);
		return sb.toString();
	}

}
