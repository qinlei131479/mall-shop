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

package com.tigshop.service.user.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.user.UserMessageCreateDTO;
import com.tigshop.bean.query.user.UserMessageListPageQuery;
import com.tigshop.bean.model.user.UserMessage;
import com.tigshop.bean.vo.user.UserMessageVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.mapper.user.UserMessageMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.user.UserMessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 站内消息服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class UserMessageServiceImpl extends BaseServiceImpl<UserMessageMapper, UserMessage> implements UserMessageService {
    @Override
    public Page<UserMessageVO> list(Integer userId, UserMessageListPageQuery listDTO) {
        // 分页
        Page<UserMessage> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<UserMessage> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        queryWrapper.eq(UserMessage::getUserId, userId);

        //unRead筛选
        if (listDTO.getUnread() != null && listDTO.getUnread() == 1) {
            queryWrapper.eq(UserMessage::getIsRead, 0);
        }

        // 执行查询
        Page<UserMessage> userMessagePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserMessage> userMessagePageRecords = userMessagePage.getRecords();
        // 转换为VO
        List<UserMessageVO> userMessageVOList = userMessagePageRecords.stream()
                .map(userMessage -> {
                    UserMessageVO userMessageVO = new UserMessageVO();
                    BeanUtils.copyProperties(userMessage, userMessageVO);
                    DateTime date = DateUtil.date(userMessage.getAddTime() * 1000);
                    userMessageVO.setAddTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
                    userMessageVO.setAddTimeDate(DateUtil.format(date, "yyyy-MM-dd"));
                    userMessageVO.setAddTimeHms(DateUtil.format(date, "HH:mm:ss"));
                    userMessageVO.setLink(JsonUtil.fromJson(userMessage.getLink(), JSONObject.class));
                    return userMessageVO;
                }).toList();
        return PageUtil.convertPage(userMessagePage, userMessageVOList);
    }

    @Override
    public UserMessageVO detail(Integer id) {
        if (id != null) {
            UserMessage userMessage = this.getById(id);
            UserMessageVO userMessageVO = new UserMessageVO();
            BeanUtils.copyProperties(userMessage, userMessageVO);
            return userMessageVO;
        }
        return new UserMessageVO();
    }

    @Override
    public boolean create(UserMessageCreateDTO createDTO) {
        if (createDTO != null) {
            UserMessage userMessage = new UserMessage();
            BeanUtils.copyProperties(createDTO, userMessage);
            return this.save(userMessage);
        }
        return false;
    }

    @Override
    public boolean setAllMessageRead(Integer userId) {
        if (userId == null) {
            return false;
        }
        LambdaUpdateWrapper<UserMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserMessage::getUserId, userId);
        updateWrapper.set(UserMessage::getIsRead, 1);
        return this.update(updateWrapper);
    }

    @Override
    public boolean setMessageRead(Integer userId, Integer messageId) {
        if (userId == null || messageId == null) {
            return false;
        }
        LambdaUpdateWrapper<UserMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserMessage::getUserId, userId);
        updateWrapper.eq(UserMessage::getMessageId, messageId);
        updateWrapper.set(UserMessage::getIsRead, 1);
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserMessage(Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        if (id == null || id == 0) {
            throw new GlobalException("删除失败，消息不存在");
        }
        UserMessage userMessage = this.getById(id);
        if (userMessage != null) {
            Integer messageUserId = userMessage.getUserId();
            if (messageUserId == null || !messageUserId.equals(userId)){
                throw new GlobalException("删除失败，您没有权限删除此消息");
            }
        }
        this.removeById(id);
    }

}
