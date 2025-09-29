package com.tigshop.service.vendor.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.vendor.product.VendorProductVideo;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;
import com.tigshop.mapper.vendor.product.VendorProductVideoMapper;
import com.tigshop.service.vendor.VendorProductVideoService;
import org.springframework.stereotype.Service;

/**
* @author kidd
*/
@Service
public class VendorProductVideoServiceImpl extends ServiceImpl<VendorProductVideoMapper, VendorProductVideo> implements VendorProductVideoService {

    @Override
    public void create(VendorProductSaveParam param, Long vendorProductId) {
        VendorProductVideo vendorProductVideo = param.createVendorProductVideo(vendorProductId);
        this.baseMapper.insert(vendorProductVideo);
    }

    @Override
    public void update(VendorProductEditParam param, Long vendorProductId) {
        this.lambdaUpdate().eq(VendorProductVideo::getVendorProductId, vendorProductId).remove();
        this.create(param, vendorProductId);
    }
}




