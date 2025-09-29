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

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.UserWithDrawApplyDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyDelDTO;
import com.tigshop.bean.dto.finance.WithdrawApplyAccountUpdateDTO;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.enums.finance.UserWithdrawAccountType;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.enums.user.ChangTypeEnum;
import com.tigshop.bean.model.finance.UserBalanceLog;
import com.tigshop.bean.model.finance.UserWithdrawApply;
import com.tigshop.bean.model.finance.WithdrawApplyAccount;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.param.finance.withdrawapply.WithdrawApplyAccountCreateParam;
import com.tigshop.bean.query.finance.userwithdrawapply.UserWithdrawApplyListQuery;
import com.tigshop.bean.vo.finance.WithdrawApplyAccountVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.finance.UserBalanceLogMapper;
import com.tigshop.mapper.finance.UserWithdrawApplyMapper;
import com.tigshop.mapper.finance.WithdrawApplyAccountMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.WithdrawApplyAccountService;
import com.tigshop.service.msg.AdminMsgService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员提现账号服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class WithdrawApplyAccountServiceImpl extends BaseServiceImpl<WithdrawApplyAccountMapper, WithdrawApplyAccount> implements WithdrawApplyAccountService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserWithdrawApplyMapper userWithdrawApplyMapper;
    @Resource
    private UserBalanceLogMapper userBalanceLogMapper;
    @Resource
    private AdminMsgService adminMsgService;
    @Resource
    private TranslatePackageImpl translatePackage;

    @Override
    public Page<WithdrawApplyAccountVO> list(UserWithdrawApplyListQuery query) {
        Page<WithdrawApplyAccount> page = new Page<>(query.getPage(), query.getSize());
        Integer userId = SecurityUtils.getCurrentUserId();
        Page<WithdrawApplyAccount> withdrawApplyAccountPage = this.lambdaQuery()
                .eq(query.getAccountId() != null && query.getAccountId() > 0, WithdrawApplyAccount::getAccountId, query.getAccountId())
                .eq(query.getAccountType() != null && query.getAccountType() > 0, WithdrawApplyAccount::getAccountType, query.getAccountType())
                .eq(userId > 0, WithdrawApplyAccount::getUserId, userId)
                .page(page);

        List<WithdrawApplyAccountVO> list = withdrawApplyAccountPage.getRecords().stream()
                .map(withdrawApplyAccount -> {
                    WithdrawApplyAccountVO withdrawApplyAccountVO = new WithdrawApplyAccountVO();
                    BeanUtils.copyProperties(withdrawApplyAccount, withdrawApplyAccountVO);
                    withdrawApplyAccountVO.setAccountTypeName(UserWithdrawAccountType.getDescription(withdrawApplyAccount.getAccountType()));
                    return withdrawApplyAccountVO;
                })
                .toList();
        return PageUtil.convertPage(withdrawApplyAccountPage, list);
    }

    @Override
    public WithdrawApplyAccountVO detail(Integer id, Integer userId) {
        if (id != null) {
            LambdaQueryWrapper<WithdrawApplyAccount> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WithdrawApplyAccount::getAccountId, id);
            queryWrapper.eq(WithdrawApplyAccount::getUserId, userId);
            WithdrawApplyAccount withdrawApplyAccount = this.getOne(queryWrapper);
            WithdrawApplyAccountVO withdrawApplyAccountVO = new WithdrawApplyAccountVO();
            BeanUtils.copyProperties(withdrawApplyAccount, withdrawApplyAccountVO);
            return withdrawApplyAccountVO;
        }
        return null;
    }

    @Override
    public void create(WithdrawApplyAccountCreateParam param) {
        Integer userId = SecurityUtils.getCurrentUserId();

        Long withdrawApplyAccountCount = this.lambdaQuery().eq(WithdrawApplyAccount::getUserId, userId).count();
        Assert.isTrue(withdrawApplyAccountCount <= 15, () -> new GlobalException(translatePackage.translate("最多添加15个卡")));

        // 检测是否在相同的卡号
        Long WithdrawApplyAccountCount = this.lambdaQuery()
                .eq(WithdrawApplyAccount::getUserId, userId)
                .eq(WithdrawApplyAccount::getAccountNo, param.getAccountNo())
                .eq(WithdrawApplyAccount::getAccountType, param.getAccountType())
                .count();
        Assert.isTrue(WithdrawApplyAccountCount == 0, () -> new GlobalException(translatePackage.translate("当前账号已存在")));

        WithdrawApplyAccount withdrawApplyAccount = param.createWithdrawApplyAccount(userId);
        this.save(withdrawApplyAccount);
    }

    @Override
    public boolean update(WithdrawApplyAccountUpdateDTO updateDTO, Integer userId) {
        if (updateDTO != null) {
            //检测是否在相同的卡号
            LambdaQueryWrapper<WithdrawApplyAccount> queryWrapperCheck = new LambdaQueryWrapper<>();
            queryWrapperCheck.eq(WithdrawApplyAccount::getUserId, userId)
                    .eq(WithdrawApplyAccount::getAccountId, updateDTO.getAccountId())
                    .eq(WithdrawApplyAccount::getAccountNo, updateDTO.getAccountNo())
                    .eq(WithdrawApplyAccount::getAccountType, updateDTO.getAccountType())
                    .last("limit 1");
            WithdrawApplyAccount withdrawApplyAccount = new WithdrawApplyAccount();
            BeanUtils.copyProperties(updateDTO, withdrawApplyAccount);
            return this.updateById(withdrawApplyAccount);
        }
        return false;
    }

    @Override
    public boolean delAccount(UserWithdrawApplyDelDTO dto, Integer userId) {
        LambdaQueryWrapper<WithdrawApplyAccount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WithdrawApplyAccount::getUserId, userId)
                .eq(WithdrawApplyAccount::getAccountId, dto.getAccountId());
        return this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(UserWithDrawApplyDTO dto, Integer userId) {
        if (dto == null) {
            throw new GlobalException(translatePackage.translate("请填写提现账号信息"));
        }
        User user = userMapper.selectById(userId);
        BigDecimal balance = user != null ? user.getBalance() : BigDecimal.ZERO;
        if (dto.getAmount().compareTo(balance) > 0) {
            throw new GlobalException(translatePackage.translate("提现金额大于账户的可用余额"));
        }
        UserWithdrawApply userWithdrawApply = new UserWithdrawApply();
        userWithdrawApply.setUserId(userId);
        userWithdrawApply.setAmount(dto.getAmount());
        userWithdrawApply.setAddTime(StringUtils.getCurrentTime());
        userWithdrawApply.setAccountData(JsonUtil.toJson(dto.getAccountData()));
        userWithdrawApplyMapper.insert(userWithdrawApply);
        //提现冻结余额
        if (user != null) {
            user.setFrozenBalance(user.getFrozenBalance().add(dto.getAmount()));
            userMapper.updateById(user);
        }
        UserBalanceLog userBalanceLog = new UserBalanceLog();
        userBalanceLog.setUserId(userId);
        userBalanceLog.setChangeDesc("提现冻结余额");
        userBalanceLog.setFrozenBalance(dto.getAmount());
        userBalanceLog.setChangeType(ChangTypeEnum.INC_POINTS.getValue());
        userBalanceLog.setChangeTime(StringUtils.getCurrentTime());
        userBalanceLogMapper.insert(userBalanceLog);
        //减余额
        User userDec = userMapper.selectById(userId);
        BigDecimal beforeBalance = userDec.getBalance();
        if (userDec != null) {
            userDec.setBalance(userDec.getBalance().subtract(dto.getAmount()));
            userMapper.updateById(userDec);
        }
        UserBalanceLog userBalanceLogDec = new UserBalanceLog();
        userBalanceLogDec.setUserId(userId);
        userBalanceLogDec.setChangeDesc("提现扣除余额");
        userBalanceLogDec.setBeforeBalance(beforeBalance);
        userBalanceLogDec.setBalance(dto.getAmount());
        userBalanceLogDec.setAfterBalance(beforeBalance.subtract(dto.getAmount()));
        userBalanceLogDec.setChangeType(ChangTypeEnum.DEC_POINTS.getValue());
        userBalanceLogDec.setChangeTime(StringUtils.getCurrentTime());
        userBalanceLogMapper.insert(userBalanceLogDec);

        // 发送后台消息  -- 提现申请
        User userInfo = userMapper.selectById(userId);
        AdminMsgCreateDTO adminMsgCreateDTO = new AdminMsgCreateDTO();
        adminMsgCreateDTO.setMsgType(AdminMsgTypeEnum.WITHDRAWAL_REQUEST.getCatId());
        adminMsgCreateDTO.setTitle("您有新的提现申请,申请用户：" + userInfo.getUsername());
        adminMsgCreateDTO.setContent("用户【" + userInfo.getUsername() + "】申请提现,申请金额：" + dto.getAmount() + "元");
        Map<String, Object> relatedData = new HashMap<>();
        relatedData.put("withdrawApplyId", userWithdrawApply.getId());
        adminMsgCreateDTO.setRelatedData(relatedData);
        adminMsgService.createMessage(adminMsgCreateDTO);
        return true;
    }
}
