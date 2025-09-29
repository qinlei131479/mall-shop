package com.tigshop.service.promotion.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.promotion.*;
import com.tigshop.bean.enums.promotion.PromotionRangeEnum;
import com.tigshop.bean.enums.promotion.PromotionType;
import com.tigshop.bean.enums.promotion.SeckillStatusEnum;
import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.product.ProductSku;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.model.promotion.Seckill;
import com.tigshop.bean.model.promotion.SeckillItem;
import com.tigshop.bean.model.promotion.TimeDiscountItem;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.bean.vo.promotion.SeckillListVO;
import com.tigshop.bean.vo.promotion.SeckillPromotionVO;
import com.tigshop.bean.vo.promotion.SeckillVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.mapper.promotion.SeckillItemMapper;
import com.tigshop.mapper.promotion.SeckillMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.ProductSkuService;
import com.tigshop.service.promotion.PromotionService;
import com.tigshop.service.promotion.SeckillItemService;
import com.tigshop.service.promotion.SeckillService;
import com.tigshop.service.promotion.TimeDiscountItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.constant.promotion.SeckillConstants.PRODUCT_EXIST_SECKILL;
import static com.tigshop.common.constant.promotion.SeckillConstants.PRODUCT_STOCK_NOT_ENOUGH;
import static com.tigshop.common.constant.promotion.TimeDiscountConstants.PRODUCT_EXIST_TIME_DISCOUNT;

/**
 * 限时秒杀服务实现类
 *
 * @author kidd
 * @create 2025/7/2
 */
@RequiredArgsConstructor
@Service
public class SeckillServiceImpl extends BaseServiceImpl<SeckillMapper, Seckill> implements SeckillService {

    private final ProductMapper productMapper;

    private final ProductSkuService productSkuService;

    private final SeckillItemService seckillItemService;

    private final TimeDiscountItemService timeDiscountItemService;

    private final PromotionService promotionService;

    private final AdminLogService adminLogService;

    private final SeckillItemMapper seckillItemMapper;

    @Override
    public Page<SeckillVO> list(SeckillListDTO listDTO) {
        // 分页
        Page<Seckill> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Seckill> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Seckill::getSeckillName, keyword);
        }

        Integer shopId = getShopId();
        if (shopId != null && shopId >= 0) {
            queryWrapper.eq(Seckill::getShopId, shopId);
        }

        // 执行查询
        Page<Seckill> seckillPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Seckill> seckillPageRecords = seckillPage.getRecords();
        if (seckillPageRecords.isEmpty()) {
            return new Page<>();
        }

        // 查询商品id
        List<Integer> productIds = seckillPageRecords.stream()
                .map(Seckill::getProductId)
                .distinct()
                .toList();

        List<Product> productList = productMapper.selectBatchIds(productIds);
        // 将商品信息存入 map 中
        Map<Integer, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        // 转换为VO
        List<SeckillVO> seckillVOList = seckillPageRecords.stream()
                .map(seckill -> {
                    SeckillVO seckillVO = new SeckillVO();
                    convert(seckill, seckillVO);
                    BeanUtils.copyProperties(seckill, seckillVO);
                    Product product = productMap.get(seckill.getProductId());
                    if (product != null) {
                        seckillVO.setProductName(product.getProductName());
                    }
                    return seckillVO;
                }).toList();
        return PageUtil.convertPage(seckillPage, seckillVOList);
    }

    /**
     * 数据格式转换
     */
    public void convert(Seckill seckill, SeckillVO seckillVO) {
        DateTime startDate = DateUtil.date(seckill.getSeckillStartTime() * 1000);
        seckillVO.setSeckillStartTime(DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss"));

        DateTime endDate = DateUtil.date(seckill.getSeckillEndTime() * 1000);
        seckillVO.setSeckillEndTime(DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss"));

        long currentTime = StringUtils.getCurrentTime();
        if (currentTime < seckill.getSeckillStartTime()) {
            seckillVO.setStatusName(SeckillStatusEnum.STATUS_NOT_STARTED.getDescription());
        } else if (currentTime > seckill.getSeckillEndTime()) {
            seckillVO.setStatusName(SeckillStatusEnum.STATUS_ENDED.getDescription());
        } else {
            seckillVO.setStatusName(SeckillStatusEnum.STATUS_STARTED.getDescription());
        }
    }

    @Override
    public SeckillVO detail(Integer id) {
        Seckill seckill = this.getById(id);
        Assert.notNull(seckill, () -> new GlobalException("未知的秒杀活动"));

        // 查询产品信息
        Product product = productMapper.selectById(seckill.getProductId());

        List<SeckillItem> seckillItems = seckillItemService.lambdaQuery().eq(SeckillItem::getSeckillId, id).list();
        List<Integer> skuIds = seckillItems.stream().map(SeckillItem::getSkuId).toList();
        List<ProductSku> productSkus = productSkuService.lambdaQuery().in(ProductSku::getSkuId, skuIds).list();
        Map<Integer, ProductSku> productSkuMap = productSkus.stream().collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));

        return new SeckillVO(seckill, product, seckillItems, productSkuMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(SeckillCreateDTO createDTO) {
        if (createDTO != null) {
            Seckill seckill = new Seckill();
            Integer shopId = getShopId();
            shopId = shopId == null ? 0 : shopId;
            seckill.setShopId(shopId);
            seckill.setSeckillStartTime(StringUtils.dateToTimestampExample(createDTO.getSeckillStartTime()));
            seckill.setSeckillEndTime(StringUtils.dateToTimestampExample(createDTO.getSeckillEndTime()));
            BeanUtils.copyProperties(createDTO, seckill);
            checkSeckillItem(createDTO.getSeckillItem(), seckill);
            boolean isSave = this.save(seckill);
            if (isSave) {
                // 活动商品 + 营销写入 + 判断商品是否已有活动
                saveSeckillItem(createDTO.getSeckillItem(), seckill);
                savePromotion(createDTO.getSeckillItem(), seckill);
                adminLogService.createByLogInfo(StrUtil.format("新增秒杀活动:{}", seckill.getSeckillName()));
            }
            return isSave;
        }
        return false;
    }

    /**
     * 活动商品批量写入
     *
     * @param seckillItem 活动商品
     * @param seckill     秒杀数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSeckillItem(List<SeckillItemDTO> seckillItem, Seckill seckill) {
        // 需要保存的实体对象
        List<SeckillItem> seckillItems = seckillItem.stream().map(dto -> {
            SeckillItem item = new SeckillItem();
            Integer skuId = dto.getSkuId();
            if (skuId == null) {
                skuId = 0;
            }
            if (dto.getSecondsSeckill() == 1) {
                item.setSeckillId(seckill.getSeckillId());
                item.setProductId(seckill.getProductId());
                item.setSkuId(skuId);
                item.setSeckillPrice(dto.getSeckillPrice());
                item.setSeckillStock(dto.getSeckillStock());
                item.setSeckillStartTime(seckill.getSeckillStartTime());
                item.setSeckillEndTime(seckill.getSeckillEndTime());
            }
            return item;
        }).collect(Collectors.toList());

        boolean isSuccess = seckillItemService.saveBatch(seckillItems);
        if (!isSuccess) {
            throw new GlobalException(SERVICE_ERROR);
        }
    }

    /**
     * 判断商品是否已有活动
     *
     * @param seckillItem 活动商品
     * @param seckill     秒杀数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkSeckillItem(List<SeckillItemDTO> seckillItem, Seckill seckill) {
        Integer productId = seckill.getProductId();
        Long endTime = seckill.getSeckillEndTime();
        Long startTime = seckill.getSeckillStartTime();
        Integer shopId = seckill.getShopId();

        // 秒杀活动校验 - 通过关联查询seckill表获取shop_id进行比较
        if (seckillItemMapper.selectCount(new LambdaQueryWrapper<SeckillItem>()
                .eq(SeckillItem::getProductId, productId)
                .le(SeckillItem::getSeckillStartTime, endTime)
                .ge(SeckillItem::getSeckillEndTime, startTime)
                .ne(seckill.getSeckillId() != null, SeckillItem::getSeckillId, seckill.getSeckillId())
                // 关联查询seckill表获取shop_id进行比较
                .apply("EXISTS (SELECT 1 FROM seckill ps WHERE ps.seckill_id = seckill_item.seckill_id AND ps.shop_id = {0})", shopId)) > 0) {
            throw new GlobalException(PRODUCT_EXIST_SECKILL);
        }
        // 限时折扣校验
        if (timeDiscountItemService.count(new LambdaQueryWrapper<TimeDiscountItem>()
                .eq(TimeDiscountItem::getProductId, productId)
                .le(TimeDiscountItem::getStartTime, endTime)
                .ge(TimeDiscountItem::getEndTime, startTime)) > 0) {
            throw new GlobalException(PRODUCT_EXIST_TIME_DISCOUNT);
        }

        // 商品库存校验
        for (SeckillItemDTO item : seckillItem) {
            if (item.getSecondsSeckill() == 0) {
                continue;
            }
            int skuId = item.getSkuId() == null ? 0 : item.getSkuId();

            if (skuId == 0) {
                if (productMapper.selectById(productId).getProductStock() < item.getSeckillStock()) {
                    throw new GlobalException(PRODUCT_STOCK_NOT_ENOUGH);
                }
            } else {
                if (productSkuService.getById(skuId).getSkuStock() < item.getSeckillStock()) {
                    throw new GlobalException(PRODUCT_STOCK_NOT_ENOUGH);
                }
            }
        }
    }

    /**
     * 营销写入
     *
     * @param seckillItem 活动商品
     * @param seckill     秒杀数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void savePromotion(List<SeckillItemDTO> seckillItem, Seckill seckill) {
        Integer relationId = seckill.getSeckillId();
        relationId = relationId == null ? 0 : relationId;
        // 根据 relationId 查询是否有营销活动
        if (promotionService.count(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getRelationId, relationId)
                .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode())) > 0) {
            // 更新部分字段
            promotionService.update(new LambdaUpdateWrapper<Promotion>()
                    .eq(Promotion::getRelationId, relationId)
                    .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode())
                    .set(Promotion::getStartTime, seckill.getSeckillStartTime())
                    .set(Promotion::getEndTime, seckill.getSeckillEndTime())
                    .set(Promotion::getPromotionName, seckill.getSeckillName()));
        } else {
            Promotion promotion = new Promotion();
            Integer shopId = getShopId();
            shopId = shopId == null ? 0 : shopId;
            Integer productId = seckill.getProductId();
            promotion.setPromotionName(seckill.getSeckillName());
            promotion.setStartTime(seckill.getSeckillStartTime());
            promotion.setEndTime(seckill.getSeckillEndTime());
            promotion.setType(PromotionType.TYPE_SECKILL.getCode());
            promotion.setShopId(shopId);
            promotion.setRelationId(seckill.getSeckillId());
            promotion.setRange(PromotionRangeEnum.PROMOTION_RANGE_PRODUCT.getCode());

            Integer[] rangeDataArray = {productId};
            List<Integer> rangeData = Arrays.asList(rangeDataArray);
            promotion.setRangeData(rangeData.toString());

            List<Integer> skuIds = seckillItem.stream()
                    .filter(item -> item.getSkuId() != null && item.getSecondsSeckill() == 1)
                    .map(SeckillItemDTO::getSkuId)
                    .toList();
            if (!skuIds.isEmpty()) {
                promotion.setSkuIds(skuIds.toString());
            }

            boolean isSuccess = promotionService.save(promotion);
            if (!isSuccess) {
                throw new GlobalException(SERVICE_ERROR);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SeckillUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Seckill seckill = new Seckill();
            seckill.setSeckillStartTime(StringUtils.dateToTimestampExample(updateDTO.getSeckillStartTime()));
            seckill.setSeckillEndTime(StringUtils.dateToTimestampExample(updateDTO.getSeckillEndTime()));
            BeanUtils.copyProperties(updateDTO, seckill);
            boolean isSave = this.updateById(seckill);
            if (isSave) {
                // 活动商品 + 营销写入 + 判断商品是否已有活动
                checkSeckillItem(updateDTO.getSeckillItem(), seckill);
                savePromotion(updateDTO.getSeckillItem(), seckill);
                adminLogService.createByLogInfo(StrUtil.format("更新秒杀活动:{}", seckill.getSeckillName()));
            }
            return isSave;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void incSales(Integer productId, Integer skuId, Integer quantity) {
        SeckillItem productActivityInfo = getProductActivityInfo(productId, skuId);
        if (productActivityInfo != null) {
            LambdaUpdateWrapper<SeckillItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SeckillItem::getRecId, productActivityInfo.getRecId()).setIncrBy(SeckillItem::getSeckillSales, quantity);
            seckillItemMapper.update(updateWrapper);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decStock(Integer productId, Integer skuId, Integer quantity) {
        SeckillItem productActivityInfo = getProductActivityInfo(productId, skuId);
        if (productActivityInfo != null) {
            LambdaUpdateWrapper<SeckillItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SeckillItem::getRecId, productActivityInfo.getRecId()).setDecrBy(SeckillItem::getSeckillStock, quantity);
            seckillItemMapper.update(updateWrapper);
        }
    }

    @Override
    public CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO) {
        CartPromotionParsePriceDTO cartPromotionParsePriceDTO = new CartPromotionParsePriceDTO();

        for (CartVO cart : cartList) {
            SeckillItem one = seckillItemService.getOne(new LambdaQueryWrapper<SeckillItem>().eq(SeckillItem::getSeckillId, promotionVO.getRelationId())
                    .eq(SeckillItem::getSkuId, cart.getSkuId()));
            if (one != null) {
                cart.setPrice(one.getSeckillPrice());
            }
        }
        cartPromotionParsePriceDTO.setCartList(cartList);
        cartPromotionParsePriceDTO.setPromotion(promotionVO);
        return cartPromotionParsePriceDTO;
    }

    @Override
    public void decSales(Integer productId, Integer skuId, Integer quantity) {
        SeckillItem productActivityInfo = getProductActivityInfo(productId, skuId);
        if (productActivityInfo != null) {
            LambdaUpdateWrapper<SeckillItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SeckillItem::getRecId, productActivityInfo.getRecId()).setDecrBy(SeckillItem::getSeckillSales, quantity);
            seckillItemMapper.update(updateWrapper);
        }
    }

    @Override
    public void incStock(Integer productId, Integer skuId, Integer quantity) {
        SeckillItem productActivityInfo = getProductActivityInfo(productId, skuId);
        if (productActivityInfo != null) {
            LambdaUpdateWrapper<SeckillItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SeckillItem::getRecId, productActivityInfo.getRecId()).setIncrBy(SeckillItem::getSeckillStock, quantity);
            seckillItemMapper.update(updateWrapper);
        }
    }

    @Override
    public Page<SeckillListVO> getSeckillProductList(Integer size, Integer page, Integer unStarted) {
        Page<Seckill> seckillPage = this.lambdaQuery()
                .gt(unStarted != 0, Seckill::getSeckillStartTime, StringUtils.getCurrentTime())
                .and(unStarted == 0,
                        query -> query
                                .lt(Seckill::getSeckillStartTime, StringUtils.getCurrentTime())
                                .gt(Seckill::getSeckillEndTime, StringUtils.getCurrentTime())
                )
                .page(new Page<>(page, size));

        List<Seckill> seckillRecords = seckillPage.getRecords();
        if (seckillRecords.isEmpty()) {
            return new Page<>(page, size);
        }

        List<Integer> seckillIds = seckillRecords.stream().map(Seckill::getSeckillId).toList();
        List<SeckillItem> seckillItems = seckillItemService.lambdaQuery().in(SeckillItem::getSeckillId, seckillIds).list();

        List<Integer> productIds = seckillItems.stream().map(SeckillItem::getProductId).distinct().toList();
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Integer, Product> productMap = products.stream().collect(Collectors.toMap(Product::getProductId, product -> product));


        List<SeckillListVO> resultRecords = seckillRecords.stream()
                .map(seckill -> {
                    List<SeckillItem> currSeckillItems = seckillItems.stream().filter(item -> item.getSeckillId().equals(seckill.getSeckillId())).toList();

                    SeckillListVO listVO = new SeckillListVO();

                    SeckillItem seckillItem = currSeckillItems.getFirst();
                    BeanUtil.copyProperties(seckillItem, listVO);

                    Product product = productMap.get(seckillItem.getProductId());
                    if (product != null) {
                        BeanUtil.copyProperties(product, listVO);
                        listVO.setMarketPrice(product.getProductPrice());
                        listVO.setProductPrice(product.getMarketPrice());
                        listVO.setOrgProductPrice(product.getProductPrice().toString());
                    }

                    SeckillListVO.SeckillData seckillData = new SeckillListVO.SeckillData();
                    BeanUtil.copyProperties(seckill, seckillData);
                    // 处理 seckillData 时间
                    seckillData.setSeckillStartTime(TigUtils.handelTime(seckill.getSeckillStartTime()));
                    seckillData.setSeckillEndTime(TigUtils.handelTime(seckill.getSeckillEndTime()));

                    seckillData.setSeckillItem(currSeckillItems);
                    listVO.setSeckkillData(seckillData);
                    return listVO;
                })
                .toList();

        return PageUtil.convertPage(seckillPage, resultRecords);
    }

    @Override
    public List<SeckillPromotionVO> listForDecorate(SeckillListDTO listDTO) {
        Long currentTime = StringUtils.getCurrentTime();
        Page<Seckill> seckillPage = this.baseMapper.listForDecorate(new Page<>(listDTO.getPage(), listDTO.getSize()), currentTime);

        List<Seckill> records = seckillPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }

        Map<Integer, Seckill> seckillMap = records.stream().collect(Collectors.toMap(Seckill::getProductId, Function.identity()));

        List<Integer> seckillIds = records.stream().map(Seckill::getSeckillId).toList();
        List<SeckillItem> seckillItems = seckillItemService.lambdaQuery()
                .in(SeckillItem::getSeckillId, seckillIds)
                .list();

        Map<Integer, List<SeckillItem>> seckillItemMap = seckillItems.stream()
                .collect(Collectors.groupingBy(SeckillItem::getProductId));

        List<Integer> productIds = seckillItems.stream().map(SeckillItem::getProductId).distinct().toList();
        List<Product> products = productMapper.selectBatchIds(productIds);

        List<Integer> skuIds = seckillItems.stream().map(SeckillItem::getSkuId).toList();
        List<ProductSku> productSkus = productSkuService.listByIds(skuIds);
        Map<Integer, ProductSku> productSkuMap = productSkus.stream().collect(Collectors.toMap(ProductSku::getSkuId, Function.identity()));

        List<ProductListResDTO> productsRes = products.stream()
                .map(item -> {
                    List<ProductSku> currProductSkus = productSkus.stream()
                            .filter(productSku -> productSku.getProductId().equals(item.getProductId()))
                            .toList();
                    return new ProductListResDTO(item, currProductSkus);
                })
                .toList();
        return productsRes.stream()
                .map(product -> {
                    SeckillPromotionVO seckillProduct = BeanUtil.copyProperties(product, SeckillPromotionVO.class);
                    seckillProduct.setOrgProductPrice(product.getProductPrice().toString());

                    Seckill seckill = seckillMap.get(product.getProductId());

                    seckillProduct.setSeckillLimitNum(seckill.getSeckillLimitNum());

                    List<SeckillItem> currSeckillItems = seckillItemMap.get(product.getProductId());

                    Integer seckillSales = currSeckillItems.stream().map(SeckillItem::getSeckillSales).reduce(0, Integer::sum);
                    seckillProduct.setSeckillSales(seckillSales);

                    Integer seckillStock = currSeckillItems.stream().map(SeckillItem::getSeckillStock).reduce(0, Integer::sum);
                    seckillProduct.setSeckillSales(seckillStock);

                    List<SeckillItem> seckillItemsBySeckillId = seckillItems.stream().filter(item -> Objects.equals(item.getSeckillId(), seckill.getSeckillId())).toList();

                    SeckillVO seckillVO = new SeckillVO(seckill, seckillItemsBySeckillId);
                    seckillProduct.setSeckkillData(seckillVO);

                    ProductSku productSku = productSkuMap.get(product.getProductId());
                    if (productSku != null) {
                        seckillProduct.setSkuId(productSku.getSkuId());
                        seckillProduct.setSkuSn(productSku.getSkuSn());
                        seckillProduct.setMarketPrice(product.getMarketPrice());
                        seckillProduct.setOrgProductPrice(productSku.getSkuPrice().toString());
                    }

                    return seckillProduct;
                })
                .toList();
    }

    /**
     * 删除
     *
     * @param id id
     * @return boolean
     */
    @Override
    public boolean del(Integer id) {
        if (id == null) {
            return false;
        }

        boolean isMove = this.removeById(id);

        if (isMove) {
            // 删除活动商品
            seckillItemService.remove(new LambdaQueryWrapper<SeckillItem>()
                    .eq(SeckillItem::getSeckillId, id));
            // 删除对应营销活动
            promotionService.remove(new LambdaQueryWrapper<Promotion>()
                    .eq(Promotion::getRelationId, id)
                    .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode()));

        }
        return isMove;
    }

    /**
     * 批量操作
     *
     * @param batchDTO 批量操作参数
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        if ("del".equals(batchDTO.getType())) {
            List<Integer> ids = batchDTO.getIds();
            boolean isMove = this.removeByIds(ids);
            if (isMove) {
                // 删除活动商品
                seckillItemService.remove(new LambdaQueryWrapper<SeckillItem>()
                        .in(SeckillItem::getSeckillId, ids));
                // 删除对应营销活动
                promotionService.remove(new LambdaQueryWrapper<Promotion>()
                        .in(Promotion::getRelationId, ids)
                        .eq(Promotion::getType, PromotionType.TYPE_SECKILL.getCode()));
            }
            return isMove;
        }
        return false;
    }

    @Override
    public List<SeckillItemInfoDTO> getSeckillInfo(Integer productId) {
        Long currentTime = StringUtils.getCurrentTime();
        Seckill seckill = this.lambdaQuery().eq(Seckill::getProductId, productId)
                .lt(Seckill::getSeckillStartTime, currentTime)
                .gt(Seckill::getSeckillEndTime, currentTime)
                .one();
        if (seckill == null) {
            return Collections.emptyList();
        }

        List<SeckillItem> seckillItems = seckillItemService.lambdaQuery().eq(SeckillItem::getSeckillId, seckill.getSeckillId()).list();

        return seckillItems.stream().map(item -> new SeckillItemInfoDTO(seckill, item)).toList();
    }

    public SeckillItem getProductActivityInfo(Integer productId, Integer skuId) {
        LambdaQueryWrapper<SeckillItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SeckillItem::getProductId, productId)
                .le(SeckillItem::getSeckillStartTime, StringUtils.getCurrentTime())
                .ge(SeckillItem::getSeckillEndTime, StringUtils.getCurrentTime())
                .eq(SeckillItem::getSkuId, skuId).last("limit 1");

        return seckillItemMapper.selectOne(queryWrapper);
    }

}
