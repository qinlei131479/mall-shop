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

package com.tigshop.service.shop.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.*;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.finance.StatementType;
import com.tigshop.bean.enums.shop.ShopWithdrawStatus;
import com.tigshop.bean.model.authority.AdminRole;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.finance.RecordRate;
import com.tigshop.bean.model.shop.ShopWithdraw;
import com.tigshop.bean.param.finance.statement.StatementOutputSaveParam;
import com.tigshop.bean.param.finance.statement.StatementSaveParam;
import com.tigshop.bean.vo.common.StatusListVO;
import com.tigshop.bean.vo.shop.ShopWithdrawVO;
import com.tigshop.common.annotation.RetryLimit;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.shop.ShopWithdrawMapper;
import com.tigshop.service.authority.AdminRoleService;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.RecordRateService;
import com.tigshop.service.finance.StatementOutputService;
import com.tigshop.service.finance.StatementService;
import com.tigshop.service.shop.AccountService;
import com.tigshop.service.shop.ShopWithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tigshop.bean.enums.finance.StatementType.SHOP_WITHDRAWAL;

/**
 * 商家账户表服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
@RequiredArgsConstructor
public class ShopWithdrawServiceImpl extends BaseServiceImpl<ShopWithdrawMapper, ShopWithdraw> implements ShopWithdrawService {

    private final AdminRoleService adminRoleService;

    private final AdminUserService adminUserService;

    private final AccountService accountService;
    private final RecordRateService recordRateService;
    private final StatementOutputService statementOutputService;
    private final StatementService statementService;

    private final RedisCache redisCache;


    @Override
    public Page<ShopWithdrawVO> list(ShopWithdrawListDTO listDTO) {
        // 分页
        Page<ShopWithdraw> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ShopWithdraw> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer status = listDTO.getStatus();
        if (status != null) {
            queryWrapper.eq(ShopWithdraw::getStatus, status);
        }
        String addTimeStart = listDTO.getAddTimeStart();
        //将时间格式转换成时间戳
        if (StringUtils.isNotEmpty(addTimeStart)) {
            addTimeStart = addTimeStart + " 00:00:00";
            long addTimeStartTimestamp = DateUtil.parse(addTimeStart).getTime() / 1000;
            queryWrapper.ge(ShopWithdraw::getAddTime, addTimeStartTimestamp);
        }
        String addTimeEnd = listDTO.getAddTimeEnd();
        if (StringUtils.isNotEmpty(addTimeEnd)) {
            addTimeEnd = addTimeEnd + " 23:59:59";
            long addTimeEndTimestamp = DateUtil.parse(addTimeEnd).getTime() / 1000;
            queryWrapper.le(ShopWithdraw::getAddTime, addTimeEndTimestamp);
        }

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("", keyword);
        }*/
        // 执行查询
        Page<ShopWithdraw> shopWithdrawPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ShopWithdraw> shopWithdrawPageRecords = shopWithdrawPage.getRecords();
        // 转换为VO
        List<ShopWithdrawVO> shopWithdrawVOList = shopWithdrawPageRecords.stream()
                .map(shopWithdraw -> {
                    ShopWithdrawVO shopWithdrawVO = new ShopWithdrawVO();
                    BeanUtils.copyProperties(shopWithdraw, shopWithdrawVO);
                    long timestampInSeconds = shopWithdraw.getAddTime();
                    Date date = new Date(timestampInSeconds * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    shopWithdrawVO.setAddTime(sdf.format(date));
                    shopWithdrawVO.setStatusText(ShopWithdrawStatus.getStatusName(shopWithdraw.getStatus()));
                    shopWithdrawVO.setAccountData(JSONUtil.parseObj(shopWithdraw.getAccountData()));
                    return shopWithdrawVO;
                }).toList();
        return PageUtil.convertPage(shopWithdrawPage, shopWithdrawVOList);
    }

    @Override
    public ShopWithdrawVO detail(Integer id) {
        if (id != null) {
            ShopWithdraw shopWithdraw = this.getById(id);
            ShopWithdrawVO shopWithdrawVO = new ShopWithdrawVO();
            BeanUtils.copyProperties(shopWithdraw, shopWithdrawVO);
            shopWithdrawVO.setAccountData(JSONUtil.parseObj(shopWithdraw.getAccountData()));
            shopWithdrawVO.setAddTime(TigUtils.handelTime(shopWithdraw.getAddTime()));
            shopWithdrawVO.setStatusText(ShopWithdrawStatus.getStatusName(shopWithdraw.getStatus()));
            return shopWithdrawVO;
        }
        return null;
    }

    @Override
    public boolean create(ShopWithdrawCreateDTO createDTO) {
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("shopWithdrawCreateShopId::" + HeaderUtils.getShopId(), uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            try {
                if (createDTO != null) {
                    ShopWithdraw shopWithdraw = new ShopWithdraw();
                    shopWithdraw.setShopId(getShopId());
                    String snPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    String snSuffix = RandomUtil.randomNumbers(5);
                    shopWithdraw.setWithdrawSn(snPrefix + snSuffix);
                    shopWithdraw.setAmount(createDTO.getAmount());
                    shopWithdraw.setStatus(ShopWithdrawStatus.PENDING_REVIEW.getCode());
                    shopWithdraw.setAddTime(StringUtils.getCurrentTime());
                    shopWithdraw.setRemark(createDTO.getRemark());
                    shopWithdraw.setMerchantAccountId(createDTO.getMerchantAccountId());
                    shopWithdraw.setAccountData(JSONUtil.toJsonStr(createDTO.getAccountData()));
                    boolean save = this.save(shopWithdraw);
                    accountService.addWithDrawLog(shopWithdraw.getShopId(), shopWithdraw.getAmount());
                    recordRateService.saveRecordRate(shopWithdraw.getShopWithdrawLogId(), SHOP_WITHDRAWAL.getCode(), shopWithdraw.getShopId(), 0);
                    return save;
                }
            } finally {
                redisCache.unlock("shopWithdrawCreateShopId::" + HeaderUtils.getShopId(), uuid);
            }
        }

        return false;
    }

    @Override
    public boolean update(ShopWithdrawUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ShopWithdraw shopWithdraw = new ShopWithdraw();
            BeanUtils.copyProperties(updateDTO, shopWithdraw);
            return this.updateById(shopWithdraw);
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
    public void audit(ShopWithdrawAuditDTO dto) {
        AdminUser adminUser = adminUserService.getById(SecurityUtils.getCurrentAdminId());
        AdminRole adminRole = adminRoleService.getById(adminUser.getRoleId());
        if (!adminRole.getAdminType().equals(AdminTypeEnum.ADMIN.getCode())) {
            throw new GlobalException("没有权限");
        }
        ShopWithdraw shopWithdraw = this.getById(dto.getId());

        if (!shopWithdraw.getStatus().equals(ShopWithdrawStatus.PENDING_REVIEW.getCode())) {
            throw new GlobalException("不是待审核状态");
        }

        if (shopWithdraw.getStatus().equals(ShopWithdrawStatus.AUDIT_NOT_PASS.getCode())) {
            accountService.refundWithDrawLog(shopWithdraw.getShopId(), shopWithdraw.getAmount());

        }
        shopWithdraw.setStatus(dto.getStatus());
        shopWithdraw.setAuditRemark(dto.getAuditRemark());
        this.updateById(shopWithdraw);

        // 添加流水
        StatementOutputSaveParam statementOutputSaveParam = new StatementOutputSaveParam(shopWithdraw, false);
        statementOutputService.saveStatementOutput(statementOutputSaveParam);
        // 添加提现对账单
        StatementSaveParam statementSaveParam = new StatementSaveParam(shopWithdraw);
        statementService.saveStatement(statementSaveParam);

        // 获取手续费率
        RecordRate recordRate = statementService.getRecordRate(shopWithdraw.getShopWithdrawLogId(), StatementType.SHOP_WITHDRAWAL.getCode());
        // 添加手续费流水
        StatementOutputSaveParam statementOutputSaveParam2 = new StatementOutputSaveParam(shopWithdraw, true);
        BigDecimal newAmount = shopWithdraw.getAmount().multiply(recordRate.getShopWithdrawalFee())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        statementOutputSaveParam2.setExpenditure(newAmount);
        statementOutputService.saveStatementOutput(statementOutputSaveParam2);
    }

    @Override
    public void uploadPayVoucher(ShopWithdrawPayVoucherDTO dto) {
        if (dto.getId() == null) {
            throw new GlobalException("#id错误");
        }
        ShopWithdraw detail = this.getById(dto.getId());
        if (!ShopWithdrawStatus.STATUS_WAIT_PAYMENT.getCode().equals(detail.getStatus())) {
            throw new GlobalException("状态错误");
        }
        if (dto.getPaymentVoucher() == null || dto.getPaymentVoucher().isEmpty()) {
            throw new GlobalException("请上传打款凭证");
        }
        detail.setPaymentVoucher(dto.getPaymentVoucher());
        detail.setStatus(ShopWithdrawStatus.AUDIT_PASS.getCode());
        this.updateById(detail);

        accountService.completeWithDrawLog(detail.getShopId(), detail.getAmount());
    }
}
