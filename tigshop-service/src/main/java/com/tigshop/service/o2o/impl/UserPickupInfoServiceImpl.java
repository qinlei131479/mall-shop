package com.tigshop.service.o2o.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.o2o.UserPickupInfo;
import com.tigshop.bean.param.o2o.UserPickupParam;
import com.tigshop.bean.query.o2o.UserPickupQuery;
import com.tigshop.bean.vo.o2o.UserPickupInfoVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.mapper.o2o.UserPickupInfoMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.o2o.UserPickupInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tigshop.common.constant.Constants.NO;
import static com.tigshop.common.constant.Constants.YES;

/**
 * 用户自提信息服务实现类
 * @author Tigshop项目组
 */
@Service
@RequiredArgsConstructor
public class UserPickupInfoServiceImpl extends BaseServiceImpl<UserPickupInfoMapper, UserPickupInfo> implements UserPickupInfoService {

    private final TranslatePackage translatePackage;

    @Override
    public Page<UserPickupInfoVO> getUserPickups(UserPickupQuery query) {
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        query.setSortField("is_selected");

        Page<UserPickupInfo> page = this.buildSortOrder(query);

        LambdaQueryWrapper<UserPickupInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPickupInfo::getUserId, currentUserId);

        Page<UserPickupInfo> result = this.page(page, queryWrapper);

        List<UserPickupInfoVO> userPickupInfos = result.getRecords().stream().map(userPickupInfo -> {
            UserPickupInfoVO userPickupInfoVO = new UserPickupInfoVO();
            BeanUtils.copyProperties(userPickupInfo, userPickupInfoVO);
            return userPickupInfoVO;
        }).toList();

        return PageUtil.convertPage(page, userPickupInfos);
    }

    @Override
    public void create(UserPickupParam param) {
        if (ObjectUtil.isEmpty(param.getConsignee())) {
            throw new GlobalException("收货人姓名不能为空");
        }
        if (ObjectUtil.isEmpty(param.getMobile())) {
            throw new GlobalException("收货人手机号不能为空");
        }
        UserPickupInfo userPickupInfo = UserPickupInfo.builder()
                .userId(SecurityUtils.getCurrentUserId())
                .consignee(param.getConsignee())
                .mobile(param.getMobile())
                .mobileAreaCode(param.getMobileAreaCode())
                .isDefault(param.getIsDefault())
                .isSelected(param.getIsSelected())
                .build();

        this.save(userPickupInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserPickupParam param) {
        Integer userPickupId = param.getUserPickupId();
        Assert.notNull(userPickupId, () -> new GlobalException(translatePackage.translate("用户自提信息ID不能为空")));

        UserPickupInfo userPickupInfo = this.getById(userPickupId);
        Assert.notNull(userPickupInfo, () -> new GlobalException(translatePackage.translate("用户自提信息不存在")));

        String consignee = param.getConsignee();
        if (StrUtil.isNotBlank(consignee)) {
            userPickupInfo.setConsignee(consignee);
        }

        String mobile = param.getMobile();
        if (StrUtil.isNotBlank(mobile)) {
            userPickupInfo.setMobile(mobile);
        }

        String mobileAreaCode = param.getMobileAreaCode();
        if (StrUtil.isNotBlank(mobileAreaCode)) {
            userPickupInfo.setMobileAreaCode(mobileAreaCode);
        }

        Integer isDefault = param.getIsDefault();
        if (isDefault != null && isDefault > 0) {
            // 先取消该用户其他自提点的选中状态
            lambdaUpdate()
                    .eq(UserPickupInfo::getUserId, userPickupInfo.getUserId())
                    .ne(UserPickupInfo::getUserPickupId, userPickupId)
                    .set(UserPickupInfo::getIsDefault, NO)
                    .update();

            // 设置当前为选中
            userPickupInfo.setIsDefault(YES);
        }

        Integer isSelected = param.getIsSelected();
        if (isSelected != null && isSelected > 0) {
            // 先取消该用户其他自提点的选中状态
            lambdaUpdate()
                    .eq(UserPickupInfo::getUserId, userPickupInfo.getUserId())
                    .ne(UserPickupInfo::getUserPickupId, userPickupId)
                    .set(UserPickupInfo::getIsSelected, NO)
                    .update();

            // 设置当前为选中
            userPickupInfo.setIsSelected(YES);
        }

        this.updateById(userPickupInfo);
    }

    @Override
    public UserPickupInfoVO detail(Integer userPickupId) {
        UserPickupInfo userPickupInfo = this.getById(userPickupId);
        UserPickupInfoVO userPickupInfoVO = new UserPickupInfoVO();
        BeanUtils.copyProperties(userPickupInfo, userPickupInfoVO);
        return userPickupInfoVO;
    }
}
