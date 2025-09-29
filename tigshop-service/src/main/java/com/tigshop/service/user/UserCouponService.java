// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserCouponCreateDTO;
import com.tigshop.bean.dto.user.UserCouponListDTO;
import com.tigshop.bean.dto.user.UserCouponUpdateDTO;
import com.tigshop.bean.model.user.UserCoupon;
import com.tigshop.bean.param.user.UserCouponClaimParam;
import com.tigshop.bean.vo.order.OrderCheckVO;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.user.UserCouponVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 用户优惠券服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface UserCouponService extends BaseService<UserCoupon> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<UserCouponVO> list(UserCouponListDTO listDTO);

    /**
     * 详情
     */
    CouponDetailVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(UserCouponCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(UserCouponUpdateDTO updateDTO);

    /**
     * 获取用户优惠券列表
     *
     * @param shopId 店铺id
     * @return List
     */
    List<UserCoupon> getUserShopCouponList(Integer shopId);

    /**
     * 领取优惠券
     */
    void claim(UserCouponClaimParam param);

    /*
     * 使用优惠券
     */
    void useCoupon(List<Integer> useCouponIds, Integer currentUserId, Integer orderId);

    /**
     * 获取用户优惠券数量
     *
     * @param userId 用户id
     * @return long
     */
    Long getUserCouponCount(int userId);

    /**
     * 根据优惠券id获取优惠券信息
     *
     * @param currentUserId 用户id
     * @param relationId    优惠券id
     * @return UserCoupon
     */
    UserCoupon getUserCouponByCouponId(Integer currentUserId, Integer relationId);

    List<OrderCheckVO.Coupon> listUserCouponList(Integer currentUserId, Integer shopId);
}
