package com.tigshop.service.vendor.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.vendor.Vendor;
import com.tigshop.bean.model.vendor.VendorAccountLog;
import com.tigshop.bean.query.vendor.AccountListPageQuery;
import com.tigshop.bean.vo.vendor.AccountVO;
import com.tigshop.bean.vo.vendor.VendorAccountLogVO;
import com.tigshop.bean.vo.vendor.VendorDetailVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.vendor.VendorAccountLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.vendor.VendorAccountLogService;
import com.tigshop.service.vendor.VendorAccountService;
import com.tigshop.service.vendor.VendorService;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author Admin
 * @description 针对表【vendor_account_log(供应商资金变化)】的数据库操作Service实现
 * @createDate 2025-07-16 14:30:30
 */
@Service
@RequiredArgsConstructor
public class VendorAccountLogServiceImpl extends BaseServiceImpl<VendorAccountLogMapper, VendorAccountLog> implements VendorAccountLogService {

    private final VendorService vendorService;
    private final OrderService orderService;
    private final VendorAccountService vendorAccountService;


    @Override
    public AccountVO index() {
        VendorDetailVO detail = vendorService.detail(getVendorId());
        Vendor vendor = vendorService.getById(getVendorId());
        AccountVO accountVO = new AccountVO();
        accountVO.setVendor(detail);
        accountVO.setVendorMoney(vendor.getVendorMoney());
        accountVO.setFrozenMoney(vendor.getFrozenMoney());

        accountVO.setUnSettlementMoney(orderService.getOrderUnSettlementAmountByVendor(getVendorId()));
        accountVO.setCardCount(vendorAccountService.getCardCount(getVendorId()));
        return accountVO;
    }

    @Override
    public Page<VendorAccountLogVO> logList(AccountListPageQuery pageQuery) {
        // 分页
        Page<VendorAccountLog> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<VendorAccountLog> queryWrapper = new LambdaQueryWrapper<>();
        // 非平台用户只能查看自己的数据
        if (AdminTypeEnum.fromCode(getAdminType()) != AdminTypeEnum.ADMIN) {
            queryWrapper.eq(VendorAccountLog::getVendorId, getVendorId());
        } else {
            queryWrapper.eq(pageQuery.getVendorId() != null, VendorAccountLog::getVendorId, pageQuery.getVendorId());
        }
        if (ObjectUtil.isNotEmpty(pageQuery.getKeyword())) {
            List<Vendor> list = vendorService.list(new LambdaQueryWrapper<Vendor>().like(Vendor::getVendorName, pageQuery.getKeyword()));
            if (list.isEmpty()) {
                return new Page<>();
            }
            List<Integer> vendorIds = list.stream().mapToInt(Vendor::getVendorId).boxed().toList();
            queryWrapper.in(VendorAccountLog::getVendorId, vendorIds);
        }
        // 排序字段
        buildSortOrder(page, pageQuery.getSortField(), pageQuery.getSortOrder());

        //时间查询
        String startTime = pageQuery.getStartTime();
        String endTime = pageQuery.getEndTime();
        if (!StringUtil.isNullOrEmpty(startTime)) {
            startTime = startTime + " 00:00:00";
            long addTimeStartTimestamp = DateUtil.parse(startTime).getTime() / 1000;
            queryWrapper.ge(VendorAccountLog::getAddTime, addTimeStartTimestamp);
        }
        if (!StringUtil.isNullOrEmpty(endTime)) {
            endTime = endTime + " 23:59:59";
            long addTimeEndTimestamp = DateUtil.parse(endTime).getTime() / 1000;
            queryWrapper.le(VendorAccountLog::getAddTime, addTimeEndTimestamp);
        }
        // 执行查询
        Page<VendorAccountLog> accountPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<VendorAccountLog> accountPageRecords = accountPage.getRecords();
        // 转换为VO
        List<VendorAccountLogVO> vendorAccountLogVOList = accountPageRecords.stream()
                .map(account -> {
                    VendorAccountLogVO vendorAccountLogVO = new VendorAccountLogVO();
                    BeanUtils.copyProperties(account, vendorAccountLogVO);
                    vendorAccountLogVO.setAddTime(account.getAddTime().toString());
                    Vendor byId = vendorService.getById(account.getVendorId());
                    if (byId != null) {
                        vendorAccountLogVO.setVendorName(byId.getVendorName());
                    }
                    return vendorAccountLogVO;
                }).toList();
        return PageUtil.convertPage(accountPage, vendorAccountLogVOList);
    }

    @Override
    public void addWithDrawLog(Integer vendorId, BigDecimal amount) {
        Vendor oldVendor = vendorService.getById(vendorId);
        VendorAccountLog accountLog = new VendorAccountLog();
        accountLog.setVendorMoney(oldVendor.getVendorMoney());
        accountLog.setFrozenMoney(oldVendor.getFrozenMoney());
        Vendor vendor = this.changeShopAccount(vendorId, amount, 1);
        accountLog.setNewVendorMoney(vendor.getVendorMoney());
        accountLog.setNewFrozenMoney(vendor.getFrozenMoney());
        accountLog.setVendorId(vendorId);
        accountLog.setType(1);
        accountLog.setAddTime(StringUtils.getCurrentTime());
        this.save(accountLog);
    }

    @Override
    public void refundWithDrawLog(Integer vendorId, BigDecimal amount) {
        Vendor vendor = this.changeShopAccount(vendorId, amount, 2);
        BigDecimal newShopMoney = vendor.getVendorMoney().add(amount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newFrozenMoney = vendor.getFrozenMoney().subtract(amount).setScale(2, RoundingMode.HALF_UP);

        VendorAccountLog accountLog = new VendorAccountLog();
        accountLog.setVendorMoney(vendor.getVendorMoney());
        accountLog.setFrozenMoney(vendor.getFrozenMoney());
        accountLog.setNewVendorMoney(newShopMoney);
        accountLog.setNewFrozenMoney(newFrozenMoney);
        accountLog.setVendorId(vendorId);
        accountLog.setType(1);
        accountLog.setAddTime(StringUtils.getCurrentTime());
        this.save(accountLog);
    }

    @Override
    public void completeWithDrawLog(Integer vendorId, BigDecimal amount) {
        Vendor oldVendor = vendorService.getById(vendorId);
        VendorAccountLog accountLog = new VendorAccountLog();
        accountLog.setVendorMoney(oldVendor.getVendorMoney());
        accountLog.setFrozenMoney(oldVendor.getFrozenMoney());
        Vendor vendor = this.changeShopAccount(vendorId, amount, 3);
        accountLog.setNewVendorMoney(vendor.getVendorMoney());
        accountLog.setNewFrozenMoney(vendor.getFrozenMoney());
        accountLog.setVendorId(vendorId);
        accountLog.setType(1);
        accountLog.setAddTime(StringUtils.getCurrentTime());
        this.save(accountLog);
    }

    public Vendor changeShopAccount(Integer vendorId, BigDecimal amount, int type) {

        Vendor vendor = vendorService.getById(vendorId);
        if (vendor == null) {
            throw new RuntimeException("供应商不存在");
        }
        switch (type) {
            case 1:
                vendor.setVendorMoney(vendor.getVendorMoney().subtract(amount));
                vendor.setFrozenMoney(vendor.getFrozenMoney().add(amount));
                break;
            case 2:
                vendor.setVendorMoney(vendor.getVendorMoney().add(amount));
                vendor.setFrozenMoney(vendor.getFrozenMoney().subtract(amount));
                break;
            case 3:
                vendor.setFrozenMoney(vendor.getFrozenMoney().subtract(amount));
                break;
            case 4:
                vendor.setVendorMoney(vendor.getVendorMoney().add(amount));
                break;
            default:
                break;
        }
        vendorService.updateById(vendor);
        return vendor;
    }


}




