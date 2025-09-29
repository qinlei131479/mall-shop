package com.tigshop.bean.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发送用户类型
 *
 * @author kidd
 * @since 2025/3/25 13:40
 */
@Getter
@AllArgsConstructor
public enum SendUserTypeEnum {

    ALL(0, "全部"),
    SINGLE(1, "单个"),
    RANK(2, "等级"),
    PART(3, "部分"),
    ;

    private final Integer code;

    private final String description;
}
