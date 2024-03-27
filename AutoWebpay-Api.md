
#平台账号颁发

__1.商户 主邮箱__:商户需要提供一个邮箱作为主邮箱 , 后续登录商户平台使用

__2.商户 ip__ : 鉴于安全性验证 , 商户需要提供ip地址 , api服务接口会进行ip校验 , 只放行白名单内的ip , 允许多个

__3.商户 主域名__ : 鉴于安全性验证 , 商户提供主域名 , 格式为 www.xxx.com , 需要提供 xxx.com 结构的主域名 , 服务在提供支付页后 , 当用户打开支付页链接时 , 服务会验证上一级域名是否在服务白名单中 , 若不存在的域名会被拒绝验证 , 允许多个

__4.回调地址__ : 商户在收到平台派发的账号信息后 , 应到对应环境的商户后台的系统信息中配置同步回调地址及异步回调地址 


<div STYLE="page-break-after: always;"></div>


#api文档:

## 1.生产和测试域

|环境|url|
|---|---|
|生产|https://api.autowebpay.com|
|测试|https://test-api.autowebpay.com|

<div STYLE="page-break-after: always;"></div>


## 2.安全校验规则
请求api接口时 , 所有接口存在统一校验规则 , 所有接口必须携带以下请求头

|字段名|描述|
|---|---|
|X-SID|商户识别id|
|X-TOKEN|商户识别token|
|X-TIMESTAMP|请求时间戳(ms)|
|X-ALG|请求密文|

X-ALG 请求密文的格式为: 按顺序拼接 sign,timestamp,参数组合(参数组合为请求参数的属性名首字母排序后 , 进行值拼接) 后 , 进行md5运算

其中 , 商户识别id , 商户识别token , 及商户sign 由平台颁发X-TIMESTAMP 请求时间戳(ms) , 当时间戳超过当前时间5分钟 , 请求将被拒绝


<div STYLE="page-break-after: always;"></div>


## 3.订单查询接口

GET /merchant-api/v2/query
> 请求示例
> 
> https://test-api.autowebpay.com/merchant-api/v2/query?merchantOrderNo=101611721820A20230919
 
### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|merchantOrderNo|query|string| 是 |商户订单id（关联订单号）|

### 返回参数

|参数名|类型|必选|描述|
|:-----|:-----|:-----|:-----|
|status|String|Y|状态码成功200|
|message|String|N|响应信息，Success或错误提示语|
|data|OrderCallBackVo|N|数据|
|merchantOrderNo|String|Y|商户订单id（关联订单号）|
|tranNo|String|Y|平台订单id|
|payUrl|String|Y|支付地址|
|payAmount|Long|Y|订单交易金额 (单位: 分)|
|currencyType|String|Y|货币类型 (USD-美金)|
|status | Integer |Y| 支付状态  (1-成功,2-失败,3-取消)|
|countryCode|String|N|交易国家|
|cardType|String|N|卡类型|
|cardBank|String|N|卡银行|
|cardNo|String|N|支付卡号|
|payName|String|N|付款人名称|
|failureDesc|String|N|支付渠道返回的错误信息|
|extendData|String|N|扩展数据|
|transactionDate|Date|Y|交易完成时间(GTM-0)|



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
    "cardType": "MC",
    "cardBank": "MasterCard",
    "cardNo": "542288XXXXXX2007",
    "payName": "zac",
    "transactionDate": 1695090489000,
    "failureDesc": "",
    "extendData": ""
  },
  "status": "200"
}



```

<div STYLE="page-break-after: always;"></div>


## 4.订单创建接口

GET /merchant-api/1/order

> 请求示例
> 
> https://test-api.autowebpay.com/merchant-api/1/order?orderNo=101611721820A20230919&payAmount=1999&extendData=1


#### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|orderNo|query|string| 是 |商户订单id（关联订单号）必须21位(仅数字及字母)|
|payAmount|query|string| 是 |订单交易金额 (单位: 分)必须大于0|
|currencyType|query|string| 否 |货币类型 (USD-美金)(默认USD)|
|extendData|query|string| 否 |扩展数据 (用于商户扩展信息,支付完成后会完整返回,最大长度4096)|

#### 响应参数
| 参数名 | 类型 | 必选 | 描述 |
|:-----|:-----|:-----|:-----|
| status | String | Y | 状态码成功200 |
| message | String | N | 响应信息，Success或错误提示语 |
| data | OrderResponseVo | N | 数据 |
| merchantOrderNo | String | Y | 商户订单id（关联订单号） |
| tranNo | String | Y | 平台订单id |
| payUrl | String | Y | 支付地址 |
| payAmount | Long | Y | 订单交易金额 (单位: 分) |
| currencyType | Integer | Y | 货币类型 (USD-美金) |
| extendData | String | N | 扩展数据 |


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
    "extendData": ""
  },
  "status": "200"
}
```

__注意事项__ :

1.正常情况下，支付地址所指向的支付页只允许被用户打开一次，该链接被打开一次后，后续开启页签复制页签会导致链接失效无法完成支付

2.如若长时间停留支付页未操作支付，支付链接失效无法完成支付

3.支付页链接必须由白名单域名下发起的跳转请求，否则域名验证不通过(页面提示：Domain Validation Failed)


<div STYLE="page-break-after: always;"></div>



## 5.api错误信息

|错误码|错误|错误信息|
|---|---|---|
|MER1001|AUTHENTICATION DATA EMPTY!|  请求头不合法,缺少请求头 |
|MER1003|AUTHENTICATION DATA MATCH ERROR!|  商户信息没有匹配  |
|MER1004|AUTHENTICATION ERROR!|  商户token错误  |
|MER1005|AUTHENTICATION EXPIRE!|  商户账号过期 , 请联系平台管理员处理  |
|MER1006|AUTHENTICATION REQUEST EXPIRE!|  请求逾时  |
|MER1009|ORDER AUTHENTICATION ERROR!|   请求头请求密文错误  |
|MER1007|ORDER NUM DOES NOT COMPLY WITH THE RULES!|  订单id格式异常(必须21位 ,仅数字及字母)  |
|MER1008|ORDER AMOUNT ERROR!|   订单金额异常 (单位: 分 , 必须大于0) |
|MER1010|ORDER CHANNEL ERROR!|  订单渠道异常 , 请联系平台管理员处理  |
|MER1011|ORDER NUMBER DUPLICATION ERROR!|  订单no已存在  |
|MER1012|ORDER CURRENCY TYPE ERROR!|  订单货币类型异常 |
|MER1015|LOWER THAN THE MINIMUM SINGLE LIMIT!|  订单价格小于商户最小交易价格 |


<div STYLE="page-break-after: always;"></div>


## 6.同步回调数据结构
用户在支付完成后 , 页面会跳转到商户配置的同步回调地址 , 此时会在url后携带以下数据

> 回调示例
> 
> https://{callBackUrl}?tranNo=101611721820A20230919&orderNo=202309191015170564997&status=1&extendData=1

|参数名|类型|描述|
|---|---|---|
|tranNo|String|	平台订单id|
|orderNo|String|	商户订单id|
|status|Integer|支付状态 (1-成功,2-失败,3-取消)|
|extendData|String|	扩展数据|

其中商户需要关注extendData这个参数 , 商户可以根据在extendData中设置不同的数据 , 以达到不同的结果页效果 , 比如商品类型 , 用户信息等

__注意事项 :__ 一般而言 , 由于同步回调依赖用户页面流转 , 往往同步请求会比异步请求更晚到达应用服务器
<div STYLE="page-break-after: always;"></div>


## 7.异步回调数据结构
在服务器确认到用户支付完成后 , 会向商户服务器发起回调 , 此时会向商户配置的异步回调地址发起POST请求并在body中以jsopn结构携带以下参数

| 参数名 | 类型 | 描述 |
| --- | --- | --- |
| tranNo | String | 平台订单id |
| merchantOrderNo | String | 商户订单id |
| payAmount | Long | 订单交易金额(单位: 分) |
| payChannelType|	Integer|	支付渠道类型|
| currencyType | String | 货币类型 (USD-美金)|
| status | Integer | 支付状态  (1-成功,2-失败,3-取消)|
| countryCode | String | 交易国家 |
| cardType | String | 卡类型 |
| cardBank | String | 卡银行 |
| cardNo | String | 支付卡号 |
| payName | String | 付款人名称 |
| failureDesc | String | 支付渠道返回的错误信息 |
| extendData | String | 扩展数据 |
| transactionDate | Date | 交易成功时间 |

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
        "failureDesc":null
    },
    "status":"200"
}

```

__注意事项 :__ 一般而言 , 由于同步回调依赖用户页面流转 , 往往同步请求会比异步请求更晚到达应用服务器

<div STYLE="page-break-after: always;"></div>

## 8.测试卡号

__注意事项 :__ 

1.本文档中的所有测试卡号仅可使用于AutowebPay测试环境,如果将该账号使用于生产环境,用户同意承担所有费用

2.部分卡号可能出于安全原因被禁用,若出现请尝试其他卡号

3.卡号到期日请填写未来的月份和年份

4.卡号认证CVV请使用对应位数的任意3位数字值即可（American Express卡为任意4位数，输入3位数可测试支付失败订单）


####信用卡（美国)

|卡类型|卡号|
|---|---|
|Visa| 4112 3441 1234 4113|
|Visa Commercial Card| 4110 1441 1014 4115|
|Visa Corporate Card II| 4114 3601 2345 6785|
|Visa Purchasing Card III| 4061 7240 6172 4061|
|MasterCard| 5111 0051 1105 1128|
|MasterCard| 5112 3451 1234 5114|
|MasterCard| 2221 0000 0000 0009|
|MasterCard| 2321 0000 0000 0008|
|MasterCard| 2421 0000 0000 0007|
|MasterCard| 2521 0000 0000 0006|
|MasterCard| 2621 0000 0000 0005|
|MasterCard| 2720 9900 0000 0007|
|MasterCard II| 5115 9151 1591 5118|
|MasterCard III| 5116 6012 3456 7894|
|American Express| 3711 4437 1144 376|
|American Express| 3411 3411 3411 347|
|Discover| 6011 0160 1101 6011|
|Discover| 6559 9065 5990 6557|
|Discover II| 6545 0000 0000 0009|
|Discover| 8171 9999 0000 0018|
|Discover Diners| 3611 0361 1036 12|
|Diners| 3643 8936 4389 36|
|JCB| 3566 0035 6600 3566|
|JCB| 3528 0000 0000 0007|

####信用卡（加拿大)

|卡类型|卡号|
|---|---|
|Visa| 4257 0211 1111 1116|
|MasterCard| 5193 9111 1111 1112|

####信用卡（英国)

|卡类型|卡号|
|---|---|
|Visa| 4077 0411 1111 1112|
|MasterCard| 5116 5611 1111 1119|
|American Express| 3411 3411 3411 347|
|Discover| 3645 8811 1111 19|

####信用卡（爱尔兰)

|卡类型|卡号|
|---|---|
|Visa| 4142 6211 1111 1112|
|MasterCard| 5473 5511 1111 1117|
|American Express| 3411 3411 3411 347|
|Discover| 3645 8811 1111 19|

####信用卡（全球)

|卡类型|卡号|
|---|---|
|Visa| 4142 6211 1111 1112|
|MasterCard| 5473 5511 1111 1117|
|American Express| 3411 3411 3411 347|
|Discover| 3641 3711 1111 15|



