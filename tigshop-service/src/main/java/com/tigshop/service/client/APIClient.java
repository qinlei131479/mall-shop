// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.client;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 前台接口
 *
 * @author Tigshop团队
 * @create 2025年03月11日 13:27
 */
@Service
public class APIClient {
    @Value("${project.apiUrl:}")
    private String apiUrl;

    public String sendMessage(String targetUserId, String message) {
        String pcDomain = "";
        if (StrUtil.isBlank(apiUrl)) {
            pcDomain = "http://127.0.0.1:8181";
        } else {
            pcDomain = apiUrl;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("targetUserId", targetUserId);
        paramMap.put("message", message);

        // 构造完整 URL
        String url = pcDomain + "/api/socket/send";

        // 发起 GET 请求
        return HttpUtil.get(url, paramMap);
    }
}