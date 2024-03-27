package com.autowebpay.payment.demos.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class PayOrderNotifyDTO implements Serializable {
    
    private static final long serialVersionUID = -1099393515492408379L;
    /**
     * 商户订单id（关联订单号）必须32位
     */
    private String orderNo;

    /**
     * 商户订单id（关联订单号）
     */
    private String merchantOrderNo;

    /**
     * 订单交易金额 必须大于0
     */
    private Long payAmount;


    /**
     * 支付渠道类型
     */
    private Integer payChannelType;

    /**
     * 货币类型
     */
    private String currencyType;

    /**
     * 扩展数据
     */
    private String extendData;

    /**
     * 平台订单id
     */
    private String tranNo;

    /**
     * 支付状态 0-INIT 1-SUCCESS 2-FAILED 3-CANCELLED 4-ERROR
     */
    private Integer status;

    /**
     * 交易国家
     */
    private String countryCode;

    /**
     * 卡类型
     */
    private String cardType;

    /**
     * 卡银行
     */
    private String cardBank;

    /**
     * 支付卡号
     */
    private String cardNo;

    /**
     * 付款人名称
     */
    private String payName;

    /**
     * 交易成功时间
     */
    private Date transactionDate;

    /**
     * 支付渠道返回的错误信息
     */
    private String failureDesc;
}
