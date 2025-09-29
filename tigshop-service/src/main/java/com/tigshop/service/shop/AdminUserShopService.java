// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.shop;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.AdminUserShopCreateDTO;
import com.tigshop.bean.query.shop.AdminUserShopPageQuery;
import com.tigshop.bean.dto.shop.AdminUserShopModifyDTO;
import com.tigshop.bean.dto.shop.AdminUserShopUpdateDTO;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.vo.shop.AdminUserShopVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 店铺服务
 *
 * @author Tigshop团队
 * @create 2024年12月30日 16:30
 */
public interface AdminUserShopService extends BaseService<AdminUserShop> {
    /**
     * 创建管理员店铺
     *
     * @param createDTO 创建参数
     */
    void createAdminUserShop(AdminUserShopCreateDTO createDTO);

    /**
     * 更新管理员店铺
     * @param updateDTO 更新参数
     */
    void updateAdminUserShop(AdminUserShopUpdateDTO updateDTO);

    /**
     * 删除
     * @param id id
     * @param adminId adminId
     */
    void delShop(Integer id, Integer adminId);

    /**
     * 修改店铺员工
     * @param modifyDTO 修改参数
     */
    void modifyUser(AdminUserShopModifyDTO modifyDTO);

    /**
     * 判断用户是否存在店铺
     * @param userId 用户id
     * @return boolean
     */
    boolean hasShop(Integer userId);

    /**
     * 获取店铺员工列表
     */
    Page<AdminUserShopVO> list(AdminUserShopPageQuery pageQuery);

    /**
     * 获取详情
     * @param id id
     * @return AdminUserShopVO
     */
    AdminUserShopVO detail(Integer id);

    /**
     * 获取详情
     * @param id id
     * @param shopId 商户id
     * @return AdminUserShopVO
     */
    AdminUserShopVO info(Integer id, Integer shopId);

    /**
     * 获取店铺id集合
     * @param adminId 管理员id
     * @return List
     */
    List<Integer> getShopIds(Integer adminId);

}
