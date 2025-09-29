package com.tigshop.service.vendor.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tigshop.bean.model.vendor.product.VendorProductGallery;
import com.tigshop.bean.param.vendor.product.VendorProductEditParam;
import com.tigshop.bean.param.vendor.product.VendorProductSaveParam;
import com.tigshop.mapper.vendor.product.VendorProductGalleryMapper;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.vendor.VendorProductGalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author kidd
 */
@RequiredArgsConstructor
@Service
public class VendorProductGalleryServiceImpl extends ServiceImpl<VendorProductGalleryMapper, VendorProductGallery> implements VendorProductGalleryService {

    private final GalleryPicService galleryPicService;

    @Transactional
    @Override
    public void saveBatchGalleries(VendorProductSaveParam param, Long vendorProductId) {
        List<VendorProductGallery> vendorProductGalleries = param.createVendorProductGalleries(vendorProductId);

        // 处理图片清晰度
        String size400 = galleryPicService.getImageSize().get("size_400");
        String size800 = galleryPicService.getImageSize().get("size_800");
        vendorProductGalleries.forEach(productGallery -> {
            productGallery.setPicUrl(productGallery.getPicOriginal() + size400);
            productGallery.setPicLarge(productGallery.getPicOriginal() + size800);
        });

        this.saveBatch(vendorProductGalleries);
    }

    @Transactional
    @Override
    public void updateBatchGalleries(VendorProductEditParam param, Long vendorProductId) {
        List<VendorProductGallery> updateVendorProductGalleries = param.updateVendorProductGalleries(vendorProductId);
        this.updateBatchById(updateVendorProductGalleries);

        this.saveBatchGalleries(param, vendorProductId);
    }
}




