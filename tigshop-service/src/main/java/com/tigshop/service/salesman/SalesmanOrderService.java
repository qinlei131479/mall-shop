// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.salesman;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.salesman.SalesmanOrderPageQuery;
import com.tigshop.bean.model.order.OrderItem;
import com.tigshop.bean.model.salesman.SalesmanOrder;
import com.tigshop.bean.query.salesman.CustomerTransactionPageQuery;
import com.tigshop.bean.vo.salesman.CustomerTransactionOrderVO;
import com.tigshop.bean.vo.salesman.SalesmanOrderVO;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * 分销订单服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface SalesmanOrderService extends BaseService<SalesmanOrder> {

    /**
     * 列表
     */
    Page<SalesmanOrderVO> list(SalesmanOrderPageQuery pageQuery, HttpServletResponse response);


    /**
     * 新增分销订单
     *
     * @param orderItemList 订单项列表
     * @param salesmanOrder 分销订单
     */
    void addSalesmanOrder(List<OrderItem> orderItemList, Map<String, Integer> salesmanOrder) ;

    /**
     * 新增订单项
     *
     * @param item      订单项
     * @param salesmanId 分销商id
     */
    void addNewOrderItem(OrderItem item, Integer salesmanId, boolean isParent);

    /**
     * 成交客户列表
     * @param customerTransactionPageQuery 查询参数
     * @return Page
     */
    Page<CustomerTransactionOrderVO> listCustomerTransaction(CustomerTransactionPageQuery customerTransactionPageQuery);

}
