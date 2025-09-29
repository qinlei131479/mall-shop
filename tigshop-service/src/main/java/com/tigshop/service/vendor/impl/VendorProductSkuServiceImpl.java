package com.tigshop.service.vendor.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.idev.excel.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.vendor.VendorProductSkuStockBizEnum;
import com.tigshop.bean.enums.vendor.VendorProductSkuStockOperationEnum;
import com.tigshop.bean.model.vendor.product.VendorProductSku;
import com.tigshop.bean.model.vendor.product.VendorProductSkuStockLog;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;
import com.tigshop.bean.param.vendor.product.VendorProductSkuStockParam;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.mapper.vendor.product.VendorProductSkuMapper;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.vendor.VendorProductSkuService;
import com.tigshop.service.vendor.VendorProductSkuStockLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author kidd
 */
@RequiredArgsConstructor
@Service
public class VendorProductSkuServiceImpl extends ServiceImpl<VendorProductSkuMapper, VendorProductSku> implements VendorProductSkuService {

    private final VendorProductSkuStockLogService vendorProductSkuStockLogService;
    private final ConfigService configService;

    @Transactional
    @Override
    public void saveBatchSku(VendorProductSaveParam param, Long vendorProductId) {
        List<VendorProductSku> vendorProductSkus = param.createVendorProductSkus(vendorProductId);

        String snPrefix = configService.getConfigVal(SettingsEnum.SN_PREFIX);
        vendorProductSkus.forEach(sku -> {
            // 处理商品规格编码
            String skuSn = this.generateSkuSn(param.getProductSnGenerateType(), sku, snPrefix);
            sku.setSkuSn(skuSn);
            this.save(sku);
        });

        // 新增供应商商品规格库存日志
        List<VendorProductSkuStockLog> stockLogs = vendorProductSkus.stream()
                .map(sku -> VendorProductSkuStockLog.builder()
                        .vendorProductId(sku.getVendorProductId())
                        .vendorProductSkuId(sku.getId())
                        .operationType(VendorProductSkuStockOperationEnum.ADD.getCode())
                        .beforeStock(0)
                        .changeNum(sku.getSkuStock())
                        .afterStock(sku.getSkuStock())
                        .bizType(VendorProductSkuStockBizEnum.INSERT.getCode())
                        .bizRemark("商品规格新增")
                        .vendorId(sku.getVendorId())
                        .build())
                .toList();
        vendorProductSkuStockLogService.saveBatch(stockLogs);
    }

    /**
     * 生成商品编码
     *
     * @param sku 商品规格
     * @param productSnGenerateType 商品编码生成类型；1-系统生成，2-手动输入
     * @param snPrefix              货号前缀
     */
    private String generateSkuSn(Integer productSnGenerateType, VendorProductSku sku, String snPrefix) {
        if (productSnGenerateType == 2) {
            boolean exists = this.getBaseMapper().exists(
                    Wrappers.lambdaQuery(VendorProductSku.class).eq(VendorProductSku::getSkuSn, sku.getSkuSn())
            );
            Assert.isFalse(exists, () -> new GlobalException("该货号已存在"));
        }

        // 货号前缀 + 供货商 ID（三位，不够前面补零） + 时分秒毫秒
        Integer vendorId = HeaderUtils.getVendorId();
        String skuSn = snPrefix + String.format("%03d", vendorId) + DateUtils.format(new Date(), "HHmmssSSS");

        while (this.getBaseMapper().exists(new QueryWrapper<VendorProductSku>().eq("sku_sn", skuSn))) {
            skuSn = generateSkuSn(productSnGenerateType, sku, snPrefix);
        }

        return skuSn;
    }


    @Transactional
    @Override
    public void updateBatchSku(VendorProductEditParam param, Long vendorProductId) {
        // 1. 查询数据库中供应商商品规格
        List<VendorProductSku> dbSkus = this.lambdaQuery().eq(VendorProductSku::getVendorProductId, vendorProductId).list();
        Map<String, VendorProductSku> dbSkuMap = dbSkus.stream().collect(Collectors.toMap(VendorProductSku::getSkuAttrVal, Function.identity()));

        // 2. 构造前端传入的供应商商品规格
        List<VendorProductSku> inputSkus = param.createVendorProductSkus(vendorProductId);
        Map<String, VendorProductSku> inputSkuMap = inputSkus.stream().collect(Collectors.toMap(sku -> sku.getSkuAttrVal() != null ? sku.getSkuAttrVal() : "", Function.identity()));

        // 3. 找到删除的商品规格、添加的商品规格、修改的商品规格
        // 3.1. 添加的商品规格
        List<VendorProductSku> addSkus = inputSkus.stream()
                .filter(vendorProductSku -> !dbSkuMap.containsKey(vendorProductSku.getSkuAttrVal()))
                .toList();

        // 3.2. 修改的商品规格，并设置数据库的供应商商品规格 ID
        List<VendorProductSku> updateSkus = inputSkus.stream()
                .filter(vendorProductSku -> dbSkuMap.containsKey(vendorProductSku.getSkuAttrVal()))
                .peek(vendorProductSku -> vendorProductSku.setId(dbSkuMap.get(vendorProductSku.getSkuAttrVal()).getId()))
                .toList();

        // 3.3. 删除的商品规格
        List<VendorProductSku> delSkus = dbSkus.stream()
                .filter(dbVendorProductSku -> !inputSkuMap.containsKey(dbVendorProductSku.getSkuAttrVal()))
                .toList();

        // 4. 删除商品规格、添加商品规格、修改商品规格
        // 4.1. 删除商品规格
        if (CollUtil.isNotEmpty(delSkus)) {
            List<Long> delIds = delSkus.stream().map(VendorProductSku::getId).toList();
            this.removeBatchByIds(delIds);

            List<VendorProductSkuStockLog> delStockLogs = delSkus.stream()
                    .map(sku -> VendorProductSkuStockLog.builder()
                            .vendorProductId(sku.getVendorProductId())
                            .vendorProductSkuId(sku.getId())
                            .operationType(VendorProductSkuStockOperationEnum.SUBTRACT.getCode())
                            .beforeStock(sku.getSkuStock())
                            .changeNum(sku.getSkuStock())
                            .afterStock(0)
                            .bizType(VendorProductSkuStockBizEnum.UPDATE.getCode())
                            .bizRemark("商品规格编辑：删除商品规格")
                            .vendorId(sku.getVendorId())
                            .build())
                    .toList();
            vendorProductSkuStockLogService.saveBatch(delStockLogs);
        }

        // 4.2. 添加商品规格
        if (CollUtil.isNotEmpty(addSkus)) {
            this.saveBatch(addSkus);

            List<VendorProductSkuStockLog> addStockLogs = addSkus.stream()
                    .map(sku -> VendorProductSkuStockLog.builder()
                            .vendorProductId(sku.getVendorProductId())
                            .vendorProductSkuId(sku.getId())
                            .operationType(VendorProductSkuStockOperationEnum.ADD.getCode())
                            .beforeStock(0)
                            .changeNum(sku.getSkuStock())
                            .afterStock(sku.getSkuStock())
                            .bizType(VendorProductSkuStockBizEnum.UPDATE.getCode())
                            .bizRemark("商品规格编辑：添加商品规格")
                            .vendorId(sku.getVendorId())
                            .build())
                    .toList();
            vendorProductSkuStockLogService.saveBatch(addStockLogs);
        }

        // 4.3. 修改商品规格
        if (CollUtil.isNotEmpty(updateSkus)) {
            this.updateBatchById(updateSkus);

            List<VendorProductSkuStockLog> addStockLogs = addSkus.stream()
                    .map(sku -> {
                        VendorProductSku dbSku = dbSkuMap.get(sku.getSkuAttrVal());

                        int changeNum = sku.getSkuStock() - dbSku.getSkuStock();
                        int operationType = changeNum > 0 ? VendorProductSkuStockOperationEnum.ADD.getCode() : VendorProductSkuStockOperationEnum.SUBTRACT.getCode();

                        return VendorProductSkuStockLog.builder()
                                .vendorProductId(sku.getVendorProductId())
                                .vendorProductSkuId(sku.getId())
                                .operationType(operationType)
                                .beforeStock(dbSku.getSkuStock())
                                .changeNum(changeNum)
                                .afterStock(sku.getSkuStock())
                                .bizType(VendorProductSkuStockBizEnum.UPDATE.getCode())
                                .bizRemark("商品规格编辑：修改商品规格")
                                .vendorId(sku.getVendorId())
                                .build();
                    })
                    .toList();
            vendorProductSkuStockLogService.saveBatch(addStockLogs);
        }

    }

    @Transactional
    @Override
    public void incStock(VendorProductSkuStockParam param) {
        VendorProductSku vendorProductSku = this.getById(param.getVendorProductSkuId());
        Assert.notNull(vendorProductSku, () -> new GlobalException("商品规格不存在"));

        Integer beforeStock = vendorProductSku.getSkuStock();
        int afterStock = beforeStock + param.getChangeNum();
        vendorProductSku.setSkuStock(afterStock);

        this.updateById(vendorProductSku);

        VendorProductSkuStockLog skuStockLog = VendorProductSkuStockLog.builder()
                .vendorProductId(vendorProductSku.getVendorProductId())
                .vendorProductSkuId(vendorProductSku.getId())
                .operationType(VendorProductSkuStockOperationEnum.ADD.getCode())
                .beforeStock(beforeStock)
                .changeNum(param.getChangeNum())
                .afterStock(afterStock)
                .bizType(param.getBizType().getCode())
                .bizRemark(param.getBizRemark())
                .vendorId(vendorProductSku.getVendorId())
                .build();
        vendorProductSkuStockLogService.save(skuStockLog);
    }

    @Transactional
    @Override
    public void incStockAndSalesVolume(VendorProductSkuStockParam param) {
        VendorProductSku vendorProductSku = this.getById(param.getVendorProductSkuId());
        Assert.notNull(vendorProductSku, () -> new GlobalException("商品规格不存在"));

        Integer beforeStock = vendorProductSku.getSkuStock();
        int afterStock = beforeStock + param.getChangeNum();
        vendorProductSku.setSkuStock(afterStock);

        // 处理销量
        Integer beforeSalesVolume = vendorProductSku.getSalesVolume();
        Integer afterSalesVolume = beforeSalesVolume - param.getChangeNum();
        vendorProductSku.setSalesVolume(afterSalesVolume);

        this.updateById(vendorProductSku);

        VendorProductSkuStockLog skuStockLog = VendorProductSkuStockLog.builder()
                .vendorProductId(vendorProductSku.getVendorProductId())
                .vendorProductSkuId(vendorProductSku.getId())
                .operationType(VendorProductSkuStockOperationEnum.ADD.getCode())
                .beforeStock(beforeStock)
                .changeNum(param.getChangeNum())
                .afterStock(afterStock)
                .bizType(param.getBizType().getCode())
                .bizRemark(param.getBizRemark())
                .vendorId(vendorProductSku.getVendorId())
                .build();
        vendorProductSkuStockLogService.save(skuStockLog);
    }

    @Transactional
    @Override
    public void batchIncStock(List<VendorProductSkuStockParam> params) {
        for (VendorProductSkuStockParam param : params) {
            this.incStock(param);
        }
    }

    @Transactional
    @Override
    public void decStock(VendorProductSkuStockParam param) {
        VendorProductSku vendorProductSku = this.getById(param.getVendorProductSkuId());
        Assert.notNull(vendorProductSku, () -> new GlobalException("商品规格不存在"));

        Assert.isTrue(vendorProductSku.getSkuStock() >= param.getChangeNum(), () -> new GlobalException("商品规格库存不足"));

        Integer beforeStock = vendorProductSku.getSkuStock();
        int afterStock = beforeStock - param.getChangeNum();
        vendorProductSku.setSkuStock(afterStock);
        this.updateById(vendorProductSku);

        VendorProductSkuStockLog skuStockLog = VendorProductSkuStockLog.builder()
                .vendorProductId(vendorProductSku.getVendorProductId())
                .vendorProductSkuId(vendorProductSku.getId())
                .operationType(VendorProductSkuStockOperationEnum.SUBTRACT.getCode())
                .beforeStock(beforeStock)
                .changeNum(param.getChangeNum())
                .afterStock(afterStock)
                .bizType(param.getBizType().getCode())
                .bizRemark(param.getBizRemark())
                .vendorId(vendorProductSku.getVendorId())
                .build();
        vendorProductSkuStockLogService.save(skuStockLog);
    }

    @Override
    public void decStockAndSalesVolume(VendorProductSkuStockParam param) {
        VendorProductSku vendorProductSku = this.getById(param.getVendorProductSkuId());
        Assert.notNull(vendorProductSku, () -> new GlobalException("商品规格不存在"));

        Assert.isTrue(vendorProductSku.getSkuStock() >= param.getChangeNum(), () -> new GlobalException("商品规格库存不足"));

        Integer beforeStock = vendorProductSku.getSkuStock();
        int afterStock = beforeStock - param.getChangeNum();
        vendorProductSku.setSkuStock(afterStock);

        // 处理销量
        Integer beforeSalesVolume = vendorProductSku.getSalesVolume();
        Integer afterSalesVolume = beforeSalesVolume + param.getChangeNum();
        vendorProductSku.setSalesVolume(afterSalesVolume);

        this.updateById(vendorProductSku);

        VendorProductSkuStockLog skuStockLog = VendorProductSkuStockLog.builder()
                .vendorProductId(vendorProductSku.getVendorProductId())
                .vendorProductSkuId(vendorProductSku.getId())
                .operationType(VendorProductSkuStockOperationEnum.SUBTRACT.getCode())
                .beforeStock(beforeStock)
                .changeNum(param.getChangeNum())
                .afterStock(afterStock)
                .bizType(param.getBizType().getCode())
                .bizRemark(param.getBizRemark())
                .vendorId(vendorProductSku.getVendorId())
                .build();
        vendorProductSkuStockLogService.save(skuStockLog);
    }

    @Transactional
    @Override
    public void batchDecStock(List<VendorProductSkuStockParam> params) {
        for (VendorProductSkuStockParam param : params) {
            this.decStock(param);
        }
    }
}




