package com.tigshop.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * 飞鹅云打印机API工具类
 *
 * @author Tigshop团队
 */
@Slf4j
public class FeieyunApiUtils {

    /**
     * 生成签名
     *
     * @param user  飞鹅云后台注册的账号名
     * @param ukey  飞鹅云后台注册账号后生成的UKEY
     * @param stime 当前UNIX时间戳，10位，精确到秒
     * @return 签名
     */
    public static String generateSign(String user, String ukey, String stime) {
        try {
            String signStr = user + ukey + stime;
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(signStr.getBytes());
            byte[] digest = md.digest();
            StringBuilder hexStr = new StringBuilder();
            for (byte b : digest) {
                String shaHex = Integer.toHexString(b & 0xFF);
                if (shaHex.length() < 2) {
                    hexStr.append(0);
                }
                hexStr.append(shaHex);
            }
            return hexStr.toString();
        } catch (Exception e) {
            log.error("生成飞鹅云API签名失败", e);
            return "";
        }
    }

    /**
     * 获取当前UNIX时间戳，10位，精确到秒
     *
     * @return 时间戳字符串
     */
    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 格式化对齐商品价格信息
     *
     * @param price    单价
     * @param quantity 数量
     * @param subtotal 小计
     * @return 格式化后的字符串，字段间通过空格对齐
     */
    public static String formatLine(String price, int quantity, String subtotal) {
        // 定义每个字段的宽度（以字符数为单位）
        int priceWidth = 10;
        int quantityWidth = 10;
        int subtotalWidth = 10;

        // 格式化为字符串，右对齐
        String priceStr = String.format("%-" + priceWidth + "s", price);
        String quantityStr = centerString(String.valueOf(quantity), quantityWidth);
        String subtotalStr = String.format("%" + subtotalWidth + "s", subtotal);

        return priceStr + quantityStr + subtotalStr;
    }

    /**
     * 格式化对齐（支持汉字宽度）
     *
     * @param left  左侧字段（左对齐）
     * @param right 右侧字段（右对齐）
     * @return 对齐后的字符串
     */
    public static String formatLine(String left, String right) {
        boolean contains = right.contains("<BOLD>");
        if (contains) {
            right = right.replace("<BOLD>", "");
        }
        int leftDisplayWidth = 15;
        int rightDisplayWidth = 15;

        String leftStr = padRightConsideringChinese(left, leftDisplayWidth);
        String rightStr = padLeftConsideringChinese(right, rightDisplayWidth);

        return leftStr + (contains ? "<BOLD>" : "") + rightStr;
    }

    /**
     * 格式化对齐（支持汉字宽度）多行
     *
     * @param left  左侧字段（左对齐）
     * @param right 右侧字段（右侧长文本）
     * @return 对齐后的字符串（带<BR>换行）
     */
    public static String formatMultpartLine(String left, String right) {
        int leftWidth = getDisplayWidth(left);
        int availableRightWidth = 28 - leftWidth;

        List<String> rightLines = splitByDisplayWidth(right, availableRightWidth);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < rightLines.size(); i++) {
            if (i == 0) {
                result.append(left).append(rightLines.get(i));
            } else {
                result.append("<BR>").append(" ".repeat(leftWidth)).append(rightLines.get(i));
            }
        }
        return result.toString();
    }

    /**
     * 将字符串按显示宽度拆分成多行
     */
    private static List<String> splitByDisplayWidth(String str, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        int currentWidth = 0;

        for (char c : str.toCharArray()) {
            int charWidth = isChinese(c) ? 2 : 1;
            if (currentWidth + charWidth > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder().append(c);
                currentWidth = charWidth;
            } else {
                line.append(c);
                currentWidth += charWidth;
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    /**
     * 计算字符串显示宽度：英文=1，汉字=2
     */
    private static int getDisplayWidth(String str) {
        int width = 0;
        for (char c : str.toCharArray()) {
            width += isChinese(c) ? 2 : 1;
        }
        return width;
    }

    /**
     * 判断字符是否为中文
     */
    private static boolean isChinese(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    /**
     * 右填充空格（左对齐）
     */
    private static String padRightConsideringChinese(String str, int targetDisplayWidth) {
        int currentWidth = getDisplayWidth(str);
        int paddingSpaces = targetDisplayWidth - currentWidth;
        return str + " ".repeat(Math.max(0, paddingSpaces));
    }

    /**
     * 左填充空格（右对齐）
     */
    private static String padLeftConsideringChinese(String str, int targetDisplayWidth) {
        int currentWidth = getDisplayWidth(str);
        int paddingSpaces = targetDisplayWidth - currentWidth;
        return " ".repeat(Math.max(0, paddingSpaces)) + str;
    }

    public static String centerString(String text, int width) {
        if (text == null) text = "";
        int textLength = text.length();
        if (textLength >= width) return text;

        int padding = width - textLength;
        int paddingLeft = padding / 2;
        int paddingRight = padding - paddingLeft;

        return " ".repeat(paddingLeft) + text + " ".repeat(paddingRight);
    }
}