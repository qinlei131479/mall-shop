package com.tigshop.bean.enums.o2o;

import lombok.Getter;

/**
 * 管理员消息分类
 *
 * @author tigshop
 * @create 2024-12-25
 */
@Getter
public enum TipStatusEnum {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用"),;

    private final int code;
    private final String description;

    TipStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

}
