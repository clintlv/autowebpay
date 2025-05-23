
## 目录

- [平台账号颁发](#平台账号颁发)
- [API文档](#api文档)
   - [环境URL](#环境url)
   - [安全校验规则](#安全校验规则)
   - [订单查询接口](#订单查询接口)
   - [订单创建接口](#订单创建接口)
- [注意事项](#注意事项)
- [API错误信息](#api错误信息)
- [同步回调数据结构](#同步回调数据结构)
- [异步回调数据结构](#异步回调数据结构)
- [测试卡号](#测试卡号)
- [常见问题](#常见问题)

<div STYLE="page-break-after: always;"></div>


## 平台账号颁发

__1.商户 主邮箱__:商户需要提供一个邮箱作为主邮箱 , 后续登录商户平台使用

__2.商户 ip__ : 鉴于安全性验证 , 商户需要提供ip地址 , api服务接口会进行ip校验 , 只放行白名单内的ip , 允许多个

__3.商户 主域名__ : 鉴于安全性验证 , 商户提供主域名 , 格式为 www.xxx.com , 需要提供 xxx.com 结构的主域名 , 服务在提供支付页后 , 当用户打开支付页链接时 , 服务会验证上一级域名是否在服务白名单中 , 若不存在的域名会被拒绝验证 , 允许多个

__4.回调地址__ : 商户在收到平台派发的账号信息后 , 应到对应环境的商户后台的系统信息中配置同步回调地址及异步回调地址


<div STYLE="page-break-after: always;"></div>


## api文档:

## 1.生产和测试域

|环境| api                             |
|---|---------------------------------|
|生产| https://api.autowebpay.com      |
|测试| https://test-api.autowebpay.com |

|环境| 商户平台                            |
|---|---------------------------------|
|生产| https://merchant.autowebpay.com      |
|测试| https://test-merchant.autowebpay.com |

<div STYLE="page-break-after: always;"></div>


## 2.安全校验规则
请求api接口时 , 所有接口存在统一校验规则 , 所有接口必须携带以下请求头

| 字段名         |类型| 描述                 |
|-------------|---|--------------------|
| X-SID       |String| 商户识别id             |
| X-TOKEN     |String| 商户识别token          |
| X-TIMESTAMP |String| 请求时间戳(ms) 13位gmt+0 |
| X-ALG       |String| 请求密文               |


1.将自然排序后的参数名和参数值按 key=value 格式拼接，并⽤ & 连接成字符串，形成待签名字符
串。(注意：不包括返回的signature签名字段参数，该参数不参与签名计算,只用于验签校验)

> 此⽰例不为固定格式，实际情况请根据具体返回参数：
> 
> cardBank=MasterCard&cardNo=511100XXXXXX1128&cardType=MC&currencyType=USD&merchantOrderNo=202406201649996d95e00&orderNo=4908675497A2024062008&payAmount=2000&payChannelType=2&payName=MARTY Tang&status=1&tranNo=4908675497A2024062008&transactionDate=1718873602784


> java实列代码如下：

````
    //设置请求头，包含必须的识别信息和时间戳等
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-SID", X_SID); // 商户识别id
    headers.set("X-TOKEN", X_TOKEN); // 商户识别token
    final String timestamp = String.valueOf(System.currentTimeMillis());
    headers.set("X-TIMESTAMP", timestamp); // 请求时间戳13位，gmt+0当时间戳超过当前时间5分钟,请求将被拒绝

    //将自然排序后的参数名和参数值按 key=value 格式拼接，并⽤ & 连接成字符串
    Map<String, String> sortedParams = new TreeMap<>(params);
    StringBuilder stringToSign = new StringBuilder();
    for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
        if (StringUtils.isEmpty(entry.getValue())) {
            continue;
        }
        stringToSign.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
    }
    // 移除最后⼀个"&"
    stringToSign.deleteCharAt(stringToSign.length() - 1);
    log.info("stringToSign======:"+stringToSign);
    //md5运算拼接顺序为 sign + stringToSign(排序拼接后的key,value值)
    headers.set("X-ALG", DigestUtils.md5Hex(sign + stringToSign));
````

2.商户识别id,商户识别token,商户sign由平台颁发,如不清楚请联系商户对接人

3.X-TIMESTAMP请求时间戳(ms),当时间戳超过当前时间5分钟,请求将被拒绝


<div STYLE="page-break-after: always;"></div>


## 3.订单查询接口

> method: POST
> 
> path: /merchant-api/v2/queryOrder
> 
> Content-Type: application/json
> 
> 请求示例
> 
> https://test-api.autowebpay.com/merchant-api/v2/queryOrder
> ```json
> {
>  "orderNo": "1812398161A2024060611",
>  "timestamp": 1718877388136
> }
> ```


 
### 请求参数

| 名称        |位置|类型|必选| 说明            |
|-----------|---|---|---|---------------|
| orderNo   |query|string| 是 | 平台订单id（tranNo） |
| timestamp |query|string| 是 | 时间戳需要和请求头X-TIMESTAMP一致 |        |

### 返回参数

| 参数名             | 类型              |必选| 描述                          |
|:----------------|:----------------|:-----|:----------------------------|
| status          | String          |Y| 状态码成功200                    |
| message         | String          |N| 响应信息，Success或错误提示语          |
| data            | OrderCallBackVo |N| 数据                          |
| merchantOrderNo | String          |Y| 商户订单id（关联订单号）               |
| tranNo          | String          |Y| 平台订单id                      |
| payUrl          | String          |Y| 支付地址                        |
| payAmount       | Long            |Y| 订单交易金额 (单位: 分)              |
| currencyType    | String          |Y| 货币类型 (USD-美金)               |
| status          | Integer         |Y| 支付状态  (1-成功,2-失败)      |
| cardNo          | String          |N| 支付卡号                        |
| failureDesc     | String          |N| 支付渠道返回的错误信息                 |
| extendData      | String          |N| 扩展数据                        |
| transactionDate | Date            |Y| 交易完成时间(GTM-0)               |
| refundStatus    | Date            |Y| 订单退款状态 (1-退款中,2-已退款,3-退款失败) |                 |
| signature       | String          |Y| 验签                          |



> 成功示例

```json
{
  "message": "Success",
  "data": {
    "tranNo": "101611721820A20230919",
    "payUrl": "https://pay.autowebpay.com/JD3736BB4IHJHA98HNE0UYMD773",
    "merchantOrderNo": "202309191015170564997",
    "payAmount": 100,
    "currencyType": "USD",
    "countryCode": "",
    "cardNo": "542288XXXXXX2007",
    "transactionDate": 1695090489000,
    "failureDesc": "",
    "extendData": "",
    "refundStatus": 2,
    "signature": "f7cdc13ac619b2e20ea84cd162a6fd93"
  },
  "status": "200"
}



```

<div STYLE="page-break-after: always;"></div>


## 4.订单创建接口

> method: POST
>
> path: /merchant-api/v2/10/createOrder
>
> Content-Type: application/json
> 
> 请求示例
>
> https://test-api.autowebpay.com/merchant-api/v2/10/createOrder
> 
>  ```json
> {
>   "orderNo": "S20240229231534177DEV",
>   "payAmount": "4000",
>   "currencyType": "USD",
>   "extendData": "",
>   "timestamp": 1718879359505
> }
> ```

#### 请求参数

| 名称           | 类型       | 必选  | 说明                                  |
|--------------|----------|-----|-------------------------------------|
| orderNo      | string   | 是   | 商户订单id（关联订单号）最小21位最大32位(仅数字及字母)     |
| payAmount    | string   | 是   | 订单交易金额 (单位: 分)必须大于0                 |
| currencyType | string   | 否   | 货币类型 (USD-美金)(默认USD)                |
| extendData   | string   | 否   | 扩展数据 (用于商户扩展信息,支付完成后会完整返回,最大长度4096) |
| timestamp    | string   | 是   | 时间戳需要和请求头X-TIMESTAMP一致              |        |
| email        | string   | 是   | 邮箱                                  |
| ip           | string   | 是   | 用户请求IP地址                            |

#### 响应参数

| 参数名             | 类型              | 必选 | 描述                 |
|:----------------|:----------------|:---|:-------------------|
| status          | String          | Y  | 状态码成功200           |
| message         | String          | N  | 响应信息，Success或错误提示语 |
| data            | OrderResponseVo | N  | 数据                 |
| merchantOrderNo | String          | Y  | 商户订单id（关联订单号）      |
| tranNo          | String          | Y  | 平台订单id             |
| payUrl          | String          | Y  | 支付地址               |
| payAmount       | Long            | Y  | 订单交易金额 (单位: 分)     |
| currencyType    | Integer         | Y  | 货币类型 (USD-美金)      |
| extendData      | String          | N  | 扩展数据               |
| signature       | String          | Y  | 验签                 |

<div STYLE="page-break-after: always;"></div>

> 成功示例

```json
{
  "message": "Success",
  "data": {
    "tranNo": "101611721820A20230919",
    "payUrl": "https://pay.autowebpay.com/JD3736BB4IHJHA98HNE0UYMD773",
    "merchantOrderNo": "202309191015170564997",
    "payAmount": 100,
    "currencyType": "USD",
    "extendData": "",
    "signature": "f7cdc13ac619b2e20ea84cd162a6fd93"
  },
  "status": "200"
}
```

> 异常示例（异常请查看错误码）

```json
{
  "error":"Internal Server Error",
  "errorCode": "2101",
  "message": "Merchant does not exist or has not yet been initialized!",
  "status": "500"
}

```


__注意事项__ :

1.正常情况下，支付地址所指向的支付页允许多次打开但只能提交一次有效支付请求,已提交的支付订单不可再次发起支付.

2.如若长时间离开支付窗口未操作支付，支付链接会过期失效,请用户及时支付,失效后请重新发起支付.


<div STYLE="page-break-after: always;"></div>


## 4.退款接口（全额退）

> method: POST
>
> path: /merchant-api/v2/10/refund
>
> Content-Type: application/json
>
> 请求示例
>
> https://test-api.autowebpay.com/merchant-api/v2/10/refund
>
>  ```json
> {
>   "orderNo": "0102020072A2024072915",
>   "timestamp": 1718879359505
> }
> ```

#### 请求参数

| 名称           | 类型     |必选| 说明                                  |
|--------------|--------|---|-------------------------------------|
| orderNo      | string | 是 | 平台订单id（tranNo）     |
| timestamp    | string | 是 | 时间戳需要和请求头X-TIMESTAMP一致              |        |

#### 响应参数

| 参数名               | 类型              | 必选 | 描述                          |
|:------------------|:----------------|:---|:----------------------------|
| status            | String          | Y  | 状态码成功200                    |
| message           | String          | N  | 响应信息，Success或错误提示语          |
| data              | OrderResponseVo | N  | 数据                          |
| merchantOrderNo   | String          | Y  | 商户订单id（关联订单号）               |
| tranNo            | String          | Y  | 平台订单id                      |
| refundAmount      | Long            | Y  | 退款金额                        |
| refundStatus      | Integer         | Y  | 订单退款状态 (1-退款中,2-已退款,3-退款失败) |
| refundOrderNo     | String          | Y  | 退款订单号                       |
| signature         | String          | Y  | 验签                          |

<div STYLE="page-break-after: always;"></div>

> 成功示例

```json
{
  "message": "Success",
  "data": {
    "merchantOrderNo": "S202402725095895944DEV",
    "refundAmount": "40.00",
    "refundOrderNo": "T20240731161605828269",
    "refundStatus": 2,
    "signature": "996dc95c45ea688435acbcafaf4fe618",
    "tranNo": "0354064784A2024073018"
  },
  "status": "200"
}
```

<div STYLE="page-break-after: always;"></div>

## 5.api错误信息

| 错误码       | 错误                                               | 错误信息                    |
|-----------|--------------------------------------------------|-------------------------|
| MER1001   | AUTHENTICATION DATA EMPTY!                       | 请求头不合法,缺少请求头            |
| MER1003   | AUTHENTICATION DATA MATCH ERROR!                 | 商户信息没有匹配                |
| MER1004   | AUTHENTICATION ERROR!                            | 商户token错误               |
| MER1005   | AUTHENTICATION EXPIRE!                           | 商户账号过期 , 请联系平台管理员处理     |
| MER1006   | AUTHENTICATION REQUEST EXPIRE!                   | 请求逾时                    |
| MER1009   | ORDER AUTHENTICATION ERROR!                      | 请求头请求密文错误               |
| MER1007   | ORDER NUM DOES NOT COMPLY WITH THE RULES!        | 订单id格式异常(仅数字及字母) |
| MER1008   | ORDER AMOUNT ERROR!                              | 订单金额异常 (单位: 分 , 必须大于0)  |
| MER1010   | ORDER CHANNEL ERROR!                             | 订单渠道异常 , 请联系平台管理员处理     |
| MER1011   | ORDER NUMBER DUPLICATION ERROR!                  | 订单no已存在                 |
| MER1012   | ORDER CURRENCY TYPE ERROR!                       | 订单货币类型异常                |
| MER1014   | CHANNEL ORDER CHECK ERROR!                       | 订单扩展数据异常                |
| MER1015   | LOWER THAN THE MINIMUM SINGLE LIMIT!             | 订单价格小于商户最小交易价格          |
| MER1016   | HIGHER THAN THE MAXIMUM SINGLE LIMIT!            | 订单价格高于最高单一限额！           |
| MER1017   | SINGLE-DAY TRANSACTION AMOUNT EXCEEDS THE LIMIT! | 单日交易金额超限！               |
| MER1018   | Not on File!                                     | 无档案！                    |
| MER1019   | ORDER NOT EXIST!                                 | 订单不存在！                  |
| MER1020   | STORED TOKEN ERROR!                              | 存储令牌错误！                 |
| A20001       | The query date cannot be longer than one month     | 查询日期不能超过一个月     |
| A20002       | Refund amount has exceeded the limit               | 退款金额超出限制           |
| A20003       | Refund amount is greater than capture amount.      | 退款金额大于捕获金额       |
| A20004       | Capture amount is greater than pre authorization amount. | 捕获金额大于预授权金额   |
| A20005       | Card year cannot be less than current year.        | 卡年份不能早于当前年份     |
| A20006       | Wrong card type.                                   | 卡类型错误                 |
| A20007       | Currency errors.                                   | 货币错误                   |
| A20008       | Country is wrong.                                  | 国家错误                   |
| A20009       | Transaction does not exist.                        | 交易不存在                 |
| A20010       | Order does not exist.                              | 订单不存在                 |
| A20011       | Merchant does not exist.                           | 商户不存在                 |
| A20012       | Merchant account does not exist.                   | 商户账户不存在             |
| A20013       | Order already exists.                               | 订单已存在                 |
| A20014       | Transaction already exists.                         | 交易已存在                 |
| A20015       | Refund already exists.                              | 退款已存在                 |
| A20016       | Collection not allowed for account.                | 账户不允许收款             |
| A20017       | Pre authorization cannot be revoked.               | 预授权无法撤销             |
| A20018       | The transaction cannot be refunded.                | 交易无法退款               |
| A20019       | The transaction cannot be captured.                | 交易无法捕获               |
| A20020       | The transaction cannot be voided.                  | 交易无法取消               |
| A20021       | The refund cannot be voided.                       | 退款无法取消               |
| A20022       | Merchant does not open the transaction source authority. | 商户未开通交易源权限 |
| A20023       | No refund allowed for this account.                | 此账户不允许退款           |
| A20024       | Partial refund is not allowed for this account.    | 此账户不允许部分退款       |
| A20025       | This payment method is not supported in this account. | 此账户不支持此支付方式 |
| A20026       | Product not opened.                                | 产品未开通                 |
| A20027       | Merchant account does not exist.                   | 商户账户不存在             |
| A20028       | Query too frequently.                              | 查询过于频繁               |
| A20029       | The merchant does not support this currency: {0}.   | 商户不支持该货币：{0}      |
| B30001       | Pick up card                                        | 取卡                       |
| B30002       | Lost card                                           | 丢失卡                     |
| B30003       | Stolen card                                         | 被盗卡                     |
| B30004       | Invalid card number                                 | 卡号无效                   |
| B30005       | Invalid CAM, dCVV, iCVV, CVV or CVV2                | 无效的CAM、dCVV、iCVV、CVV或CVV2 |
| B30006       | Expired card                                        | 过期卡                     |
| B30007       | Insufficient funds                                  | 资金不足                   |
| B30008       | No such issuer                                      | 发卡机构不存在             |
| B30009       | Refer to card issuer                                | 请咨询发卡机构             |
| B30010       | Do not honor                                        | 不予受理                   |
| B30011       | Transaction not permitted to cardholder             | 交易未被持卡人允许         |
| B30012       | Suspected Fraud                                     | 涉嫌欺诈                   |
| B30013       | Fraud/Security related reasons                      | 欺诈/安全相关原因          |
| B30014       | Internal Policies                                    | 内部政策                   |
| B30015       | Unregistered Website:{0}.                            | 未注册网站：{0}            |
| B30016       | Unable to route transaction.                        | 无法路由交易               |
| B30017       | System malfunction.                                  | 系统故障                   |
| B30018       | Error.                                              | 错误                       |
| B30019       | No completed 3D Secure.                             | 未完成3D Secure            |
| B30020       | Please don't submit payments repeatedly             | 请勿重复提交付款           |
| P10001       | Parameter [{0}] is required.                        | 参数[{0}]是必需的         |
| P10002       | The value of [{0}] length is {1}.                   | [{0}]的值长度为{1}         |
| P10003       | The value of [{0}] is a string of a-z, A-Z, 0-9, and characters. | [{0}]的值是一个包含a-z、A-Z、0-9和字符的字符串 |
| P10004       | The value of [{0}] is a string of a-z, A-Z, and 0-9. | [{0}]的值是一个包含a-z、A-Z和0-9的字符串 |
| P10005       | The value of [{0}] is a string of 0-9.             | [{0}]的值必须是一个由数字0到9组成的字符串。                          |
| P10006       | The value of [{0}] is a string of a-z and A-Z.     | [{0}]的值必须是一个由小写字母a-z和大写字母A-Z组成的字符串。          |
| P10007       | The value of [{0}] is a string of 0-9 and '.' characters. | [{0}]的值必须是一个由数字0-9和点号'.'组成的字符串。                  |
| P10008       | The value of [{0}] is a string of a-z, A-Z, 0-9, '@' and '.' characters. | [{0}]的值必须是一个由小写字母a-z、大写字母A-Z、数字0-9、@符号和点号'.'组成的字符串。 |
| P10010       | The value of [{0}] can only be a number between 1 and 12. | [{0}]的值只能是介于1和12之间的数字。                                 |
| P10011       | The value of [{0}] is a string of a-z, _, -, A-Z, and 0-9. | [{0}]的值必须是一个由小写字母a-z、下划线_、破折号-、大写字母A-Z和数字0-9组成的字符串。 |
| P10012       | The value of [{0}] is invalid.                     | [{0}]的值无效。                                                      |
| P10013       | The data format is incorrect: [{0}]                | 数据格式不正确：[{0}]                                                 |
| P10014       | Signature Verification Failed.                     | 签名验证失败。                                                       |
| P10015       | The amount must be greater than 0.                 | 金额必须大于0。                                                      |
| P10016       | Invalid Alpha Currency Code.                       | 无效的字母货币代码。                                                 |
| P10017       | The amount must be kept to two decimal places: {0}. | 金额必须保留两位小数：{0}。                                          |
| P10018       | Amount format is incorrect.[{0}]:{1}               | 金额格式不正确:[{0}]:{1}                                             |
| P10020       | The card year cannot be earlier than the current year. | 卡年份不能早于当前年份。                                             |
| P10022       | The card month cannot be earlier than the current month or the month is incorrect. | 卡月份不能早于当前月份或月份错误。                                   |
| P10025       | The country is incorrect.                          | 国家错误。                                                           |
| P10026       | The IP address is invalid.                         | IP地址无效。                                                         |



<div STYLE="page-break-after: always;"></div>


## 6.同步回调数据结构
用户在支付完成后 , 页面会跳转到商户配置的同步回调地址 , 此时会在url后携带以下数据

> 回调示例
> 
> https://{callBackUrl}?tranNo=101611721820A20230919&orderNo=202309191015170564997&status=1&extendData=1

| 参数名        | 类型      | 描述                    |
|------------|---------|-----------------------|
| tranNo     | String  | 	平台订单id               |
| orderNo    | String  | 	商户订单id               |
| status     | Integer | 支付状态 (1-成功,2-失败) |
| extendData | String  | 	扩展数据                 |

其中商户需要关注extendData这个参数 , 商户可以根据在extendData中设置不同的数据 , 以达到不同的结果页效果 , 比如商品类型 , 用户信息等

__注意事项 :__ 一般而言 , 由于同步回调依赖用户页面流转 , 往往同步请求会比异步请求更晚到达应用服务器
<div STYLE="page-break-after: always;"></div>


## 7.异步回调数据结构
在服务器确认到用户支付完成后 , 会向商户服务器发起回调 , 此时会向商户配置的异步回调地址发起POST请求并在body中以json结构携带以下参数

| 参数名             | 类型       | 描述                     |
|-----------------|-----------|------------------------|
| tranNo          | String    | 平台订单id                 |
| merchantOrderNo | String    | 商户订单id                 |
| payAmount       | Long      | 订单交易金额(单位: 分)          |
| payChannelType  | Integer   | 支付渠道类型                |
| currencyType    | String    | 货币类型 (USD-美金)          |
| status          | Integer   | 支付状态  (1-成功,2-失败) |
| countryCode     | String    | 交易国家                   |
| cardType        | String    | 卡类型                    |
| cardBank        | String    | 卡银行                    |
| cardNo          | String    | 支付卡号                   |
| payName         | String    | 付款人名称                  |
| failureDesc     | String    | 支付渠道返回的错误信息            |
| extendData      | String    | 扩展数据                   |
| transactionDate | Date      | 交易成功时间                 |
| signature       | String    | 验签                     |
| token           | String    | 用户记卡后返回记卡id            |

<div STYLE="page-break-after: always;"></div>

>成功示例

```json
{
    "message":"Success",
    "data":{
        "merchantOrderNo":"202309191015170564997",
        "payAmount":100,
        "currencyType":"USD",
        "extendData":"",
        "tranNo":"101611721820A20230919",
        "payUrl":"https://pay.autowebpay.com/JD3736BB4IHJHA98HNE0UYMD773",
        "status":1,
        "countryCode":"",
        "cardType":"MC",
        "cardBank":"MasterCard",
        "cardNo":"542288XXXXXX2007",
        "payName":"zac",
        "transactionDate":1695090489000,
        "failureDesc":null,
        "signature": "f7cdc13ac619b2e20ea84cd162a6fd93",
        "token": "dd7369131d6c2a7df90d7365c1f773fadf6807cac168cde8fcac44e0f1fe0ff5"
    },
    "status":"200"
}

```

__注意事项 :__

1.一般而言 , 由于同步回调依赖用户页面流转 , 往往同步请求会比异步请求更晚到达应用服务器

2.异步回调重试机制为：5分钟，十分钟，半小时，一小时，两小时，两小时，每次最多延迟或者提前1分钟，最大重试6次

<div STYLE="page-break-after: always;"></div>

## 8.测试卡号

__注意事项 :__ 

1.本文档中的所有测试卡号仅可使用于AutowebPay测试环境,如果将该账号使用于生产环境,用户同意承担所有费用

2.部分卡号可能出于安全原因被禁用,若出现请尝试其他卡号

3.卡号到期日请填写未来的月份和年份

4.卡号认证CVV请使用对应位数的任意3位数字值即可


####测试信用卡

| 卡类型        | 卡号                 | 有效期 | CVV | 备注                 |
|------------|--------------------|------|-----|--------------------|
| VISA       | 445653 000000 1005 |      |     | 测试卡号               |
| VISA       | 4000000000000002   |      |     | 非3D测试卡号            |
| VISA       | 4000000000000010   | 27/12| 212 | 无感3D测试卡号           |
| MASTERCARD | 4000000000000010   | 27/12| 212 | 无感3D测试卡号           |
| VISA       | 4000000000000003   | 27/12| 212 | 有感3D测试卡号           |
| MASTERCARD | 5000000000000003   | 27/12| 212 | 有感3D测试卡号，验证码123456 |



<div STYLE="page-break-after: always;"></div>

## 9.常见问题

1. 商户需要提供哪些信息才能使用平台账号？
    - 商户需要提供一个邮箱作为主邮箱，后续登录商户平台使用。
    - 鉴于安全性验证，商户需要提供IP地址，API服务接口会进行IP校验，只放行白名单内的IP，允许多个。
    - 商户在收到平台派发的账号信息后，应到对应环境的商户后台的系统信息中配置同步回调地址及异步回调地址。


2. 请求API接口时需要遵守哪些安全校验规则？
    - 所有接口存在统一校验规则，必须携带以下请求头：X-SID（商户识别id）、X-TOKEN（商户识别token）、X-TIMESTAMP（请求时间戳(ms)）和X-ALG（请求密文）。
    - X-ALG请求密文的格式需按顺序拼接：sign + 参数组合（参数组合为请求参数的key首字母排序后， value值进行拼接）进行MD5运算。


3. 如何实现Java代码中的请求头设置？
    - 创建HttpHeaders对象并设置内容类型为MediaType.APPLICATION_JSON。
    - 设置必要的识别信息和时间戳等请求头字段：X_SID、X_TOKEN、X_TIMESTAMP。
    - 对请求参数的key首字母排序后， value值进行拼接，并进行MD5运算以生成X_ALG请求密文。


4. 如果不清楚商户识别id、商户识别token或商户sign，应该如何处理？
    - 如果不清楚这些信息，请联系商户对接人。


5. X-TIMESTAMP的作用是什么？如果时间戳超过当前时间5分钟会怎样？
    - X-TIMESTAMP是请求时间戳(ms)，用于验证请求的时效性。如果时间戳超过当前时间5分钟，请求将被拒绝。


6. 订单查询接口和订单创建接口分别用于什么目的？它们有哪些请求参数和响应参数？
    - 订单查询接口用于根据商户订单id查询订单详情。它有一个必需的请求参数merchantOrderNo（商户订单id）。
    - 订单创建接口用于创建新的支付订单。它有多个请求参数，包括orderNo（商户订单id）、payAmount（交易金额）、currencyType（货币类型）和extendData（扩展数据）。响应参数包括支付成功页面的链接、商户订单号、支付金额、货币类型等信息。


7. 同步回调和异步回调的区别是什么？它们的数据结构分别是怎样的？
    - 同步回调依赖于用户页面流转，往往比异步回调到达应用服务器更晚。它通过URL后携带tranNo、orderNo、status和extendData等数据。
    - 异步回调由服务器确认用户支付完成后发起，通常通过POST请求发送到异步回调地址，并在body中以json结构携带更多详细的支付状态信息和其他相关数据。


8. 回调地址在哪里填写？只填写一个可以吗？
    - 回调地址是商户平台系统信息中配置的，同步回调地址和异步回调地址都要填写，同步地址用于支付后跳转页面，异步回调推送支付完整参数商户需要根据请求参数进行业务处理。
![img.png](img.png)
