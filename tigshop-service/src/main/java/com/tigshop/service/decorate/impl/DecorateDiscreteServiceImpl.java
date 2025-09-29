package com.tigshop.service.decorate.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.tigshop.bean.bo.decorate.discrete.OpenAdvertisingBO;
import com.tigshop.bean.dto.decorate.DecorateDiscreteMenuItemDTO;
import com.tigshop.bean.enums.decorate.DecorateDiscreteTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.decorate.DecorateDiscrete;
import com.tigshop.bean.param.decorate.DecorateDiscreteUpdateParam;
import com.tigshop.bean.vo.decorate.DecorateDiscreteVO;
import com.tigshop.bean.vo.decorate.DecorateMemberDataVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.mapper.decorate.DecorateDiscreteMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateDiscreteService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 装修组件服务实现
 *
 * @author Kidd
 * @since 2025/6/30
 */
@Service
@RequiredArgsConstructor
public class DecorateDiscreteServiceImpl extends BaseServiceImpl<DecorateDiscreteMapper, DecorateDiscrete> implements DecorateDiscreteService {

    private final TigshopProperties tigshopProperties;
    private final ConfigService configService;

    @Override
    public DecorateDiscreteVO detail(String decorateSn) {
        Integer shopId = HeaderUtils.getShopId() != null ? HeaderUtils.getShopId() : Integer.valueOf(0);
        DecorateDiscrete decorateDiscrete = this.lambdaQuery()
                .eq(DecorateDiscrete::getDecorateSn, decorateSn)
                .eq(DecorateDiscrete::getShopId, shopId)
                .one();

        if (DecorateDiscreteTypeEnum.OPEN_ADVERTISING.getCode().equals(decorateSn) && decorateDiscrete == null) {
            return null;
        }

        if (decorateDiscrete == null) {
            return new DecorateDiscreteVO();
        }

        return new DecorateDiscreteVO(decorateDiscrete);
    }

    @Override
    public void update(DecorateDiscreteUpdateParam param) {
        Integer shopId = HeaderUtils.getShopId() != null ? HeaderUtils.getShopId() : Integer.valueOf(0);
        DecorateDiscrete decorateDiscrete = this.lambdaQuery()
                .eq(DecorateDiscrete::getDecorateSn, param.getDecorateSn())
                .eq(DecorateDiscrete::getShopId, shopId)
                .one();

        if (decorateDiscrete == null) {
            this.saveDecorateDiscrete(param);
        } else {
            this.editDecorateDiscrete(param, decorateDiscrete);
        }
    }

    private void editDecorateDiscrete(DecorateDiscreteUpdateParam param, DecorateDiscrete decorateDiscrete) {
        DecorateDiscrete editDecorateDiscrete = param.createEditDecorateDiscrete(decorateDiscrete);
        this.updateById(editDecorateDiscrete);
    }

    private void saveDecorateDiscrete(DecorateDiscreteUpdateParam param) {
        DecorateDiscrete decorateDiscrete = param.createSaveDecorateDiscrete();
        this.save(decorateDiscrete);
    }

    @Override
    public DecorateMemberDataVO<List<DecorateDiscreteMenuItemDTO>> getMemberDecorateData() {
        List<DecorateDiscreteMenuItemDTO> menus = new ArrayList<>();
        // 添加所有菜单项
        menus.add(new DecorateDiscreteMenuItemDTO("default", "账号管理",
                "https://oss.tigshop.com/static/user/zhanghaoguanli.png",
                "https://oss.tigshop.com/static/user/zhanghaoguanli.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "账号管理", "账号管理", "/pages/user/profile/index")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "收货地址",
                "https://oss.tigshop.com/static/user/shouhuodizhi.png",
                "https://oss.tigshop.com/static/user/shouhuodizhi.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "收货地址", "收货地址", "/pages/address/list")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "商家入驻",
                "https://oss.tigshop.com/static/user/shangjiaruzhu.png",
                "https://oss.tigshop.com/static/user/shangjiaruzhu.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "商家入驻", "商家入驻", "/pages/user/merchantEnter/principalType")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "站内消息",
                "https://oss.tigshop.com/static/user/xiaoxi.png",
                "https://oss.tigshop.com/static/user/xiaoxi.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "站内消息", "站内消息", "/pages/user/messageLog/index")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "帮助中心",
                "https://oss.tigshop.com/static/user/issue.png",
                "https://oss.tigshop.com/static/user/issue.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "帮助中心", "帮助中心", "/pages/article/issue/list")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "资讯中心",
                "https://oss.tigshop.com/static/user/news.png",
                "https://oss.tigshop.com/static/user/news.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "资讯中心", "资讯中心", "/pages/article/news/list?id=bzgg")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "分销员中心",
                "https://oss.tigshop.com/static/user/salesmanIco.png",
                "https://oss.tigshop.com/static/user/salesmanIco.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "分销员中心", "分销员中心", "/pages/salesman/index")));

        menus.add(new DecorateDiscreteMenuItemDTO("default", "积分商城",
                "https://oss.tigshop.com/static/user/jifen.png",
                "https://oss.tigshop.com/static/user/jifen.png",
                new DecorateDiscreteMenuItemDTO.PicLink("default", "积分商城", "积分商城", "/pages/exchange/list")));

        String closeAuth = configService.getConfigVal(SettingsEnum.CLOSE_AUTH);
        if (tigshopProperties.getIsB2b() == 1 && "1".equals(closeAuth)) {
            menus.add(new DecorateDiscreteMenuItemDTO("default", "实名认证",
                    "https://oss.tigshop.com/static/user/realName.png",
                    "https://oss.tigshop.com/static/user/realName.png",
                    new DecorateDiscreteMenuItemDTO.PicLink("default", "实名认证", "实名认证", "/pages/user/userCertification/index")));
        }
        if (tigshopProperties.getIsOverseas() == 0) {
            menus.add(new DecorateDiscreteMenuItemDTO("default", "发票管理",
                    "https://oss.tigshop.com/static/user/fapiao.png",
                    "https://oss.tigshop.com/static/user/fapiao.png",
                    new DecorateDiscreteMenuItemDTO.PicLink("default", "发票管理", "发票管理", "/pages/user/invoiceManagement/index")));
        }

        if (tigshopProperties.getIsO2o() == 1) {
            menus.add(new DecorateDiscreteMenuItemDTO("default", "联系人管理",
                    "https://oss.tigshop.com/img/gallery/202509/175790157950oQc07DHYyKmevrHE.jpeg",
                    "https://oss.tigshop.com/img/gallery/202509/175790157950oQc07DHYyKmevrHE.jpeg",
                    new DecorateDiscreteMenuItemDTO.PicLink("default", "联系人管理", "联系人管理", "/pages/store/contacts/list")));
        }

        return new DecorateMemberDataVO<>(menus);
    }

    @Override
    public DecorateDiscreteVO getShopHead(Integer shopId) {
        LambdaQueryWrapper<DecorateDiscrete> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DecorateDiscrete::getShopId, shopId);
        queryWrapper.eq(DecorateDiscrete::getDecorateSn, "shopHead");
        DecorateDiscrete decorateDiscrete = this.getOne(queryWrapper);
        if (decorateDiscrete == null) {
            return new DecorateDiscreteVO();
        }
        DecorateDiscreteVO decorateDiscreteVO = new DecorateDiscreteVO();
        BeanUtils.copyProperties(decorateDiscrete, decorateDiscreteVO);
        JsonNode data = JsonUtil.fromJson(decorateDiscrete.getData(), JsonNode.class);
        decorateDiscreteVO.setData(data);
        return decorateDiscreteVO;
    }

    @Override
    public OpenAdvertisingBO getOpenAdvertising() {
        DecorateDiscrete decorateDiscrete = this.lambdaQuery().eq(DecorateDiscrete::getDecorateSn, DecorateDiscreteTypeEnum.OPEN_ADVERTISING.getCode()).one();

        if (decorateDiscrete == null) {
            return new OpenAdvertisingBO();
        }

        return JSON.parseObject(decorateDiscrete.getData(), OpenAdvertisingBO.class);
    }
}