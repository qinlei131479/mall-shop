// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tigshop.common.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.tigshop.common.constant.Constants.*;
import static com.tigshop.common.constant.ExceptionConstants.SYSTEM_ERROR;

/**
 * 头部工具
 *
 * @author Tigshop团队
 * @create 2025年01月07日 13:32
 */
public class HeaderUtils {

    /**
     * 获取请求头中的值
     *
     * @param key 键名
     * @return Integer
     */
    public static String getHeaderValue(String key) {
        // 获取当前请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new GlobalException(SYSTEM_ERROR);
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取请求头中的值
        String header = request.getHeader(key);
        if (StrUtil.isEmpty(header)) {
            return null;
        }
        return header;
    }

    /**
     * 获取属性值
     *
     * @param key
     * @return
     */
    public static String getAttrValue(String key) {
        // 获取当前请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new GlobalException(SYSTEM_ERROR);
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取请求头中的值
        Object header = request.getAttribute(key);
        if (header != null) {
            return String.valueOf(header);
        }
        return null;
    }

    /**
     * 设置属性
     */
    public static void setAttrValue(String key, String value) {
        // 获取当前请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new GlobalException(SYSTEM_ERROR);
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        request.setAttribute(key, value);
    }

    /**
     * 获取账号类型
     *
     * @return Integer
     */
    public static String getAdminType() {
        String adminType = getHeaderValue(X_ADMIN_TYPE);
        if (ObjectUtil.equals(adminType, "shop")) {
            if (ObjectUtil.equals(getAttrValue(X_SHOP_TYPE), "2")) {
                return "store";
            }
            if (ObjectUtil.equals(getAttrValue(X_SHOP_TYPE), "3")) {
                return "pickup";
            }
        }
        return StrUtil.isEmpty(adminType) ? null : adminType;
    }

    /**
     * 获取店铺ID
     *
     * @return Integer
     */
    public static Integer getShopId() {
        String adminType = getHeaderValue(X_ADMIN_TYPE);
        if (ObjectUtil.equals(adminType, "admin")) {
            return 0;
        }
        if (ObjectUtil.equals(adminType, "shop")) {
            String shopId = getHeaderValue(X_SHOP_ID);
            return StrUtil.isEmpty(shopId) ? null : Integer.parseInt(shopId);
        }
        return null;
    }

    /**
     * 获取供应商ID
     *
     * @return Integer
     */
    public static Integer getVendorId() {
        if (ObjectUtil.equals(getAdminType(), "vendor")) {
            String vendorId = getHeaderValue(X_VENDOR_ID);
            return StrUtil.isEmpty(vendorId) ? null : Integer.parseInt(vendorId);
        } else {
            return null;
        }
    }

    /**
     * 获取客户端类型
     *
     * @return String
     */
    public static String getClientType() {
        String clientType = getHeaderValue("X-Client-Type");
        switch (clientType) {
            // PC端
            case "pc" -> {
                return "pc";
            }
            // 公众号
            case "wechat" -> {
                return "wechat";
            }
            // h5
            case "h5" -> {
                return "h5";
            }
            // 小程序
            case "miniProgram" -> {
                return "miniProgram";
            }
            // 安卓
            case "android" -> {
                return "android";
            }
            // ios
            case "ios" -> {
                return "ios";
            }
            case "app" -> {
                return "app";
            }
            case null -> {
            }
            default -> {
                return "";
            }
        }
        return "";
    }
}
