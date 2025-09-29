package com.tigshop.service.vendor;

import com.tigshop.bean.model.vendor.product.VendorProductGallery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;

/**
* @author kidd
*/
public interface VendorProductGalleryService extends IService<VendorProductGallery> {

    /**
     * 保存商品相册
     */
    void saveBatchGalleries(VendorProductSaveParam param, Long vendorProductId);

    /**
     * 修改商品相册
     */
    void updateBatchGalleries(VendorProductEditParam param, Long vendorProductId);
}
