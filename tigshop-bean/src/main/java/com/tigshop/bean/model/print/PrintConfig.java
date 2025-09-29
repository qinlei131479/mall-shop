package com.tigshop.bean.model.print;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 打印机配置表
 * @TableName print_config
 */
@TableName(value ="print_config")
@Data
public class PrintConfig {
    /**
     * 自增ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 打印机ID
     */
    private Integer printId;

    /**
     * 打印模版JSON
     */
    private String template;

    /**
     * 类型 1购物小票 
     */
    private Integer type;

    /**
     * 创建时间
     */
    private Long addTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}