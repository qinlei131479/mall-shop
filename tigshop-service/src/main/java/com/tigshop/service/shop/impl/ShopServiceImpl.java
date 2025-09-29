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

package com.tigshop.service.shop.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.merchant.MerchantAdminDataDTO;
import com.tigshop.bean.dto.shop.*;
import com.tigshop.bean.enums.merchant.MerchantType;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.shop.ShopType;
import com.tigshop.bean.enums.vendor.VendorIsAdminEnum;
import com.tigshop.bean.model.authority.AdminLog;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.merchant.Merchant;
import com.tigshop.bean.model.merchant.MerchantUser;
import com.tigshop.bean.model.o2o.AreaStoreManagerShop;
import com.tigshop.bean.model.o2o.StoreProduct;
import com.tigshop.bean.model.o2o.StoreTip;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.shop.CollectShop;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.param.login.LoginChooseAuth;
import com.tigshop.bean.param.login.LoginChooseParam;
import com.tigshop.bean.param.vendor.ShopVendorSettingUpdateParam;
import com.tigshop.bean.query.shop.PickupPageQuery;
import com.tigshop.bean.query.shop.StoreDetailQuery;
import com.tigshop.bean.query.shop.StorePageQuery;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.o2o.StoreDetailVO;
import com.tigshop.bean.vo.o2o.StoreListVO;
import com.tigshop.bean.vo.o2o.TipListVO;
import com.tigshop.bean.vo.setting.config.ShopSettingsVO;
import com.tigshop.bean.vo.shop.*;
import com.tigshop.bean.vo.vendor.ShopVendorSettingVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.o2o.StoreProductMapper;
import com.tigshop.mapper.o2o.StoreTipMapper;
import com.tigshop.mapper.o2o.TipMapper;
import com.tigshop.mapper.promotion.PromotionMapper;
import com.tigshop.mapper.shop.CollectShopMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.merchant.MerchantService;
import com.tigshop.service.merchant.MerchantUserService;
import com.tigshop.service.o2o.ShopPickupConfigService;
import com.tigshop.service.o2o.ShopPickupTplService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.RegionService;
import com.tigshop.service.setting.ShippingTplService;
import com.tigshop.service.shop.AdminUserShopService;
import com.tigshop.service.shop.ShopService;
import com.tigshop.service.vendor.AdminUserVendorService;
import com.tigshop.service.vendor.VendorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tigshop.common.constant.Constants.*;
import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.constant.shop.ShopConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentAdminId;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 店铺表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@Service
public class ShopServiceImpl extends BaseServiceImpl<ShopMapper, Shop> implements ShopService {
    @Resource
    MerchantService merchantService;
    @Resource
    MerchantUserService merchantUserService;
    @Resource
    ConfigService configService;
    @Resource
    AdminUserShopService adminUserShopService;
    @Resource
    ProductService productService;
    @Resource
    ShippingTplService shippingTplService;
    @Resource
    DecorateService decorateService;
    @Resource
    AdminRoleService adminRoleService;
    @Resource
    CollectShopMapper collectShopMapper;
    @Resource
    TranslatePackageImpl translatePackage;
    @Resource
    private AdminLogService adminLogService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private AdminUserVendorService adminUserVendorService;
    @Resource
    private VendorService vendorService;
    @Resource
    private RedisTemplate<String, LoginChooseAuth> redisTemplate;
    @Resource
    private TigshopProperties tigshopProperties;
    @Resource
    private ShopPickupConfigService shopPickupConfigService;
    @Resource
    private ShopPickupTplService shopPickupTplService;
    @Resource
    private RegionService regionService;
    @Resource
    private PromotionMapper promotionMapper;

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private StoreTipMapper storeTipMapper;
    @Resource
    private TipMapper tipMapper;
    @Autowired
    private StoreProductMapper storeProductMapper;


    @Override
    public Page<ShopVO> list(ShopListDTO listDTO) {
        // 分页
        Page<Shop> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        MPJLambdaWrapper<Shop> queryWrapper = new MPJLambdaWrapper<>();
        if (listDTO.getIsMyShop() != 1) {
            queryWrapper.eq(Shop::getShopType, listDTO.getShopType());
        }
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        // 状态筛选
        Integer status = listDTO.getStatus();
        if (status != null && status > 0) {
            queryWrapper.eq(Shop::getStatus, status);
        }

        if (StringUtils.isNotEmpty(listDTO.getAddStartTime())) {
            queryWrapper.ge(Shop::getAddTime, StringUtils.dateToTimestampExample(listDTO.getAddStartTime() + " 00:00:00"));
        }
        if (StringUtils.isNotEmpty(listDTO.getAddEndTime())) {
            queryWrapper.le(Shop::getAddTime, StringUtils.dateToTimestampExample(listDTO.getAddEndTime() + " 23:59:59"));
        }
        if (ObjectUtil.isNotNull(listDTO.getStoreParentId())) {
            queryWrapper.eq(Shop::getStoreParentId, listDTO.getStoreParentId());
        }

        if (ObjectUtil.isNotEmpty(listDTO.getMainAccount())) {
            List<Integer> shopIds = adminUserService.getBindMainShopIds(listDTO.getMainAccount(), true);
            if (ObjectUtil.isEmpty(shopIds)) {
                return new Page<>(0, listDTO.getSize());
            } else {
                queryWrapper.in(Shop::getShopId, shopIds);
            }
        }

        if (ObjectUtil.isNotEmpty(listDTO.getAccount())) {
            List<Integer> shopIds = adminUserService.getBindMainShopIds(listDTO.getAccount(), false);
            if (ObjectUtil.isEmpty(shopIds)) {
                return new Page<>(0, listDTO.getSize());
            } else {
                queryWrapper.in(Shop::getShopId, shopIds);
            }
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(Shop::getShopTitle, keyword);
        }

        if (listDTO.getShopIds() != null && !listDTO.getShopIds().isEmpty()) {
            queryWrapper.in(Shop::getShopId, listDTO.getShopIds());
        } else {
            Integer shopId = getShopId();
            if (shopId != null && shopId > 0) {
                queryWrapper.eq(Shop::getShopId, shopId);
            }
        }
        Integer areaStoreManagerId = listDTO.getAreaStoreManagerId();
        if (areaStoreManagerId != null) {
            queryWrapper.leftJoin(AreaStoreManagerShop.class, AreaStoreManagerShop::getShopId, Shop::getShopId)
                    .eq(AreaStoreManagerShop::getAreaStoreManagerId, areaStoreManagerId);
        }
        if (listDTO.getShopRegionId() != null) {
            queryWrapper.like(Shop::getShopRegionId, listDTO.getShopRegionId());
        }
        // 执行查询
        Page<Shop> shopPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Shop> shopPageRecords = shopPage.getRecords();
        // 转换为VO
        List<ShopVO> shopVOList = new ArrayList<>(shopPageRecords.stream().map(shop -> {
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
            long timestampInSeconds = shop.getAddTime();
            Date date = new Date(timestampInSeconds * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            shopVO.setAddTime(sdf.format(date));
            //查询出商户的数据
            MerchantVO merchantVO = merchantService.getMerchantVO(shop.getMerchantId());
            Optional<AdminUserShop> first = adminUserShopService.list(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getShopId, shop.getShopId())
                    .eq(AdminUserShop::getIsAdmin, VendorIsAdminEnum.YES.getCode())
            ).stream().findFirst();

            if (first.isPresent()) {
                AdminUser adminUser = adminUserService.getById(first.get().getAdminId());
                if (adminUser != null) {
                    MerchantAdminDataDTO admin = new MerchantAdminDataDTO();
                    admin.setAdminId(adminUser.getAdminId());
                    admin.setUsername(adminUser.getUsername());
                    merchantVO.setAdmin(admin);
                }
            }
            shopVO.setMerchant(merchantVO);
            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
            if (tigshopProperties.getIsO2o() == 1) {
                try {
                    shopVO.setShopShowPicture(JSONUtil.toList(shop.getShopShowPicture(), StoreExtendedDto.PicInfo.class));
                    shopVO.setShopContactConfig(JSONUtil.toList(shop.getShopContactJson(), StoreExtendedDto.ShopContactConfig.class));
                    shopVO.setShopOpenCloseConfig(JSONUtil.toBean(shop.getShopOpenCloseJson(), StoreExtendedDto.ShopOpenCloseConfig.class));
                    shopVO.setShopRegionIds(JSONUtil.toList(shop.getShopRegionId(), Integer.class));
                    shopVO.setShopRegionNames(regionService.getRegionNamesByRegionIds(shopVO.getShopRegionIds()));
                    Shop getShopByStoreParentId = this.getById(shop.getStoreParentId());
                    if (shop.getShopType() == ShopTypeEnum.PICKUP.getCode()) {
                        if (getShopByStoreParentId != null) {
                            shopVO.setStoreParentName(getShopByStoreParentId.getShopTitle());
                        } else {
                            shopVO.setStoreParentName(0 == shop.getStoreParentId() ? "自营店铺" : "");
                        }
                    }
                    List<StoreProduct> storeProducts = storeProductMapper.selectList(Wrappers.lambdaQuery(StoreProduct.class)
                            .eq(StoreProduct::getShopId, shop.getShopId())
                            .eq(StoreProduct::getProductId, listDTO.getProductId())
                            .last("limit 1"));
                    shopVO.setCheck(!storeProducts.isEmpty());
                } catch (Exception e) {
                    log.error("解析 showPicture JSON 出错: {}", e.getMessage(), e);
                }
            }
            return shopVO;
        }).toList());
        if (tigshopProperties.getIsO2o() == 1 && listDTO.getSelect() == 1) {
            if (ObjectUtil.isEmpty(listDTO.getKeyword()) || "自营店铺".contains(listDTO.getKeyword())) {
                ShopVO shopVO = new ShopVO();
                shopVO.setShopId(0);
                shopVO.setShopTitle("自营店铺");
                shopVOList.add(shopVO);
            }
        }
        return PageUtil.convertPage(shopPage, shopVOList);
    }

    @Override
    public ShopVO detail(Integer id) {
        if (id != null) {
            Shop shop = this.getById(id);
            if (shop == null) {
                throw new GlobalException(SHOP_NOT_EXIST);
            }
            ShopVO shopVO = new ShopVO();
            BeanUtils.copyProperties(shop, shopVO);
            if (tigshopProperties.getIsO2o() == 1) {
                try {
                    shopVO.setShopShowPicture(JSONUtil.toList(shop.getShopShowPicture(), StoreExtendedDto.PicInfo.class));
                    shopVO.setShopContactConfig(JSONUtil.toList(shop.getShopContactJson(), StoreExtendedDto.ShopContactConfig.class));
                    shopVO.setShopOpenCloseConfig(JSONUtil.toBean(shop.getShopOpenCloseJson(), StoreExtendedDto.ShopOpenCloseConfig.class));
                    shopVO.setShopRegionIds(JSONUtil.toList(shop.getShopRegionId(), Integer.class));
                    List<StoreTip> storeTips = storeTipMapper.selectList(new LambdaQueryWrapper<StoreTip>().eq(StoreTip::getShopId, shop.getShopId()).orderByAsc(StoreTip::getSort));
                    shopVO.setShopTips(storeTips
                            .stream()
                            .map(StoreTip::getTipId)
                            .toList()
                    );
                    List<TipListVO> tips = new ArrayList<>();
                    storeTips.forEach(item -> {
                        TipListVO tipListVO = new TipListVO();
                        tipListVO.setTipId(item.getTipId());
                        tipListVO.setTipName(tipMapper.selectById(item.getTipId()).getTipName());
                        tips.add(tipListVO);
                    });
                    shopVO.setTips(tips);
                    if (shop.getShopShowCategory() != null) {
                        shopVO.setShopShowCategory(JSONUtil.toBean(shop.getShopShowCategory(), ShopShowCategoryConfig.class));
                    } else {
                        ShopShowCategoryConfig shopShowCategoryConfig = new ShopShowCategoryConfig();
                        shopShowCategoryConfig.setType(1);
                        shopVO.setShopShowCategory(shopShowCategoryConfig);
                    }
                } catch (Exception e) {
                    log.error("解析 showPicture JSON 出错: {}", e.getMessage(), e);
                }
            }
            shopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));
            long timestampInSeconds = shop.getAddTime();
            Date date = new Date(timestampInSeconds * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            shopVO.setAddTime(sdf.format(date));
            MerchantVO merchantVO = merchantService.getMerchantVO(shop.getMerchantId());
            //获取adminUserShop
            LambdaQueryWrapper<AdminUserShop> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminUserShop::getShopId, id);
            queryWrapper.last("limit 1");
            AdminUserShop adminUserShop = adminUserShopService.getOne(queryWrapper);
            if (adminUserShop != null) {
                AdminUserShopVO adminUserShopVO = new AdminUserShopVO();
                BeanUtils.copyProperties(adminUserShop, adminUserShopVO);
                adminUserShopVO.setAddTime(TigUtils.handelTime(adminUserShop.getAddTime()));
                if (adminUserShop.getAuthList() != null && !"[]".equals(adminUserShop.getAuthList())) {
                    adminUserShopVO.setAuthList(JsonUtil.fromJson(adminUserShop.getAuthList(), JSONArray.class));
                } else {
                    adminUserShopVO.setAuthList(new JSONArray());
                }
                shopVO.setAdminUserShop(adminUserShopVO);
            }

            //base_data shop_data 需要隐藏
            merchantVO.setBaseData(null);
            merchantVO.setShopData(null);
            shopVO.setMerchant(merchantVO);
            if (ObjectUtil.isEmpty(shop.getKefuInlet())) {
                shopVO.setKefuInlet(new ArrayList<>());
            } else {
                shopVO.setKefuInlet(JSONUtil.parseArray(shop.getKefuInlet()).toList(Integer.class));
            }
            return shopVO;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean create(ShopCreateDTO createDTO) {
        Shop shop = new Shop();
        checkStoreParam(createDTO, shop);

        Integer shopId = getShopId();
        Integer currentAdminId = getCurrentAdminId();
        if (createDTO != null) {

            BeanUtils.copyProperties(createDTO, shop);
            if (shopId > 0) {
                Shop currentShop = this.getById(shopId);
                shop.setMerchantId(currentShop.getMerchantId());
                createDTO.setMerchantId(currentShop.getMerchantId());
            }

            if (createDTO.getMerchantId() == null) {
                throw new GlobalException(translatePackage.translate(MERCHANT_ID_NOT_NULL));
            }
            shop.setAddTime(StringUtils.getCurrentTime());
            Shop shopCreate = applicationContext.getBean(ShopService.class).updateShop(shopId, shop, true);
            // 更新门店标签管理相关
            List<Integer> shopTips = createDTO.getShopTips();
            int sort = 0;
            if (shopTips != null && !shopTips.isEmpty()) {
                for (Integer tipId : shopTips) {
                    StoreTip storeTip = new StoreTip();
                    storeTip.setShopId(shopCreate.getShopId());
                    storeTip.setTipId(tipId);
                    storeTip.setSort(sort++);
                    storeTip.setAddTime(System.currentTimeMillis());
                    storeTipMapper.insert(storeTip);
                }
            }
            MerchantUser merchantUser = merchantUserService.getDetailByMerchantId(createDTO.getMerchantId(), true);
            AdminUserShopCreateDTO adminUserShopCreateDTO = new AdminUserShopCreateDTO();
            adminUserShopCreateDTO.setShopId(shopCreate.getShopId());
            if (tigshopProperties.getIsO2o() == 1 && createDTO.getShopType().equals(ShopTypeEnum.PICKUP.getCode()) && createDTO.getStoreParentId() == 0) {
                merchantUser = new MerchantUser();
                merchantUser.setAdminUserId(getCurrentAdminId());
            }
            adminUserShopCreateDTO.setAdminId(merchantUser.getAdminUserId());
            adminUserShopCreateDTO.setIsAdmin(1);
            adminUserShopCreateDTO.setAuthList(List.of("all"));
            adminUserShopService.createAdminUserShop(adminUserShopCreateDTO);
            if (tigshopProperties.getIsO2o() == 1 && createDTO.getShopType().equals(ShopTypeEnum.STORE.getCode())) {
                // 添加自提用户
                Shop pickUp = new LambdaQueryChainWrapper<>(getBaseMapper()).eq(Shop::getStoreParentId, shop.getShopId()).one();
                AdminUserShopCreateDTO pickupUserShopCreateDTO = new AdminUserShopCreateDTO();
                pickupUserShopCreateDTO.setShopId(pickUp.getShopId());
                pickupUserShopCreateDTO.setAdminId(merchantUser.getAdminUserId());
                pickupUserShopCreateDTO.setIsAdmin(1);
                pickupUserShopCreateDTO.setAuthList(List.of("all"));
                adminUserShopService.createAdminUserShop(pickupUserShopCreateDTO);
            }
            return true;
        }
        return false;
    }

    private void checkStoreParam(StoreExtendedDto dto, Shop shop) {
        if (tigshopProperties.getIsO2o() == 1 &&
                (dto.getShopType().equals(ShopTypeEnum.STORE.getCode()) || dto.getShopType().equals(ShopTypeEnum.PICKUP.getCode()))) {
            if (dto.getShopType().equals(ShopTypeEnum.STORE.getCode())) {
                if (ObjectUtil.isEmpty(dto.getShopCoverPicture())) {
                    throw new GlobalException("门店封面不能为空");
                }
            }
            if (dto.getShopContactConfig() == null) {
                throw new GlobalException("请填写联系方式");
            }
            if (dto.getShopOpenCloseConfig() == null) {
                throw new GlobalException("请填写营业时间");
            }
            if (dto.getShopRegionIds() == null) {
                throw new GlobalException("请选择区域");
            }
            if (dto.getShopLongitude() == null || dto.getShopLatitude() == null) {
                throw new GlobalException("经纬度不能为空");
            }
            if (dto.getShopType() == null) {
                throw new GlobalException("请选择店铺类型");
            }
            if (!Arrays.asList(1, 2, 3).contains(dto.getShopType())) {
                throw new GlobalException("类型错误");
            }
            shop.setShopType(dto.getShopType());
            if (dto.getShopType().equals(ShopTypeEnum.PICKUP.getCode())) {
                if (dto.getStoreParentId() == null) {
                    throw new GlobalException("父门店id不能为空");
                }
                shop.setStoreParentId(dto.getStoreParentId());
            }
            shop.setShopCoverPicture(dto.getShopCoverPicture());
            shop.setShopShowPicture(JSONUtil.toJsonStr(dto.getShopShowPicture()));
            shop.setDescription(dto.getDescription());
            shop.setStatus(dto.getStatus());
            shop.setShopContactJson(JSONUtil.toJsonStr(dto.getShopContactConfig()));
            shop.setShopOpenCloseJson(JSONUtil.toJsonStr(dto.getShopOpenCloseConfig()));
            shop.setShopRegionId(JSONUtil.toJsonStr(dto.getShopRegionIds()));
            shop.setShopDetailedAddress(dto.getShopDetailedAddress());
            shop.setShopLatitude(dto.getShopLatitude());
            shop.setShopLongitude(dto.getShopLongitude());
        }
        if (tigshopProperties.getIsO2o() == 1 && dto.getShopType().equals(ShopTypeEnum.PICKUP.getCode())) {
            if (dto.getStoreParentId() == 0) {
                //选中自营
                shop.setMerchantId(0);
                if (dto instanceof ShopCreateDTO) {
                    ((ShopCreateDTO) dto).setMerchantId(0);
                }
            } else {
                //选中门店
                Shop parentId = getOne(new LambdaQueryWrapper<Shop>().eq(Shop::getShopId, dto.getStoreParentId()));
                shop.setMerchantId(parentId.getMerchantId());
                if (dto instanceof ShopCreateDTO) {
                    ((ShopCreateDTO) dto).setMerchantId(parentId.getMerchantId());
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(ShopUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Shop shop = new Shop();
            BeanUtils.copyProperties(updateDTO, shop);

            checkStoreParam(updateDTO, shop);

            updateShop(shop.getShopId(), shop, false);

            // 更新门店标签管理相关
            List<Integer> shopTips = updateDTO.getShopTips();
            // 删除所有门店标签，添加新的门店标签
            storeTipMapper.delete(new LambdaQueryWrapper<StoreTip>().eq(StoreTip::getShopId, shop.getShopId()));
            int sort = 0;
            if (shopTips != null && !shopTips.isEmpty()) {
                for (Integer tipId : shopTips) {
                    StoreTip storeTip = new StoreTip();
                    storeTip.setShopId(shop.getShopId());
                    storeTip.setTipId(tipId);
                    storeTip.setSort(sort++);
                    storeTip.setAddTime(System.currentTimeMillis());
                    storeTipMapper.insert(storeTip);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public BigDecimal getSumShopMoney(Integer shopId) {
        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        //sum shop_money
        if (shopId > 0) {
            queryWrapper.eq("shop_id", shopId).select("sum(shop_money) as shop_money");
        } else {
            queryWrapper.select("sum(shop_money) as shop_money");
        }

        Shop shop = this.getOne(queryWrapper);

        return null != shop ? shop.getShopMoney() : BigDecimal.ZERO;

    }

    @Override
    public BigDecimal getSumFrozenMoney(Integer shopId) {
        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        //sum shop_money

        if (shopId > 0) {
            queryWrapper.eq("shop_id", shopId).select("sum(frozen_money) as frozen_money");
        } else {
            queryWrapper.select("sum(frozen_money) as frozen_money");
        }

        Shop shop = this.getOne(queryWrapper);

        return null != shop ? shop.getFrozenMoney() : BigDecimal.ZERO;
    }

    @Override
    public List<Shop> getShopByShopTitle(String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Shop::getShopTitle, keyword);
        return this.list(queryWrapper);
    }

    @Override
    public Page<ClientShopVO> clientList(ShopListDTO listDTO) {
        //分页
        Page<Shop> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        // 搜索关键字
        if (StringUtils.isNotEmpty(listDTO.getKeyword())) {
            queryWrapper.like(Shop::getShopTitle, listDTO.getKeyword());
        }
        queryWrapper.eq(Shop::getStatus, 1);
        if (tigshopProperties.getIsO2o() == 1) {
            queryWrapper.ne(Shop::getShopType, ShopTypeEnum.PICKUP.getCode());
        }

        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 执行查询
        Page<Shop> shopPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Shop> queryPageRecords = shopPage.getRecords();
        List<ClientShopVO> clientShopVOList = queryPageRecords.stream().map(shop -> {
            ClientShopVO clientShopVO = new ClientShopVO();
            BeanUtils.copyProperties(shop, clientShopVO);
            clientShopVO.setStatusText(ShopType.getTypeName(shop.getStatus()));

            long timestampInSeconds = shop.getAddTime();
            Date date = new Date(timestampInSeconds * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            clientShopVO.setAddTime(sdf.format(date));

            //查询最新上架的5个商品
            LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
            productQueryWrapper.eq(Product::getShopId, shop.getShopId()).eq(Product::getProductStatus, 1).eq(Product::getIsDelete, 0).orderByDesc(Product::getProductId).last("limit 5");
            List<Product> productList = productService.list(productQueryWrapper);
            if (productList != null && !productList.isEmpty()) {
                clientShopVO.setListingProduct(productList);
            } else {
                clientShopVO.setListingProduct(List.of());
            }

            //统计收藏的店铺
            LambdaQueryWrapper<CollectShop> collectShopQueryWrapper = new LambdaQueryWrapper<>();
            collectShopQueryWrapper.eq(CollectShop::getShopId, shop.getShopId());
            Long collectCount = collectShopMapper.selectCount(collectShopQueryWrapper);
            clientShopVO.setCollectCount(collectCount);

            //统计商品的总数
            LambdaQueryWrapper<Product> productQueryWrapperCount = new LambdaQueryWrapper<>();
            productQueryWrapperCount.eq(Product::getShopId, shop.getShopId());
            productQueryWrapperCount.eq(Product::getProductStatus, 1);
            productQueryWrapperCount.eq(Product::getIsDelete, 0);
            Long listingCount = productService.count(productQueryWrapperCount);
            clientShopVO.setListingCount(listingCount);
            return clientShopVO;
        }).toList();

        return PageUtil.convertPage(shopPage, clientShopVOList);
    }

    @Override
    public ClientShopDetailVO clientDetail(Integer id) {
        if (id == 0) {
            ClientShopDetailVO e = new ClientShopDetailVO();
            e.setShopId(0);
            e.setShopTitle("商城自营");
            e.setShopLogo(configService.getConfigVal(SettingsEnum.SHOP_LOGO));
            return e;
        }

        Shop shop = this.getById(id);
        if (shop == null) {
            throw new GlobalException(translatePackage.translate(SHOP_NOT_EXIST));
        }
        ClientShopDetailVO clientShopDetailVO = new ClientShopDetailVO();
        BeanUtils.copyProperties(shop, clientShopDetailVO);
        clientShopDetailVO.setStatusText(ShopType.getTypeName(shop.getStatus()));

        long timestampInSeconds = shop.getAddTime();
        Date date = new Date(timestampInSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        clientShopDetailVO.setAddTime(sdf.format(date));

        if (shop.getMerchantId() > 0) {
            LambdaQueryWrapper<Merchant> merchantQueryWrapper = new LambdaQueryWrapper<>();
            merchantQueryWrapper.eq(Merchant::getMerchantId, shop.getMerchantId());
            Merchant merchant = merchantService.getOne(merchantQueryWrapper);
            ClientShopDetailVO.Merchant merchantVO = new ClientShopDetailVO.Merchant();
            BeanUtils.copyProperties(merchant, merchantVO);
            merchantVO.setTypeText(MerchantType.getTypeName(merchant.getType()));

            long timeInSeconds = shop.getAddTime();
            Date dateMerchant = new Date(timeInSeconds * 1000L);
            SimpleDateFormat sdfMerchant = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            merchantVO.setAddTime(sdfMerchant.format(dateMerchant));
            merchantVO.setMerchantData(JsonUtil.fromJson(merchant.getMerchantData(), JSONObject.class));

            clientShopDetailVO.setMerchant(merchantVO);
        }

        //统计商品总数
        LambdaQueryWrapper<Product> productQueryWrapper = new LambdaQueryWrapper<>();
        productQueryWrapper.eq(Product::getShopId, id);
        clientShopDetailVO.setProductCount(productService.count(productQueryWrapper));

        //统计新商品总数
        LambdaQueryWrapper<Product> productQueryWrapperNew = new LambdaQueryWrapper<>();
        productQueryWrapperNew.eq(Product::getShopId, id);
        productQueryWrapperNew.eq(Product::getIsNew, 1);
        clientShopDetailVO.setNewProductCount(productService.count(productQueryWrapperNew));

        //查看是否有收藏店铺
        LambdaQueryWrapper<CollectShop> collectShopQueryWrapper = new LambdaQueryWrapper<>();
        collectShopQueryWrapper.eq(CollectShop::getShopId, id);
        collectShopQueryWrapper.eq(CollectShop::getUserId, getCurrentUserId());
        CollectShop collectShop = collectShopMapper.selectOne(collectShopQueryWrapper);
        clientShopDetailVO.setCollectShop(collectShop != null);

        return clientShopDetailVO;
    }

    @Override
    public List<Shop> getListByIds(List<Integer> ids) {
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Shop::getShopId, ids);
        return this.list(queryWrapper);
    }

    @Transactional
    @Override
    public Shop updateShop(Integer id, Shop shopData, boolean isAdd) {
        Integer shopType = shopData.getShopType();
        if (isAdd) {
            if (shopData.getMerchantId() > 0) {
                String maxShopCountVal = configService.getConfigByCode(SettingsEnum.MAX_SHOP_COUNT.getBizCode()).getBizVal();
                long maxShopCount = Long.parseLong(maxShopCountVal);
                long shopCount = this.count(new QueryWrapper<Shop>().eq("merchant_id", shopData.getMerchantId()));
                if (shopCount >= maxShopCount) {
                    throw new GlobalException(String.format(translatePackage.translate("商户店铺数已达上限,上限为: %s 个,如需修改,请前往配置"), maxShopCount));
                }
            }
            this.save(shopData);
            //初始化运费模板
            shippingTplService.initShippingTpl(shopData.getShopId());
            //初始化装修模板
            decorateService.initDecorate(shopData.getShopId());
            //初始化角色
            adminRoleService.initAdminRole(shopData.getMerchantId(), shopData.getShopId());
            //初始化商品
            //productService.initProductByShopId(shopData.getShopId());
            //多门店版本且为门店类型时，新增默认自提点
            if (tigshopProperties.getIsO2o() == 1 && shopType.equals(ShopTypeEnum.STORE.getCode())) {
                // 门店创建自提配置
                Integer storeId = shopData.getShopId();
                Shop shopPickup = new Shop();
                BeanUtils.copyProperties(shopData, shopPickup);
                shopPickupConfigService.initPickUpConfig(storeId);
                shopPickupTplService.initPickUpTpl(storeId);

                shopPickup.setShopType(ShopTypeEnum.PICKUP.getCode());
                shopPickup.setShopTitle(shopPickup.getShopTitle() + "（自提点）");
                shopPickup.setStoreParentId(storeId);
                shopPickup.setShopId(null);
                this.save(shopPickup);
                // 自提点创建自提配置
                Integer pickUpId = shopPickup.getShopId();
                shopPickupConfigService.initPickUpConfig(pickUpId);
                shopPickupTplService.initPickUpTpl(pickUpId);
            }
            if (tigshopProperties.getIsO2o() == 1 && shopType.equals(ShopTypeEnum.PICKUP.getCode())) {
                // 自提点创建自提配置
                Integer pickUpId = shopData.getShopId();
                shopPickupConfigService.initPickUpConfig(pickUpId);
                shopPickupTplService.initPickUpTpl(pickUpId);
            }
        } else {
            if (id <= 0) {
                throw new GlobalException(SERVICE_ERROR);
            }
            Shop shop = new Shop();
            BeanUtils.copyProperties(shopData, shop);
            this.updateById(shop);
        }
        return shopData;
    }

    @Override
    public Boolean checkShopStatus(Integer shopId, String productName) {
        if (shopId != null && shopId == 0) {
            return true;
        }
        if (shopId == null) {
            return false;
        }
        Integer shopStatus = detail(shopId).getStatus();
        if (shopStatus.equals(ShopType.CLOSE.getCode())) {
            throw new GlobalException(translatePackage.translate(String.format("商品：%s 所属的店铺暂停营业中，请联系平台处理！", productName)));
        }
        return true;
    }

    @Override
    public List<Shop> getShopList(ShopListDTO dto) {
        return this.list(new LambdaQueryWrapper<Shop>().eq(Shop::getShopType, dto.getShopType()));
    }

    @Override
    public StaffShowVO staffShow(Integer shopId) {
        // 已有员工数
        long totalUsingUser = adminUserShopService.count(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getIsUsing, 0).eq(AdminUserShop::getShopId, shopId));
        // 子管理员数
        long subUsingUser = adminUserShopService.count(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getIsUsing, 0).eq(AdminUserShop::getShopId, shopId).eq(AdminUserShop::getIsAdmin, 0));

        // 停用员工数
        long stopUsingUser = adminUserShopService.count(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getIsUsing, 1).eq(AdminUserShop::getShopId, shopId));

        int maxSubAdministrator = Integer.parseInt(configService.getSettings(ShopSettingsVO.class).getMaxSubAdministrator());
        long residue = maxSubAdministrator > 0 ? maxSubAdministrator - (subUsingUser + stopUsingUser) : 0;

        List<Integer> adminIds = adminUserShopService.list(new LambdaQueryWrapper<AdminUserShop>().eq(AdminUserShop::getShopId, shopId))
                .stream()
                .map(AdminUserShop::getAdminId)
                .toList();

        List<AdminLog> list = adminLogService.list(new LambdaQueryWrapper<AdminLog>()
                .in(ObjectUtil.isNotEmpty(adminIds), AdminLog::getUserId, adminIds)
                .orderByDesc(AdminLog::getLogId)
                .last("limit 5")
        );

        StaffShowVO staffShowVO = new StaffShowVO();
        staffShowVO.setUsingUser((int) totalUsingUser);
        staffShowVO.setStopUsingUser((int) stopUsingUser);
        staffShowVO.setResidue((int) residue);
        staffShowVO.setAdminLog(list.stream().map(item -> {
            StaffShowVO.AdminLogVO adminLogVO = new StaffShowVO.AdminLogVO();
            adminLogVO.setLogId(item.getLogId());
            DateTime date = DateUtil.date(item.getLogTime() * 1000);
            adminLogVO.setLogTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
            adminLogVO.setLogInfo(item.getLogInfo());
            adminLogVO.setIpAddress(item.getIpAddress());
            adminLogVO.setUserId(item.getUserId());
            AdminUser adminUser = adminUserService.getById(item.getUserId());
            if (adminUser != null) {
                adminLogVO.setUsername(adminUser.getUsername());
            }
            return adminLogVO;
        }).toList());
        return staffShowVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Shop updateInfo(ShopUpdateInfoDTO dto) {
        // 获取店铺ID
        Integer shopId = HeaderUtils.getShopId();

        // 如果传入的店铺ID与获取的店铺ID不相等，抛出异常
        if (!Objects.equals(dto.getShopId(), shopId)) {
            throw new GlobalException("没有此店铺");
        }

        // 将传入的DTO对象转换为Shop对象
        Shop shop = BeanUtil.copyProperties(dto, Shop.class);

        checkStoreParam(dto, shop);

        // 更新店铺信息
        return this.updateShop(shopId, shop, false);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setting(ShopSettingDTO request) {
        Integer shopId = request.getShopId();
        Integer currentShopId = getShopId();

        if (!shopId.equals(currentShopId)) {
            throw new GlobalException("没有此店铺");
        }

        Shop shop = new Shop();
        BeanUtils.copyProperties(request, shop);
        if (request.getShopShowCategory() != null) {
            shop.setShopShowCategory(JSONUtil.toJsonStr(request.getShopShowCategory()));
        }
        if (request.getKefuInlet() != null) {
            shop.setKefuInlet(JsonUtil.toJson(request.getKefuInlet()));
        }
        updateById(shop);

        //店铺暂停运营后，下架店铺内所以商品
        if (request.getStatus() != null && request.getStatus() == 4) {
            productService.lambdaUpdate()
                    .set(Product::getProductStatus, 0)
                    .eq(Product::getShopId, shopId)
                    .update();
        }
    }

    @Override
    public void choose(LoginChooseParam loginChooseParam) {
        // 检验数据
        int adminId = SecurityUtils.getCurrentAdminId();
        String id = loginChooseParam.getId();

        AdminUser adminUser = adminUserService.getById(adminId);
        String authJsonArrayStr = adminUser.getAuthList();
        if (ObjectUtil.equals("shop", loginChooseParam.getAdminType())) {
            Shop byId = this.getById(id);
            if (byId == null) {
                throw new GlobalException("没有此店铺");
            }
            AdminUserShop adminUserShop = adminUserShopService.getOne(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getShopId, id)
                    .eq(AdminUserShop::getAdminId, adminId));
            if (adminUserShop == null) {
                throw new GlobalException("没有此店铺权限");
            }
            authJsonArrayStr = adminUserShop.getAuthList();
        }
        if (ObjectUtil.equals("vendor", loginChooseParam.getAdminType())) {
            Vendor byId = vendorService.getById(id);
            if (byId == null) {
                throw new GlobalException("没有此供应商");
            }
            AdminUserVendor adminUserVendor = adminUserVendorService.getOne(new LambdaQueryWrapper<AdminUserVendor>()
                    .eq(AdminUserVendor::getVendorId, id)
                    .eq(AdminUserVendor::getAdminId, adminId));
            if (adminUserVendor == null) {
                throw new GlobalException("没有此供应商权限");
            }
            authJsonArrayStr = adminUserVendor.getAuthList();
            Vendor vendor = vendorService.getById(id);
            vendor.setLastLoginTime(StringUtils.getCurrentTime());
            vendorService.updateById(vendor);
        }
        // 1. 根据选择，查询权限保存到 redis ；key 为 admin::auth::[[admin_id]]
        LoginChooseAuth loginChooseAuth = new LoginChooseAuth();
        loginChooseAuth.setLoginChooseParam(loginChooseParam);
        loginChooseAuth.setAuthorities(JSONUtil.toList(authJsonArrayStr, String.class));
        redisTemplate.opsForValue().set(StrUtil.format("{}{}{}", ADMIN_USER, "auth::", adminId), loginChooseAuth);
    }

    @Override
    public List<String> checkAndReturnAuthList(Integer adminId, String id, String adminType) {
        // 检验数据
        AdminUser adminUser = adminUserService.getById(adminId);
        String authJsonArrayStr = adminUser.getAuthList();
        if (ObjectUtil.equals("shop", adminType)) {
            Shop byId = this.getById(id);
            if (byId == null) {
                throw new GlobalException("没有此店铺");
            }
            // 设置shop类型 1 店铺，2 门店，3自提点
            HeaderUtils.setAttrValue(X_SHOP_TYPE, byId.getShopType() + "");
            AdminUserShop adminUserShop = adminUserShopService.getOne(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getShopId, id)
                    .eq(AdminUserShop::getAdminId, adminId));
            if (adminUserShop == null) {
                return List.of();
            }
            authJsonArrayStr = adminUserShop.getAuthList();
        }
        if (ObjectUtil.equals("vendor", adminType)) {
            Vendor byId = vendorService.getById(id);
            if (byId == null) {
                throw new GlobalException("没有此供应商");
            }
            AdminUserVendor adminUserVendor = adminUserVendorService.getOne(new LambdaQueryWrapper<AdminUserVendor>()
                    .eq(AdminUserVendor::getVendorId, id)
                    .eq(AdminUserVendor::getAdminId, adminId));
            if (adminUserVendor == null) {
                return List.of();
            }
            authJsonArrayStr = adminUserVendor.getAuthList();
        }
        return JSONUtil.toList(authJsonArrayStr, String.class);
    }

    @Override
    public ShopVendorSettingVO getVendorSetting() {
        Shop shop = this.getById(getShopId());
        ShopVendorSettingVO shopVendorSettingVO = new ShopVendorSettingVO();
        shopVendorSettingVO.setVendorSetPriceType(shop.getVendorSetPriceType());
        shopVendorSettingVO.setVendorSetPriceAutoValue(shop.getVendorSetPriceAutoValue());
        return shopVendorSettingVO;
    }

    @Override
    public void updateVendorSetting(ShopVendorSettingUpdateParam param) {
        Shop shop = this.getById(getShopId());
        shop.setVendorSetPriceType(param.getVendorSetPriceType());
        shop.setVendorSetPriceAutoValue(param.getVendorSetPriceAutoValue());
        updateById(shop);
    }

    @Override
    public Page<PickupListVO> pickupList(PickupPageQuery query) {
        Integer shopId = query.getShopId();
        if (getById(shopId) == null && shopId > 0) {
            throw new GlobalException("没有此门店");
        }
        String configVal = configService.getConfigVal(SettingsEnum.STORE_SHOW_OTHER_CITY_STORE);
        if ("1".equals(configVal)) {
            query.setRegionId(null);
        }
        Page<Shop> page = new Page<>(query.getPage(), query.getSize());
        if (query.getLatitude() == null || query.getLatitude().compareTo(BigDecimal.ZERO) == 0) {
            query.setLatitude(null);
        }
        Page<PickupListVO> shopPage = getBaseMapper().pickupListByLocation(page, query);
        shopPage.getRecords().forEach(pickupListVO -> {
            pickupListVO.setShopRegionIds(JSONUtil.toList(pickupListVO.getShopRegionIdsJson(), Integer.class));
            pickupListVO.setContactConfigs(JSONUtil.toList(pickupListVO.getContactJson(), StoreExtendedDto.ShopContactConfig.class));
            pickupListVO.setShopOpenCloseConfig(JSONUtil.toBean(pickupListVO.getShopOpenCloseConfigJson(), StoreExtendedDto.ShopOpenCloseConfig.class));
            pickupListVO.setShopRegionNames(regionService.getRegionNamesByRegionIds(pickupListVO.getShopRegionIds()));
        });
        return shopPage;
    }

    @Override
    public Page<StoreListVO> storeList(StorePageQuery storePageQuery) {
        String configVal = configService.getConfigVal(SettingsEnum.STORE_SHOW_OTHER_CITY_STORE);
        if ("1".equals(configVal)) {
            storePageQuery.setRegionId(null);
        }
        if (storePageQuery.getProductId() != null) {
            List<StoreProduct> storeProducts = storeProductMapper.selectList(new LambdaQueryWrapper<StoreProduct>()
                    .eq(StoreProduct::getProductId, storePageQuery.getProductId())
                    .eq(StoreProduct::getIsDelete, NO)
                    .eq(StoreProduct::getProductStatus, YES)
            );
            List<Integer> shopIds = new ArrayList<>(storeProducts.stream().map(StoreProduct::getShopId).map(Long::intValue).toList());
            Product byId = productService.getById(storePageQuery.getProductId());
            storePageQuery.setShopIds(shopIds);
            if (byId.getShopId() > 0) {
                storePageQuery.getShopIds().add(byId.getShopId());
            }
            if (ObjectUtil.isEmpty(storePageQuery.getShopIds())) {
                Page<StoreListVO> storeListVOPage = new Page<>(storePageQuery.getPage(), storePageQuery.getSize());
                Integer shopId = byId.getShopId();
                if (shopId == 0) {
                    StoreListVO e = new StoreListVO();
                    e.setShopId(0);
                    e.setShopTitle("商城自营");
                    e.setShopLogo(configService.getConfigVal(SettingsEnum.SHOP_LOGO));
                    storeListVOPage.setRecords(List.of(e));
                }
                return storeListVOPage;
            }
        }

        Page<Shop> page = new Page<>(storePageQuery.getPage(), storePageQuery.getSize());
        Page<StoreListVO> shopPage;
        if (storePageQuery.getLatitude() != null && storePageQuery.getLatitude().compareTo(BigDecimal.ZERO) != 0) {
            shopPage = getBaseMapper().storeListByLocation(page, storePageQuery);
        } else {
            shopPage = getBaseMapper().storeListByRegion(page, storePageQuery);
        }
        if (storePageQuery.getPage() == 1) {
            if ((storePageQuery.getShopIds() != null && storePageQuery.getShopIds().contains(0))) {
                StoreListVO e = new StoreListVO();
                e.setShopId(0);
                e.setShopTitle("商城自营");
                e.setShopLogo(configService.getConfigVal(SettingsEnum.SHOP_LOGO));
                if (CollUtil.isEmpty(shopPage.getRecords())) {
                    shopPage.setRecords(List.of(e));
                }else {
                    shopPage.getRecords().addFirst(e);
                }
            }
        }
        shopPage.getRecords().forEach(storeListVO -> {
            storeListVO.setContactConfigs(JSONUtil.toList(storeListVO.getContactJson(), StoreExtendedDto.ShopContactConfig.class));
            Long countCoupon = promotionMapper.selectCount(new LambdaQueryWrapper<Promotion>()
                    .eq(Promotion::getRelationId, storeListVO.getShopId())
                    .eq(Promotion::getType, 2)
                    .eq(Promotion::getIsAvailable, YES)
                    .eq(Promotion::getIsDelete, NO)
                    .gt(Promotion::getEndTime, StringUtils.getCurrentTime())
            );
            storeListVO.setHasCoupon(countCoupon > 0 ? 1 : 0);
            List<StoreTip> storeTips = storeTipMapper.selectList(new LambdaQueryWrapper<StoreTip>().eq(StoreTip::getShopId, storeListVO.getShopId()).orderByAsc(StoreTip::getSort));
            storeListVO.setTipNames(storeTips
                    .stream()
                    .map(item -> tipMapper.selectById(item.getTipId()).getTipName())
                    .toList()
            );
            Long collectShopCount = collectShopMapper.selectCount(new LambdaQueryWrapper<CollectShop>().eq(CollectShop::getShopId, storeListVO.getShopId()));
            storeListVO.setShopCollect(collectShopCount);
            //查看是否有收藏店铺
            LambdaQueryWrapper<CollectShop> collectShopQueryWrapper = new LambdaQueryWrapper<>();
            collectShopQueryWrapper.eq(CollectShop::getShopId, storeListVO.getShopId());
            collectShopQueryWrapper.eq(CollectShop::getUserId, getCurrentUserId());
            CollectShop collectShop = collectShopMapper.selectOne(collectShopQueryWrapper);
            storeListVO.setCollectShop(collectShop != null);
        });
        return shopPage;
    }

    @Override
    public StoreDetailVO storeDetail(StoreDetailQuery query) {
        if (query.getShopId() == 0) {
            StoreDetailVO e = new StoreDetailVO();
            e.setShopId(0);
            e.setShopTitle("商城自营");
            e.setShopLogo(configService.getConfigVal(SettingsEnum.SHOP_LOGO));
            return e;
        }
        Integer shopId = query.getShopId();
        if (shopId != null) {
            Shop shop = this.getById(shopId);
            if (shop == null) {
                throw new GlobalException(SHOP_NOT_EXIST);
            }
            StoreDetailVO storeDetailVO = new StoreDetailVO();
            BeanUtils.copyProperties(shop, storeDetailVO);
            if (tigshopProperties.getIsO2o() == 1) {
                try {
                    storeDetailVO.setShopShowPicture(JSONUtil.toList(shop.getShopShowPicture(), StoreExtendedDto.PicInfo.class));
                    storeDetailVO.setShopContactConfig(JSONUtil.toList(shop.getShopContactJson(), StoreExtendedDto.ShopContactConfig.class));
                    storeDetailVO.setShopOpenCloseConfig(JSONUtil.toBean(shop.getShopOpenCloseJson(), StoreExtendedDto.ShopOpenCloseConfig.class));
                    storeDetailVO.setShopRegionIds(JSONUtil.toList(shop.getShopRegionId(), Integer.class));
                    storeDetailVO.setShopRegionNames(regionService.getRegionNamesByRegionIds(storeDetailVO.getShopRegionIds()));
                } catch (Exception e) {
                    log.error("解析 showPicture JSON 出错: {}", e.getMessage(), e);
                }
            }
            storeDetailVO.setShopStar(shop.getShopStar());
            storeDetailVO.setShopSales(shop.getShopSales());
            storeDetailVO.setShopStarNum(shop.getShopStarNum());
            List<StoreTip> storeTips = storeTipMapper.selectList(new LambdaQueryWrapper<StoreTip>().eq(StoreTip::getShopId, shop.getShopId()).orderByAsc(StoreTip::getSort));
            storeDetailVO.setTipNames(storeTips
                    .stream()
                    .map(item -> tipMapper.selectById(item.getTipId()).getTipName())
                    .toList()
            );

            try {
                LambdaQueryWrapper<CollectShop> collectShopQueryWrapper = new LambdaQueryWrapper<>();
                collectShopQueryWrapper.eq(CollectShop::getShopId, shopId);
                collectShopQueryWrapper.eq(CollectShop::getUserId, getCurrentUserId());
                CollectShop collectShop = collectShopMapper.selectOne(collectShopQueryWrapper);
                storeDetailVO.setCollectShop(collectShop != null);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            if (query.getLatitude() != null && query.getLatitude().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal distance = GpsDistanceUtil.getDistance(
                        query.getLongitude(),
                        query.getLatitude(),
                        shop.getShopLongitude(),
                        shop.getShopLatitude()
                );
                storeDetailVO.setDistance(distance.toString());
            }
            return storeDetailVO;
        }
        return null;
    }

    @Override
    public List<StoreListVO> getStoreListByProductId(Integer productId) {
        // 获取所有门店
        List<Shop> shops = this.lambdaQuery()
                .eq(Shop::getShopType, ShopTypeEnum.STORE.getCode())
                .list();
        // 已分配该商品的门店
        List<StoreListVO> storeList = shops.stream().map(shop -> {
                    StoreListVO storeListVO = new StoreListVO();
                    // 获取门店分配该商品的数量
                    Long count = storeProductMapper.selectCount(Wrappers.lambdaQuery(StoreProduct.class)
                            .eq(StoreProduct::getShopId, shop.getShopId())
                            .eq(StoreProduct::getProductId, productId));
                    // 如果分配了，就返回对应的门店信息
                    if (count > 0) {
                        storeListVO.setShopId(shop.getShopId());
                        storeListVO.setShopTitle(shop.getShopTitle());
                    }
                    return storeListVO;
                }).filter(storeListVO -> storeListVO.getShopId() != null)
                .toList();
        return storeList;
    }

    @Override
    public PickupListVO pickupDetail(Integer shopId) {
        Shop pickup = getById(shopId);
        if (pickup == null) {
            throw new GlobalException("没有此自提点");
        }
        PickupListVO pickupListVO = new PickupListVO();
        BeanUtils.copyProperties(pickup, pickupListVO);
        pickupListVO.setLatitude(pickup.getShopLatitude());
        pickupListVO.setLongitude(pickup.getShopLongitude());
        pickupListVO.setAddress(pickup.getShopDetailedAddress());
        pickupListVO.setShopRegionIds(JSONUtil.toList(pickup.getShopRegionId(), Integer.class));
        pickupListVO.setContactConfigs(JSONUtil.toList(pickup.getShopContactJson(), StoreExtendedDto.ShopContactConfig.class));
        pickupListVO.setShopOpenCloseConfig(JSONUtil.toBean(pickup.getShopOpenCloseJson(), StoreExtendedDto.ShopOpenCloseConfig.class));
        pickupListVO.setShopRegionNames(regionService.getRegionNamesByRegionIds(pickupListVO.getShopRegionIds()));
        return pickupListVO;
    }
}
