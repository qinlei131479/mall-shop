package com.tigshop.mapper.o2o;

import com.tigshop.bean.model.o2o.StoreProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author kidd
 * @description 针对表【store_product(分配店铺商品)】的数据库操作Mapper
 * @createDate 2025-08-25 13:08:31
 * @Entity com.tigshop.bean.model.o2o.StoreProduct
 */
@Mapper
public interface StoreProductMapper extends BaseMapper<StoreProduct>, com.tigshop.mapper.common.BaseMapper<StoreProduct> {

}




