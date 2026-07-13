package com.medicine.query.service;

import com.medicine.query.exception.MedException;
import com.medicine.query.model.LoginFormData;
import com.medicine.query.model.MedEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import javax.net.ssl.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MedicineGrabberCallable implements Callable<List<MedEntity>> {

    private static final int TIMEOUT_MS = 60000;
    private static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");
    private static final LoginFormData form = new LoginFormData();
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";

    private String queryName;

    private static String getBaseUrl() {
        String reverseProxyUrl = System.getenv("REVERSE_PROXY_URL");
        return (reverseProxyUrl != null && !reverseProxyUrl.trim().isEmpty()) ? reverseProxyUrl : "https://www.chahwa.com.tw";
    }

    private static String getAuthHeader() {
        String user = System.getenv("PROXY_USER");
        String pass = System.getenv("PROXY_PASS");
        if (user != null && pass != null) {
            String auth = user + ":" + pass;
            return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
        }
        return null;
    }

    private static SSLSocketFactory getTrustAllSSLSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory factory = sc.getSocketFactory();
            
            return new SSLSocketFactory() {
                @Override
                public String[] getDefaultCipherSuites() { return factory.getDefaultCipherSuites(); }
                @Override
                public String[] getSupportedCipherSuites() { return factory.getSupportedCipherSuites(); }
                
                private Socket enableSNI(Socket socket, String host) {
                    if (socket instanceof SSLSocket) {
                        SSLSocket sslSocket = (SSLSocket) socket;
                        SSLParameters params = sslSocket.getSSLParameters();
                        params.setServerNames(Collections.singletonList(new SNIHostName(host)));
                        sslSocket.setSSLParameters(params);
                    }
                    return socket;
                }

                @Override
                public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
                    return enableSNI(factory.createSocket(s, host, port, autoClose), host);
                }
                @Override
                public Socket createSocket(String host, int port) throws IOException {
                    return enableSNI(factory.createSocket(host, port), host);
                }
                @Override
                public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
                    return enableSNI(factory.createSocket(host, port, localHost, localPort), host);
                }
                @Override
                public Socket createSocket(InetAddress host, int port) throws IOException {
                    return factory.createSocket(host, port);
                }
                @Override
                public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
                    return factory.createSocket(address, port, localAddress, localPort);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLSocketFactory", e);
        }
    }

    public MedicineGrabberCallable(String queryName) {
        this.queryName = queryName;
    }

    @Override
    public List<MedEntity> call() throws Exception {
        return getMedicine(queryName);
    }

    public List<MedEntity> getMedicine(String queryName) {

        String cookie = getCookies();
        List<MedEntity> meds = new CopyOnWriteArrayList<>();
        // clean code
        String parseString = decode(getContext(queryName, cookie).replace("\\//", "")).replace("\\", "").replace("}", "")
                .replace("{", "").replace("rn", "");
        Document resault = Jsoup.parse(parseString);
        int total_page = 1;
        org.jsoup.select.Elements pagenavi = resault.getElementsByClass("pagenavi");
        if (pagenavi != null && !pagenavi.isEmpty()) {
            org.jsoup.select.Elements spanBs = pagenavi.select("span").select("b");
            if (spanBs.size() > 1) {
                try {
                    total_page = Integer.parseInt(spanBs.get(1).text());
                } catch (NumberFormatException e) {
                    log.error("Parse total page error", e);
                }
            }
        } else {
            log.warn("Cannot find pagenavi, parseString content: {}", parseString);
        }
        log.info("total_page = {}", total_page);
        if (total_page > 1) {
            for (int i = 1; i <= total_page; i++) {
                parseString = decode(getContextPage(queryName, cookie, i).replace("\\//", "")).replace("\\", "")
                        .replace("}", "").replace("{", "").replace("rn", "");
                resault = Jsoup.parse(parseString);
                this.setResponseEntities(meds, resault, queryName);
            }
        } else {
            this.setResponseEntities(meds, resault, queryName);
        }
        return meds;
    }

    public String getContext(String name, String cookiePara) {
        try {
            String targetUrl = getBaseUrl() + "/order.php?act=query&&drug=" + URLEncoder.encode(name, "utf-8");
            return executeGetRequest(targetUrl, cookiePara);
        } catch (Exception e) {
            log.error("Getcontext Error ", e);
            throw new MedException(e);
        }
    }

    public String getContextPage(String name, String cookiePara, int Now_page) {
        try {
            String targetUrl = getBaseUrl() + "/order.php?act=query&&drug=" + URLEncoder.encode(name, "utf-8") + "&page=" + Now_page;
            return executeGetRequest(targetUrl, cookiePara);
        } catch (Exception e) {
            log.error("Getcontext Error ", e);
            throw new MedException(e);
        }
    }

    private String executeGetRequest(String targetUrl, String cookiePara) throws Exception {
        StringBuilder context = new StringBuilder();
        URL getUrl = new URL(targetUrl);
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(getTrustAllSSLSocketFactory());
        }
        
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
        connection.addRequestProperty("Cookie", cookiePara);
        
        if (System.getenv("REVERSE_PROXY_URL") != null) {
            connection.setRequestProperty("ngrok-skip-browser-warning", "69420");
            String authHeader = getAuthHeader();
            if (authHeader != null) {
                connection.setRequestProperty("Authorization", authHeader);
            }
        }
        
        connection.connect();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String lines;
            while ((lines = reader.readLine()) != null) {
                context.append(decode(lines));
            }
        } finally {
            connection.disconnect();
        }
        return context.toString();
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
            if (System.getenv("REVERSE_PROXY_URL") != null) {
                log.info("Using Reverse Proxy for getCookies: {}", getBaseUrl());
            } else {
                log.info("Not using Reverse Proxy for getCookies");
            }

            // 套用自訂 SSL 設定與強制 TLSv1.2
            Connection con = Jsoup.connect(getBaseUrl() + "/user.php")
                    .sslSocketFactory(getTrustAllSSLSocketFactory())
                    .data("username", new String(Base64.getDecoder().decode(form.getUsername())), 
                          "password", new String(Base64.getDecoder().decode((form.getPassword()))), 
                          "wsrc", form.getWsrc(), "act", form.getAct(), "back_act", form.getBack_act())
                    .method(Connection.Method.POST)
                    .timeout(TIMEOUT_MS)
                    .userAgent(DEFAULT_USER_AGENT)
                    .followRedirects(false);
            
            if (System.getenv("REVERSE_PROXY_URL") != null) {
                con.header("ngrok-skip-browser-warning", "69420");
                String authHeader = getAuthHeader();
                if (authHeader != null) {
                    con.header("Authorization", authHeader);
                }
            }
            
            Connection.Response res = con.execute();
            cookies = res.cookies();

        } catch (Exception e) {
            log.error("getCookies Error ", e);
            throw new MedException(e);
        }
        for (String cookie : cookies.keySet()) {
            cookiePara = cookiePara + cookie.toString() + "=" + cookies.get(cookie) + ";";
        }
        // log.info(cookiePara.toString());
        return cookiePara;
    }

    private void setResponseEntities(List<MedEntity> meds, Document resault, String company) {
        for (org.jsoup.nodes.Element n : resault.getElementsByClass("item_text")) {
            MedEntity m = new MedEntity();
            m.setOid(n.getElementsByClass("code").text());
            m.setOidPrice(n.getElementsByClass("price").text());
            m.setIsEnough(n.getElementsByClass("sell_price").text());
            m.setName(n.getElementsByClass("name").text());
            m.setCompany(company);
            meds.add(m);
        }
    }
}
