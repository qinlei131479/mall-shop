// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.common.enums;


import lombok.Getter;

/**
 * @author Tigshop团队
 * @create 2025/4/9 10:53
 */
@Getter
public enum KuaiDiNiaoTypes {

    KDNIAO_JSCX("8001", "即时查询"),
    ;

    private final String code;
    private final String description;

    KuaiDiNiaoTypes(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
