package com.tigshop.service.vendor;

import com.tigshop.bean.model.vendor.product.VendorProductVideo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;

/**
* @author kidd
*/
public interface VendorProductVideoService extends IService<VendorProductVideo> {

    /**
     * 新增供货商商品视频
     */
    void create(VendorProductSaveParam param, Long vendorProductId);

    /**
     * 修改供货商商品视频
     */
    void update(VendorProductEditParam param, Long vendorProductId);
}
