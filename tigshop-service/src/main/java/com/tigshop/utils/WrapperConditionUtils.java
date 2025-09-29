package com.tigshop.utils;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.product.Product;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 *
 * @author kidd
 * @since 2025/8/25 14:38
 */
public class WrapperConditionUtils {

    private static final Pattern CONDITION_PATTERN =
            Pattern.compile("(\\w+)\\.(\\w+)\\s*(IN|NOT IN)\\s*\\(([^)]*)\\)|" +
                    "(\\w+)\\.(\\w+)\\s*(=|LIKE)\\s*\\?");

    // StoreProduct 可用字段白名单
    private static final Set<String> STORE_PRODUCT_COLUMNS = Set.of(
            "shop_id", "product_status", "product_name", "is_delete", "category_id", "shop_category_id",
            "is_best", "is_hot", "is_new", "brand_id", "check_status", "product_id"
    );

    public static <A> void copyConditionsWithOr(MPJLambdaWrapper<A> wrapper) {
        wrapper.leftJoin(StoreProduct.class, "sp3", StoreProduct::getProductId, Product::getProductId);
        String sqlSegment = wrapper.getTargetSql();
        Map<String, Object> params = wrapper.getParamNameValuePairs();

        // 收集 SQL 中所有 ? 的全局位置
        List<Integer> qPosList = new ArrayList<>();
        for (int p = sqlSegment.indexOf('?'); p != -1; p = sqlSegment.indexOf('?', p + 1)) {
            qPosList.add(p);
        }

        Matcher matcher = CONDITION_PATTERN.matcher(sqlSegment);

        wrapper.or(w -> {
            Set<String> columnSet = new HashSet<>();
            while (matcher.find()) {

                // ---- 处理 IN / NOT IN (...) ----
                if (matcher.group(1) != null) {
                    String operator = matcher.group(3).trim().toUpperCase(); // IN 或 NOT IN
                    String column = matcher.group(2);
                    String inParams = matcher.group(4);

                    if (!STORE_PRODUCT_COLUMNS.contains(column)) {
                        continue; // 跳过 Product 独有字段
                    }

                    // 计算 ? 的个数并取对应值
                    int startPos = matcher.start(4);
                    int countQ = inParams.split(",").length;
                    List<Object> values = new ArrayList<>();
                    for (int k = 0; k < countQ; k++) {
                        int qPos = sqlSegment.indexOf("?", startPos);
                        int idx = qPosList.indexOf(qPos); // 0-based
                        String paramKey = "MPGENVAL" + (idx + 1);
                        values.add(params.get(paramKey));
                        startPos = qPos + 1;
                    }

                    // 根据操作符选择 in 或 notIn
                    if ("IN".equals(operator)) {
                        if (ObjectUtil.equals(column, "shop_category_id")) {
                            w.in("sp2." + column, values);
                        }else {
                            w.in("t." + column, values);
                        }
                    } else if ("NOT IN".equals(operator)) {
                        w.notIn("sp3." + column, values);
                    }

                    columnSet.add(column);
                }

                // ---- 处理 = ? / LIKE ? ----
                if (matcher.group(5) != null) {
                    String column = matcher.group(6);
                    if (column == null) {
                        continue;
                    }
                    String operator = matcher.group(7);

                    if (!STORE_PRODUCT_COLUMNS.contains(column)) {
                        continue; // 跳过 Product 独有字段
                    }

                    int qPos = sqlSegment.indexOf("?", matcher.start(7));
                    int idx = qPosList.indexOf(qPos); // 0-based
                    String paramKey = "MPGENVAL" + (idx + 1);
                    Object value = params.get(paramKey);

                    switch (column) {
                        case "shop_id" -> w.eq(StoreProduct::getShopId, value);
                        case "product_status" -> w.apply(
                                "(CASE WHEN t.product_status = 1 " +
                                        "AND (SELECT sp2.product_status " +
                                        "       FROM store_product sp2 " +
                                        "      WHERE sp2.product_id = t.product_id " +
                                        "      LIMIT 1) = 1 " +
                                        "THEN 1 ELSE 0 END) = {0}", value
                        );
                        case "product_name" -> w.like(StoreProduct::getProductName, value);
                        case "is_delete" -> w.eq(StoreProduct::getIsDelete, value);
                        case "category_id" -> w.eq(Product::getCategoryId, value);
                        case "shop_category_id" -> w.eq(StoreProduct::getShopCategoryId, value);
                        case "is_best" -> w.eq(Product::getIsBest, value);
                        case "is_hot" -> w.eq(Product::getIsHot, value);
                        case "is_new" -> w.eq(Product::getIsNew, value);
                        case "brand_id" -> w.eq(Product::getBrandId, value);
                        case "check_status" -> w.eq(Product::getCheckStatus, value);
                        default -> {
                            if ("LIKE".equalsIgnoreCase(operator)) {
                                w.like(column, value);
                            } else {
                                w.eq(column, value);
                            }
                        }
                    }
                    columnSet.add(column);
                }
            }
        });
    }


    public static <A, B> Map<SFunction<A, ?>, SFunction<B, ?>> fieldMap() {
        return new HashMap<>();
    }

}
