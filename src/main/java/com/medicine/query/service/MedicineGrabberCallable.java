package com.medicine.query.service;

import com.medicine.query.common.Constant;
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
import java.util.*;
import javax.net.ssl.*;
import java.nio.charset.StandardCharsets;
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
    private final boolean isCompanyQuery;

    public static final List<String> KNOWN_COMPANIES;
    static {
        Set<String> set = new LinkedHashSet<>();
        // 台灣主要藥廠與知名品牌清單
        set.addAll(Arrays.asList(
                "健喬信元", "百時美施貴寶", "百靈佳殷格翰", "台灣塩野義", "台灣第一三共", "台灣東洋",
                "曼秀雷敦", "美納里尼", "諾和諾德", "田邊三菱", "安斯泰來", "中化裕民", "合春製藥",
                "山德士", "生達", "晟德", "永信", "南光", "衛達", "友華", "福元", "意欣", "一成", "陽生",
                "皇佳", "黃氏", "國嘉", "新瑞", "泰宗", "鼎泰", "井田", "天義", "衛肯", "順華", "鴻文",
                "星寶", "曼哈頓", "永勝", "韋淳", "拜耳", "海喬", "益普生", "幸生", "永茂", "西海",
                "柏理", "榮民", "羅得", "應元", "元宙", "五洲", "華興", "中化", "信東", "瑪科隆",
                "政德", "武田", "盈盈", "台裕", "派頓", "明大", "杏林", "聯亞", "健康", "美強",
                "十全", "瑞士", "保瑞", "明德", "大豐", "回春堂", "壽元", "信隆", "汎生", "杏輝",
                "諾華", "輝瑞", "賽諾菲", "葛蘭素", "GSK", "MSD", "默沙東", "亞培", "健喬", "健亞",
                "友霖", "培力", "東洋", "瑩碩", "順天", "科達", "莊松榮", "港香蘭", "順天堂", "萬應",
                "白花油", "大塚", "衛采", "第一三共", "嬌生", "愛爾康", "博士倫", "參天", "三菱", "禮來",
                "艾伯維", "濟生", "百靈佳", "大正", "鹽野義", "小林", "裕利", "久裕", "美時"
        ));
        set.addAll(Constant.medCompanys);
        // 依字串長度由長到短排序，優先匹配較長名稱（例如「健喬信元」優於「健喬」）
        List<String> list = new ArrayList<>(set);
        list.sort((a, b) -> Integer.compare(b.length(), a.length()));
        KNOWN_COMPANIES = Collections.unmodifiableList(list);
    }

    static String getBaseUrl() {
        String reverseProxyUrl = System.getenv("REVERSE_PROXY_URL");
        return (reverseProxyUrl != null && !reverseProxyUrl.trim().isEmpty()) ? reverseProxyUrl : "https://www.chahwa.com.tw";
    }

    static String getAuthHeader() {
        String user = System.getenv("PROXY_USER");
        String pass = System.getenv("PROXY_PASS");
        if (user != null && pass != null) {
            String auth = user + ":" + pass;
            return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
        }
        return null;
    }

    private static final SSLSocketFactory TRUST_ALL_SSL_SOCKET_FACTORY = createTrustAllSSLSocketFactory();

    static SSLSocketFactory getTrustAllSSLSocketFactory() {
        return TRUST_ALL_SSL_SOCKET_FACTORY;
    }

    private static SSLSocketFactory createTrustAllSSLSocketFactory() {
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

    private String cleanHtml(String raw) {
        if (raw == null) {
            return "";
        }
        return decode(raw.replace("\\//", ""))
                .replace("\\r", "").replace("\\n", "").replace("\r", "").replace("\n", "")
                .replace("\\", "").replace("}", "").replace("{", "");
    }

    public static String normalizeQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.replace('\u3000', ' ')
                .replaceAll("[【】\\[\\]\\(\\)（）「」『』\"'<>《》]", "")
                .trim();
    }

    public static boolean isHealthCode(String s) {
        if (s == null) {
            return false;
        }
        String trimmed = s.trim();
        return trimmed.length() == 10 && trimmed.matches("^[a-zA-Z]{1,2}[0-9a-zA-Z]{8,9}$");
    }

    public MedicineGrabberCallable(String queryName) {
        this(queryName, false);
    }

    public MedicineGrabberCallable(String queryName, boolean isCompanyQuery) {
        this.queryName = normalizeQuery(queryName);
        this.isCompanyQuery = isCompanyQuery;
    }

    @Override
    public List<MedEntity> call() throws Exception {
        return getMedicine(queryName);
    }

    public List<MedEntity> getMedicine(String queryName) {

        String cookie = getCookies();
        List<MedEntity> meds = new ArrayList<>();
        // clean code
        String parseString = cleanHtml(getContext(queryName, cookie));

        // 若大盤商 Session 在伺服端提早失效，自動清除快取並重試登入
        if (parseString.contains("act_login") || parseString.contains("user.php?act=login") || parseString.contains("請登入")) {
            log.warn("大盤商 Session 已在伺服端失效，重新執行登入刷新 Cookie...");
            CrawlerSessionManager.invalidateCookie();
            cookie = getCookies();
            parseString = cleanHtml(getContext(queryName, cookie));
        }
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
        this.setResponseEntities(meds, resault, queryName);
        if (total_page > 1) {
            for (int i = 2; i <= total_page; i++) {
                parseString = cleanHtml(getContextPage(queryName, cookie, i));
                resault = Jsoup.parse(parseString);
                this.setResponseEntities(meds, resault, queryName);
            }
        }
        return meds;
    }

    public String getContext(String name, String cookiePara) {
        try {
            String param = isHealthCode(name) ? "hid" : "drug";
            String targetUrl = getBaseUrl() + "/order.php?act=query&" + param + "=" + URLEncoder.encode(name, "utf-8");
            return executeGetRequest(targetUrl, cookiePara);
        } catch (Exception e) {
            log.error("Getcontext Error ", e);
            throw new MedException(e);
        }
    }

    public String getContextPage(String name, String cookiePara, int Now_page) {
        try {
            String param = isHealthCode(name) ? "hid" : "drug";
            String targetUrl = getBaseUrl() + "/order.php?act=query&" + param + "=" + URLEncoder.encode(name, "utf-8") + "&page=" + Now_page;
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
            HttpsURLConnection httpsConn = (HttpsURLConnection) connection;
            httpsConn.setSSLSocketFactory(getTrustAllSSLSocketFactory());
            httpsConn.setHostnameVerifier((hostname, session) -> true);
        }
        
        connection.setRequestMethod("GET");
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
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
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
        return CrawlerSessionManager.getValidCookie();
    }


    private static final Pattern PARENTHESES_PATTERN = Pattern.compile("[\\(（]([^\\(\\)（）]+)[\\)）]");
    private static final Pattern NON_COMPANY_WORDS = Pattern.compile(".*(瓶|盒|支|袋|組|條|包|罐|粒|顆|錠|劑|管|桶|箱|卷|片|排|打|健保|自費|口服|外用|停產|缺藥|斷貨|售完|贈品|另贈|買|送|特大|大|小|綠|紫|白|紅|黃|藍|黑|薄荷|酷涼|莓果|香橙|山茶花|口溶|膜衣|緩釋|注射|懸浮|糖漿|軟膏|乳膏|點眼|滴劑|膠囊|顆粒|粉劑|栓劑|貼布|噴劑|含稅|未稅|促銷|特價).*");

    public static String extractCompany(String rawName, String fallbackCompany) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return fallbackCompany;
        }

        // 1. 優先從括號 (...) 或 （...） 解析藥廠名稱（如「(山德士)」、「(永信)」）
        Matcher matcher = PARENTHESES_PATTERN.matcher(rawName);
        String lastCandidate = null;
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (isValidCompanyCandidate(candidate)) {
                lastCandidate = cleanCompanyName(candidate);
            }
        }
        if (lastCandidate != null) {
            return lastCandidate;
        }

        // 2. 從常見知名藥廠清單比對藥品名稱（如「曼秀雷敦...」、「萬應...」）
        for (String c : KNOWN_COMPANIES) {
            if (rawName.contains(c)) {
                return c;
            }
        }

        // 3. 若為公司批次查詢，且未解析出其他藥廠，才使用查詢的公司名稱作為 fallback
        return fallbackCompany;
    }

    private static boolean isValidCompanyCandidate(String text) {
        if (text == null) return false;
        String s = text.trim();
        // 長度需在 2 到 10 字元之間
        if (s.length() < 2 || s.length() > 10) return false;
        // 不能包含任何數字 (排除 100T, 60ML, 10支, 14T, 12.5G 等劑量規格)
        if (s.matches(".*\\d.*")) return false;
        // 必須包含中文字元
        if (!s.matches(".*[\\u4e00-\\u9fa5].*")) return false;
        // 排除單位、狀態、促銷字眼
        if (NON_COMPANY_WORDS.matcher(s).matches()) return false;
        return true;
    }

    private static String cleanCompanyName(String s) {
        return s.replaceAll("(藥廠|製藥|生技|化學|藥品)$", "").trim();
    }

    private void setResponseEntities(List<MedEntity> meds, Document resault, String company) {
        for (org.jsoup.nodes.Element n : resault.getElementsByClass("item_text")) {
            MedEntity m = new MedEntity();
            m.setOid(n.getElementsByClass("code").text());
            m.setOidPrice(n.getElementsByClass("price").text());
            m.setIsEnough(n.getElementsByClass("sell_price").text());
            String rawName = n.getElementsByClass("name").text();
            m.setName(rawName);

            // 若非公司批次篩選查詢，fallbackCompany 設為 null，避免將搜尋關鍵字（例如「諾快」、「白花油」）誤植為藥廠
            String fallback = this.isCompanyQuery ? company : null;
            m.setCompany(extractCompany(rawName, fallback));

            meds.add(m);
        }
    }
}
