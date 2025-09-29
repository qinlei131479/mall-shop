package com.tigshop.service.finance.impl;

import cn.hutool.core.date.DateUtil;
import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.mapper.finance.RecordRateMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.vendor.VendorMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.setting.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.tigshop.bean.enums.setting.SettingsEnum.SUPPLIER_GENERAL_SERVICE_FEE_RATE;

/**
 * 费率记录接口实现
 *
 * @author Tigshop项目组
 * @create 2025年08月15日 09:45
 */
@Service
@RequiredArgsConstructor
public class RecordRateServiceImpl extends BaseServiceImpl<RecordRateMapper, RecordRate> implements RecordRateService {

    private final ShopMapper shopMapper;
    private final VendorMapper vendorMapper;
    private final ConfigService configService;
    private final TigshopProperties tigshopProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRecordRate(int recordId, int recordType, int shopId, int vendorId) {
        RecordRate recordRate = new RecordRate();
        recordRate.setRecordId(recordId);
        recordRate.setRecordType(recordType);
        recordRate.setShopId(shopId);
        recordRate.setVendorId(vendorId);

        String shopServiceFeeRateVal = configService.getConfigVal(SettingsEnum.STORE_GENERAL_SERVICE_FEE_RATE);
        BigDecimal shopServiceFeeRate = new BigDecimal(shopServiceFeeRateVal);

        String shopFeeRateVal = configService.getConfigVal(SettingsEnum.STORE_WITHDRAWAL_FEE_RATE);
        BigDecimal feeRate = new BigDecimal(shopFeeRateVal);

        String shopfrontServiceFeeRateVal = configService.getConfigVal(SettingsEnum.STOREFRONT_GENERAL_SERVICE_FEE_RATE);
        BigDecimal shopfrontServiceFeeRate = new BigDecimal(shopfrontServiceFeeRateVal);
        String shopfrontFeeRateVal = configService.getConfigVal(SettingsEnum.STOREFRONT_WITHDRAWAL_FEE_RATE);
        BigDecimal shopfrontFeeRate = new BigDecimal(shopfrontFeeRateVal);

        if (shopId > 0) {
            Shop shop = shopMapper.selectById(shopId);

            if (shop.getShopType() == ShopTypeEnum.STORE.getCode()) {
                shopServiceFeeRate = shopfrontServiceFeeRate;
                feeRate = shopfrontFeeRate;
            }

            // 店铺的服务费率
            if (shop.getServiceFeeRate() != null && shop.getServiceFeeRate().compareTo(BigDecimal.ZERO) >= 0) {
                shopServiceFeeRate = shop.getServiceFeeRate();
            }

            // 店铺的手续费率
            if (shop.getFeeRate() != null && shop.getFeeRate().compareTo(BigDecimal.ZERO) >= 0) {
                feeRate = shop.getFeeRate();
            }
        }
        recordRate.setShopServiceFee(shopServiceFeeRate);
        recordRate.setShopWithdrawalFee(feeRate);

        String vendorServiceFeeVal = configService.getConfigVal(SUPPLIER_GENERAL_SERVICE_FEE_RATE);
        BigDecimal vendorServiceFeeRate = new BigDecimal(vendorServiceFeeVal);

        String vendorFeeRateVal = configService.getConfigVal(SettingsEnum.SUPPLIER_WITHDRAWAL_FEE_RATE);
        BigDecimal vendorFeeRate = new BigDecimal(vendorFeeRateVal);
        if (vendorId > 0) {
            Vendor vendor = vendorMapper.selectById(vendorId);
            // 供应商的管理费
            if (vendor.getServiceFeeRate() != null && vendor.getServiceFeeRate().compareTo(BigDecimal.ZERO) >= 0) {
                vendorServiceFeeRate = vendor.getServiceFeeRate();
            }
            // 供应商提现手续费
            if (vendor.getFeeRate() != null && vendor.getFeeRate().compareTo(BigDecimal.ZERO) >= 0) {
                vendorFeeRate = vendor.getFeeRate();
            }
        }
        recordRate.setSupplierServiceFee(vendorServiceFeeRate);
        recordRate.setSupplierWithdrawalFee(vendorFeeRate);

        recordRate.setGmtCreate(DateUtil.now());
        this.save(recordRate);
    }
}
