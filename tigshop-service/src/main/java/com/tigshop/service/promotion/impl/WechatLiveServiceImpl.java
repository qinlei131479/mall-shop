// **---------------------------------------------------------------------+
// ** 文件 --
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

package com.tigshop.service.promotion.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.WechatLiveListDTO;
import com.tigshop.bean.dto.promotion.WechatLiveUpdateDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.GetLiveInfoParam;
import com.tigshop.bean.feign.wechat.GetLiveInfoResult;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.model.promotion.WechatLive;
import com.tigshop.bean.vo.promotion.WechatLiveVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.mapper.promotion.WechatLiveMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.promotion.WechatLiveService;
import com.tigshop.service.setting.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.tigshop.common.constant.promotion.WechatLiveConstants.*;

/**
 * 微信直播服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class WechatLiveServiceImpl extends BaseServiceImpl<WechatLiveMapper, WechatLive> implements WechatLiveService {

    @Resource
    private ConfigService configService;

    @Resource
    private WechatApiClient wechatApiClient;

    @Override
    public Page<WechatLiveVO> list(WechatLiveListDTO listDTO) {
        // 分页
        Page<WechatLive> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<WechatLive> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(WechatLive::getWechatLiveTitle, keyword);
        }

        Integer shopId = getShopId();
        if (shopId != null && shopId > 0) {
            queryWrapper.eq(WechatLive::getShopId, shopId);
        }

        // 执行查询
        Page<WechatLive> wechatLivePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<WechatLive> wechatLivePageRecords = wechatLivePage.getRecords();
        // 转换为VO
        List<WechatLiveVO> wechatLiveVOList = wechatLivePageRecords.stream()
                .map(wechatLive -> {
                    WechatLiveVO wechatLiveVO = new WechatLiveVO();
                    BeanUtils.copyProperties(wechatLive, wechatLiveVO);
                    // 处理其他字段的转换
                    convert(wechatLive, wechatLiveVO);
                    return wechatLiveVO;
                }).toList();
        return PageUtil.convertPage(wechatLivePage, wechatLiveVOList);
    }

    @Override
    public WechatLiveVO detail(Integer id) {
        if (id != null) {
            WechatLive wechatLive = this.getById(id);
            WechatLiveVO wechatLiveVO = new WechatLiveVO();
            BeanUtils.copyProperties(wechatLive, wechatLiveVO);
            // 处理格式转换
            convert(wechatLive, wechatLiveVO);
            return wechatLiveVO;
        }
        return null;
    }

    /**
     * 格式转换
     *
     * @param wechatLive   WechatLive
     * @param wechatLiveVO WechatLiveVO
     */
    public void convert(WechatLive wechatLive, WechatLiveVO wechatLiveVO) {
        // 处理其他字段的转换
        DateTime startDate = DateUtil.date(wechatLive.getStartTime() * 1000);
        wechatLiveVO.setStartTime(DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss"));

        DateTime endDate = DateUtil.date(wechatLive.getEndTime() * 1000);
        wechatLiveVO.setEndTime(DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));

        DateTime lastDate = DateUtil.date(wechatLive.getLastUpdateTime() * 1000);
        wechatLiveVO.setLastUpdateTime(DateUtil.format(lastDate, "yyyy-MM-dd HH:mm:ss"));

        wechatLiveVO.setActRangeExt(StringUtils.str2IntList(wechatLive.getActRangeExt()));
        wechatLiveVO.setProductData(StringUtils.str2IntList(wechatLive.getProductData()));
    }

    public Long getCount(Integer id) {
        return this.count(new LambdaQueryWrapper<WechatLive>()
                .eq(WechatLive::getWechatLiveId, id)
                .select(WechatLive::getWechatLiveId));
    }

    @Override
    public boolean update(WechatLiveUpdateDTO updateDTO) {
        if (updateDTO != null) {
            WechatLive wechatLive = new WechatLive();
            Long count = getCount(updateDTO.getWechatLiveId());
            if (count == 0) {
                throw new GlobalException(WECHAT_LIVE_NOT_EXISTS);
            }
            // 集合转换为json
            wechatLive.setActRangeExt(updateDTO.getActRangeExt().toString());
            BeanUtils.copyProperties(updateDTO, wechatLive);
            return this.updateById(wechatLive);
        }
        return false;
    }

    @Override
    public boolean refresh() {
        // 获取配置
        String wechatMiniProgramAppId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode()).getBizVal();
        String wechatMiniProgramSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode()).getBizVal();

        //获取access_toke
        GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", wechatMiniProgramAppId,
                wechatMiniProgramSecret);

        if (tokenResult.getAccessToken() == null) {
            throw new GlobalException(WECHAT_LIVE_ACCESS_TOKEN_FAIL);
        }

        //查询直播
        GetLiveInfoParam param = new GetLiveInfoParam();
        param.setStart(0);
        param.setLimit(10);
        GetLiveInfoResult result = wechatApiClient.getLiveInfo(tokenResult.getAccessToken(), param);
        if (result == null || ObjectUtil.notEqual(result.getErrcode(), 0)) {
            throw new GlobalException(WECHAT_LIVE_GET_LIVE_INFO_FAIL);
        }

        //循环返回结果的room_info
        for (GetLiveInfoResult.RoomInfo roomInfo : result.getRoomInfoList()) {
            WechatLive wechatLive = new WechatLive();
            wechatLive.setWechatLiveId(roomInfo.getRoomId());
            wechatLive.setWechatLiveTitle(roomInfo.getName());
            wechatLive.setCoverImg(roomInfo.getCoverImg());
            wechatLive.setStartTime(roomInfo.getStartTime());
            wechatLive.setEndTime(roomInfo.getEndTime());
            wechatLive.setAnchorName(roomInfo.getAnchorName());
            //wechatLive.setAnchorImg(roomInfo.getAnchorImg());
            wechatLive.setLiveStatus(roomInfo.getStatus());
            wechatLive.setShareImg(roomInfo.getShareImg());
            wechatLive.setProductData(JsonUtil.toJson(roomInfo.getGoods()));

            //查询是否有room_id的信息，有就更新，没有就插入
            WechatLive wechatLive1 = this.getOne(new LambdaQueryWrapper<WechatLive>()
                    .eq(WechatLive::getRoomId, roomInfo.getRoomId()));
            wechatLive.setLastUpdateTime(StringUtils.getCurrentTime());
            if (wechatLive1 != null) {
                this.updateById(wechatLive);
            } else {
                this.save(wechatLive);
            }
        }
        return true;
    }
}
