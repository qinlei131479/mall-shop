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

package com.tigshop.service.finance.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.PaylogCreateDTO;
import com.tigshop.bean.dto.finance.PaylogUpdateDTO;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.query.finance.PaylogListPageQuery;
import com.tigshop.bean.vo.finance.PaylogVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.finance.PaylogMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.PaylogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.finance.PaylogConstants.PAY_STATUS_NAME;

/**
 * 交易日志服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class PaylogServiceImpl extends BaseServiceImpl<PaylogMapper, Paylog> implements PaylogService {

    private final OrderMapper orderMapper;

    @Override
    public Page<PaylogVO> list(PaylogListPageQuery pageQuery) {
        Page<Paylog> page = buildSortOrder(pageQuery);

        Integer payStatus = pageQuery.getPayStatus();
        boolean payStatusFlag = payStatus != null && payStatus > -1;

        String keyword = pageQuery.getKeyword();

        Page<Paylog> paylogPage = this.lambdaQuery()
                .eq(payStatusFlag, Paylog::getPayStatus, payStatus)
                .and(StrUtil.isNotBlank(keyword), query -> query.like(Paylog::getOrderSn, keyword).or().like(Paylog::getPaySn, keyword))
                .page(page);

        List<Paylog> paylogPageRecords = paylogPage.getRecords();
        if (CollUtil.isEmpty(paylogPageRecords)) {
            return new Page<>();
        }

        List<Integer> orderIds = paylogPageRecords.stream().map(Paylog::getOrderId).toList();
        List<Order> orders = orderMapper.selectBatchIds(orderIds);
        Map<Integer, String> orderConsigneeMap = orders.stream().collect(Collectors.toMap(Order::getOrderId, Order::getConsignee));


        // 转换为VO
        List<PaylogVO> paylogVOList = paylogPageRecords.stream()
                .map(paylog -> {
                    PaylogVO paylogVO = new PaylogVO();
                    BeanUtils.copyProperties(paylog, paylogVO);

                    DateTime date = DateUtil.date(paylog.getAddTime() * 1000);
                    paylogVO.setAddTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));

                    paylogVO.setPayStatusName(PAY_STATUS_NAME.get(paylog.getPayStatus()));

                    String consignee = orderConsigneeMap.get(paylog.getOrderId());
                    paylogVO.setConsignee(consignee);

                    return paylogVO;
                }).toList();
        return PageUtil.convertPage(paylogPage, paylogVOList);
    }

    @Override
    public PaylogVO detail(Integer id) {
        if (id != null) {
            Paylog paylog = this.getById(id);
            PaylogVO paylogVO = new PaylogVO();
            BeanUtils.copyProperties(paylog, paylogVO);
            return paylogVO;
        }
        return null;
    }

    @Override
    public boolean create(PaylogCreateDTO createDTO) {
        if (createDTO != null) {
            Paylog paylog = new Paylog();
            BeanUtils.copyProperties(createDTO, paylog);
            return this.save(paylog);
        }
        return false;
    }

    @Override
    public boolean update(PaylogUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Paylog paylog = new Paylog();
            BeanUtils.copyProperties(updateDTO, paylog);
            return this.updateById(paylog);
        }
        return false;
    }

    private String generatePaySn() {
        return StringUtils.getRandomString(16);
    }

    @Override
    public Paylog creatPayLog(OrderVO detail, String type) {
        // 查询该订单的最新一条支付日志（按id降序取第一条）
        Paylog latestPayLog = this.getOne(new LambdaQueryWrapper<Paylog>()
                .eq(Paylog::getOrderId, detail.getOrderId())
                .eq(Paylog::getPayCode, type)
                .orderByDesc(Paylog::getPaylogId)
                .last("LIMIT 1"));

        if (latestPayLog != null && ObjectUtil.notEqual(latestPayLog.getPayCode(), "wechat")) {
            // 未支付
            if (latestPayLog.getPayStatus() == 0) {
                return latestPayLog;
            }
            if (latestPayLog.getPayStatus() == 1) {
                throw new GlobalException("订单已支付");
            }
        }

        Paylog paylog = new Paylog();
        paylog.setPaySn(generatePaySn());
        paylog.setOrderId(detail.getOrderId());
        paylog.setOrderSn(detail.getOrderSn());
        paylog.setOrderAmount(detail.getTotalAmount());
        paylog.setPayAmount(detail.getUnpaidAmount());
        paylog.setOrderType(detail.getOrderType());
        paylog.setAddTime(StringUtils.getCurrentTime());
        paylog.setNotifyData("");
        paylog.setPayCode(type);
        try {
            boolean isSaved = this.save(paylog);
            if (isSaved) {
                return paylog;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
