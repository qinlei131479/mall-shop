package com.tigshop.service.vendor.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.vendor.VendorWithdrawAuditDTO;
import com.tigshop.bean.dto.vendor.VendorWithdrawCreateDTO;
import com.tigshop.bean.dto.vendor.VendorWithdrawListDTO;
import com.tigshop.bean.dto.vendor.VendorWithdrawPayVoucherDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.shop.ShopWithdrawStatus;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.vendor.VendorWithdraw;
import com.tigshop.bean.param.finance.statement.StatementOutputSaveParam;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.vo.common.StatusListVO;
import com.tigshop.bean.vo.vendor.VendorWithdrawVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.vendor.VendorWithdrawMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.finance.StatementOutputService;
import com.tigshop.service.finance.StatementService;
import com.tigshop.service.vendor.VendorAccountLogService;
import com.tigshop.service.vendor.VendorWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.tigshop.bean.enums.finance.StatementType.SUPPLIER_WITHDRAWAL;

/**
 * @author Admin
 */
@Service
@RequiredArgsConstructor
public class VendorWithdrawServiceImpl extends BaseServiceImpl<VendorWithdrawMapper, VendorWithdraw> implements VendorWithdrawService {

    private final VendorAccountLogService accountLogService;
    private final RecordRateService recordRateService;
    private final StatementOutputService statementOutputService;
    private final StatementService statementService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean create(VendorWithdrawCreateDTO createDTO) {
        if (createDTO != null) {
            VendorWithdraw vendorWithdraw = new VendorWithdraw();
            vendorWithdraw.setVendorId(getVendorId());
            String snPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String snSuffix = RandomUtil.randomNumbers(5);
            vendorWithdraw.setWithdrawSn(snPrefix + snSuffix);
            vendorWithdraw.setAmount(createDTO.getAmount());
            vendorWithdraw.setStatus(ShopWithdrawStatus.PENDING_REVIEW.getCode());
            vendorWithdraw.setAddTime(StringUtils.getCurrentTime());
            vendorWithdraw.setRemark(createDTO.getRemark());
            vendorWithdraw.setVendorAccountId(createDTO.getVendorAccountId());
            vendorWithdraw.setAccountData(JSONUtil.toJsonStr(createDTO.getAccountData()));
            boolean save = this.save(vendorWithdraw);
            accountLogService.addWithDrawLog(vendorWithdraw.getVendorId(), createDTO.getAmount());
            recordRateService.saveRecordRate(vendorWithdraw.getVendorWithdrawLogId(), SUPPLIER_WITHDRAWAL.getCode(), 0, vendorWithdraw.getVendorId());
            return save;
        }
        return false;
    }

    @Override
    public StatusListVO<List<String>> getStatusList() {
        List<String> statusList = new ArrayList<>();
        for (ShopWithdrawStatus status : ShopWithdrawStatus.values()) {
            statusList.add(status.getStatus());
        }
        return new StatusListVO<>(statusList);
    }

    @Override
    public Page<VendorWithdrawVO> list(VendorWithdrawListDTO listDTO) {
        // 分页
        Page<VendorWithdraw> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<VendorWithdraw> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.fromCode(getAdminType()) != AdminTypeEnum.ADMIN) {
            queryWrapper.eq(VendorWithdraw::getVendorId, getVendorId());
        }
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer status = listDTO.getStatus();
        if (status != null) {
            queryWrapper.eq(VendorWithdraw::getStatus, status);
        }
        String addTimeStart = listDTO.getAddTimeStart();
        //将时间格式转换成时间戳
        if (StringUtils.isNotEmpty(addTimeStart)) {
            addTimeStart = addTimeStart + " 00:00:00";
            long addTimeStartTimestamp = DateUtil.parse(addTimeStart).getTime() / 1000;
            queryWrapper.ge(VendorWithdraw::getAddTime, addTimeStartTimestamp);
        }
        String addTimeEnd = listDTO.getAddTimeEnd();
        if (StringUtils.isNotEmpty(addTimeEnd)) {
            addTimeEnd = addTimeEnd + " 23:59:59";
            long addTimeEndTimestamp = DateUtil.parse(addTimeEnd).getTime() / 1000;
            queryWrapper.le(VendorWithdraw::getAddTime, addTimeEndTimestamp);
        }

        // 执行查询
        Page<VendorWithdraw> vendorWithdrawPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<VendorWithdraw> vendorWithdrawPageRecords = vendorWithdrawPage.getRecords();
        // 转换为VO
        List<VendorWithdrawVO> vendorWithdrawVOList = vendorWithdrawPageRecords.stream()
                .map(shopWithdraw -> {
                    VendorWithdrawVO vendorWithdrawVO = new VendorWithdrawVO();
                    BeanUtils.copyProperties(shopWithdraw, vendorWithdrawVO);
                    long timestampInSeconds = shopWithdraw.getAddTime();
                    Date date = new Date(timestampInSeconds * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    vendorWithdrawVO.setAddTime(sdf.format(date));
                    vendorWithdrawVO.setStatusText(ShopWithdrawStatus.getStatusName(shopWithdraw.getStatus()));
                    vendorWithdrawVO.setAccountData(JSONUtil.parseObj(shopWithdraw.getAccountData()));
                    return vendorWithdrawVO;
                }).toList();
        return PageUtil.convertPage(vendorWithdrawPage, vendorWithdrawVOList);
    }

    @Override
    public VendorWithdrawVO detail(Integer id) {
        VendorWithdraw shopWithdraw = this.getById(id);
        VendorWithdrawVO vendorWithdrawVO = new VendorWithdrawVO();
        BeanUtils.copyProperties(shopWithdraw, vendorWithdrawVO);
        long timestampInSeconds = shopWithdraw.getAddTime();
        Date date = new Date(timestampInSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        vendorWithdrawVO.setAddTime(sdf.format(date));
        vendorWithdrawVO.setStatusText(ShopWithdrawStatus.getStatusName(shopWithdraw.getStatus()));
        vendorWithdrawVO.setAccountData(JSONUtil.parseObj(shopWithdraw.getAccountData()));
        return vendorWithdrawVO;
    }

    @Override
    public void audit(VendorWithdrawAuditDTO dto) {
        if (AdminTypeEnum.fromCode(getAdminType()) != AdminTypeEnum.ADMIN) {
            throw new GlobalException("没有权限");
        }
        VendorWithdraw vendorWithdraw = this.getById(dto.getId());

        if (!vendorWithdraw.getStatus().equals(ShopWithdrawStatus.PENDING_REVIEW.getCode())) {
            throw new GlobalException("不是待审核状态");
        }

        if (dto.getStatus().equals(ShopWithdrawStatus.AUDIT_NOT_PASS.getCode())) {
            accountLogService.refundWithDrawLog(vendorWithdraw.getVendorId(), vendorWithdraw.getAmount());
        }
        vendorWithdraw.setStatus(dto.getStatus());
        vendorWithdraw.setAuditRemark(dto.getAuditRemark());
        this.updateById(vendorWithdraw);

        // 添加流水
        StatementOutputSaveParam statementOutputSaveParam = new StatementOutputSaveParam(vendorWithdraw, false);
        statementOutputService.saveStatementOutput(statementOutputSaveParam);
        // 添加对账单
        StatementSaveParam statementSaveParam = new StatementSaveParam(vendorWithdraw);
        statementService.saveStatement(statementSaveParam);

        // 获取手续费率
        RecordRate recordRate = statementService.getRecordRate(vendorWithdraw.getVendorWithdrawLogId(), StatementType.SHOP_WITHDRAWAL.getCode());
        // 添加手续费流水
        StatementOutputSaveParam statementOutputSaveParam2 = new StatementOutputSaveParam(vendorWithdraw, true);
        BigDecimal newAmount = vendorWithdraw.getAmount().multiply(recordRate.getSupplierWithdrawalFee())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        statementOutputSaveParam2.setExpenditure(newAmount);
        statementOutputService.saveStatementOutput(statementOutputSaveParam2);
    }

    @Override
    public void uploadPayVoucher(VendorWithdrawPayVoucherDTO dto) {
        if (dto.getId() == null) {
            throw new GlobalException("#id错误");
        }
        VendorWithdraw detail = this.getById(dto.getId());
        if (!ShopWithdrawStatus.STATUS_WAIT_PAYMENT.getCode().equals(detail.getStatus())) {
            throw new GlobalException("状态错误");
        }
        if (dto.getPaymentVoucher() == null || dto.getPaymentVoucher().isEmpty()) {
            throw new GlobalException("请上传打款凭证");
        }
        detail.setPaymentVoucher(dto.getPaymentVoucher());
        detail.setStatus(ShopWithdrawStatus.AUDIT_PASS.getCode());
        this.updateById(detail);

        accountLogService.completeWithDrawLog(detail.getVendorId(),detail.getAmount());
    }
}




