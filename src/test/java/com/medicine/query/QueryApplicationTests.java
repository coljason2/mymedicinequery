package com.medicine.query;

import com.medicine.query.controller.ApiController;
import com.medicine.query.controller.AppController;
import com.medicine.query.model.MedEntity;
import com.medicine.query.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class QueryApplicationTests {

    @Autowired
    MedicineService medicineService;

    @Autowired
    AppController appController;

    @Autowired
    ApiController apiController;

    @Autowired
    org.springframework.web.context.WebApplicationContext webApplicationContext;

    @Test
    public void testBracketAndHealthCodeQueries() throws Exception {
        // 1. 測試帶括號的搜尋：【安咳】
        List<MedEntity> resBracket = medicineService.getMedicine("【安咳】");
        log.info("【安咳】（帶括號）查詢筆數: {}", resBracket.size());
        assertNotNull(resBracket);
        assertFalse(resBracket.isEmpty(), "【安咳】查詢應成功獲取藥品資料！");
        for (MedEntity m : resBracket) {
            log.info("【安咳】結果: 藥名={}, 健保碼={}, 庫存/價格={}", m.getName(), m.getOid(), m.getIsEnough());
        }

        // 2. 測試純藥名搜尋：安咳
        List<MedEntity> resPlain = medicineService.getMedicine("安咳");
        log.info("安咳（純字串）查詢筆數: {}", resPlain.size());
        assertNotNull(resPlain);
        assertFalse(resPlain.isEmpty(), "安咳查詢應成功獲取藥品資料！");

        // 3. 測試 10 碼健保碼搜尋：AC33052100 (安咳錠)
        List<MedEntity> resHid1 = medicineService.getMedicine("AC33052100");
        log.info("AC33052100 (健保碼) 查詢筆數: {}", resHid1.size());
        assertNotNull(resHid1);
        assertFalse(resHid1.isEmpty(), "AC33052100 健保碼查詢應成功獲取藥品資料！");

        // 4. 測試 10 碼健保碼搜尋：A029634151 (安咳糖漿)
        List<MedEntity> resHid2 = medicineService.getMedicine("A029634151");
        log.info("A029634151 (健保碼) 查詢筆數: {}", resHid2.size());
        assertNotNull(resHid2);
        assertFalse(resHid2.isEmpty(), "A029634151 健保碼查詢應成功獲取藥品資料！");

        // 5. 測試 諾快（藥廠應為山德士，而非關鍵字「諾快」）
        List<MedEntity> resNuokuai = medicineService.getMedicine("諾快");
        assertNotNull(resNuokuai);
        assertFalse(resNuokuai.isEmpty(), "查詢【諾快】應有結果！");
        for (MedEntity m : resNuokuai) {
            log.info("【諾快】name=[{}], company=[{}]", m.getName(), m.getCompany());
            assertFalse("諾快".equals(m.getCompany()), "藥廠不可直接誤植為搜尋關鍵字【諾快】！");
            if (m.getName().contains("山德士")) {
                assertTrue("山德士".equals(m.getCompany()), "帶有(山德士)之藥品，解析出的藥廠應為山德士！");
            }
        }

        // 6. 測試 白花油（PASTEL 舒緩棒等不應誤植為白花油）
        List<MedEntity> resBaihua = medicineService.getMedicine("白花油");
        assertNotNull(resBaihua);
        assertFalse(resBaihua.isEmpty(), "查詢【白花油】應有結果！");
        for (MedEntity m : resBaihua) {
            log.info("【白花油】name=[{}], company=[{}]", m.getName(), m.getCompany());
            if (m.getName().contains("PASTEL")) {
                assertFalse("白花油".equals(m.getCompany()), "PASTEL 產品之藥廠不可誤植為【白花油】！");
            }
            if (m.getName().contains("曼秀雷敦")) {
                assertTrue("曼秀雷敦".equals(m.getCompany()), "曼秀雷敦產品之藥廠應正確識別為【曼秀雷敦】！");
            }
        }

        // 7. 測試 Web Controller POSTQuery 【安咳】
        Model model = new ConcurrentModel();
        String view = appController.POSTQuery("【安咳】", model);
        @SuppressWarnings("unchecked")
        List<MedEntity> modelMeds = (List<MedEntity>) model.getAttribute("meds");
        log.info("Web Controller POSTQuery 【安咳】 view: {}, meds: {}", view, modelMeds != null ? modelMeds.size() : 0);
        assertNotNull(modelMeds);
        assertFalse(modelMeds.isEmpty(), "Controller 查詢【安咳】應有結果！");

        // 6. 測試 ApiController 【安咳】
        List<MedEntity> apiMeds = apiController.getMedicine("【安咳】");
        log.info("ApiController getMedicine 【安咳】 筆數: {}", apiMeds.size());
        assertNotNull(apiMeds);
        assertFalse(apiMeds.isEmpty(), "API 查詢【安咳】應有結果！");

        // 7. 驗證 Web MVC 與 Thymeleaf 模板完整渲染無誤
        org.springframework.test.web.servlet.MockMvc mockMvc =
                org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String homeHtml = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/home"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(homeHtml.contains("search-panel") && homeHtml.contains("toggleTheme"), "home.html 應正常渲染整合搜尋面板與主題切換");

        String resultHtml = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/query")
                        .param("querystring", "安咳"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(resultHtml.contains("exportdata") && resultHtml.contains("安咳"), "result.html 應正常渲染結果表格");

        String loginHtml = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/login"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(loginHtml.contains("login-card"), "login.html 應正常渲染");
    }
}
