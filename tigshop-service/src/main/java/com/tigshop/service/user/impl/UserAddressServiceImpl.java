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

package com.tigshop.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserAddressCreateDTO;
import com.tigshop.bean.dto.user.UserAddressListDTO;
import com.tigshop.bean.dto.user.UserAddressUpdateDTO;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.model.user.UserAddress;
import com.tigshop.bean.vo.user.UserAddressVO;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.user.UserAddressMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.user.UserAddressService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.tigshop.common.constant.user.UserAddressConstants.USER_ADDRESS_NOT_EXIST;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 收货人信息表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class UserAddressServiceImpl extends BaseServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {
    @Resource
    RegionService regionService;
    @Resource
    private TranslatePackage translatePackage;

    @Override
    public Page<UserAddressVO> list(UserAddressListDTO listDTO) {
        Integer userId = getCurrentUserId();
        //分页
        Page<UserAddress> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId);

        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 执行查询
        Page<UserAddress> result = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserAddress> records = result.getRecords();

        List<UserAddressVO> userAddressVOList = records.stream()
                .map(userAddress -> {
                    UserAddressVO userAddressVO = new UserAddressVO();
                    BeanUtils.copyProperties(userAddress, userAddressVO);
                    List<String> regionNames = JsonUtil.jsonToList(userAddress.getRegionNames(), String.class);
                    userAddressVO.setRegionNames(regionNames);
                    userAddressVO.setRegionIds(JsonUtil.jsonToList(userAddress.getRegionIds(), Integer.class));
                    //循环region_names
                    if (regionNames != null && !regionNames.isEmpty()) {
                        if (regionNames.size() > 1 && regionNames.get(0).equals(regionNames.get(1))) {
                            regionNames.remove(1);
                        }
                        userAddressVO.setRegionName(String.join(" ", regionNames.stream().map(regionName -> translatePackage.translate(regionName)).toList()));
                    } else {
                        userAddressVO.setRegionName("");
                    }
                    return userAddressVO;
                }).toList();

        return PageUtil.convertPage(result, userAddressVOList);
    }

    @Override
    public UserAddressVO detail(Integer id) {
        UserAddress userAddress = this.getById(id);
        if (userAddress == null) {
            throw new GlobalException(USER_ADDRESS_NOT_EXIST);
        }
        UserAddressVO userAddressVO = new UserAddressVO();
        BeanUtils.copyProperties(userAddress, userAddressVO);
        userAddressVO.setRegionNames(JsonUtil.jsonToList(userAddress.getRegionNames(), String.class));
        userAddressVO.setRegionIds(JsonUtil.jsonToList(userAddress.getRegionIds(), Integer.class));
        return userAddressVO;
    }

    @Override
    @Transactional
    public Integer create(UserAddressCreateDTO createDTO) {
        UserAddress userAddress = new UserAddress();
        if (!createDTO.getRegionIds().isEmpty()) {
            LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Region::getRegionId, createDTO.getRegionIds());
            List<Region> regions = regionService.list(queryWrapper);
            List<String> regionNames = new ArrayList<>();
            if (regions != null && !regions.isEmpty()) {
                regionNames = regions.stream().map(Region::getRegionName).toList();
            }
            userAddress.setRegionNames(JsonUtil.toJson(regionNames));
            userAddress.setRegionIds(JsonUtil.toJson(createDTO.getRegionIds()));
            userAddress.setUserId(getCurrentUserId());

        }
        userAddress.setAddress(createDTO.getAddress());
        userAddress.setConsignee(createDTO.getConsignee());
        userAddress.setEmail(createDTO.getEmail());
        userAddress.setMobile(createDTO.getMobile());
        userAddress.setMobileAreaCode(createDTO.getMobileAreaCode());
        userAddress.setTelephone(createDTO.getTelephone());
        userAddress.setIsDefault(createDTO.getIsDefault());
        // 将默认设置为0
        if (createDTO.getIsDefault() != null && createDTO.getIsDefault() == 1) {
            update(new LambdaUpdateWrapper<UserAddress>()
                    .set(UserAddress::getIsDefault, 0)
                    .eq(UserAddress::getUserId, getCurrentUserId()));
        }
        // 再添加一条记录
        this.save(userAddress);
        return userAddress.getAddressId();
    }

    @Override
    public boolean update(UserAddressUpdateDTO updateDTO) {
        UserAddress userAddress = new UserAddress();
        BeanUtils.copyProperties(updateDTO, userAddress);
        if (!updateDTO.getRegionIds().isEmpty()) {
            LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Region::getRegionId, updateDTO.getRegionIds());
            List<Region> regions = regionService.list(queryWrapper);
            List<String> regionNames = new ArrayList<>();
            if (regions != null && !regions.isEmpty()) {
                regionNames = regions.stream().map(Region::getRegionName).toList();
            }
            userAddress.setRegionNames(JsonUtil.toJson(regionNames));
            userAddress.setRegionIds(JsonUtil.toJson(updateDTO.getRegionIds()));
            userAddress.setUserId(getCurrentUserId());
        }
        userAddress.setAddress(updateDTO.getAddress());
        userAddress.setConsignee(updateDTO.getConsignee());
        userAddress.setEmail(updateDTO.getEmail());
        userAddress.setMobile(updateDTO.getMobile());
        userAddress.setMobileAreaCode(updateDTO.getMobileAreaCode());
        userAddress.setTelephone(updateDTO.getTelephone());

        if (updateDTO.getIsDefault() != null && updateDTO.getIsDefault() > 0) {
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(UserAddress::getIsDefault, 0);
            updateWrapper.eq(UserAddress::getUserId, getCurrentUserId());
            updateWrapper.ne(UserAddress::getAddressId, updateDTO.getAddressId());
            this.update(updateWrapper);
        }

        return this.updateById(userAddress);
    }

    @Override
    public void setSelected(Integer id) {
        this.update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, getCurrentUserId())
                .set(UserAddress::getIsSelected, 0));
        this.update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getAddressId, id)
                .eq(UserAddress::getUserId, getCurrentUserId())
                .set(UserAddress::getIsSelected, 1));
    }

    @Override
    public UserAddressVO getUserSelectedAddress() {
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, getCurrentUserId());
        queryWrapper.eq(UserAddress::getIsSelected, 1);
        UserAddress userAddress = this.getOne(queryWrapper);
        if (userAddress == null) {
            userAddress = this.lambdaQuery()
                    .eq(UserAddress::getUserId, getCurrentUserId())
                    .last("limit 1")
                    .orderByAsc(UserAddress::getAddressId)
                    .one();
        }
        UserAddressVO userAddressVO = new UserAddressVO();
        BeanUtils.copyProperties(userAddress, userAddressVO);
        userAddressVO.setRegionNames(JsonUtil.jsonToList(userAddress.getRegionNames(), String.class));
        userAddressVO.setRegionIds(JsonUtil.jsonToList(userAddress.getRegionIds(), Integer.class));
        return userAddressVO;
    }
}
