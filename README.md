# 福誠查藥系統 (Medicine Query System)

本系統為一款現代化、基於 Spring Boot 開發的醫療藥品資訊爬蟲與查詢平台。主要功能為從大盤商藥品網站自動抓取藥品資訊，並提供使用者無縫的 AJAX 查詢體驗，同時整合了健保署 API 與 7-11 IBON 雲端列印服務。

## 💡 核心特色與功能
* **非同步無縫查詢 (AJAX/SPA)**：前端採用局部更新技術，查詢過程中畫面不閃爍，提供流暢的現代化 App 操作體驗。
* **智慧健保碼串接**：自動判斷並解析 10 碼健保代碼，精準介接台灣衛生福利部健保署 API，獲取最新的官方藥品資訊（內建 SSL 憑證 TrustAll 繞過機制）。
* **多元匯出與雲端列印**：支援將查詢結果匯出為 Excel、CSV、PDF 格式，並實作直接上傳至 7-11 IBON 雲端列印系統，產生專屬 QR Code 與取件碼。
* **高質感響應式 UI**：採用 Bootstrap 框架配合客製化 CSS，提供動態 Loading 遮罩、現代化表格控制列 (DataTables)，完美支援手機與電腦端瀏覽。
* **多執行緒高效爬蟲**：善用並行運算與執行緒池 (ExecutorService)，提升向外部網站爬取資料的吞吐量。

## 🛠️ 技術架構
* **後端框架**: Spring Boot, Spring Security, Spring MVC
* **爬蟲與連線**: Jsoup, OkHttp3
* **前端視圖**: Thymeleaf, jQuery, DataTables, Bootstrap
* **佈署支援**: GCP, Render, Cloudflare 等雲端代管平台。

## 📂 專案目錄結構
```text
mymedicinequery
├── src
│   ├── main
│   │   ├── java/com/medicine/query
│   │   │   ├── common       # 共用常數與工具類別
│   │   │   ├── config       # Spring Security、ThreadPool 等設定檔
│   │   │   ├── controller   # MVC 控制器 (處理 Web 與 AJAX 請求)
│   │   │   ├── exception    # 系統自訂例外與全域錯誤處理
│   │   │   ├── model        # 資料物件模型 (DTO, Entity)
│   │   │   └── service      # 核心業務邏輯、爬蟲任務、API 串接
│   │   └── resources
│   │       ├── static       # 靜態資源 (CSS 等)
│   │       └── templates    # Thymeleaf 頁面與共用元件片段 (支援區域刷新)
│   └── test                 # 單元測試目錄
├── pom.xml                  # Maven 專案依賴與建置設定檔
└── README.md                # 專案說明文件
```

## 🚀 執行與測試
* **環境要求**: JDK 8+ / Maven 3.6+
* **啟動方式**: 在 IDE 中執行 `MedQueryApplication.java` 或使用指令 `mvn spring-boot:run`。
* **系統登入**: 系統已開啟安全防護，本地端測試請使用預設帳號密碼 `root` / `root` 進行登入。
