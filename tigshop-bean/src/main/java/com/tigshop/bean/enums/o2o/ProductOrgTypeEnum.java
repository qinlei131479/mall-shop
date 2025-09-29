package com.tigshop.bean.enums.o2o;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品组织类型枚举
 *
 * @author kidd
 * @since 2025/8/21 14:54
 */
@Getter
@AllArgsConstructor
public enum ProductOrgTypeEnum {

    STORE(1, "门店"),
    REGION(2, "区域"),
    ;

    private final Integer code;
    private final String desc;
}
