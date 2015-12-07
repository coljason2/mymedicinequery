package medquery.comman;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;


public class Connectchawa extends HttpServlet {

	private static final long serialVersionUID = 1L;
	final String Agent = "Mozilla/5.0";
	// Initialize FormData
	LoginFormData form = new LoginFormData();
	public Map<String, String> cookies;

	public Map<String, String> Connect() {
		try {
			Connection.Response res = Jsoup
					.connect("http://www.chahwa.com.tw/user.php")
					.userAgent(Agent)
					.data("username", form.getUsername(), "password",
							form.getPassword(), "wsrc", form.getWsrc(), "act",
							form.getAct(), "back_act", form.getBack_act())
					.method(Method.POST).execute();
			cookies = res.cookies();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cookies;
	}

	public static void main(String[] args) {
		Connectchawa con = new Connectchawa();
		con.Connect();

	}
}