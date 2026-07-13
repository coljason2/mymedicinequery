# 專案開發規範與開發脈絡 (context.md)

本文件定義此專案的架構特徵、開發規範與最佳實作方式，供開發人員與自動化工具遵循。

## 專案背景與佈署環境
* 專案名稱：福誠查藥系統 (mymedicinequery)
* 佈署平台：Render (雲端代管平台)
* 架構限制：Render 等雲端服務的 IP 容易被目標網站 (https://www.chahwa.com.tw) 阻擋，因此專案採用本地反向代理 (Reverse Proxy, Caddy + Ngrok) 架構進行請求轉發。

---

## 核心開發規範

### 1. 嚴禁魔術字串與寫死數值
* 所有外部連接的 URL、API 端點、逾時時間與設定值，嚴禁直接寫死在方法中。
* 必須抽取至類別頂部定義為常數 (private static final)。
  * 範例：目標登入路徑抽取為 `TARGET_LOGIN_URL`，逾時設定為 `TIMEOUT_MS`。

### 2. 網路逾時設定與連線控制
* 凡是使用 HttpURLConnection、Jsoup 或 OkHttp3 發送網路請求，必須同時設定 Connect Timeout 與 Read Timeout。
* 由於本專案會透過外部代理隧道，網路延遲較大，超時設定至少需為 60 秒 (TIMEOUT_MS = 60000)，嚴禁使用過短的逾時限制。

### 3. 反向代理 (Reverse Proxy) 與 SSL 繞過機制
* **代理開關**：凡涉及向目標網站發送 HTTP 請求時，皆須判斷環境變數 `REVERSE_PROXY_URL`。有設定時走代理並帶上認證，未設定時則採本地直連。
* **Ngrok 警告繞過與授權**：走反向代理時，必須帶上 `ngrok-skip-browser-warning` Header 以繞過 Ngrok 免費版的安全警告頁面，並攜帶基於 `PROXY_USER` 與 `PROXY_PASS` 產生的 Basic Auth `Authorization` Header。
* **SSL 交握失敗防護與 SNI 保留**：由於雲端環境 (如 Render) 的 JDK 可能因憑證驗證或 TLS 演算法不相容而拋出 `SSLHandshakeException`，發送請求 (Jsoup / HttpURLConnection) 時必須強制注入自訂的 `SSLSocketFactory`，以無條件信任所有憑證 (TrustAll) 並強制使用 `TLSv1.2`。**注意：自訂 `SSLSocketFactory` 時必須手動為 SSL 接口配置 SNI (Server Name Indication)，否則 Ngrok 閘道會因遺失 SNI 標頭而主動斷開連線。**

### 4. 檔案編碼標準
* 新增或修改任何專案檔案（包含 Java 原始碼、Thymeleaf 模板、設定檔等）時，必須確保編碼為標準 UTF-8（無 BOM）。
* 嚴禁在檔案開頭寫入 `\ufeff` (UTF-8 BOM) 字元，避免編譯器報錯 `illegal character: '\ufeff'`。

---

## Spring Boot 全端開發規範

### 1. 依賴注入方式
* 雖然舊代碼採用 `@Autowired` 欄位注入，但新撰寫的元件建議採用建構子注入 (Constructor Injection)。這能確保依賴的不可變性，並方便進行單元測試。
* 可以利用 Lombok 的 `@RequiredArgsConstructor` 簡化建構子注入的樣板程式碼。

### 2. Controller 設計與 AJAX 局部刷新
* 本專案的前端互動大量採用 Thymeleaf Fragment 局部刷新（例如 `result :: resultFragment`）。
* 在 Controller 中處理此類請求時，應明確返回對應的 Fragment 名稱，而非返回整個頁面，以維持流暢的 SPA 互動體驗。
* 涉及純資料交互的 API 端點，應使用 `@RestController` 或在方法上標註 `@ResponseBody`，並返回 JSON 格式。

### 3. 全域異常處理與日誌紀錄
* 系統中應避免在 Controller 層或 Service 層中濫用 try-catch 後只打印堆疊資訊。
* 應利用 Spring Boot 的 `@ControllerAdvice` 或 `@RestControllerAdvice` 進行全域異常攔截與格式化處理。
* 統一使用 Lombok 的 `@Slf4j` 進行日誌紀錄，嚴禁使用 `System.out.println` 輸出日誌。敏感資訊（如明文密碼、個人隱私資料）在紀錄日誌時必須進行脫敏處理。

---

## 核心邏輯參考
* 爬蟲核心與連線邏輯集中在 `com.medicine.query.service.MedicineGrabberCallable`。在開發或修改網路請求邏輯時，請以此類別的最新實作作為主要參考。
