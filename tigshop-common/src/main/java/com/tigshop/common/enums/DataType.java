package com.tigshop.common.enums;

import lombok.Getter;

/**
 * @author Tigshop团队
 */
@Getter
public enum DataType {
    /**
     * 页面
     */
    PAGE(0, "页面"),
    /**
     * 接口
     */
    API(1, "接口"),
    /**
     * 商品
     */
    PRODUCT(2, "商品"),
    /**
     * 分类
     */
    CATEGORY(3, "分类"),
    /**
     * 品牌
     */
    BRAND(4, "品牌"),
    /**
     * 设置
     */
    SETTING(5, "设置"),
    /**
     * 文章标题
     */
    ARTICLE_TITLE(6, "文章标题"),


    /**
     * 图片多语言
     */
    IMAGE_TITLE(7, "商品图片"),


    /**
     * 规格
     */
    SKU(8, "规格"),

    /**
     * 商品描述
     */
    PRODUCT_DESC(9, "商品描述"),

    /**
     * 优惠券标题
     */
    COUPON_TITLE(10, "优惠券标题"),

    /**
     * 优惠券描述
     */
    COUPON_DESC(11, "优惠券描述"),

    /**
     * 文章内容
     */
    ARTICLE_CONTENT_DESC(12, "文章内容");

    private final int code;
    private final String description;

    DataType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据来源标签代码获取对应的描述
     *
     * @param code 来源标签代码
     * @return 对应的描述，如果代码无效则返回空字符串
     */
    public static String getName(int code) {
        for (DataType tag : values()) {
            if (tag.getCode() == code) {
                return tag.getDescription();
            }
        }
        return "";
    }

    /**
     * 判断一个值是否为有效的来源标签代码
     *
     * @param code 来源标签代码
     * @return 如果代码有效则返回 true；否则返回 false
     */
    public static boolean isValidCode(int code) {
        for (DataType tag : values()) {
            if (tag.getCode() == code) {
                return true;
            }
        }
        return false;
    }
}
