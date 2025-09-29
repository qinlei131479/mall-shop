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
 * 打印机详情
 *
 * @author Tigshop团队
 * @since 2025/1/15
 */
@Data
public class PrintDetailVO {

    @Schema(description = "打印机ID")
    private Integer printId;

    @Schema(description = "打印机名称")
    private String printName;

    @Schema(description = "打印机SN")
    private String printSn;

    @Schema(description = "打印机Key")
    private String printKey;

    @Schema(description = "第三方平台对接账号")
    private String thirdAccount;

    @Schema(description = "第三方平台key")
    private String thirdKey;

    @Schema(description = "打印联数")
    private Integer printNumber;

    @Schema(description = "第三方平台")
    private Integer platform;

    @Schema(description = "打印机类型")
    private String platformText;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "下单自动打印")
    private Integer autoPrint;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "更新时间")
    private String updateTime;

    public PrintDetailVO(Print print) {
        this.printId = print.getPrintId();
        this.printName = print.getPrintName();
        this.printSn = print.getPrintSn();
        this.printKey = print.getPrintKey();
        this.thirdAccount = print.getThirdAccount();
        this.thirdKey = print.getThirdKey();
        this.printNumber = print.getPrintNumber();
        this.platform = print.getPlatform();
        this.platformText = PrintPlatformEnum.getDescByCode(print.getPlatform());
        this.status = print.getStatus();
        this.autoPrint = print.getAutoPrint();
        this.statusText = PrintStatusEnum.getDescByCode(print.getStatus());
        this.shopId = print.getShopId();
        this.addTime = TigUtils.handelTime(print.getAddTime());
        this.updateTime = TigUtils.handelTime(print.getUpdateTime());
    }
}