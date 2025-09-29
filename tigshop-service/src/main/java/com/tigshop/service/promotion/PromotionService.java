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
import com.tigshop.bean.dto.promotion.PromotionCreateDTO;
import com.tigshop.bean.dto.promotion.PromotionListDTO;
import com.tigshop.bean.dto.promotion.PromotionUpdateDTO;
import com.tigshop.bean.model.promotion.Promotion;
import com.tigshop.bean.vo.promotion.PromotionCountVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.service.common.BaseService;

import java.util.List;

/**
 * 营销管理服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface PromotionService extends BaseService<Promotion> {
    /**
     * 列表
     *
     * @param listDTO 查询参数
     * @return ListResVO
     */
    Page<PromotionVO> list(PromotionListDTO listDTO);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(PromotionCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(PromotionUpdateDTO updateDTO);

    /**
     * 获取活动数量
     * @return map
     */
    PromotionCountVO getCount();

    /**
     * 获取所有可用的营销活动
     *
     * @param shopId 店铺ID
     * @return List<Promotion>
     */
    List<PromotionVO> getAllAvailablePromotion(Integer shopId);
}
