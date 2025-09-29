// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.print;

import com.tigshop.bean.enums.print.PrintPlatformEnum;
import com.tigshop.bean.enums.print.PrintStatusEnum;
import com.tigshop.bean.model.print.Print;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 打印机列表
 *
 * @author Tigshop团队
 * @since 2025/1/15
 */
@Data
public class PrintPageVO {

    @Schema(description = "打印机ID")
    private Integer printId;

    @Schema(description = "打印机名称")
    private String printName;

    @Schema(description = "打印机SN")
    private String printSn;

    @Schema(description = "打印机类型")
    private String platformText;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description = "添加时间")
    private String addTime;

    public PrintPageVO(Print print) {
        this.printId = print.getPrintId();
        this.printName = print.getPrintName();
        this.printSn = print.getPrintSn();
        this.status = print.getStatus();
        this.statusText = PrintStatusEnum.getDescByCode(print.getStatus());
        this.platformText = PrintPlatformEnum.getDescByCode(print.getPlatform());
        this.updateTime = TigUtils.handelTime(print.getUpdateTime());
        this.addTime = TigUtils.handelTime(print.getAddTime());
    }
}