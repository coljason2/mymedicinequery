package com.comman;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
			Connection.Response res = Jsoup.connect("https://www.chahwa.com.tw/user.php").userAgent(Agent)
					.data("username", form.getUsername(), "password", form.getPassword(), "wsrc", form.getWsrc(), "act",
							form.getAct(), "back_act", form.getBack_act())
					.method(Method.POST).ignoreHttpErrors(true).ignoreContentType(true).execute();
			cookies = res.cookies();
			// System.out.println(cookies.values());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cookies;
	}

	public static void main(String[] args) {
		Connectchawa con = new Connectchawa();
		trustAllHosts();
		con.Connect();
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