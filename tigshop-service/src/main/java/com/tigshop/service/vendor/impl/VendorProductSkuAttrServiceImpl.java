package com.tigshop.service.vendor.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.vendor.product.VendorProductSkuAttr;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;
import com.tigshop.mapper.vendor.product.VendorProductSkuAttrMapper;
import com.tigshop.service.vendor.VendorProductSkuAttrService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author kidd
*/
@Service
public class VendorProductSkuAttrServiceImpl extends ServiceImpl<VendorProductSkuAttrMapper, VendorProductSkuAttr> implements VendorProductSkuAttrService {

    @Transactional
    @Override
    public void saveBatchSkuAttr(VendorProductSaveParam param, Long vendorProductId) {
        List<VendorProductSkuAttr> vendorProductSkuAttrs = param.createVendorProductSkuAttrs(vendorProductId);
        if (CollUtil.isEmpty(vendorProductSkuAttrs)) {
            return;
        }
        this.saveBatch(vendorProductSkuAttrs);
    }

    @Transactional
    @Override
    public void updateBatchSkuAttr(VendorProductEditParam param, Long vendorProductId) {
        this.lambdaUpdate().eq(VendorProductSkuAttr::getVendorProductId, vendorProductId).remove();
        this.saveBatchSkuAttr(param, vendorProductId);
    }
}




