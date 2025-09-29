// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.security.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tigshop.common.exception.GlobalException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static com.tigshop.common.constant.ExceptionConstants.PARAM_ERROR;

/**
 * jwt工具类
 * @author Tigshop团队
 * @create 2024年11月21日 14:30
 */
@Component
@Slf4j
public class JwtUtil {
    private static final String SECRET = "lyecs@2023";
    private static final long EXPIRE = 60 * 24 * 30;
    public static final String HEADER = "Authorization";

    /**
     * 生成jwt token
     * @param userId 用户id，可能是前台也有可能是后台
     * @param isAdmin true是后台，false是前台
     * @param username 用户名
     * @return String
     */
    @SneakyThrows
    public String generateToken(Integer userId, boolean isAdmin, String username) {
        // 使用 SHA-512 生成哈希值
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] keyBytes = digest.digest(SECRET.getBytes());
        SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);
        //过期时间
        LocalDateTime tokenExpirationTime = LocalDateTime.now().plusMinutes(EXPIRE);
        return Jwts.builder()
                .signWith(signingKey, Jwts.SIG.HS512)
                .header().add("typ", "JWT").and()
                .issuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .issuer("tigshop")
                .subject(username)
                .expiration(Timestamp.valueOf(tokenExpirationTime))
                .claims(
                        Map.of(
                                "username", username,
                                "userId", userId,
                                    "isAdmin", isAdmin
                        )
                )
                .compact();
    }

    public Claims getClaimsByToken(String token) {
        try {
            // 使用 SHA-512 生成哈希值
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] keyBytes = digest.digest(SECRET.getBytes());
            SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e){
            throw new GlobalException(PARAM_ERROR);
        }

    }

    /**
     * 检查token是否过期
     *
     * @return true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /**
     * 获得token中的自定义信息,一般是获取token的username，无需secret解密也能获得
     * @param token jwt token
     * @param filed 字段
     * @return String
     */
    public String getClaimFiled(String token, String filed){
        try{
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(filed).asString();
        } catch (JWTDecodeException e){
            log.error("JwtUtil getClaimFiled error: ", e);
            return null;
        }
    }
}