package com.autowebpay.payment;

import com.autowebpay.payment.demos.dto.PayOrderNotifyDTO;
import com.autowebpay.payment.demos.web.payController;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

import static com.autowebpay.payment.demos.web.payController.generateRandomNumber;
import static com.autowebpay.payment.demos.web.payController.getStringResponseEntity;

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



    @Test
    void orderPlaceOrder() {
        String X_SID = "103";
        String X_TOKEN = "xWyZegKBRG2vOEon";
        String X_SIGN = "sjDBlHx96U7I6xrg";
        // 时间戳
        String timestamp = "1714013608727";
        String orderNo = "1714013601440";

        final HashMap<String, String> param = new HashMap<String, String>() {{
            //商户订单id（关联订单号）必须21位(仅数字及字母)
            put("orderNo", orderNo);
            //订单交易金额 (单位: 分)必须大于0
            put("payAmount", "10000");
            //货币类型 (USD-美金)(默认USD)
            put("currencyType", "USD");
            //扩展数据 (用于商户扩展信息,支付完成后会完整返回,最大长度4096)
            put("extendData", "");
        }};

        //X-ALG 请求密文的格式需按顺序拼接: 拼接顺序为 sign + timestamp + 参数组合(参数组合为请求参数的key首字母排序后,value值进行拼接)进行md5运算
        //例如：sign=123,timestamp=456,b=8&a=7&c=9 拼接后：123456789
        final String propertie = param.keySet().stream()
                .sorted(Comparator.comparing(s -> s.substring(0, 1)))
                .map(param::get)
                .collect(Collectors.joining());
        String md5 = DigestUtils.md5Hex(X_SIGN + timestamp + propertie);

        // 设置请求头，包含必须的识别信息和时间戳等
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-SID", X_SID); // 商户识别id
        headers.set("X-TOKEN", X_TOKEN); // 商户识别token
        headers.set("X-TIMESTAMP", timestamp); // 请求时间戳
        headers.set("X-ALG", md5);

        System.out.println("propertie".concat(":").concat(propertie).concat("===").concat(md5));

    }

}
