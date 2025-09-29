// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.feign;

import com.tigshop.bean.feign.wechat.*;
import com.tigshop.bean.param.user.CreateQrcodeParam;
import feign.Headers;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 微信接口
 *
 * @author Tigshop团队
 * @create 2024年12月17日 15:46
 */
@FeignClient(name = "wechat-api", url = "https://api.weixin.qq.com")
public interface WechatApiClient {
    /**
     * 删除小程序模板
     * @param accessToken 接口调用凭证
     * @param body 要删除的模板id
     * @return WechatApiResult
     */
    @PostMapping("/wxaapi/newtmpl/deltemplate")
    WechatApiResult deleteTemplate(@RequestParam("access_token") String accessToken,
                                   @RequestBody @Param("body") Map<String, String> body);

    /**
     * 添加小程序模板
     * @param accessToken 接口调用凭证
     * @param param 新增模板入参
     * @return AddTemplateResult
     */
    @PostMapping("/wxaapi/newtmpl/addtemplate")
    AddTemplateResult addTemplate(@RequestParam("access_token") String accessToken,
                                  @RequestBody AddTemplateParam param);

    /**
     * 获取小程序模板
     * @param accessToken 接口调用凭证
     * @return GetTemplateResult
     */
    @GetMapping("/wxaapi/newtmpl/gettemplate")
    GetTemplateResult getTemplate(@RequestParam("access_token") String accessToken);

    /**
     * 获取公众号所有模板
     * @param accessToken 接口调用凭证
     * @return GetAllTemplateResult
     */
    @GetMapping("/cgi-bin/template/get_all_private_template")
    GetAllTemplateResult getAllTemplate(@RequestParam("access_token") String accessToken);

    /**
     * 删除公众号模板
     * @param accessToken 接口调用凭证
     * @param body 要删除的模板id
     * @return WechatApiResult
     */
    @PostMapping("/cgi-bin/template/del_private_template")
    WechatApiResult delPrivateTemplate(@RequestParam("access_token") String accessToken,
                                      @RequestBody Map<String, String> body);

    /**
     * 发送模板消息
     * @param accessToken 接口调用凭证
     * @param body 微信公众号发送模板消息
     * @return SendTemplateMessageResult
     */
    @PostMapping("/cgi-bin/message/template/send")
    SendTemplateMessageResult sendTemplateMessage(@RequestParam("access_token") String accessToken,
                                                  @RequestBody SendTemplateMessageParam body);

    /**
     * 获取token
     * @param grantType 获取access_token填写client_credential
     * @param appid 获取access_token填写client_credential
     * @param secret 第三方用户唯一凭证密钥，即appSecret
     * @return GetTokenResult
     */
    @GetMapping("/cgi-bin/token")
    GetTokenResult getToken(@RequestParam("grant_type") String grantType,
                                       @RequestParam("appid") String appid,
                                       @RequestParam("secret") String secret);

    @PostMapping("/wxa/business/getliveinfo")
    GetLiveInfoResult getLiveInfo(@RequestParam("access_token") String accessToken,
                                  @RequestBody GetLiveInfoParam body);

    /**
     * 获取小程序码
     * @param accessToken 接口调用凭证
     * @param param 获取小程序码入参
     * @return GetWechatCodeResult
     */
    @PostMapping(value = "/wxa/getwxacodeunlimit", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Headers("Content-Type: application/json")
    ResponseEntity<byte[]> getWechatCode(@RequestParam("access_token") String accessToken,
                                         @RequestBody GetWechatCodeParam param);

    /**
     * 小程序登录
     */
    @GetMapping("/sns/jscode2session")
    String code2Session(@RequestParam("appid") String appid,
                                     @RequestParam("secret") String secret,
                                     @RequestParam("js_code") String jsCode,
                                     @RequestParam(value = "grant_type") String grantType
                                     );

    /**
     * 生成微信二维码
     */
    @GetMapping("/cgi-bin/qrcode/create")
    CreateQrcodeResult qrcodeCreate(@RequestParam("access_token") String accessToken, @RequestBody CreateQrcodeParam param);

    /**
     * 发货信息录入
     */
    @GetMapping("wxa/sec/order/upload_shipping_info")
    UploadShippingInfoResult uploadShippingInfo(@RequestParam("access_token") String accessToken, @RequestBody UploadShippingInfoParam param);

}
