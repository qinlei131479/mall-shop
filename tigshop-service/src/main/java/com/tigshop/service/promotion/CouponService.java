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

package com.tigshop.service.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.cart.CartPromotionParsePriceDTO;
import com.tigshop.bean.dto.promotion.CouponDTO;
import com.tigshop.bean.query.promotion.coupon.CouponPageQuery;
import com.tigshop.bean.model.promotion.Coupon;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.CouponClientVO;
import com.tigshop.bean.vo.promotion.CouponDetailVO;
import com.tigshop.bean.vo.promotion.CouponVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 优惠券服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface CouponService extends BaseService<Coupon> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<CouponVO> list(CouponPageQuery listDTO);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    CouponVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(CouponDTO createDTO);

    /**
     * 更新
     */
    void update(CouponDTO updateDTO);

    /**
     * 配置
     * @return ItemVO
     */
    List<UserRank> config();

    /**
     * 优惠券列表
     */
    Page<CouponClientVO> getList(CouponPageQuery pageQuery);

    /**
     * 获取商品优惠券列表
     * @param productId 商品ID
     * @param shopId 店铺ID
     * @return List<CouponDetailVO>
     */
    List<CouponDetailVO> getProductCouponList(int productId, int shopId);

    /**
     * 解析价格
     *
     * @param cartList    cartList
     * @param promotionVO 优惠数据
     */
    CartPromotionParsePriceDTO parsePrice(List<CartVO> cartList, PromotionVO promotionVO);

    String getCouponPromotionDesc(Coupon coupon);
}
