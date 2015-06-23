package medquery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import medquery.comman.LoginFormData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;



public class test {
	static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

	public static void main(String[] args) throws IOException {
		LoginFormData form = new LoginFormData();
		Connection.Response res = Jsoup
				.connect("https://www.chahwa.com.tw/user.php")
				.data("username", form.getUsername(), "password",
						form.getPassword(), "wsrc", form.getWsrc(), "act",
						form.getAct(), "back_act", form.getBack_act())
				.method(Method.POST).execute();
		Map<String, String> cookies = res.cookies();
		String getURL = "http://www.chahwa.com.tw/order.php?act=query&&drug="
				+ URLEncoder.encode("���J", "utf-8");

		URL getUrl = new URL(getURL);
		// �ھګ��ꪺURL�A���}�s���AURL.openConnection��Ʒ|�ھ�URL�������A
		// ��^���P��URLConnection�l������H�A�o��URL�O�@��http�A�]����ڪ�^���OHttpURLConnection
		HttpURLConnection connection = (HttpURLConnection) getUrl
				.openConnection();
		// �i��s���A���O��ڤWget request�n�b�U�@�y��connection.getInputStream()��Ƥ��~�|�u���o��
		// �A�Ⱦ�
		String cookiePara = null;
		for (String cookie : cookies.keySet()) {
			System.out.println(cookie.toString() + "=" + cookies.get(cookie));
			cookiePara = cookiePara + cookie.toString() + "="
					+ cookies.get(cookie) + ";";
		}
		System.out.println(cookiePara);
		connection.addRequestProperty("Cookie", cookiePara);
		connection.connect();
		// ���o��J�y�A�èϥ�ReaderŪ��
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream(), "utf-8"));// �]�m�s�X,�_�h����ýX

		String lines;
		while ((lines = reader.readLine()) != null) {
			// lines = new String(lines.getBytes(), "utf-8");
			System.out.println(decode1(lines));
		}
		reader.close();
		// �_�}�s��
		connection.disconnect();

	}

	public static String decode1(String s) {
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
