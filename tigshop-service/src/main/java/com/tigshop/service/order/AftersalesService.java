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

package com.tigshop.service.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.*;
import com.tigshop.bean.model.order.Aftersales;
import com.tigshop.bean.param.order.AftersalesApplyParam;
import com.tigshop.bean.param.order.AftersalesEditParam;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.vo.order.AfterSalesClientDetailVO;
import com.tigshop.bean.vo.order.AftersalesVO;
import com.tigshop.service.common.BaseService;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
public interface AftersalesService extends BaseService<Aftersales> {
    /**
     * 列表
     */
    Page<AftersalesVO> list(AftersalesListPageQuery pageQuery);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    AftersalesVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(AftersalesApplyParam createDTO);

    /**
     * 更新
     */
    void update(AftersalesEditParam param);

    /**
     * 接收
     * @param id 主键
     * @return boolean
     */
    boolean receive(Integer id);

    /**
     * 提交反馈记录
     * @param aftersaleId 售后id
     * @param aftersalesRecordCreateDTO 售后参数
     * @param currentAdminId 当前管理员id
     * @return boolean
     */
    boolean submitFeedbackRecord(Integer aftersaleId, AftersalesRecordCreateDTO aftersalesRecordCreateDTO, Integer currentAdminId);

    /**
     * 售后申请
     */
    void afterSalesApply(AftersalesApplyParam param);

    /**
     * 售后申请修改
     */
    void afterSalesApplyUpdate(ClientAfterSalesUpdateDTO updateDTO);

    /**
     * 售后详情
     * @param id 主键
     * @return AfterSalesClientDetailVO
     */
    AfterSalesClientDetailVO clientDetail(Integer id);

    /**
     * 提交售后反馈记录
     * @param dto 参数
     * @return boolean
     */
    boolean submitFeedbackRecord(AfterSalesFeedbackDTO dto);

    /**
     * 取消售后申请
     * @param dto 取消参数
     * @return boolean
     */
    boolean cancel(AfterSalesCancelDTO dto);

    /**
     * 完结售后
     * @param completeDTO
     * @return
     */
    void complete(AfterSalesCompleteDTO completeDTO);
}
