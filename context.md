# 專案開發規範與開發脈絡 (context.md)

本文件定義此專案的架構特徵、開發規範與最佳實作方式，供開發人員與自動化工具遵循。

## 專案背景與佈署環境
* **專案名稱**：福誠查藥系統 (mymedicinequery)
* **執行環境**：Java 11 (LTS)
* **佈署平台**：Render (雲端容器化環境)
* **資源限制**：容器環境 CPU 與記憶體有限（約 0.1 vCPU / 512MB RAM），JVM 配置強制啟用輕量化參數（`-Xms128m -Xmx256m -XX:+UseSerialGC`）。
* **架構限制**：Render 等雲端服務的 IP 容易被目標網站阻擋，因此專案採用本地反向代理 (Reverse Proxy: Caddy + Ngrok) 架構進行請求轉發。

---

## 核心連線與爬蟲開發規範

### 1. 嚴禁魔術字串與寫死數值
* 所有外部連接的 URL、API 端點、逾時時間與設定值，嚴禁直接寫死在方法中。
* 必須抽取至類別頂部定義為常數 (`private static final`)。

### 2. 網路逾時設定與連線控制
* 凡是使用 `HttpURLConnection`、`Jsoup` 或 `OkHttp3` 發送網路請求，必須同時設定 Connect Timeout 與 Read Timeout。
* 由於本專案會透過外部代理隧道，超時設定至少需為 60 秒 (`TIMEOUT_MS = 60000`)，嚴禁使用過短的逾時限制。

### 3. 反向代理 (Reverse Proxy) 與 SSL 繞過機制
* **代理開關**：凡涉及向目標網站發送 HTTP 請求時，皆須判斷環境變數 `REVERSE_PROXY_URL`。有設定時走代理並帶上認證，未設定時則採本地直連。
* **Ngrok 警告繞過與授權**：走反向代理時，必須帶上 `ngrok-skip-browser-warning` Header 以繞過 Ngrok 免費版的安全警告頁面，並攜帶基於 `PROXY_USER` 與 `PROXY_PASS` 產生的 Basic Auth `Authorization` Header。
* **單例 SSLSocketFactory 與 SNI 保留**：
  * 必須透過靜態單例 `TRUST_ALL_SSL_SOCKET_FACTORY` 重複利用連線工廠，**嚴禁在每個 HTTP 請求中重新建立 `SSLContext` 與 `SecureRandom`**，以避免在低規格 CPU 容器下產生高頻 CPU 浪費。
  * 自訂 `SSLSocketFactory` 時必須手動為 SSL 接口配置 SNI (Server Name Indication)，否則 Ngrok 閘道會因遺失 SNI 標頭而主動斷開連線。

### 4. Session 與爬蟲登入管理
* 大盤商 Session Cookie 必須透過 `CrawlerSessionManager.getValidCookie()` 統一取得。
* 具備 3 小時本地快取與 Double-Checked Locking 機制。
* 爬蟲流程若偵測到伺服器回傳登入頁特徵（例如 `act_login`、`請登入`），應主動調用 `CrawlerSessionManager.invalidateCookie()` 使快取失效，並自動重新執行登入刷新。

### 5. 字串清理與正規化防護
* **嚴禁使用 `.replace("rn", "")`**：舊程式碼曾將 `\r\n` 替換後遺留的字元以 `"rn"` 刪除，導致英文藥品名稱與成分（如 Morning、Modern、Burn 等）被截斷。
* 字串清理一律使用專屬方法處理換行符號（針對 `\r`、`\n`、`\\r`、`\\n` 進行精準替換）。

---

## 快取與效能架構規範

### 1. Caffeine 本地快取
* 藥品查詢實作於 `MedicineService`，配置 `@Cacheable(value = "medicineQuery", key = "#p0")`。
* 快取存活時間（TTL）設定為 45 分鐘，最大容量 500 筆，避免重複爬取相同關鍵字。
* 多廠商查詢應採用標準化排序後的查詢鍵，避免因參數順序差異造成快取穿透。

### 2. 靜態與範本快取
* 正式環境必須啟用 Thymeleaf 範本快取 (`spring.thymeleaf.cache: true`)。
* 避免重複解析 HTML 模板消耗記憶體。

---

## API 與行動端 (Android App) 相容規範

### 1. 行動端 REST API 例外隔離
* 提供行動端呼叫之端點（如 `/get/{med}`）：
  * 嚴禁讓未處理之例外向上拋出導致 Spring Boot 導向 HTML 錯誤頁（例如 `error.html`）。
  * 發生異常時應記錄日誌並回傳空的 JSON 陣列 (`Collections.emptyList()`)，防止 Android App 之 JSON 解析器崩潰。

### 2. App 版本管理與發布
* 版本設定檔（`version.json`）於更新儲存時必須採用原子寫入機制（`StandardCopyOption.ATOMIC_MOVE`），避免寫入中途遭讀取導致 JSON 格式損毀。
* 下載與版本資訊介面需公開存取，供無憑證之行動端直接檢查更新。

---

## 安全防護與權限控制規範

### 1. 權限邊界
* **公開端點**：藥品查詢 (`/`、`/home`、`/query`)、APP 下載專區 (`/download`)、行動端 API (`/version.json`、`/api/app/**`、`/get/{med}`)。
* **管理後台**：`/admin/**` 強制限制為 `ROLE_ADMIN` 角色存取。

### 2. CORS 與跨域防護
* 嚴禁對所有路徑啟用萬用字元跨域（`/**`）。
* CORS 映射僅嚴格限制於 `/api/**`、`/get/**`、`/version.json` 與 `/download`。

### 3. XSS 防護
* Thymeleaf 模板應統一使用標準 URL 表達式 `@{}...` 與 `th:text` 進行輸出跳脫，嚴禁在未脫敏的情況下直接輸出未過濾的請求參數。

---

## 程式碼修改與協作通則

### 1. 檔案編碼標準
* 所有專案檔案（Java 原始碼、Thymeleaf 模板、設定檔等）必須保持標準 UTF-8 without BOM。
* 嚴禁在檔案開頭寫入 `\ufeff`。

### 2. 版本庫與 Git 控制原則
* 本專案嚴格禁止自動化工具未經授權執行任何 `git` 指令（如 `git add`、`git reset`、`git commit` 等）。
* 所有檔案修改直接反映在工作區中，由專案負責人手動比對 Diff 並進行版本提交。
