package com.tigshop.service.vendor.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.AccountListDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.merchant.MerchantAccountType;
import com.tigshop.bean.model.vendor.VendorAccount;
import com.tigshop.bean.param.vendor.VendorAccountCreateParam;
import com.tigshop.bean.param.vendor.VendorAccountUpdateParam;
import com.tigshop.bean.param.vendor.VendorAccountVO;
import com.tigshop.bean.vo.vendor.AdminVendorAccountVO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.vendor.VendorAccountMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.vendor.VendorAccountService;
import com.tigshop.service.vendor.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@RequiredArgsConstructor
public class VendorAccountServiceImpl extends BaseServiceImpl<VendorAccountMapper, VendorAccount> implements VendorAccountService {


    private final VendorService vendorService;
    private final OrderService orderService;
    private final ConfigService configService;

    @Override
    public AdminVendorAccountVO index(AccountListDTO listDTO) {
        AdminVendorAccountVO accountVO = new AdminVendorAccountVO();
        BigDecimal sumShopMoney = vendorService.getSumVendorMoney();
        BigDecimal sumFrozenMoney = vendorService.getSumFrozenMoney();
        BigDecimal sumOrderUnSettlementAmount = orderService.getOrderUnSettlementAmountByVendor(0);
        accountVO.setVendorMoney(sumShopMoney);
        accountVO.setFrozenMoney(sumFrozenMoney);
        accountVO.setUnSettlementMoney(sumOrderUnSettlementAmount);
        return accountVO;
    }

    @Override
    public Integer getCardCount(Integer vendorId) {
        QueryWrapper<VendorAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("vendor_id", vendorId);
        queryWrapper.select("count(*) as account_id");
        return this.getOne(queryWrapper).getAccountId();
    }

    @Override
    public void create(VendorAccountCreateParam param) {
        if (getVendorId() == null) {
            throw new GlobalException("请登录");
        }
        VendorAccount vendorAccount = new VendorAccount();
        BeanUtils.copyProperties(param, vendorAccount);
        vendorAccount.setAddTime(StringUtils.getCurrentTime());
        vendorAccount.setVendorId(getVendorId());
        save(vendorAccount);
    }

    @Override
    public void update(VendorAccountUpdateParam param) {
        if (getVendorId() == null) {
            throw new GlobalException("请登录");
        }
        VendorAccount vendorAccount = new VendorAccount();
        BeanUtils.copyProperties(param, vendorAccount);
        updateById(vendorAccount);
    }

    @Override
    public Page<VendorAccountVO> listAccount(BasePage param) {
        Page<VendorAccount> page = Page.of(param.getPage(), param.getSize());
        LambdaQueryWrapper<VendorAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VendorAccount::getVendorId, getVendorId());
        // 查询设置的提现方式
        String withdrawalReceiptMethod = configService.getConfigVal(SettingsEnum.WITHDRAWAL_RECEIPT_METHOD);
        if (StrUtil.isEmpty(withdrawalReceiptMethod)) {
            queryWrapper.in(VendorAccount::getAccountType, JSON.toJSONString(Arrays.asList(1,2,3)));
        }else {
            JSONArray ja = JSON.parseArray(withdrawalReceiptMethod);
            if (!ja.isEmpty()){
                queryWrapper.in(VendorAccount::getAccountType, ja);
            }
        }
        Page<VendorAccount> vendorAccountPage = this.page(page, queryWrapper);
        List<VendorAccountVO> collect = vendorAccountPage.getRecords().stream().map(vendorAccount -> {
            VendorAccountVO vendorAccountVO = new VendorAccountVO();
            BeanUtils.copyProperties(vendorAccount, vendorAccountVO);
            vendorAccountVO.setAccountTypeText(MerchantAccountType.getTypeName(vendorAccountVO.getAccountType()));
            return vendorAccountVO;
        }).collect(Collectors.toList());
        return PageUtil.convertPage(vendorAccountPage, collect);
    }

    @Override
    public VendorAccountVO detail(Integer id) {
        VendorAccount vendorAccount = getById(id);
        VendorAccountVO vendorAccountVO = new VendorAccountVO();
        BeanUtils.copyProperties(vendorAccount, vendorAccountVO);
        return vendorAccountVO;
    }
}




