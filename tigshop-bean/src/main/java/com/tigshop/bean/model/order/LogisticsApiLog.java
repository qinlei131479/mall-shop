package com.tigshop.bean.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Tigshop
 */
@TableName(value = "logistics_api_log")
@Data
public class LogisticsApiLog implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 商城订单号
     */
    private Integer orderId;

    /**
     * 快递订单编号
     */
    private String orderCode;

    /**
     * 物流单号
     */
    private String logisticCode;

    /**
     * 发起时间
     */
    private Integer addTime;

    /**
     * 返回模板
     */
    private String printTemplate;

}