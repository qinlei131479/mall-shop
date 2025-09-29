package com.tigshop.service.o2o;

import com.tigshop.bean.model.o2o.ShopPickupTpl;
import com.tigshop.bean.param.o2o.ShopPickupConfigParam;
import com.tigshop.bean.vo.o2o.ShopPickupConfigVO;
import com.tigshop.service.common.BaseService;

/**
* @author Admin
* @description 针对表【shop_pickup_tpl(门店自提模板表)】的数据库操作Service
* @createDate 2025-08-21 10:07:40
*/
public interface ShopPickupTplService extends BaseService<ShopPickupTpl> {

    void initPickUpTpl(Integer shopId);

    ShopPickupConfigVO getShopPickupTpl(Integer shopId);

    void saveShopPickupTpl(ShopPickupConfigParam param);
}
