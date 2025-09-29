package com.tigshop.bean.vo.user;

import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tigshop.common.config.TimestampToDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户详情VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户详情VO")
public class UserClientDetailVO {
    @Schema(description = "脱敏用户名")
    private String dimUsername;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "成长值")
    private Integer growthPoints;

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "生日")
    private String birthday;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "等级ID")
    private Integer rankId;

    @Schema(description = "微信头像")
    private String wechatImg;

    @Schema(description = "是否企业认证")
    private Integer isCompanyAuth;

    @Schema(description = "等级名称")
    private String rankName;

    @Schema(description = "最小成长积分")
    private BigDecimal minGrowthPoints;

    @Schema(description = "等级图标")
    private String rankIco;

    @Schema(description = "折扣")
    private BigDecimal discount;

    @Schema(description = "等级类型")
    private Integer rankType;

    @Schema(description = "等级背景")
    private String rankBg;

    @Schema(description = "等级积分")
    private String rankPoint;

    @Schema(description = "免邮")
    private Integer freeShipping;

    @Schema(description = "权益")
    private JSONArray rights;

    @Schema(description = "等级级别")
    private Integer rankLevel;

    @Schema(description = "会员图标")
    private String rankLogo;

    @Schema(description = "总余额")
    private BigDecimal totalBalance;

    @Schema(description = "优惠券数量")
    private Long coupon;

    @JsonSerialize(using = TimestampToDateSerializer.class)
    @Schema(description = "rank_expire_time")
    private String rankExpireTime;

    @Schema(description = "growth")
    private Integer growth;

    @Schema(description = "等级配置")
    private RankConfig rankConfig;

    @Schema(description = "是否绑定微信")
    private Boolean isBindWechat;

    @Schema(description = "销售员信息")
    private Salesman salesman;

    @Schema(description = "是否显示签到")
    private Integer showSign;

    @Schema(description = "是否有店铺")
    private Boolean hasShop;

    @Data
    @Schema(description = "等级配置类")
    public static class RankConfig {
        @Schema(description = "ID")
        private Integer id;

        @Schema(description = "代码")
        private String code;

        @Schema(description = "等级类型")
        private Integer rankType;

        @Schema(description = "数据")
        private JSONObject data;
    }

    @Data
    @Schema(description = "销售员类")
    public static class Salesman {
        @Schema(description = "销售员ID")
        private Integer salesmanId;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "等级")
        private Integer level;

        @Schema(description = "组ID")
        private Integer groupId;

        @Schema(description = "父ID")
        private Integer pid;

        @Schema(description = "添加时间")
        private String addTime;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "销售金额")
        private BigDecimal saleAmount;
    }
}
