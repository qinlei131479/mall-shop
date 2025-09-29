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
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.tigshop.bean.dto.finance.UserBalanceLogCreatDTO;
import com.tigshop.bean.dto.finance.UserBalanceLogListDTO;
import com.tigshop.bean.enums.finance.UserBalanceLogType;
import com.tigshop.bean.model.finance.UserBalanceLog;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.finance.UserBalanceLogVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.mapper.finance.UserBalanceLogMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.UserBalanceLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 示例服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class UserBalanceLogServiceImpl extends BaseServiceImpl<UserBalanceLogMapper, UserBalanceLog> implements UserBalanceLogService {
    @Resource
    private UserMapper userMapper;

    @Override
    public Page<UserBalanceLogVO> list(UserBalanceLogListDTO listDTO) {
        // 分页
        Page<UserBalanceLog> page = buildSortOrder(listDTO);
        // 构造查询构造器
        MPJLambdaWrapper<UserBalanceLog> queryWrapper = new MPJLambdaWrapper<>();

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        queryWrapper.like(StringUtils.isNotBlank(keyword), UserBalanceLog::getChangeDesc, keyword)
                .eq(listDTO.getUserId() != null && listDTO.getUserId() > 0, UserBalanceLog::getUserId, listDTO.getUserId())
                .gt(listDTO.getBalance() != null && listDTO.getBalance() > 0, UserBalanceLog::getBalance, 0)
                .leftJoin(User.class, User::getUserId, UserBalanceLog::getUserId)
                .select(User::getUsername)
                .selectAll(UserBalanceLog.class)
                .like(StrUtil.isNotBlank(listDTO.getUsername()), User::getUsername, listDTO.getUsername());
        // 执行查询
        Page<UserBalanceLog> userBalanceLogPage = this.selectJoinListPage(page, UserBalanceLog.class, queryWrapper);
        // 获取查询结果
        List<UserBalanceLog> userBalanceLogPageRecords = userBalanceLogPage.getRecords();
        if (CollUtil.isEmpty(userBalanceLogPageRecords)) {
            return new Page<>();
        }
        List<UserBalanceLogVO> userBalanceLogVOList = userBalanceLogPageRecords.stream()
                .map(userBalanceLog -> {
                    UserBalanceLogVO userBalanceLogVO = new UserBalanceLogVO();
                    BeanUtils.copyProperties(userBalanceLog, userBalanceLogVO);

                    userBalanceLogVO.setChangeTime(TigUtils.handelTime(userBalanceLog.getChangeTime()));
                    userBalanceLogVO.setChangeTypeName(UserBalanceLogType.getDescription(userBalanceLog.getChangeType()));

                    return userBalanceLogVO;
                }).toList();
        return PageUtil.convertPage(userBalanceLogPage, userBalanceLogVOList);
    }

    /**
     * 获取所有用户的余额变动总和
     *
     * @return BigDecimal
     */
    @Override
    public BigDecimal getUserBalanceLogTotal(String startTime, String endTime) {
        LambdaQueryWrapper<UserBalanceLog> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimeDate = DateUtil.parse(startTime);
            Date endTimeDate = DateUtil.parse(endTime);
            // 将时间设置为当天的结束时间
            endTimeDate = DateUtil.endOfDay(endTimeDate);

            // 添加时间范围的条件，假设 changeTime 是 Unix 时间戳
            queryWrapper.ge(UserBalanceLog::getChangeTime, startTimeDate.getTime() / 1000)
                    .le(UserBalanceLog::getChangeTime, endTimeDate.getTime() / 1000);
        }

        // 查询所有符合条件的余额变动
        queryWrapper.select(UserBalanceLog::getBalance);
        List<UserBalanceLog> userBalanceLogs = this.list(queryWrapper);

        // 计算余额变动总和
        return userBalanceLogs.stream()
                .map(UserBalanceLog::getBalance)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 获取所有用户的 冻结余额变动总和
     *
     * @return BigDecimal
     */
    @Override
    public BigDecimal getUserFrozenBalanceLogTotal(String startTime, String endTime) {
        LambdaQueryWrapper<UserBalanceLog> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimeDate = DateUtil.parse(startTime);
            Date endTimeDate = DateUtil.parse(endTime);
            // 将时间设置为当天的结束时间
            endTimeDate = DateUtil.endOfDay(endTimeDate);

            // 添加时间范围的条件，假设 changeTime 是 Unix 时间戳
            queryWrapper.ge(UserBalanceLog::getChangeTime, startTimeDate.getTime() / 1000)
                    .le(UserBalanceLog::getChangeTime, endTimeDate.getTime() / 1000);
        }

        // 查询所有符合条件的余额变动
        queryWrapper.select(UserBalanceLog::getFrozenBalance);
        List<UserBalanceLog> userBalanceLogs = this.list(queryWrapper);

        // 计算余额变动总和
        return userBalanceLogs.stream()
                .map(UserBalanceLog::getFrozenBalance)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changesInFunds(UserBalanceLog userBalanceLog) {
        return this.save(userBalanceLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accountChange(Integer userId, UserBalanceLog data) {
        // 创建余额日志记录
        UserBalanceLog log = new UserBalanceLog();
        log.setUserId(userId);
        log.setBalance(data.getBalance() != null ? data.getBalance() : BigDecimal.ZERO);
        log.setFrozenBalance(data.getFrozenBalance() != null ? data.getFrozenBalance() : BigDecimal.ZERO);
        log.setChangeDesc(data.getChangeDesc() != null ? data.getChangeDesc() : "");
        log.setChangeType(data.getChangeType() != null ? data.getChangeType() : 99);

        save(log);

        // 更新用户余额
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new GlobalException("用户不存在");
        }

        if (log.getChangeType() == 1) {
            user.setBalance(user.getBalance().add(log.getBalance()));
            user.setFrozenBalance(user.getFrozenBalance().add(log.getFrozenBalance()));
        } else {
            user.setBalance(user.getBalance().subtract(log.getBalance()));
            user.setFrozenBalance(user.getFrozenBalance().subtract(log.getFrozenBalance()));
        }

        userMapper.updateById(user);
    }

    @Override
    public boolean create(UserBalanceLogCreatDTO userBalanceLogCreatDTO) {
        UserBalanceLog userBalanceLog = new UserBalanceLog();
        BeanUtils.copyProperties(userBalanceLogCreatDTO, userBalanceLog);
        userBalanceLog.setChangeTime(StringUtils.getCurrentTime());
        return this.save(userBalanceLog);
    }

}
