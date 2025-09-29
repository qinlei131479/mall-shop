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

package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.PaylogCreateDTO;
import com.tigshop.bean.query.finance.PaylogListPageQuery;
import com.tigshop.bean.dto.finance.PaylogUpdateDTO;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.vo.finance.PaylogVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.service.common.BaseService;

/**
 * 交易日志服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface PaylogService extends BaseService<Paylog> {
    /**
     * 列表
     */
    Page<PaylogVO> list(PaylogListPageQuery pageQuery);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    PaylogVO detail(Integer id);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(PaylogCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(PaylogUpdateDTO updateDTO);

    Paylog creatPayLog(OrderVO detail, String type);
}
