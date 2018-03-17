package medquery.comman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class getQuery {
	final Logger log = Logger.getLogger(getQuery.class.getName());
	final String getdrug = "http://www.chahwa.com.tw/order.php?act=query&&drug=";

	static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
	LoginFormData form = new LoginFormData();

	public List<MedEntity> getMedicine(String name) {
		//trustAllHosts();
		String cookie = getCookies();
		List<MedEntity> meds = new ArrayList<MedEntity>();
		// clean code
		String parseString = decode(Getcontext(name, cookie).replace("\\//", "")).replace("\\", "").replace("}", "")
				.replace("{", "").replace("rn", "");
		Document resault = Jsoup.parse(parseString);

		for (Element n : resault.getElementsByClass("item_text")) {
			MedEntity m = new MedEntity();
			m.setOid(n.getElementsByClass("code").text());
			m.setOidprice(n.getElementsByClass("price").text());
			m.setIsenough(n.getElementsByClass("sell_price").text());
			m.setName(n.getElementsByClass("name").text());
			meds.add(m);
		}

		return meds;
	}

	public String Getcontext(String name, String cookiePara) {
		String context = "";

		try {

			URL getUrl = new URL(getdrug + URLEncoder.encode(name, "utf-8"));
			HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setReadTimeout(3000);
			connection.addRequestProperty("Cookie", cookiePara);
			connection.connect();
			log.log(Level.INFO, "Response code = {} ", connection.getResponseCode());
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String lines;
			while ((lines = reader.readLine()) != null) {
				context = context + decode(lines);
			}
			// log.info("html size:" + context.length());
			// log.info("html :" + context);
			reader.close();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return context;
	}

	public String decode(String s) {

		Matcher m = reUnicode.matcher(s);
		StringBuffer sb = new StringBuffer(s.length());
		while (m.find()) {
			m.appendReplacement(sb, Character.toString((char) Integer.parseInt(m.group(1), 16)));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public String getCookies() {
		Map<String, String> cookies = null;
		String cookiePara = "";
		try {
			Connection.Response res = Jsoup.connect("http://www.chahwa.com.tw/user.php")
					.data("username", form.getUsername(), "password", form.getPassword(), "wsrc", form.getWsrc(), "act",
							form.getAct(), "back_act", form.getBack_act())
					.method(Method.POST).execute();
			cookies = res.cookies();

		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String cookie : cookies.keySet()) {
			cookiePara = cookiePara + cookie.toString() + "=" + cookies.get(cookie) + ";";
		}
		log.info(cookiePara.toString());
		return cookiePara;
	}

	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) {

			}
		} };

		// Install the all-trusting trust manager
		// 忽略HTTPS请求的SSL证书，必须在openConnection之前调用
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
