// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.mapper.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.query.shop.PickupPageQuery;
import com.tigshop.bean.query.shop.StorePageQuery;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.o2o.StoreListVO;
import com.tigshop.mapper.common.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 店铺表映射
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:03
 */
public interface ShopMapper extends BaseMapper<Shop> {
    Page<StoreListVO> storeListByLocation(@Param("page") Page<Shop> page,
                                          @Param("query") StorePageQuery pageQuery);

    Page<StoreListVO> storeListByRegion(@Param("page") Page<Shop> page,
                                        @Param("query") StorePageQuery pageQuery);

    Page<PickupListVO> pickupListByLocation(@Param("page") Page<Shop> page,
                                            @Param("query") PickupPageQuery query);

}
