package com.tigshop.service.vendor;

import com.tigshop.bean.model.vendor.product.VendorProductSkuAttr;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;

/**
* @author kidd
*/
public interface VendorProductSkuAttrService extends IService<VendorProductSkuAttr> {

    /**
     * 批量保存商品SKU属性
     */
    void saveBatchSkuAttr(VendorProductSaveParam param, Long vendorProductId);

    /**
     * 批量更新商品SKU属性
     */
    void updateBatchSkuAttr(VendorProductEditParam param, Long vendorProductId);
}
