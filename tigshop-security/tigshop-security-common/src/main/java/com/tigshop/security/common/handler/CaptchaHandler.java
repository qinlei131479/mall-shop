// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.security.common.handler;

import cn.hutool.core.util.StrUtil;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.tigshop.common.constant.Constants.LOGIN_ERROR;
import static com.tigshop.common.constant.login.LoginConstants.ERROR_THRESHOLD;

/**
 * 行为验证
 *
 * @author Jayce
 * @create 2024年11月25日 14:17
 */
@Component
public class CaptchaHandler {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CaptchaService captchaService;

    /**
     * 验证验证码(ture=已经到底阀值)
     *
     * @param username     用户名
     * @param verifyToken 验证码
     * @return 是否验证通过
     */
    public Boolean captcha(String username, String verifyToken) {
        int count = getErrorCount(username);
        if (count > ERROR_THRESHOLD) {
            if (StrUtil.isEmpty(verifyToken) || !validateCaptcha(verifyToken)) {
                return false;
            }
            resetErrorCount(username);
        }
        return true;
    }

    /**
     * 获取错误次数
     *
     * @param username 用户名
     * @return 错误次数
     */
    public int getErrorCount(String username) {
        Object errorCountObj = redisTemplate.opsForValue().get(StrUtil.format("{}{}", LOGIN_ERROR, username));
        int count = (errorCountObj == null) ? 1 : Integer.parseInt(errorCountObj.toString()) + 1;

        redisTemplate.opsForValue().set(StrUtil.format("{}{}", LOGIN_ERROR, username), count);
        return count;
    }

    /**
     * 验证验证码是否正确
     *
     * @param verifyToken 验证码
     * @return 是否正确
     */
    public boolean validateCaptcha(String verifyToken) {
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(verifyToken);
        ResponseModel response = captchaService.verification(captchaVO);
        return response.isSuccess();
    }

    /**
     * 重置错误次数
     *
     * @param username 用户名
     */
    public void resetErrorCount(String username) {
        redisTemplate.opsForValue().set(StrUtil.format("{}{}", LOGIN_ERROR, username), 0);
    }

}
