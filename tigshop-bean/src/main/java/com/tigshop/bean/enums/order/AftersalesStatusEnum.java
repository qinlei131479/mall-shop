// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.order;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;

import java.util.List;

import static com.tigshop.common.constant.ExceptionConstants.DATA_TYPE_ERROR;

/**
 * @author Tigshop
 */
@Getter
public enum AftersalesStatusEnum {
    /**
     * 审核处理中
     */
    IN_REVIEW(1, "审核处理中"),
    /**
     * 审核通过
     */
    APPROVED_FOR_PROCESSING(2, "审核通过"),
    /**
     * 审核未通过
     */
    REFUSE(3, "审核未通过"),
    /**
     * 待用户回寄
     */
    SEND_BACK(4, "待用户回寄"),
    /**
     * 待商家收货
     */
    RETURNED(5, "待商家收货"),
    /**
     * 已完成
     */
    COMPLETE(6, "已完成"),
    /**
     * 已取消
     */
    CANCEL(7, "已取消"),
    /**
     * 待供应商审核
     */
    WAIT_FOR_SUPPLIER_AUDIT(21, "待供应商审核"),
    /**
     * 待供应商收货
     */
    WAIT_FOR_SUPPLIER_RECEIPT(22, "待供应商收货");

    private final int code;
    private final String description;

    AftersalesStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 判断一个值是否为有效的 code
     *
     * @param code 状态码
     * @return 如果 code 有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (AftersalesStatusEnum status : values()) {
            if (status.code == code) {
                return true;
            }
        }
        return false;
    }

    public static String getStatusName(Integer status) {
        for (AftersalesStatusEnum aftersalesStatusEnum : values()) {
            if (aftersalesStatusEnum.code == status) {
                return aftersalesStatusEnum.getDescription();
            }
        }
        return "";
    }

    public static AftersalesStatusEnum getStatus(Integer status) {
        for (AftersalesStatusEnum aftersalesStatusEnum : values()) {
            if (aftersalesStatusEnum.code == status) {
                return aftersalesStatusEnum;
            }
        }
        throw new GlobalException(DATA_TYPE_ERROR);
    }

    /**
     * 代表该售后有效的状态
     *
     * @return
     */
    public static List<Integer> validStatus() {
        return List.of(
                IN_REVIEW.getCode(),
                APPROVED_FOR_PROCESSING.getCode(),
                REFUSE.getCode(),
                SEND_BACK.getCode(),
                RETURNED.getCode(),
                COMPLETE.getCode());
    }

    /**
     * 去除 已完成 和 取消
     *
     * @return
     */
    public static List<Integer> noContainCompleteAndCancelStatus() {
        return List.of(
                IN_REVIEW.getCode(),
                APPROVED_FOR_PROCESSING.getCode(),
                REFUSE.getCode(),
                SEND_BACK.getCode(),
                RETURNED.getCode());
    }
}
