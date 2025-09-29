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

package com.tigshop.service.im.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.*;
import com.tigshop.bean.enums.im.MessageType;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.im.Config;
import com.tigshop.bean.model.im.ImConversation;
import com.tigshop.bean.model.im.ImMessage;
import com.tigshop.bean.model.im.ImServant;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.im.ContentVO;
import com.tigshop.bean.vo.im.ImMessageListVO;
import com.tigshop.bean.vo.im.ImMessageSendVO;
import com.tigshop.bean.vo.im.ImMessageVO;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.im.ImConfigMapper;
import com.tigshop.mapper.im.ImConversationMapper;
import com.tigshop.mapper.im.ImMessageMapper;
import com.tigshop.mapper.im.ImServantMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.client.APIClient;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.im.ImMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 会话消息服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Slf4j
@Service
public class ImMessageServiceImpl extends BaseServiceImpl<ImMessageMapper, ImMessage> implements ImMessageService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private ImConversationMapper imConversationMapper;
    @Resource
    private ImServantMapper imServantMapper;
    @Resource
    private APIClient apiClient;
    @Resource
    private ImConfigMapper imConfigMapper;
    @Resource
    TranslatePackageImpl translatePackage;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisCache redisCache;

    @Override
    public Page<ImMessageListVO> list(ImMessageListDTO listDTO) {
        // 分页
        Page<ImMessage> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        //构建查询参数
        buildQueryWrapper(queryWrapper, listDTO);

        // 执行查询
        Page<ImMessage> imMessagePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ImMessage> imMessagePageRecords = imMessagePage.getRecords();
        //将userId取出
        List<Integer> userIds = imMessagePageRecords.stream().map(ImMessage::getUserId).toList();
        if (CollUtil.isEmpty(userIds)) {
            return new Page<>();
        }
        //将userList转为map
        Map<Integer, User> userMap = getUserMap(userIds);
        //将servant_id取出
        List<Integer> servantIds = imMessagePageRecords.stream().map(ImMessage::getServantId).toList();
        List<AdminUser> adminUsers = adminUserMapper.selectBatchIds(servantIds);
        Map<Integer, AdminUser> servantMap = adminUsers.stream()
                .collect(Collectors.toMap(AdminUser::getAdminId, Function.identity()));
        // 转换为VO
        List<ImMessageListVO> imMessageVOList = new ArrayList<>(imMessagePageRecords.stream()
                .map(imMessage -> {
                    ImMessageListVO imMessageListVO = new ImMessageListVO();
                    ImMessageVO imMessageVO = getImMessageVO(imMessage);
                    BeanUtils.copyProperties(imMessageVO, imMessageListVO);
                    ImMessageListVO.UserVO userVO = new ImMessageListVO.UserVO();
                    if (userMap.get(imMessage.getUserId()) != null) {
                        BeanUtils.copyProperties(userMap.get(imMessage.getUserId()), userVO);
                        imMessageListVO.setUser(userVO);
                    } else {
                        imMessageListVO.setUser(null);
                    }
                    ImMessageListVO.ServantVO servantVO = new ImMessageListVO.ServantVO();
                    if (servantMap.get(imMessage.getServantId()) != null) {
                        BeanUtils.copyProperties(servantMap.get(imMessage.getServantId()), servantVO);
                        imMessageListVO.setServant(servantVO);
                    } else {
                        imMessageListVO.setServant(null);
                    }
                    return imMessageListVO;
                }).toList());
        imMessageVOList.sort(Comparator.comparing(ImMessageListVO::getSendTime).thenComparing(ImMessageListVO::getId));
        return PageUtil.convertPage(imMessagePage, imMessageVOList);
    }

    public ImMessageVO getImMessageVO(ImMessage imMessage) {
        ImMessageVO imMessageVO = new ImMessageVO();
        BeanUtils.copyProperties(imMessage, imMessageVO);
        imMessageVO.setMessageTypeText(MessageType.getTypeName(imMessageVO.getMessageType()));
        imMessageVO.setSendTime(TigUtils.handelTime(imMessage.getSendTime()));
        ContentVO contentVO = JsonUtil.fromJson(imMessage.getContent(), ContentVO.class);
        imMessageVO.setContent(contentVO);
        return imMessageVO;
    }

    public Map<Integer, User> getUserMap(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<User> userList = userMapper.selectBatchIds(userIds);
        //将userList转为map
        return userList.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
    }

    public void buildQueryWrapper(LambdaQueryWrapper<ImMessage> queryWrapper, ImMessageListDTO listDTO) {
        if (listDTO.getConversationId() != null && listDTO.getConversationId() > 0) {
            queryWrapper.eq(ImMessage::getConversationId, listDTO.getConversationId());
        }

        if (listDTO.getUserId() != null && listDTO.getUserId() > 0) {
            queryWrapper.eq(ImMessage::getUserId, listDTO.getUserId());
        }

        if (listDTO.getShopId() != null && listDTO.getShopId() > -1) {
            queryWrapper.eq(ImMessage::getShopId, listDTO.getShopId());
        }

        if (listDTO.getFirstId() != null && listDTO.getFirstId() > -1) {
            queryWrapper.lt(ImMessage::getId, listDTO.getFirstId());
        }

        if (listDTO.getServantId() != null) {
            queryWrapper.eq(ImMessage::getServantId, listDTO.getServantId());
        }

        if (listDTO.getType() != null) {
            queryWrapper.eq(ImMessage::getType, listDTO.getType());
        }

        if (listDTO.getIsRead() != null) {
            queryWrapper.eq(ImMessage::getIsRead, listDTO.getIsRead());
        }
    }

    @Override
    public ImMessageVO detail(Integer id) {
        if (id != null) {
            ImMessage imMessage = this.getById(id);
            ImMessageVO imMessageVO = new ImMessageVO();
            BeanUtils.copyProperties(imMessage, imMessageVO);
            return imMessageVO;
        }
        return new ImMessageVO();
    }

    @Override
    public boolean create(ImMessageCreateDTO createDTO) {
        if (createDTO != null) {
            ImMessage imMessage = new ImMessage();
            BeanUtils.copyProperties(createDTO, imMessage);
            return this.save(imMessage);
        }
        return false;
    }

    @Override
    public boolean update(ImMessageUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ImMessage imMessage = new ImMessage();
            BeanUtils.copyProperties(updateDTO, imMessage);
            return this.updateById(imMessage);
        }
        return false;
    }

    @Override
    public List<ImMessageVO> getListByConversationIds(List<Integer> conversationIds) {
        if (conversationIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ImMessage::getConversationId, conversationIds);
        List<ImMessage> imMessages = this.list(queryWrapper);
        return imMessages.stream()
                .map(imMessage -> {
                    ImMessageVO imMessageVO = new ImMessageVO();
                    BeanUtils.copyProperties(imMessage, imMessageVO);
                    imMessageVO.setContent(JsonUtil.fromJson(imMessage.getContent(), ContentVO.class));
                    imMessageVO.setMessageTypeText(MessageType.getTypeName(imMessage.getMessageType()));
                    imMessageVO.setSendTime(TigUtils.handelTime(imMessage.getSendTime()));
                    return imMessageVO;
                }).toList();

    }

    @Override
    public long getUnreadMessageCount(Integer conversationId) {
        if (conversationId != null) {
            LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImMessage::getConversationId, conversationId);
            queryWrapper.eq(ImMessage::getIsRead, 0);
            queryWrapper.eq(ImMessage::getType, 1);
            return this.count(queryWrapper);
        }
        return 0L;
    }

    @Override
    public List<ImMessageVO> getListByConversationIdLimitTwo(Integer conversationId) {
        if (conversationId != null) {
            LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImMessage::getConversationId, conversationId);
            queryWrapper.orderByDesc(ImMessage::getId);
            queryWrapper.last("limit 2");
            List<ImMessage> imMessages = this.list(queryWrapper);
            return imMessages.stream()
                    .map(this::getImMessageVO).toList();
        }
        return List.of();
    }

    @Override
    public ImMessageSendVO send(ImMessageSendDTO dto) {
        boolean isAdminUser = TigUtils.getClientTypeByToken();
        ImConversation imConversation;
        String userFrom = "";
        if (isAdminUser) {
            imConversation = imConversationMapper.selectById(dto.getConversationId());
            if (imConversation == null) {
                throw new GlobalException("会话不存在");
            }
            userFrom = imConversation.getUserFrom();
        } else {
            if (dto.getUserId() == null || dto.getUserId() == 0) {
                throw new GlobalException("token数据验证失败", 401);
            }
            LambdaQueryWrapper<ImConversation> queryWrapper = new LambdaQueryWrapper<>();
            if (dto.getConversationId() == null || dto.getConversationId() == 0) {
                queryWrapper.eq(ImConversation::getUserId, dto.getUserId());
                queryWrapper.eq(ImConversation::getShopId, dto.getShopId());
                queryWrapper.eq(ImConversation::getIsDelete, 0);
                queryWrapper.last("limit 1");
                imConversation = imConversationMapper.selectOne(queryWrapper);

                if (imConversation != null && imConversation.getStatus() != 1) {
                    ImServant imServant = getImServantByShopId(dto);
                    if (imServant != null) {
                        imConversation.setLastServantId(imServant.getServantId());
                        imConversation.setStatus(1);
                    } else {
                        imConversation.setLastServantId(0);
                        imConversation.setStatus(0);
                    }
                    imConversationMapper.updateById(imConversation);
                    dto.setConversationId(imConversation.getId());
                    dto.setServantId(imConversation.getLastServantId());
                }
                if (imConversation == null) {
                    ImServant imServant = getImServantByShopId(dto);
                    if (imServant != null) {
                        dto.setServantId(imServant.getServantId());
                    }
                    ImConversation createConversation = new ImConversation();
                    createConversation.setUserId(dto.getUserId());
                    createConversation.setShopId(dto.getShopId());
                    createConversation.setUserFrom(dto.getUserFrom());
                    createConversation.setLastServantId(dto.getServantId());
                    createConversation.setAddTime(StringUtils.getCurrentTime());
                    createConversation.setStatus(dto.getServantId() != null ? 1 : 0);
                    int num = imConversationMapper.insert(createConversation);
                    if (num == 0) {
                        throw new GlobalException("创建会话失败");
                    }
                    userFrom = dto.getUserFrom();

                    //更新客服接待数量
                    LambdaQueryWrapper<ImConversation> queryWrapperCount = new LambdaQueryWrapper<>();
                    queryWrapperCount.eq(ImConversation::getLastServantId, dto.getServantId());
                    queryWrapperCount.eq(ImConversation::getStatus, 1);
                    long count = imConversationMapper.selectCount(queryWrapperCount);
                    LambdaQueryWrapper<ImServant> servantQueryWrapper = new LambdaQueryWrapper<>();
                    servantQueryWrapper.eq(ImServant::getServantId, dto.getServantId());
                    ImServant imServantUp = imServantMapper.selectOne(servantQueryWrapper);
                    if (imServantUp != null) {
                        imServantUp.setConversationNum(count);
                        imServantMapper.updateById(imServantUp);
                    }

                    dto.setConversationId(createConversation.getId());
                    dto.setServantId(createConversation.getLastServantId());
                }
                if (imConversation != null) {
                    dto.setConversationId(imConversation.getId());
                    dto.setServantId(imConversation.getLastServantId());
                }
                if (ObjectUtil.isNotEmpty(dto.getServantId()) && dto.getServantId() > 0) {
                    // 发送首次欢迎消息到 MQ
                    AutoSendDTO autoSendDTO = new AutoSendDTO(dto.getConversationId().toString(), "autoWelcome");
                    sendAutoMessage(autoSendDTO);
                }
                if (dto.getServantId() == null || dto.getServantId() == 0) {
                    // 客服忙提醒语（有客服但status为忙碌）
                    if (hasImServantByShopId(dto) != null) {
                        AutoSendDTO autoSendDTO = new AutoSendDTO(dto.getConversationId().toString(), "autoWaiting");
                        sendAutoMessage(autoSendDTO);
                    } else {
                        AutoSendDTO autoSendDTO = new AutoSendDTO(dto.getConversationId().toString(), "autoOffWork");
                        sendAutoMessage(autoSendDTO);
                    }
                }
            } else {
                queryWrapper.eq(ImConversation::getId, dto.getConversationId());
                imConversation = imConversationMapper.selectOne(queryWrapper);
                if (imConversation.getStatus() != 1) {
                    throw new GlobalException("会话不在进行中");
                }
                dto.setServantId(imConversation.getLastServantId());
            }
            if (imConversation != null) {
                userFrom = imConversation.getUserFrom();
                dto.setConversationId(imConversation.getId());
                dto.setServantId(imConversation.getLastServantId());
            }
        }

        ImMessage imMessage = new ImMessage();
        BeanUtils.copyProperties(dto, imMessage);
        imMessage.setMessageType(dto.getContent().getMessageType() != null ? dto.getContent().getMessageType() : "text");
        imMessage.setSendTime(StringUtils.getCurrentTime());
        imMessage.setContent(JsonUtil.toJson(dto.getContent()));
        imMessage.setIsRead(0);
        if (imMessage.getServantId() == null) {
            imMessage.setServantId(0);
        }
        this.save(imMessage);

        return this.sentMessage(imMessage, false, userFrom);
    }

    private ImServant getImServantByShopId(ImMessageSendDTO dto) {
        LambdaQueryWrapper<ImServant> queryWrapperServant = new LambdaQueryWrapper<>();
        queryWrapperServant.eq(ImServant::getShopId, dto.getShopId());
        queryWrapperServant.eq(ImServant::getStatus, 1);
        queryWrapperServant.orderByAsc(ImServant::getConversationNum);
        queryWrapperServant.last("limit 1");
        return imServantMapper.selectOne(queryWrapperServant);
    }

    /**
     * 判断是否有忙碌客服
     * @param dto
     * @return
     */
    private ImServant hasImServantByShopId(ImMessageSendDTO dto) {
        LambdaQueryWrapper<ImServant> queryWrapperServant = new LambdaQueryWrapper<>();
        queryWrapperServant.eq(ImServant::getShopId, dto.getShopId());
        queryWrapperServant.eq(ImServant::getStatus, 2);
        queryWrapperServant.orderByAsc(ImServant::getConversationNum);
        queryWrapperServant.last("limit 1");
        return imServantMapper.selectOne(queryWrapperServant);
    }

    public ImMessageSendVO sentMessage(ImMessage imMessage, boolean auto, String userFrom) {
        //更新会话
        ImConversation imConversation = imConversationMapper.selectById(imMessage.getConversationId());
        imConversation.setLastUpdateTime(StringUtils.getCurrentTime());
        imConversationMapper.updateById(imConversation);
        ImMessageSendVO imMessageSendVO = sendMessageVO(imMessage);

        //构建发送结构
        Map<String, Object> msg = new HashMap<>();

        List<Object> msgTo = new ArrayList<>();
        msgTo.add(imMessageSendVO);

        msg.put("type", "message");
        msg.put("data", msgTo);
        String message = JsonUtil.toJson(msg);
        //发送消息
        if (auto) {
            //给前台发消息
            apiClient.sendMessage("false::" + imMessage.getUserId(), message);
            //给后台发消息
            apiClient.sendMessage("true::" + imMessage.getServantId(), message);
        } else {
            if (imMessage.getType() == 1) {
                apiClient.sendMessage("true::" + imMessage.getServantId(), message);
            } else if (imMessage.getType() == 2) {
                apiClient.sendMessage("false::" + imMessage.getUserId(), message);
            }
        }
        return imMessageSendVO;
    }

    //构建发送消息
    public ImMessageSendVO sendMessageVO(ImMessage imMessage) {
        ImMessageVO imMessageVO = getImMessageVO(imMessage);
        ImMessageSendVO imMessageSendVO = new ImMessageSendVO();
        BeanUtils.copyProperties(imMessageVO, imMessageSendVO);
        User user = userMapper.selectById(imMessage.getUserId());
        ImMessageSendVO.UserVO userVO = new ImMessageSendVO.UserVO();
        BeanUtils.copyProperties(user, userVO);
        imMessageSendVO.setUser(userVO);
        AdminUser adminUser = adminUserMapper.selectById(imMessage.getServantId());
        ImMessageSendVO.ServantVO servantVO = new ImMessageSendVO.ServantVO();
        if (adminUser != null) {
            BeanUtils.copyProperties(adminUser, servantVO);
        } else {
            servantVO.setAdminId(0);
            servantVO.setUsername("");
            servantVO.setAvatar("");
        }
        imMessageSendVO.setServant(servantVO);
        return imMessageSendVO;
    }

    public void sendReadToClient(ImSetReadDTO dto, boolean isAdmin) {
        //dto里set 当时的时间格式 yy-mm-dd
        dto.setTime(TigUtils.handelTime(StringUtils.getCurrentTime()));
        //构建发送结构
        Map<String, Object> read = new HashMap<>();
        List<Object> msgTo = new ArrayList<>();
        msgTo.add(dto);
        read.put("type", "read");
        read.put("data", msgTo);
        if (isAdmin) {
            apiClient.sendMessage("false::" + dto.getUserId(), JsonUtil.toJson(read));
        } else {
            apiClient.sendMessage("true::" + dto.getServantId(), JsonUtil.toJson(read));
        }
    }

    @Override
    public Long getUnreadImMessageCount(Integer shopId, Integer adminUserId) {
        LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImMessage::getShopId, shopId);
        queryWrapper.eq(ImMessage::getServantId, adminUserId);
        queryWrapper.eq(ImMessage::getIsRead, 0);
        queryWrapper.eq(ImMessage::getType, 1);
        queryWrapper.gt(ImMessage::getUserId, 0);
        return this.count(queryWrapper);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean setRead(ImSetReadDTO dto) {
        boolean isAdminUser = TigUtils.getClientTypeByToken();
        if (isAdminUser) {
            if (dto.getConversationId() == null || dto.getUserId() == null) {
                throw new GlobalException("会话id和用户id不能为空");
            }
            dto.setShopId(HeaderUtils.getShopId());
            LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImMessage::getShopId, dto.getShopId());
            queryWrapper.eq(ImMessage::getIsRead, 0);
            queryWrapper.eq(ImMessage::getType, 1);
            queryWrapper.eq(ImMessage::getUserId, dto.getUserId());
            List<ImMessage> imMessages = this.list(queryWrapper);
            //获取所有的id
            if (!imMessages.isEmpty()) {
                //将所有ids is_read 设置为1
                imMessages.forEach(imMessage -> imMessage.setIsRead(1));
                this.updateBatchById(imMessages);
            }
        } else {
            Integer currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == 0) {
                throw new GlobalException(translatePackage.translate("用户尚未登录"));
            }
            dto.setUserId(currentUserId);
            LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImMessage::getUserId, dto.getUserId());
            queryWrapper.eq(ImMessage::getIsRead, 0);
            queryWrapper.eq(ImMessage::getType, 2);
            queryWrapper.eq(ImMessage::getShopId, dto.getShopId());
            List<ImMessage> imMessages = this.list(queryWrapper);
            if (!imMessages.isEmpty()) {
                //将所有ids is_read 设置为1
                imMessages.forEach(imMessage -> imMessage.setIsRead(1));
                this.updateBatchById(imMessages);
            }
        }
        sendReadToClient(dto, isAdminUser);
        return true;
    }

    @Override
    public boolean autoTransfer(Integer conversationId, Integer servantId, Integer oldServantId) {
        LambdaQueryWrapper<ImConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImConversation::getId, conversationId);
        ImConversation imConversation = imConversationMapper.selectOne(queryWrapper);
        String content;
        if (imConversation != null) {
            if (imConversation.getStatus() == 2) {
                return false;
            }
            if (!Objects.equals(servantId, oldServantId) && oldServantId > 0) {
                AdminUser servant = adminUserMapper.selectById(servantId);
                AdminUser oldServant = adminUserMapper.selectById(oldServantId);
                content = String.format("%s将客户转接给%s", oldServant.getUsername(), servant.getUsername());
            } else {
                AdminUser servant = adminUserMapper.selectById(servantId);
                content = String.format("客户被%s接入", servant.getUsername());
                // 发送首次欢迎消息到 MQ
                AutoSendDTO autoSendDTO = new AutoSendDTO(imConversation.getId().toString(), "autoWelcome");
                sendAutoMessage(autoSendDTO);
            }
            //创建消息
            ImMessage imMessage = new ImMessage();
            imMessage.setIsRead(0);
            imMessage.setUserId(imConversation.getUserId());
            imMessage.setServantId(servantId);
            imMessage.setConversationId(conversationId);
            imMessage.setSendTime(StringUtils.getCurrentTime());
            imMessage.setType(3);
            imMessage.setShopId(imConversation.getShopId());
            imMessage.setMessageType("text");
            ContentVO contentVO = new ContentVO();
            contentVO.setMessageType("text");
            contentVO.setContent(content);
            imMessage.setContent(JsonUtil.toJson(contentVO));
            this.save(imMessage);
            //发送消息
            sentMessage(imMessage, true, "");
            return true;
        }
        return false;
    }

    @Override
    public boolean autoWelcome(Integer conversationId) {
        ImConversation imConversation = imConversationMapper.selectById(conversationId);
        Config config = getConfig("welcome", imConversation.getShopId());

        if (config == null) {
            return true;
        }
        ConfigDataDTO configData = JsonUtil.fromJson(config.getData(), ConfigDataDTO.class);
        if (!configData.isActivate()) {
            return true;
        }

        // 判断今天是否已经发送过
        String cacheKey = "autoWelcome:" + imConversation.getShopId() + ":" + imConversation.getUserId() + ":" + DateUtil.formatDate(DateUtil.date());
        Boolean alreadySent = redisCache.getCacheObject(cacheKey);
        if (alreadySent != null && alreadySent) {
            log.error("今天已经给用户 {} 在店铺 {} 发送过首次欢迎消息，跳过", imConversation.getUserId(), imConversation.getShopId());
            return true;
        }

        ImMessage imMessage = new ImMessage();
        imMessage.setIsRead(0);
        imMessage.setSendTime(StringUtils.getCurrentTime());
        imMessage.setUserId(imConversation.getUserId());
        imMessage.setServantId(imConversation.getLastServantId());
        imMessage.setConversationId(conversationId);
        imMessage.setType(2);
        imMessage.setShopId(imConversation.getShopId());

        if (configData.isSendText()) {
            imMessage.setMessageType("text");
            ContentVO contentVO = new ContentVO();
            contentVO.setMessageType("text");
            contentVO.setContent(configData.getReplyContent());
            imMessage.setContent(JsonUtil.toJson(contentVO));
            imMessage.setId(null);
            this.save(imMessage);
            sentMessage(imMessage, true, "");
        }
        if (configData.isSendWechat()) {
            imMessage.setMessageType("image");
            ContentVO contentVO = new ContentVO();
            contentVO.setMessageType("image");
            contentVO.setPic(configData.getWechatImage());
            imMessage.setContent(JsonUtil.toJson(contentVO));
            imMessage.setId(null);
            this.save(imMessage);
            sentMessage(imMessage, true, "");
        }
        redisCache.setCacheObject(cacheKey, true);
        return true;
    }

    @Override
    public boolean autoWaiting(Integer conversationId) {
        ImConversation imConversation = imConversationMapper.selectById(conversationId);
        Config config = getConfig("waiting", imConversation.getShopId());

        if (config == null) {
            return true;
        }
        ConfigDataDTO configData = JsonUtil.fromJson(config.getData(), ConfigDataDTO.class);
        if (!configData.isActivate()) {
            return true;
        }

        if (configData.isSendText()) {
            createImageBySendJob(imConversation, configData);
            return true;

        }
        return true;
    }

    @Override
    public boolean autoOffWork(Integer conversationId) {
        ImConversation imConversation = imConversationMapper.selectById(conversationId);
        Config config = getConfig("off_work", imConversation.getShopId());
        if (config == null) {
            return true;
        }
        ConfigDataDTO configData = JsonUtil.fromJson(config.getData(), ConfigDataDTO.class);
        if (!configData.isActivate()) {
            return true;
        }
        if (configData.isSendText()) {
            createImageBySendJob(imConversation, configData);
        }
        return true;
    }

    private void createImageBySendJob(ImConversation imConversation, ConfigDataDTO configData) {
        ImMessage imMessage = new ImMessage();
        imMessage.setIsRead(0);
        imMessage.setUserId(imConversation.getUserId());
        imMessage.setServantId(imConversation.getLastServantId());
        imMessage.setConversationId(imConversation.getId());
        imMessage.setType(2);
        imMessage.setSendTime(StringUtils.getCurrentTime());
        imMessage.setShopId(imConversation.getShopId());
        imMessage.setMessageType("text");
        ContentVO contentVO = new ContentVO();
        contentVO.setMessageType("text");
        contentVO.setContent(configData.getReplyContent());
        imMessage.setContent(JsonUtil.toJson(contentVO));
        this.save(imMessage);
        sentMessage(imMessage, true, "");
    }

    public Config getConfig(String code, Integer shopId) {
        LambdaQueryWrapper<Config> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Config::getCode, code);
        queryWrapper.eq(Config::getShopId, shopId);
        return imConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public void sendAutoMessage(AutoSendDTO autoSend) {
        System.out.println("发送回复消息到 RabbitMQ：" + autoSend);
        // 转json
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE,
                RabbitMQConfig.AUTO_MESSAGE_ROUTING_KEY,
                autoSend,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", 2000);
                    return message;
                }
        );
        System.out.println("发送短信消息到 RabbitMQ：" + new Date());
    }
}
