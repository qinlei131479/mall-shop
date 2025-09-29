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
import com.tigshop.bean.dto.order.deliver.BatchDeliverData;
import com.tigshop.bean.dto.order.deliver.DeliverOrderParam;
import com.tigshop.bean.feign.shipping.ShippingResult;
import com.tigshop.bean.model.order.LogisticsApiLog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.param.order.ExportItemSaveParam;
import com.tigshop.bean.param.order.OrderAdminNoteParam;
import com.tigshop.bean.param.order.OrderChangeStatusParam;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.query.order.OrderListPageQuery;
import com.tigshop.bean.query.panel.SalesProductDetailPageQuery;
import com.tigshop.bean.vo.order.*;
import com.tigshop.bean.vo.panel.SalesProductOrderItemVO;
import com.tigshop.service.common.BaseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单管理服务
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:04
 */
public interface OrderService extends BaseService<Order> {

    /**
     * 列表
     */
    Page<OrderVO> list(OrderListPageQuery pageQuery);

    /**
     * 详情
     *
     * @param id 主键
     * @return ItemVO
     */
    OrderVO detail(Integer id);

    /**
     * 前台订单详情
     */
    OrderVO detail(Integer id, Integer userId);

    /**
     * 订单数量
     *
     * @return OrderNumVO
     */
    OrderNumVO orderNum();

    /**
     * @param order
     * @param newItems
     * @param isSpiltStore
     * @param shopId
     * @return
     */
    Order createSplitOrder(OrderVO order, List<OrderItemVO> newItems, boolean isSpiltStore, Integer shopId, boolean isSingleShopSingleVendor);

    /**
     * 订单进度条
     *
     * @param orderVO 订单
     * @return OrderStepVO
     */
    OrderStepVO stepStatus(OrderVO orderVO);

    /**
     * 创建
     *
     * @param createDTO 创建参数
     * @return boolean
     */
    boolean create(OrderCreateDTO createDTO);

    /**
     * 更新
     *
     * @param updateDTO 更新参数
     * @return boolean
     */
    boolean update(OrderUpdateDTO updateDTO);

    List<OrderItemVO> getOrderItemList(List<Integer> orderIds);

    /**
     * 根据order数据生成可操作项
     *
     * @param orderVO 订单
     * @return OrderActionVO
     */
    OrderActionVO availableActions(OrderVO orderVO);

    /**
     * 设置管理员备注
     */
    void setAdminNote(@RequestBody OrderAdminNoteParam param);

    /**
     * 查询不同订单的状态数量
     *
     * @param userId 用户id
     * @param type   不同类型
     * @param status 不同状态
     * @return Long
     */
    Long getOrderStatusCount(int userId, String type, int status);

    boolean setOrderConfirm(Integer id);

    boolean splitStoreOrder(Integer id);

    void setOrderPaid(Integer id);

    boolean cancelOrder(Integer id);

    /**
     * 前台取消订单
     *
     * @param id
     * @param userId
     * @return
     */
    boolean cancelOrder(Integer id, Integer userId);

    boolean delOrder(Integer id);

    /**
     * 前台删除订单
     *
     * @param id
     * @param userId
     * @return
     */
    boolean delOrder(Integer id, Integer userId);

    boolean modifyOrderMoney(OrderModifyMoneyDTO feeData);

    boolean modifyOrderConsignee(OrderModifyConsigneeDTO modifyConsigneeDTO);

    boolean confirmReceipt(Integer orderId);

    /**
     * 前台确认收货
     *
     * @param id
     * @param userId
     * @return
     */
    boolean confirmReceipt(Integer id, Integer userId);

    void settlement(Integer orderId, Integer settlementType);

    void vendorSettlement(Integer orderId, Integer vendorId, Integer shopId);

    boolean modifyOrderShipping(OrderModifyShippingDTO modifyShippingDTO);

    /**
     * 物流信息
     *
     * @param id
     * @return
     */
    ShippingResult getShippingInfo(Integer id);

    void batchOperation(Integer id, String type, BatchDeliverData data);

    List<OrderVO> printSeveral(List<Integer> ids);

    void deliverOrder(DeliverOrderParam param);

    OrderVO getOrderPrintInfo(Integer id);

    LogisticsApiLog getOrderPrintWaybillInfo(Integer id);

    List<OrderVO> getSeveralDetail(List<Integer> ids, Integer suppliersId);

    boolean modifyOrderProduct(OrderModifyProductDTO modifyProductDTO);

    BigDecimal getOrderUnSettlementAmount(Integer shopId);

    BigDecimal getOrderUnSettlementAmountByVendor(Integer vendorId);

    BigDecimal getTodayOrderSettlementAmountByVendor(Integer vendorId, Long todayStart, Long todayEnd);

    Map<String, String> getExportItemList();

    /**
     * 保存导出标签信息
     */
    void saveExportItem(ExportItemSaveParam param);

    Map<String, String> exportItemInfo();

    /**
     * 订单导出
     */
    void orderExport(OrderListPageQuery pageQuery, HttpServletResponse response);

    void updateOrderMoney(Order order);

    void addLog(Integer orderId, String desc);

    /**
     * 售后列表
     */
    Page<AfterSalesClientVO> afterSalesList(AftersalesListPageQuery listDTO);

    /**
     * 售后详情
     */
    List<AfterSalesDetailVO> applyData(Integer itemId, Integer orderId);

    /**
     * 获取新订单数量
     */
    Long getNewOrderCount(Long startTime);

    /**
     * 获取订单余额
     */
    BigDecimal getOrderBalance(String startTime, String endTime, String type);

    /**
     * 销售商品明细
     */
    Page<SalesProductOrderItemVO> getSalesProductDetail(SalesProductDetailPageQuery pageQuery);

    /**
     * 获取订单列表配置
     */
    OrderPageConfigVO getOrderPageConfig();

    /**
     * 修改订单状态
     */
    void changeOrderStatus(OrderChangeStatusParam param);

    /**
     * 催发货
     */
    void remindDeliver(Integer id);
}
