package com.tigshop.bean.model.decorate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 装修页面管理model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("decorate")
@Schema(description = "页面管理")
public class Decorate {

    @TableId(value = "decorate_id", type = IdType.AUTO)
    @Schema(description = "页面管理ID")
    private Integer decorateId;

    @Schema(description ="模板名称")
    private String decorateTitle;

    @Schema(description ="已发布的页面模块数据")
    private String data;

    @Schema(description ="草稿页面模块数据")
    private String draftData;

    @Schema(description ="页面类型 1:h5 2:PC")
    private Integer decorateType;

    @Schema(description ="是否是首页")
    private Integer isHome;

    @Schema(description ="所属店铺")
    private Integer shopId;

    @Schema(description ="状态 1:发布 0:未发布")
    private Integer status;

    @Schema(description ="更新时间")
    private Long updateTime;

    @Schema(description = "父级id")
    private Integer parentId;

    @Schema(description = "语言ID")
    private Integer localeId;
}
