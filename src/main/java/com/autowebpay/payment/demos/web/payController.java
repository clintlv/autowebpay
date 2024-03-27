package com.autowebpay.payment.demos.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.autowebpay.payment.demos.dto.PayOrderNotifyDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pay")
public class payController {
    private static final Logger log = LogManager.getLogger(payController.class);
    private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";

    //商户识别id
    private static final String X_SID = "1";
    //商户识别token
    private static final String X_TOKEN = "1";
    //请求密文
    private static final String X_ALG = "1";
    /**
     * 订单创建接口
     * https://test-api.autowebpay.com/merchant-api/1/order
     */
    private static final String PLACE_ORDER_URL = "http://127.0.0.1:8095/merchant-api/1/order";

    // http://127.0.0.1:8080/pay/placeOrder
    @RequestMapping("/placeOrder")
    public String placeOrder(Model model)
    {

        // 生成时间戳部分
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT));
        String orderNo = timestamp + generateRandomNumber(8);

        final HashMap<String, String> param = new HashMap<String, String>() {{
            //商户订单id（关联订单号）必须21位(仅数字及字母)
            put("orderNo", orderNo);
            //订单交易金额 (单位: 分)必须大于0
            put("payAmount", "1000");
            //货币类型 (USD-美金)(默认USD)
            put("currencyType", "USD");
            //扩展数据 (用于商户扩展信息,支付完成后会完整返回,最大长度4096)
            put("extendData", "");
        }};

        final ResponseEntity<String> responseEntity = getStringResponseEntity(param);
        String response = responseEntity==null ? null : responseEntity.getBody();
        if (response!=null) {
            final JSONObject queryNode = JSON.parseObject(response);
            JSONObject data = queryNode.getJSONObject("data");
            //支付地址
            model.addAttribute("payUrl", data.get("payUrl"));
            //商户订单id（关联订单号）
            model.addAttribute("merchantOrderNo", data.get("merchantOrderNo"));
            //平台订单id
            model.addAttribute("tranNo", data.get("tranNo"));
            //订单交易金额 (单位: 分)
            model.addAttribute("payAmount", data.get("payAmount"));
            return "pay";
        }
        return response;
    }

    /**
     * 支付回调
     */
    @RequestMapping("/callback")
    @ResponseBody
    public String callback(HttpServletResponse response, HttpServletRequest request) {
        try {
            // 转换回调参数，并进行参数合法性验证
            final PayOrderNotifyDTO jsonParam = validateAndConvertParameters(request);
            log.info("回调参数：{}", jsonParam);

            // 进行业务处理
            processCallback(jsonParam);
        } catch (Exception e) {
            // 异常处理逻辑，如记录日志、返回处理结果等
            log.error("处理回调异常：", e);
            // 根据实际情况，这里可以对异常进行相应处理，如返回错误信息给客户端等
        }
        return "success";
    }


    /**
     * 测试订单通知
     */
    @ResponseBody
    @RequestMapping("/orderNotify")
    public void orderNotify() {
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

    /**
     * 转换并验证回调参数
     */
    private PayOrderNotifyDTO validateAndConvertParameters(HttpServletRequest request) throws Exception {
        // 组装回调数据
        StringBuilder sb = readBodyFromRequest(request);
        // 假设已有StringBuilder对象sb存储了请求体内容
        String requestBody = sb.toString();

        ObjectMapper mapper = new ObjectMapper();
        PayOrderNotifyDTO entity = new PayOrderNotifyDTO();
        try {
            entity = mapper.readValue(requestBody, PayOrderNotifyDTO.class);
        } catch (JsonProcessingException e) {
            // 处理反序列化异常
            e.printStackTrace();
        }
        return entity;
    }


    /**
     * 读取requestBody数据
     * @param request
     * @return
     * @throws IOException
     */
    private StringBuilder readBodyFromRequest(HttpServletRequest request) throws IOException {
        BufferedReader br = request.getReader();
        StringBuilder sb = new StringBuilder();
        String str = "";
        while ((str = br.readLine()) != null) {
            sb.append(str);
        }
        return sb;
    }

    /**
     * 处理回调逻辑，包括：查询本地订单数据、校验订单状态是否重复回调、查询订单接口二次校验回调、订单状态更新、其他业务处理
     */
    private void processCallback(PayOrderNotifyDTO jsonParam) {
        log.info("处理回调逻辑商户订单号：{}", jsonParam.getMerchantOrderNo());
        // 伪代码，表示具体的处理逻辑
        // 1. 查询本地订单数据
        // 2. 校验订单状态是否重复回调
        // 3. 查询订单接口二次校验回调
        // 4. 订单状态更新
        // 5. 其他业务处理
    }



    /**
     * 获取下单请求响应
     *
     * @param param 请求参数，键值对形式
     * @return 返回下单请求的响应实体，可能包含订单信息或其他相关数据
     */
    private static ResponseEntity<String> getStringResponseEntity(HashMap<String, String> param) {
        // 设置请求头，包含必须的识别信息和时间戳等
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-SID", X_SID); // 商户识别id
        headers.set("X-TOKEN", X_TOKEN); // 商户识别token
        final String timestamp = String.valueOf(System.currentTimeMillis());
        headers.set("X-TIMESTAMP", timestamp); // 请求时间戳
        // 生成请求密文 X-ALG 请求密文的格式为: 按顺序拼接 sign,timestamp,参数组合(参数组合为请求参数的属性名首字母排序后 , 进行值拼接) 后 , 进行md5运算
        final String propertie = param.keySet().stream()
                .sorted(Comparator.comparing(s -> s.substring(0, 1)))
                .map(param::get)
                .collect(Collectors.joining());
        headers.set("X-ALG", DigestUtils.md5Hex(X_ALG + timestamp + propertie));

        // 创建HttpEntity并使用GET方法发送请求
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        // 构建请求URL，并添加所有请求参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(PLACE_ORDER_URL);
        param.forEach(builder::queryParam);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = null;
        try{
            // 发起GET请求并获取响应实体
            responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, requestEntity, String.class);
        }catch (HttpClientErrorException httpClientErrorException) {
            // 捕获并记录HTTP客户端错误
            log.error("HTTP Client Error: {}", httpClientErrorException.getMessage());
            log.error("Response Body: {}", httpClientErrorException.getResponseBodyAsString());
        } catch (RestClientException restClientException) {
            // 捕获并记录其他Rest客户端异常
            log.error("Rest Client Exception: {}", restClientException.getMessage());
            log.error("Rest Client Exception: {}", restClientException);
        }
        return responseEntity;
    }


    /**
     * 生成指定长度的随机数字字符串。
     *
     * @param length 需要生成的随机数字字符串的长度。
     * @return 生成的随机数字字符串。
     */
    private static String generateRandomNumber(int length) {
        Random random = new Random(); // 创建随机数生成器
        StringBuilder sb = new StringBuilder(); // 创建StringBuilder用于拼接随机数字

        // 循环生成指定长度的随机数字，并将其拼接到StringBuilder中
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // 生成0到9之间的随机整数，并追加到字符串中
        }

        return sb.toString(); // 将StringBuilder转换为字符串并返回
    }
}
