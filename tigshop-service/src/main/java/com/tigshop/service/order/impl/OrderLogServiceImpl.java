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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.OrderLogCreateDTO;
import com.tigshop.bean.dto.order.OrderLogListDTO;
import com.tigshop.bean.dto.order.OrderLogUpdateDTO;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.order.OrderLog;
import com.tigshop.bean.model.order.OrderSpitLog;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.order.OrderLogVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.order.OrderLogMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderLogService;
import com.tigshop.service.order.OrderSpitLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 订单商品服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class OrderLogServiceImpl extends BaseServiceImpl<OrderLogMapper, OrderLog> implements OrderLogService {

    @Resource
    OrderSpitLogService orderSpitLogService;

    @Resource
    AdminUserService adminUserService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private OrderMapper orderMapper;

    @Override
    public Page<OrderLogVO> list(OrderLogListDTO listDTO) {
        // 分页
        Page<OrderLog> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<OrderLog> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        Integer orderId = listDTO.getOrderId();
        if (orderId != null) {
            List<Integer> orderIdArr = new ArrayList<>();
            this.getParentOrderIds(orderId, orderIdArr);
            queryWrapper.in(CollUtil.isNotEmpty(orderIdArr), OrderLog::getOrderId, orderIdArr);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(OrderLog::getDescription, keyword);
        }
        // 执行查询
        Page<OrderLog> orderLogPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<OrderLog> orderLogPageRecords = orderLogPage.getRecords();
        // 转换为VO
        List<OrderLogVO> orderLogVOList = orderLogPageRecords.stream()
                .map(orderLog -> {
                    OrderLogVO orderLogVO = new OrderLogVO();
                    BeanUtils.copyProperties(orderLog, orderLogVO);

                    DateTime logDate = DateUtil.date(orderLog.getLogTime() * 1000);
                    orderLogVO.setLogTime(DateUtil.format(logDate, DATE_FORMAT));

                    if (orderLog.getAdminId() != null && orderLog.getAdminId() > 0) {
                        AdminUser adminUser = adminUserService.getById(orderLog.getAdminId());
                        if (adminUser != null) {
                            orderLogVO.setOperator(adminUser.getUsername());
                        }
                    } else {
                        User user = userMapper.selectById(orderLog.getUserId());
                        if (user != null) {
                            orderLogVO.setOperator(user.getUsername());
                        }
                    }
                    return orderLogVO;
                }).toList();
        return PageUtil.convertPage(orderLogPage, orderLogVOList);
    }

    private void getParentOrderIds(Integer orderId, List<Integer> orderIdArr) {
        orderIdArr.add(orderId);
        OrderSpitLog orderSpitLog = orderSpitLogService.getByOrderId(orderId);
        if (orderSpitLog != null && orderSpitLog.getParentOrderId() != null && !orderIdArr.contains(orderSpitLog.getParentOrderId())) {
            this.getParentOrderIds(orderSpitLog.getParentOrderId(), orderIdArr);
        }
    }

    @Override
    public OrderLogVO detail(Integer id) {
        if (id != null) {
            OrderLog orderLog = this.getById(id);
            OrderLogVO orderLogVO = new OrderLogVO();
            BeanUtils.copyProperties(orderLog, orderLogVO);
            return orderLogVO;
        }
        return new OrderLogVO();
    }

    @Override
    public boolean create(OrderLogCreateDTO createDTO) {
        if (createDTO != null) {
            OrderLog orderLog = new OrderLog();
            BeanUtils.copyProperties(createDTO, orderLog);
            orderLog.setLogTime(StringUtils.getCurrentTime());
            if (SecurityUtils.isAdminUser()) {
                orderLog.setAdminId(SecurityUtils.getCurrentAdminId());
            } else {
                orderLog.setUserId(SecurityUtils.getCurrentUserId());
            }
            return this.save(orderLog);
        }
        return false;
    }

    @Override
    public void quickCreate(String description, Integer orderId, String orderSn) {
        Order order = orderMapper.selectById(orderId);
        OrderLogCreateDTO orderLogCreateDTO = new OrderLogCreateDTO();
        orderLogCreateDTO.setOrderId(orderId);
        orderLogCreateDTO.setDescription(description);
        orderLogCreateDTO.setOrderSn(orderSn);
        orderLogCreateDTO.setShopId(order.getShopId());
        this.create(orderLogCreateDTO);
    }

    @Override
    public boolean update(OrderLogUpdateDTO updateDTO) {
        if (updateDTO != null) {
            OrderLog orderLog = new OrderLog();
            BeanUtils.copyProperties(updateDTO, orderLog);
            return this.updateById(orderLog);
        }
        return false;
    }
}
