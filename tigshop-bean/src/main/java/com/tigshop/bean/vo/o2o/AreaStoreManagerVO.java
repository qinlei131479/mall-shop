// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.o2o;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


/**
 * 区域门店管理VO
 *
 * @author Tigshop团队
 * @create 2025年01月15日
 */
@Data
@Schema(description = "区域门店管理参数")
public class AreaStoreManagerVO {

    @Schema(description = "区域门店管理ID")
    private Integer areaStoreManagerId;

    @Schema(description = "区域门店名称")
    private String areaStoreName;

    @Schema(description = "门店id集合")
    private List<Integer> shopIds;

    @Schema(description = "排序字段")
    private Integer sortOrder;

    @Schema(description = "添加时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String createTime;

    @Schema(description = "添加时间")
    @JsonSerialize(using = TimestampToDateSerializer.class)
    private String updateTime;

    @Schema(description = "绑定店铺数量")
    private Long shopCount;
}