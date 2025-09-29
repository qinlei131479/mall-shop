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

package com.tigshop.service.order.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderSpitLogCreateDTO;
import com.tigshop.bean.dto.order.OrderSpitLogListDTO;
import com.tigshop.bean.dto.order.OrderSpitLogUpdateDTO;
import com.tigshop.bean.model.order.OrderSpitLog;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.OrderSpitLogVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.order.OrderSpitLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderSpitLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单拆分服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class OrderSpitLogServiceImpl extends BaseServiceImpl<OrderSpitLogMapper, OrderSpitLog> implements OrderSpitLogService {
    @Override
    public ListResVO<OrderSpitLogVO, OrderSpitLogListDTO> list(OrderSpitLogListDTO listDTO) {
        // 分页
        Page<OrderSpitLog> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<OrderSpitLog> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq("is_show", isShow);
        }*/

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("order_split_log", keyword);
        }*/
        // 执行查询
        Page<OrderSpitLog> orderSpitLogPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<OrderSpitLog> orderSpitLogPageRecords = orderSpitLogPage.getRecords();
        // 转换为VO
        List<OrderSpitLogVO> orderSpitLogVOList = orderSpitLogPageRecords.stream()
                .map(orderSpitLog -> {
                    OrderSpitLogVO orderSpitLogVO = new OrderSpitLogVO();
                    BeanUtils.copyProperties(orderSpitLog, orderSpitLogVO);
                    return orderSpitLogVO;
                }).toList();
        return new ListResVO<>(orderSpitLogVOList, listDTO, orderSpitLogPage.getTotal());
    }

    @Override
    public OrderSpitLogVO detail(Integer id) {
        if (id != null) {
            OrderSpitLog orderSpitLog = this.getById(id);
            OrderSpitLogVO orderSpitLogVO = new OrderSpitLogVO();
            BeanUtils.copyProperties(orderSpitLog, orderSpitLogVO);
            return orderSpitLogVO;
        }
        return new OrderSpitLogVO();
    }

    @Override
    public OrderSpitLog getByOrderId(Integer orderId) {
        LambdaQueryWrapper<OrderSpitLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderSpitLog::getOrderId, orderId);
        return this.getOne(queryWrapper);
    }

    @Override
    public JSONObject getParentDataByOrderId(Integer orderId) {
        OrderSpitLog orderSpitLog = this.getByOrderId(orderId);
        if (orderSpitLog != null && StrUtil.isNotEmpty(orderSpitLog.getParentOrderData())) {
            return JsonUtil.fromJson(orderSpitLog.getParentOrderData(), JSONObject.class);
        } else {
            return null;
        }
    }

    @Override
    public boolean create(OrderSpitLogCreateDTO createDTO) {
        if (createDTO != null) {
            OrderSpitLog orderSpitLog = new OrderSpitLog();
            BeanUtils.copyProperties(createDTO, orderSpitLog);
            return this.save(orderSpitLog);
        }
        return false;
    }

    @Override
    public boolean update(OrderSpitLogUpdateDTO updateDTO) {
        if (updateDTO != null) {
            OrderSpitLog orderSpitLog = new OrderSpitLog();
            BeanUtils.copyProperties(updateDTO, orderSpitLog);
            return this.updateById(orderSpitLog);
        }
        return false;
    }

    @Override
    public void addSplitLog(Integer orderId, Integer newOrderId, OrderVO order) {
        OrderSpitLog splitLog = new OrderSpitLog();
        splitLog.setParentOrderId(orderId);
        splitLog.setOrderId(newOrderId);
        // 设置当前时间为拆分时间
        splitLog.setSplitTime(StringUtils.getCurrentTime());
        // 将订单对象转换为JSON字符串
        splitLog.setParentOrderData(JsonUtil.toJson(order));
        this.save(splitLog);
    }
}
