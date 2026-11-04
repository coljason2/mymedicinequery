# 福誠查藥系統 (Medicine Query System)

本系統為一款基於 Spring Boot 開發的醫療藥品資訊爬蟲與查詢平台。主要功能為從大盤商藥品網站自動抓取藥品資訊，並提供使用者無縫的 AJAX 查詢體驗，同時整合了健保署 API、7-11 IBON 雲端列印服務以及 Android App 版本發布管理。

## 核心特色與功能
* **非同步無縫查詢 (AJAX/SPA)**：前端採用局部更新技術，查詢過程中畫面不閃爍，提供流暢的現代化操作體驗。
* **高效快取與資源優化**：整合 Caffeine 本地快取（查詢結果保留 45 分鐘），並實作 3 小時大盤商 Session Cookie 快取機制與自動失效重試，大幅降低對目標網站的請求頻率與伺服器負載。
* **智慧健保碼串接**：自動判斷並解析 10 碼健保代碼，精準介接台灣衛生福利部健保署 API，獲取最新的官方藥品資訊。
* **多元匯出與雲端列印**：支援將查詢結果匯出為 Excel、CSV、PDF 格式，並實作直接上傳至 7-11 IBON 雲端列印系統，產生專屬 QR Code 與取件碼。
* **App 版本發布與下載專區**：提供行動端版本檢查 API (`/version.json`、`/api/app/config`)、APK 下載頁面 (`/download`)，支援 QR Code 掃描安裝與管理員後台版本維護。
* **高質感響應式 UI**：採用 Bootstrap 框架配合客製化 CSS，提供動態 Loading 遮罩、現代化表格控制列 (DataTables)，完美支援手機與電腦端瀏覽。
* **多執行緒高效爬蟲**：善用並行運算與執行緒池 (ExecutorService)，提升向外部網站爬取資料的吞吐量。

## 技術架構
* **核心框架**: Spring Boot, Spring Security, Spring MVC
* **Java 版本**: Java 11 (LTS)
* **快取機制**: Caffeine Cache (本地記憶體快取)
* **資料序列化**: Jackson (取代舊版 Fastjson)
* **爬蟲與連線**: Jsoup, OkHttp3 (具備連線逾時保護與 SNI 保留)
* **前端技術**: Thymeleaf (啟用範本快取), jQuery, DataTables, Bootstrap
* **容器與佈署**: Docker (Multi-stage build, Eclipse Temurin 11 JRE Alpine, Render 記憶體與 SerialGC 最佳化)

## 專案目錄結構
```text
mymedicinequery
├── src
│   ├── main
│   │   ├── java/com/medicine/query
│   │   │   ├── common       # 共用常數 (Constant)
│   │   │   ├── config       # 安全配置 (SecurityConfig)、執行緒池、CORS 等設定檔
│   │   │   ├── controller   # MVC 控制器 (Web 頁面、REST API、版本管理)
│   │   │   ├── exception    # 系統自訂例外與全域錯誤處理
│   │   │   ├── model        # 資料物件模型 (DTO, Entity)
│   │   │   └── service      # 核心業務邏輯、爬蟲任務、Session 快取、版本管理
│   │   └── resources
│   │       ├── static       # 靜態資源 (CSS, JS, 圖片)
│   │       ├── templates    # Thymeleaf 頁面 (home, result, download, login 等)
│   │       └── application.yml # 應用程式組態 (快取、連線池、日誌上限)
│   └── test                 # 單元測試目錄
├── Dockerfile               # 多階段 Alpine JRE 輕量化容器建置檔
├── pom.xml                  # Maven 專案依賴與建置設定檔 (Java 11)
├── context.md               # 專案架構脈絡與開發規範
└── README.md                # 專案說明文件
```

## 反向代理架構 (Reverse Proxy)
為繞過目標網站之 WAF 及連線限制，可於具備家用 IP 之伺服器部署反向代理，讓雲端應用程式透過該代理進行請求。

### 1. 部署跳板服務 (Docker Compose)
建立 `docker-compose.yml` 並執行 `docker-compose up -d`：

```yaml
version: '3.8'
services:
  caddy:
    image: caddy:alpine
    container_name: local-caddy
    command:
      - sh
      - -c
      - |
        printf '%s' ':8080 {
          reverse_proxy https://www.XXXXX.com.tw {
            header_up Host {http.reverse_proxy.upstream.host}
            header_up -X-Forwarded-For
            header_up -X-Forwarded-Host
            header_up -X-Forwarded-Proto
            header_up -Authorization
            header_up -Ngrok-Skip-Browser-Warning
          }
        }' > /etc/caddy/Caddyfile
        caddy run --config /etc/caddy/Caddyfile --adapter caddyfile
    restart: always
  ngrok:
    image: ngrok/ngrok:latest
    container_name: local-ngrok
    command: http caddy:8080 --basic-auth="YOUR_USER:YOUR_PASSWORD"
    environment:
      - NGROK_AUTHTOKEN=YOUR_NGROK_AUTHTOKEN
    depends_on:
      - caddy
    restart: always
```

### 2. 環境變數設定
於應用程式運行環境配置以下變數以啟用代理：

* `REVERSE_PROXY_URL`: Ngrok HTTPS 網址
* `PROXY_USER`: 代理伺服器帳號
* `PROXY_PASS`: 代理伺服器密碼

未設定 `REVERSE_PROXY_URL` 時，系統預設為本地直連模式。

### 3. 技術說明
本專案已在連線層（Jsoup / HttpURLConnection）原生實作單例 `SSLSocketFactory` 與 SNI (Server Name Indication) 保留，並強制採用 TLSv1.2 協定，因此在 Render 等雲端容器環境下部署時，無需額外配置 JVM 憑證即可安全穿透代理。

## 執行與測試
* **環境要求**: JDK 11+ / Maven 3.6+
* **啟動方式**: 在 IDE 中執行 `MedQueryApplication.java` 或使用指令 `mvn spring-boot:run`。
* **權限劃分與登入**:
  * **公開頁面/端點**: 藥品查詢 (`/`、`/home`、`/query`)、APP 下載專區 (`/download`)、行動端版本檢查 (`/version.json`、`/api/app/**`)、行動端查詢 API (`/get/{med}`)。
  * **版本管理後台 (`/admin/**`)**: 需要 `ADMIN` 角色權限，預設管理者帳號 `admin` / 密碼 `admin123456`。
  * **測試帳號**: 具備一般使用者權限之帳號 `root` / 密碼 `root`。
