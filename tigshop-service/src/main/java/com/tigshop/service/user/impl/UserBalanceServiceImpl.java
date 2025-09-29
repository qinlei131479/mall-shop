// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.user.impl;

import com.tigshop.bean.dto.finance.UserBalanceLogCreatDTO;
import com.tigshop.bean.model.user.User;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.finance.UserBalanceLogService;
import com.tigshop.service.user.UserBalanceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 */
@Service
public class UserBalanceServiceImpl implements UserBalanceService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBalanceLogService userBalanceLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setBalance(user.getBalance().subtract(amount));
        addBalanceLog(userId, changeDesc, 2, amount, null);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setBalance(user.getBalance().add(amount));
        addBalanceLog(userId, changeDesc, 1, amount, null);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decFrozenBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setFrozenBalance(user.getFrozenBalance().subtract(amount));
        addBalanceLog(userId, changeDesc, 2, null, amount);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incFrozenBalance(BigDecimal amount, Integer userId, String changeDesc) {
        User user = userMapper.selectForUpdateById(userId);
        user.setFrozenBalance(user.getFrozenBalance().add(amount));
        addBalanceLog(userId, changeDesc, 1, null, amount);
        return userMapper.updateById(user) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void addBalanceLog(Integer userId, String changeDesc, int changeType, BigDecimal balanceAmount, BigDecimal frozenBalanceAmount) {
        User user = userMapper.selectById(userId);


        UserBalanceLogCreatDTO userBalanceLogCreat = new UserBalanceLogCreatDTO();
        userBalanceLogCreat.setUserId(userId);
        userBalanceLogCreat.setChangeDesc(changeDesc);
        userBalanceLogCreat.setChangeType(changeType);
        if (balanceAmount != null) {
            userBalanceLogCreat.setBeforeBalance(user.getBalance());
            userBalanceLogCreat.setBalance(balanceAmount);
            BigDecimal afterBalance = changeType == 1 ? user.getBalance().add(balanceAmount) : user.getBalance().subtract(balanceAmount);
            userBalanceLogCreat.setAfterBalance(afterBalance);
        }
        if (frozenBalanceAmount != null) {
            userBalanceLogCreat.setFrozenBalance(frozenBalanceAmount);
            userBalanceLogCreat.setBeforeFrozenBalance(user.getFrozenBalance());
            userBalanceLogCreat.setAfterFrozenBalance(user.getFrozenBalance());
        } else {
            userBalanceLogCreat.setFrozenBalance(BigDecimal.ZERO);
            userBalanceLogCreat.setBeforeFrozenBalance(user.getFrozenBalance());
            userBalanceLogCreat.setAfterFrozenBalance(user.getFrozenBalance());
        }
        userBalanceLogService.create(userBalanceLogCreat);
    }

}

