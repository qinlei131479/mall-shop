package com.tigshop.service.o2o.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.shop.StoreExtendedDto;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.model.o2o.AreaStoreManager;
import com.tigshop.bean.model.o2o.AreaStoreManagerShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.param.o2o.*;
import com.tigshop.bean.vo.o2o.AreaStoreManagerVO;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.o2o.AreaStoreManagerMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.o2o.AreaStoreManagerService;
import com.tigshop.service.o2o.AreaStoreManagerShopService;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.shop.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Admin
 * @description 针对表【area_store_manager(区域门店管理)】的数据库操作Service实现
 * @createDate 2025-08-11 17:24:00
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AreaStoreManagerServiceImpl extends BaseServiceImpl<AreaStoreManagerMapper, AreaStoreManager>
        implements AreaStoreManagerService {

    private final ShopService shopService;
    private final AreaStoreManagerShopService areaStoreManagerShopService;
    private final TigshopProperties tigshopProperties;
    private final RegionService regionService;

    @Override
    public Page<AreaStoreManagerVO> list(AreaStoreManagerQueryPage listDTO) {
        // 分页
        Page<AreaStoreManager> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        MPJLambdaWrapper<AreaStoreManager> queryWrapper = new MPJLambdaWrapper<>();
        queryWrapper
                .selectAll(AreaStoreManager.class)
                .selectCount(AreaStoreManagerShop::getAreaStoreManagerShopId, "shopCount")
                .leftJoin(AreaStoreManagerShop.class, AreaStoreManagerShop::getAreaStoreManagerId, AreaStoreManager::getAreaStoreManagerId)
                .groupBy(AreaStoreManager::getAreaStoreManagerId);

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        queryWrapper.like(StrUtil.isNotEmpty(listDTO.getKeyword()), AreaStoreManager::getAreaStoreName, listDTO.getKeyword());

        // 执行查询
        Page<AreaStoreManager> areaPage = this.page(page, queryWrapper);

        // 转换为VO
        Page<AreaStoreManagerVO> voPage = new Page<>();
        BeanUtils.copyProperties(areaPage, voPage);

        List<AreaStoreManagerVO> voList = areaPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public AreaStoreManagerVO detail(Integer id) {
        AreaStoreManager areaStoreManager = this.getById(id);
        if (areaStoreManager == null) {
            throw new GlobalException("区域不存在");
        }
        return convertToVO(areaStoreManager);
    }

    @Override
    public void create(AreaStoreManagerCreateParam createDTO) {
        AreaStoreManager areaStoreManager = new AreaStoreManager();
        areaStoreManager.setAreaStoreName(createDTO.getAreaStoreName());
        areaStoreManager.setSortOrder(createDTO.getSortOrder());
        areaStoreManager.setCreateTime(StringUtils.getCurrentTime());
        boolean save = this.save(areaStoreManager);
        List<AreaStoreManagerShop> areaStoreManagerShopList = new ArrayList<>();
        for (Integer shopId : createDTO.getShopIds()) {
            AreaStoreManagerShop areaStoreManagerShop = new AreaStoreManagerShop();
            areaStoreManagerShop.setAreaStoreManagerId(areaStoreManager.getAreaStoreManagerId());
            areaStoreManagerShop.setShopId(shopId);
            areaStoreManagerShop.setCreateTime(StringUtils.getCurrentTime());
            areaStoreManagerShopList.add(areaStoreManagerShop);
        }
        areaStoreManagerShopService.saveBatch(areaStoreManagerShopList);
    }

    @Override
    public void update(AreaStoreManagerUpdateParam updateDTO) {
        AreaStoreManager areaStoreManager = this.getById(updateDTO.getAreaStoreManagerId());
        if (areaStoreManager == null) {
            throw new GlobalException("区域不存在");
        }
        areaStoreManager.setSortOrder(updateDTO.getSortOrder());
        areaStoreManager.setAreaStoreName(updateDTO.getAreaStoreName());
        areaStoreManager.setUpdateTime(StringUtils.getCurrentTime());
        this.updateById(areaStoreManager);
        List<AreaStoreManagerShop> areaStoreManagerShopList = new ArrayList<>();
        for (Integer shopId : updateDTO.getShopIds()) {
            if (areaStoreManagerShopService.getOne(new LambdaQueryWrapper<AreaStoreManagerShop>()
                    .eq(AreaStoreManagerShop::getAreaStoreManagerId, areaStoreManager.getAreaStoreManagerId())
                    .eq(AreaStoreManagerShop::getShopId, shopId)) != null) {
                continue;
            }
            AreaStoreManagerShop areaStoreManagerShop = new AreaStoreManagerShop();
            areaStoreManagerShop.setAreaStoreManagerId(areaStoreManager.getAreaStoreManagerId());
            areaStoreManagerShop.setShopId(shopId);
            areaStoreManagerShop.setCreateTime(StringUtils.getCurrentTime());
            areaStoreManagerShopList.add(areaStoreManagerShop);
        }
        areaStoreManagerShopService.saveBatch(areaStoreManagerShopList);
    }

    @Override
    public boolean del(Integer id) {
        AreaStoreManager areaStoreManager = this.getById(id);
        if (areaStoreManager == null) {
            throw new GlobalException("区域不存在");
        }
        return this.removeById(id);
    }

    @Override
    public void bindShop(AreaStoreBindShopParam bindShopDTO) {
        AreaStoreManager areaStoreManager = this.getById(bindShopDTO.getAreaStoreManagerId());
        if (areaStoreManager == null) {
            throw new GlobalException("区域不存在");
        }

        List<Integer> shopIds = bindShopDTO.getShopIds();
        if (ObjectUtil.isEmpty(shopIds)) {
            throw new GlobalException("关联店铺不能为空");
        }

        List<AreaStoreManagerShop> areaStoreManagerShopList = new ArrayList<>();
        for (Integer shopId : shopIds) {
            AreaStoreManagerShop areaStoreManagerShop = new AreaStoreManagerShop();
            areaStoreManagerShop.setAreaStoreManagerId(areaStoreManager.getAreaStoreManagerId());
            areaStoreManagerShop.setShopId(shopId);
            areaStoreManagerShop.setCreateTime(StringUtils.getCurrentTime());
            areaStoreManagerShopList.add(areaStoreManagerShop);
        }
        areaStoreManagerShopService.saveBatch(areaStoreManagerShopList);
    }

    @Override
    public void removeShop(AreaStoreRemoveShopParam removeShopDTO) {
        AreaStoreManager areaStoreManager = this.getById(removeShopDTO.getAreaStoreManagerId());
        if (areaStoreManager == null) {
            throw new GlobalException("区域不存在");
        }
        areaStoreManagerShopService.remove(new LambdaQueryWrapper<AreaStoreManagerShop>()
                .eq(AreaStoreManagerShop::getAreaStoreManagerId, removeShopDTO.getAreaStoreManagerId())
                .eq(AreaStoreManagerShop::getShopId, removeShopDTO.getShopId())
        );
    }

    @Override
    public Page<ShopVO> getAreaShopList(AreaStoreShopListQueryPage listDTO) {
        // 获取区域信息
        AreaStoreManager areaStoreManager = this.getById(listDTO.getAreaStoreManagerId());
        if (areaStoreManager == null) {
            throw new GlobalException("区域不存在");
        }

        // 解析店铺ID
        List<AreaStoreManagerShop> shopIds = areaStoreManagerShopService.list(new LambdaQueryWrapper<AreaStoreManagerShop>()
                .eq(AreaStoreManagerShop::getAreaStoreManagerId, areaStoreManager.getAreaStoreManagerId()));

        // 如果没有绑定店铺，返回空分页
        if (listDTO.getCheck() == 1 && ObjectUtil.isEmpty(shopIds)) {
            return new Page<>(listDTO.getPage(), listDTO.getSize());
        }

        // 构造查询条件
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Shop::getShopType, ShopTypeEnum.STORE.getCode());
        if (listDTO.getCheck() == 1 && ObjectUtil.isNotEmpty(shopIds)) {
            queryWrapper.in(Shop::getShopId, shopIds.stream().map(AreaStoreManagerShop::getShopId).collect(Collectors.toList()));
        }
        if (listDTO.getCheck() == 0 && ObjectUtil.isNotEmpty(shopIds)) {
            queryWrapper.notIn(Shop::getShopId, shopIds.stream().map(AreaStoreManagerShop::getShopId).collect(Collectors.toList()));
        }

        // 如果有店铺名称搜索条件
        if (StrUtil.isNotEmpty(listDTO.getShopTitle())) {
            queryWrapper.like(Shop::getShopTitle, listDTO.getShopTitle());
        }

        // 分页查询
        Page<Shop> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        Page<Shop> shopPage = shopService.page(page, queryWrapper);

        // 转换为VO
        Page<ShopVO> voPage = new Page<>();
        BeanUtils.copyProperties(shopPage, voPage);

        List<ShopVO> shopVos = shopPage.getRecords().stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            shopVO.setCheck(CollUtil.contains(shopIds, shop.getShopId()));
            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
            shopVO.setShopContactConfig(JSONUtil.toList(shop.getShopContactJson(), StoreExtendedDto.ShopContactConfig.class));
            if (tigshopProperties.getIsO2o() == 1) {
                try {
                    shopVO.setShopRegionIds(JSONUtil.toList(shop.getShopRegionId(), Integer.class));
                    shopVO.setShopRegionNames(regionService.getRegionNamesByRegionIds(shopVO.getShopRegionIds()));
                    shopVO.setStoreParentName(shopService.getById(shop.getStoreParentId()).getShopTitle());
                } catch (Exception e) {
                    log.error("解析 showPicture JSON 出错: {}", e.getMessage(), e);
                }
            }
            return shopVO;
        }).collect(Collectors.toList());
        voPage.setRecords(shopVos);
        return voPage;
    }

    /**
     * 转换为VO
     */
    private AreaStoreManagerVO convertToVO(AreaStoreManager areaStoreManager) {
        AreaStoreManagerVO vo = new AreaStoreManagerVO();
        BeanUtils.copyProperties(areaStoreManager, vo);
        long count = areaStoreManagerShopService.count(new LambdaQueryWrapper<AreaStoreManagerShop>()
                .eq(AreaStoreManagerShop::getAreaStoreManagerId, areaStoreManager.getAreaStoreManagerId()));
        vo.setShopCount(count);
        vo.setShopIds(areaStoreManagerShopService.list(new LambdaQueryWrapper<AreaStoreManagerShop>()
                        .eq(AreaStoreManagerShop::getAreaStoreManagerId, areaStoreManager.getAreaStoreManagerId()))
                .stream().map(AreaStoreManagerShop::getShopId).collect(Collectors.toList()));
        vo.setCreateTime(areaStoreManager.getCreateTime().toString());
        vo.setUpdateTime(areaStoreManager.getUpdateTime().toString());
        return vo;
    }

    /**
     * 解析店铺ID字符串
     */
    private List<Integer> parseShopIds(String shopIdsStr) {
        if (StrUtil.isEmpty(shopIdsStr)) {
            return new ArrayList<>();
        }

        return Arrays.stream(shopIdsStr.split(","))
                .filter(StrUtil::isNotEmpty)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }
}




