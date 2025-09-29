package com.tigshop.bean.model.o2o;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 区域门店管理
 * @TableName area_store_manager
 */
@TableName(value ="area_store_manager")
@Data
public class AreaStoreManager {
    /**
     * 区域门店管理
     */
    @TableId(type = IdType.AUTO)
    private Integer areaStoreManagerId;

    /**
     * 区域门店名称 
     */
    private String areaStoreName;

    /**
     * 排序字段
     */
    private Integer sortOrder;

    /**
     * 添加时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long updateTime;
}