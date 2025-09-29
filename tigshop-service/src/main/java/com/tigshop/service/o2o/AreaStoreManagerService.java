package com.tigshop.service.o2o;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.o2o.AreaStoreManager;
import com.tigshop.bean.param.o2o.*;
import com.tigshop.bean.vo.o2o.AreaStoreManagerVO;
import com.tigshop.bean.vo.shop.ShopVO;
import com.tigshop.service.common.BaseService;

/**
* @author Admin
* @description 针对表【area_store_manager(区域门店管理)】的数据库操作Service
* @createDate 2025-08-11 17:24:00
*/
public interface AreaStoreManagerService extends BaseService<AreaStoreManager> {

    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return Page<AreaStoreManagerVO>
     */
    Page<AreaStoreManagerVO> list(AreaStoreManagerQueryPage listDTO);

    /**
     * 详情
     *
     * @param id 区域ID
     * @return AreaStoreManagerVO
     */
    AreaStoreManagerVO detail(Integer id);

    /**
     * 创建区域
     *
     * @param createDTO 创建参数
     */
    void create(AreaStoreManagerCreateParam createDTO);

    /**
     * 更新区域
     *
     * @param updateDTO 更新参数
     */
    void update(AreaStoreManagerUpdateParam updateDTO);

    /**
     * 删除区域
     *
     * @param id 区域ID
     */
    boolean del(Integer id);

    /**
     * 绑定店铺
     *
     * @param bindShopDTO 绑定参数
     */
    void bindShop(AreaStoreBindShopParam bindShopDTO);

    /**
     * 删除店铺
     *
     * @param removeShopDTO 删除参数
     */
    void removeShop(AreaStoreRemoveShopParam removeShopDTO);

    /**
     * 获取区域关联的店铺列表
     *
     * @param listDTO 查询参数
     * @return Page<ShopVO>
     */
    Page<ShopVO> getAreaShopList(AreaStoreShopListQueryPage listDTO);
}
