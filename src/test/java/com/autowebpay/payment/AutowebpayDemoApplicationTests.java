package com.autowebpay.payment;

import com.autowebpay.payment.demos.dto.PayOrderNotifyDTO;
import com.autowebpay.payment.demos.web.payController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@SpringBootTest
class AutowebpayDemoApplicationTests {
    private static final Logger log = LogManager.getLogger(AutowebpayDemoApplicationTests.class);
    @Test
    void orderNotifyTest() {
        PayOrderNotifyDTO payOrderNotifyDTO = new PayOrderNotifyDTO();
        payOrderNotifyDTO.setOrderNo("4959659568A2024032617");
        payOrderNotifyDTO.setMerchantOrderNo("2024032617495928890716");
        payOrderNotifyDTO.setPayAmount(1000L);
        payOrderNotifyDTO.setPayChannelType(1);
        payOrderNotifyDTO.setCurrencyType("USD");
        payOrderNotifyDTO.setExtendData("");
        payOrderNotifyDTO.setTranNo("4959659568A2024032617");
        payOrderNotifyDTO.setStatus(1);
        payOrderNotifyDTO.setCountryCode(null);
        payOrderNotifyDTO.setCardType("MC");
        payOrderNotifyDTO.setCardBank("MasterCard");
        payOrderNotifyDTO.setCardNo("555555XXXXXX4444");
        payOrderNotifyDTO.setPayName("fsdf");
        payOrderNotifyDTO.setTransactionDate(new Date());
        payOrderNotifyDTO.setFailureDesc(null);
        HttpEntity<PayOrderNotifyDTO> requestEntity = new HttpEntity<>(payOrderNotifyDTO);
        ResponseEntity<String> responseEntity = new RestTemplate().exchange("http://127.0.0.1:8080/pay/callback", HttpMethod.POST, requestEntity, String.class);
        final String result = responseEntity.getBody();
        log.info("订单通知结果：{}", result);
    }

}
