package com.medicine.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicine.query.model.UpPdfRsp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class QueryApplicationTests {

    @Autowired
    ObjectMapper mapper;

    @Test
    public void JsonToBean() throws JsonProcessingException {
        String json = "{\"Hash\":\"39f4905e-48c4-4487-bd78-ed3c5a59795a\",\"FileName\":\"3e59be35-7615-40b9-a7eb-1ec05fbd80bameds.pdf\",\"Size\":0,\"ResultCode\":\"00\",\"Message\":\"成功\"}".toLowerCase();
        log.info("json = {} ", json);
        UpPdfRsp rsp = mapper.readValue(json, UpPdfRsp.class);
        log.info("rsp = {} ", rsp);
    }

    @Test
    void contextLoads() {
    }

}
