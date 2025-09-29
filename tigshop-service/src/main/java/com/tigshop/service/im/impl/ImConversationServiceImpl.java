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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.im.*;
import com.tigshop.bean.enums.im.ConversationStatus;
import com.tigshop.bean.enums.im.MessageType;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.im.ImConversation;
import com.tigshop.bean.model.im.ImMessage;
import com.tigshop.bean.model.im.ImServant;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.query.im.ImConversationPageQuery;
import com.tigshop.bean.vo.im.*;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.common.utils.TigUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.im.ImConversationMapper;
import com.tigshop.mapper.im.ImServantMapper;
import com.tigshop.mapper.shop.ShopMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.im.ImConversationService;
import com.tigshop.service.im.ImMessageService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客服会话服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class ImConversationServiceImpl extends BaseServiceImpl<ImConversationMapper, ImConversation> implements ImConversationService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private ShopMapper shopMapper;

    @Resource
    private ImMessageService imMessageService;

    @Resource
    private ImServantMapper imServantMapper;

    @Resource
    private AdminUserMapper adminUserMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    TranslatePackageImpl translatePackage;

    private Page<ImConversation> getImConversationList(ImConversationPageQuery pageQuery) {

        // 分页
        Page<ImConversation> page = buildSortOrder(pageQuery);

        // 构造查询构造器
        LambdaQueryWrapper<ImConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImConversation::getIsDelete, 0);
        // 构建查询参数
        buildQueryWrapper(queryWrapper, pageQuery);

        // 执行查询
        return this.page(page, queryWrapper);
    }

    @Override
    public Page<ImConversationVO> list(ImConversationPageQuery listDTO) {
        Page<ImConversation> imConversationPage = getImConversationList(listDTO);
        List<ImConversation> imConversationPageRecords = imConversationPage.getRecords();
        //获取用户信息
        List<Integer> userIds = imConversationPageRecords.stream()
                .map(ImConversation::getUserId)
                .toList();
        if (userIds.isEmpty()) {
            return new Page<>();
        }
        //查询用户信息
        List<User> userList = userMapper.selectBatchIds(userIds);
        //将user转为map
        Map<Integer, User> userMap = userList.stream()
                .collect(java.util.stream.Collectors.toMap(User::getUserId, Function.identity()));
        //获取店铺信息
        List<Integer> shopIds = imConversationPageRecords.stream()
                .map(ImConversation::getShopId)
                .toList();
        List<Shop> shopList = shopMapper.selectBatchIds(shopIds);
        Map<Integer, Shop> shopMap = shopList.stream()
                .collect(java.util.stream.Collectors.toMap(Shop::getShopId, Function.identity()));
        //会话消息表
        List<Integer> conversationIds = imConversationPageRecords.stream()
                .map(ImConversation::getId)
                .toList();
        //从imMessage中查出符合数据
        List<ImMessageVO> imMessageList = imMessageService.getListByConversationIds(conversationIds);
        Map<Integer, List<ImMessageVO>> imMessageMap = imMessageList.stream()
                .collect(Collectors.groupingBy(ImMessageVO::getConversationId));
        // 转换为VO
        List<ImConversationVO> imConversationVOList = imConversationPageRecords.stream()
                .map(imConversation -> {
                    ImConversationVO imConversationVO = new ImConversationVO();
                    BeanUtils.copyProperties(imConversation, imConversationVO);
                    imConversationVO.setAddTime(TigUtils.handelTime(imConversation.getAddTime()));
                    imConversationVO.setLastUpdateTime(TigUtils.handelTime(imConversation.getLastUpdateTime()));
                    User user = userMap.get(imConversation.getUserId());
                    ImConversationVO.UserVO userVO = new ImConversationVO.UserVO();
                    imConversationVO.setUnreadMessageCount(imMessageService.getUnreadMessageCount(imConversation.getId()));
                    if (user != null) {
                        BeanUtils.copyProperties(user, userVO);
                        imConversationVO.setUser(userVO);
                    } else {
                        imConversationVO.setUser(null);
                    }
                    Shop shop = shopMap.get(imConversation.getShopId());
                    ImConversationVO.ShopVO shopVO = new ImConversationVO.ShopVO();
                    if (shop != null) {
                        BeanUtils.copyProperties(shop, shopVO);
                        imConversationVO.setShop(shopVO);
                    } else {
                        imConversationVO.setShop(null);
                    }
                    if (imMessageMap.get(imConversation.getId()) != null) {
                        List<ImMessageVO> imMessageVOList = imMessageMap.get(imConversation.getId());
                        imMessageVOList.sort(Comparator.comparing(ImMessageVO::getSendTime).reversed());
                        imConversationVO.setLastMessage(imMessageVOList);
                    } else {
                        imConversationVO.setLastMessage(new ArrayList<>());
                    }
                    return imConversationVO;
                }).toList();
        return PageUtil.convertPage(imConversationPage, imConversationVOList);
    }

    public void buildQueryWrapper(LambdaQueryWrapper<ImConversation> queryWrapper, ImConversationPageQuery listDTO) {
        if (listDTO.getShopId() != null && listDTO.getShopId() > 0) {
            queryWrapper.eq(ImConversation::getShopId, listDTO.getShopId());
        }

        if (listDTO.getUserId() != null && listDTO.getUserId() > 0) {
            queryWrapper.eq(ImConversation::getUserId, listDTO.getUserId());
        }

        if (listDTO.getStatus() != null && listDTO.getStatus() != -2) {
            if (listDTO.getStatus() == -1) {
                List<Integer> statusList = new ArrayList<>();
                statusList.add(ConversationStatus.IN_PROGRESS.getCode());
                statusList.add(ConversationStatus.CLOSED.getCode());
                queryWrapper.in(ImConversation::getStatus, statusList);
            } else {
                queryWrapper.eq(ImConversation::getStatus, listDTO.getStatus());
            }
        }

        if (listDTO.getLastServantId() != null && listDTO.getLastServantId() != -1) {
            queryWrapper.eq(ImConversation::getLastServantId, listDTO.getLastServantId());
        }

        if (listDTO.getUserFrom() != null) {
            queryWrapper.eq(ImConversation::getUserFrom, listDTO.getUserFrom());
        }

        if (listDTO.getIsDelete() != null) {
            queryWrapper.eq(ImConversation::getIsDelete, listDTO.getIsDelete());
        }

        if (listDTO.getStartTime() != null && listDTO.getEndTime() != null) {
            //将时间格式转为时间戳
            queryWrapper.between(ImConversation::getAddTime, StringUtils.dateToTimestampExample(listDTO.getStartTime()),
                    StringUtils.dateToTimestampExample(listDTO.getEndTime()));
        }
        if (listDTO.getRemark() != null) {
            queryWrapper.like(ImConversation::getRemark, listDTO.getRemark());
        }

        if (listDTO.getUsername() != null) {
            LambdaQueryWrapper<User> queryWrapperUser = new LambdaQueryWrapper<>();
            queryWrapperUser.like(User::getUsername, listDTO.getUsername())
                    .or().like(User::getMobile, listDTO.getUsername())
                    .or().like(User::getNickname, listDTO.getUsername());
            List<User> userList = userMapper.selectList(queryWrapperUser);
            if (userList != null && !userList.isEmpty()) {
                List<Integer> userIds = userList.stream().map(User::getUserId).toList();
                queryWrapper.in(ImConversation::getUserId, userIds);
            }
        }
    }

    @Override
    public ImConversationVO detail(Integer id) {
        if (id != null) {
            ImConversation imConversation = this.getById(id);
            ImConversationVO imConversationVO = new ImConversationVO();
            BeanUtils.copyProperties(imConversation, imConversationVO);
            imConversationVO.setAddTime(TigUtils.handelTime(imConversation.getAddTime()));
            imConversationVO.setLastUpdateTime(TigUtils.handelTime(imConversation.getLastUpdateTime()));
            User user = userMapper.selectById(imConversation.getUserId());
            if (user != null) {
                ImConversationVO.UserVO userVO = new ImConversationVO.UserVO();
                BeanUtils.copyProperties(user, userVO);
                imConversationVO.setUser(userVO);
            } else {
                imConversationVO.setUser(null);
            }
            return imConversationVO;
        }
        return null;
    }

    @Override
    public ImConversation create(ImConversationCreateDTO createDTO) {
        if (createDTO != null) {
            LambdaQueryWrapper<ImConversation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ImConversation::getShopId, createDTO.getShopId()).
                    eq(ImConversation::getUserId, createDTO.getUserId()).
                    eq(ImConversation::getIsDelete, 0);
            ImConversation imConversation = this.getOne(queryWrapper);
            if (imConversation != null) {
                imConversation.setStatus(1);
                imConversation.setLastServantId(createDTO.getLastServantId());
                this.updateById(imConversation);
            }
            return imConversation;
        }
        return null;
    }

    @Override
    public boolean update(ImConversationUpdateDTO updateDTO) {
        if (updateDTO != null) {
            ImConversation imConversation = new ImConversation();
            BeanUtils.copyProperties(updateDTO, imConversation);
            return this.updateById(imConversation);
        }
        return false;
    }

    @Override
    public List<ImSearchUserVO> getUserSearch(String keyword, Integer servantId) {
        if (StringUtils.isEmpty(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getUsername, keyword);
        List<User> userList = userMapper.selectList(queryWrapper);
        if (userList.isEmpty()) {
            return List.of();
        }
        //获取用户id
        List<Integer> userIds = userList.stream()
                .map(User::getUserId)
                .toList();
        //获取用户信息
        Map<Integer, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        LambdaQueryWrapper<ImConversation> conversionQueryWrapper = new LambdaQueryWrapper<>();
        conversionQueryWrapper.in(ImConversation::getUserId, userIds);
        conversionQueryWrapper.eq(ImConversation::getLastServantId, servantId);
        conversionQueryWrapper.groupBy(ImConversation::getUserId);
        List<ImConversation> imConversationList = this.list(conversionQueryWrapper);
        if (imConversationList.isEmpty()) {
            return List.of();
        }
        return imConversationList.stream()
                .map(imConversation -> {
                    ImSearchUserVO imSearchUserVO = new ImSearchUserVO();
                    BeanUtils.copyProperties(imConversation, imSearchUserVO);
                    imSearchUserVO.setAddTime(TigUtils.handelTime(imConversation.getAddTime()));
                    imSearchUserVO.setLastUpdateTime(TigUtils.handelTime(imConversation.getLastUpdateTime()));
                    User user = userMap.get(imConversation.getUserId());
                    ImSearchUserVO.UserVO userVO = new ImSearchUserVO.UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    imSearchUserVO.setUser(userVO);
                    return imSearchUserVO;
                }).toList();
    }

    @Override
    public List<ImSearchConversationVO> getConversationSearch(String keyword, Integer servantId) {
        if (StringUtils.isEmpty(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<ImMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ImMessage::getContent, keyword);
        queryWrapper.eq(ImMessage::getType, "text");
        List<ImMessage> imMessageList = imMessageService.list(queryWrapper);
        if (imMessageList.isEmpty()) {
            return List.of();
        }
        //转为map
        Map<Integer, ImMessage> imMessageMap = imMessageList.stream()
                .collect(Collectors.toMap(ImMessage::getConversationId, Function.identity()));
        List<Integer> conversationIds = imMessageList.stream()
                .map(ImMessage::getConversationId)
                .distinct()
                .toList();
        LambdaQueryWrapper<ImConversation> queryWrapperConversation = new LambdaQueryWrapper<>();
        queryWrapperConversation.in(ImConversation::getId, conversationIds);
        queryWrapperConversation.eq(ImConversation::getLastServantId, servantId);
        queryWrapperConversation.eq(ImConversation::getStatus, 1);
        List<ImConversation> imConversationList = this.list(queryWrapperConversation);
        if (imConversationList.isEmpty()) {
            return List.of();
        }
        //获取用户ids
        List<Integer> userIds = imConversationList.stream()
                .map(ImConversation::getUserId)
                .distinct()
                .toList();
        Map<Integer, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
        return imConversationList.stream()
                .map(imConversation -> {
                    ImSearchConversationVO imSearchConversationVO = new ImSearchConversationVO();
                    BeanUtils.copyProperties(imConversation, imSearchConversationVO);
                    imSearchConversationVO.setAddTime(TigUtils.handelTime(imConversation.getAddTime()));
                    imSearchConversationVO.setLastUpdateTime(TigUtils.handelTime(imConversation.getLastUpdateTime()));
                    User user = userMap.get(imConversation.getUserId());
                    ImSearchConversationVO.UserVO userVO = new ImSearchConversationVO.UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    imSearchConversationVO.setUser(userVO);
                    ImMessage imMessage = imMessageMap.get(imConversation.getId());
                    if (imMessage != null) {
                        imSearchConversationVO.setContent(JsonUtil.fromJson(imMessage.getContent(), ContentVO.class));
                    } else {
                        imSearchConversationVO.setContent(new ContentVO());
                    }
                    return imSearchConversationVO;
                }).toList();
    }

    @Override
    public boolean transfer(TransferDTO dto) {
        if ((dto.getServantId() == null) || dto.getConversationId() == null) {
            throw new GlobalException(translatePackage.translate("请选择要转接的客服和会话"));
        }

        ImConversation imConversation = this.getById(dto.getConversationId());
        if (imConversation == null) {
            throw new GlobalException(translatePackage.translate("会话不存在"));
        }
        //会话转接
        imConversation.setLastServantId(dto.getServantId());
        imConversation.setStatus(1);
        this.updateById(imConversation);

        dto.setType("autoTransfer");
        dto.setOldServantId(imConversation.getLastServantId());

        AutoSendDTO autoSend = new AutoSendDTO(
                dto.getConversationId().toString(),
                dto.getType(),
                dto.getOldServantId().toString(),
                dto.getServantId().toString()
        );

        imMessageService.sendAutoMessage(autoSend);

        //更新客服接待数量
        LambdaQueryWrapper<ImConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImConversation::getLastServantId, imConversation.getLastServantId())
                .eq(ImConversation::getStatus, 1);
        long count = this.count(queryWrapper);
        LambdaQueryWrapper<ImServant> servantQueryWrapper = new LambdaQueryWrapper<>();
        servantQueryWrapper.eq(ImServant::getServantId, imConversation.getLastServantId());
        ImServant imServant = imServantMapper.selectOne(servantQueryWrapper);
        if (imServant != null) {
            imServant.setConversationNum(count);
            imServantMapper.updateById(imServant);
        }

        LambdaQueryWrapper<ImConversation> queryWrapperConversation = new LambdaQueryWrapper<>();
        queryWrapperConversation.eq(ImConversation::getLastServantId, dto.getConversationId())
                .eq(ImConversation::getStatus, 1);
        long countConversation = this.count(queryWrapper);
        LambdaQueryWrapper<ImServant> servantImQueryWrapper = new LambdaQueryWrapper<>();
        servantImQueryWrapper.eq(ImServant::getServantId, dto.getConversationId());
        ImServant servant = imServantMapper.selectOne(servantQueryWrapper);
        if (servant != null) {
            servant.setConversationNum(countConversation);
            imServantMapper.updateById(servant);
        }
        return true;
    }

    @Override
    public Page<WaitServantListVO> waitServantList(WaitServantListDTO listDTO) {
        // 分页
        Page<ImConversation> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<ImConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ImConversation::getStatus, 0);
        queryWrapper.eq(ImConversation::getLastServantId, 0);
        queryWrapper.eq(ImConversation::getIsDelete, 0);
        queryWrapper.eq(ImConversation::getShopId, listDTO.getShopId());

        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());
        // 执行查询
        Page<ImConversation> conversationPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<ImConversation> queryPageRecords = conversationPage.getRecords();
        if (queryPageRecords.isEmpty()) {
            return new Page<>();
        }

        //获取用户Ids
        List<Integer> userIds = queryPageRecords.stream()
                .map(ImConversation::getUserId)
                .distinct()
                .toList();
        Map<Integer, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        List<WaitServantListVO> listVO = queryPageRecords.stream()
                .map(imConversation -> {
                    WaitServantListVO waitServantListVO = new WaitServantListVO();
                    BeanUtils.copyProperties(imConversation, waitServantListVO);
                    waitServantListVO.setAddTime(TigUtils.handelTime(imConversation.getAddTime()));
                    waitServantListVO.setLastUpdateTime(TigUtils.handelTime(imConversation.getLastUpdateTime()));
                    User user = userMap.get(imConversation.getUserId());
                    WaitServantListVO.UserVO userVO = new WaitServantListVO.UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    waitServantListVO.setUser(userVO);
                    waitServantListVO.setUserLastTwoMessage(imMessageService.getListByConversationIdLimitTwo(imConversation.getId()));
                    return waitServantListVO;
                }).toList();
        return PageUtil.convertPage(conversationPage, listVO);
    }

    @Override
    public boolean delete(DelDTO dto) {
        ImConversation imConversation = this.getById(dto.getConversationId());
        if (imConversation == null) {
            throw new GlobalException(translatePackage.translate("会话不存在"));
        }
        if (!Objects.equals(imConversation.getShopId(), dto.getShopId())) {
            throw new GlobalException(translatePackage.translate("没有权限"));
        }
        imConversation.setIsDelete(1);
        this.updateById(imConversation);

        LambdaUpdateWrapper<ImMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(ImMessage::getIsRead, 1);
        updateWrapper.eq(ImMessage::getConversationId, imConversation.getId());
        updateWrapper.eq(ImMessage::getIsRead, 0);
        return imMessageService.update(updateWrapper);
    }


    @Override
    public Page<ConsultHistoryListVO> consultHistory(ImConversationPageQuery pageQuery) {
        pageQuery.handleParam();
        Page<ConsultHistoryListVO> page = buildSortOrder(pageQuery);

        Page<ConsultHistoryListVO> conversationHistoryPage = this.baseMapper.consultHistory(page, pageQuery);

        List<ConsultHistoryListVO> imConversationPageRecords = conversationHistoryPage.getRecords();
        if (CollUtil.isEmpty(imConversationPageRecords)) {
            return new Page<>();
        }

        // 获取用户信息
        Map<Integer, User> userMap = getUserMap(imConversationPageRecords);

        // 取出last_servant_id集合
        List<Integer> servantIds = imConversationPageRecords.stream().map(ConsultHistoryListVO::getLastServantId).distinct().toList();
        List<AdminUser> adminUsers = adminUserMapper.selectList(
                Wrappers.lambdaQuery(AdminUser.class).in(AdminUser::getAdminId, servantIds)
        );
        Map<Integer, AdminUser> servantMap = adminUsers.stream().collect(Collectors.toMap(AdminUser::getAdminId, Function.identity()));


        //取出conversation_id集合
        List<Integer> conversationIds = imConversationPageRecords.stream().map(ConsultHistoryListVO::getId).distinct().toList();

        //firstUserMessage
        LambdaQueryWrapper<ImMessage> firstUserMessageQueryWrapper = new LambdaQueryWrapper<>();
        firstUserMessageQueryWrapper.in(ImMessage::getConversationId, conversationIds);
        firstUserMessageQueryWrapper.eq(ImMessage::getStatus, 1);
        firstUserMessageQueryWrapper.orderByAsc(ImMessage::getSendTime);
        firstUserMessageQueryWrapper.last("limit 1");
        List<ImMessage> firstUserMessages = imMessageService.list(firstUserMessageQueryWrapper);

        //处理firstUserMessageVO
        Map<Integer, List<ConsultHistoryListVO.FirstUserMessageVO>> firstUserMessageMap;
        if (!firstUserMessages.isEmpty()) {
            List<ConsultHistoryListVO.FirstUserMessageVO> firstUserMessageList = firstUserMessages.stream()
                    .map(firstUserMessage -> {
                        ConsultHistoryListVO.FirstUserMessageVO firstUserMessageVO = new ConsultHistoryListVO.FirstUserMessageVO();
                        BeanUtils.copyProperties(firstUserMessage, firstUserMessageVO);
                        firstUserMessageVO.setMessageTypeText(MessageType.getTypeName(firstUserMessage.getMessageType()));
                        firstUserMessageVO.setSendTime(TigUtils.handelTime(firstUserMessage.getSendTime()));
                        return firstUserMessageVO;
                    }).toList();
            //将List<ConsultHistoryListVO.FirstUserMessageVO>改为conversationId对应的map结构
            firstUserMessageMap = firstUserMessageList.stream()
                    .collect(Collectors.groupingBy(ConsultHistoryListVO.FirstUserMessageVO::getConversationId));
        } else {
            firstUserMessageMap = new HashMap<>();
        }

        List<ConsultHistoryListVO> resultRecords = imConversationPageRecords.stream()
                .map(imConversation -> {
                    ConsultHistoryListVO consultHistoryListVO = new ConsultHistoryListVO();
                    BeanUtils.copyProperties(imConversation, consultHistoryListVO);

                    consultHistoryListVO.setAddTime(TigUtils.handelTime(Long.valueOf(imConversation.getAddTime())));
                    consultHistoryListVO.setLastUpdateTime(TigUtils.handelTime(Long.valueOf(imConversation.getLastUpdateTime())));
                    consultHistoryListVO.setFirstUserMessage(firstUserMessageMap.getOrDefault(imConversation.getId(), new ArrayList<>()));
                    //ServantMessage
                    consultHistoryListVO.setServantMessage(handel(imConversation.getId()));
                    User user = userMap.get(imConversation.getUserId());
                    if (user != null) {
                        ConsultHistoryListVO.UserVO userVO = new ConsultHistoryListVO.UserVO();
                        BeanUtils.copyProperties(user, userVO);
                        consultHistoryListVO.setUser(userVO);
                    } else {
                        consultHistoryListVO.setUser(null);
                    }
                    AdminUser adminUser = servantMap.get(imConversation.getLastServantId());
                    if (adminUser != null) {
                        ConsultHistoryListVO.ServantVO servantVO = new ConsultHistoryListVO.ServantVO();
                        BeanUtils.copyProperties(adminUser, servantVO);
                        consultHistoryListVO.setServant(servantVO);
                    } else {
                        consultHistoryListVO.setServant(null);
                    }
                    return consultHistoryListVO;
                }).toList();

        return PageUtil.convertPage(conversationHistoryPage, resultRecords);
    }

    private Map<Integer, User> getUserMap(List<ConsultHistoryListVO> imConversationPageRecords) {
        List<Integer> userIds = imConversationPageRecords.stream().map(ConsultHistoryListVO::getUserId).distinct().toList();
        return userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
    }

    @Override
    public boolean saveRemark(SaveRemarkDTO dto) {
        ImConversation imConversation = this.getById(dto.getConversationId());
        if (imConversation != null) {
            if (!Objects.equals(imConversation.getShopId(), dto.getShopId())) {
                throw new GlobalException("没有权限");
            }
            imConversation.setRemark(dto.getRemark());
            imConversation.setSummary(dto.getSummary());
            return this.updateById(imConversation);
        }
        return false;
    }

    private List<ConsultHistoryListVO.ServantMessageVO> handel(Integer conversationId) {
        ConsultHistoryListVO.ServantMessageVO servantMessageVO = new ConsultHistoryListVO.ServantMessageVO();
        QueryWrapper<ImMessage> conversationIdWrapper = buildMessageQueryWrapper(conversationId);
        conversationIdWrapper.select("conversation_id", "servant_id", "message_type", "content");
        ImMessage imMessage = imMessageService.getOne(conversationIdWrapper);
        if (imMessage != null) {
            BeanUtils.copyProperties(imMessage, servantMessageVO);
            servantMessageVO.setMessageTypeText(MessageType.getTypeName(imMessage.getMessageType()));
        } else {
            return new ArrayList<>();
        }
        //获取message_count
        QueryWrapper<ImMessage> messageCountWrapper = buildMessageQueryWrapper(conversationId);
        long messageCount = imMessageService.count(messageCountWrapper);
        servantMessageVO.setMessageCount(messageCount);
        //获取最大的send_time
        QueryWrapper<ImMessage> maxSendTimeWrapper = buildMessageQueryWrapper(conversationId);
        maxSendTimeWrapper.select("max(send_time) as send_time");
        ImMessage imMessageMaxSendTime = imMessageService.getOne(maxSendTimeWrapper);
        if (imMessageMaxSendTime != null) {
            servantMessageVO.setLastSendTime(imMessageMaxSendTime.getSendTime());
        } else {
            servantMessageVO.setLastSendTime(null);
        }
        //first_send_time
        QueryWrapper<ImMessage> firstSendTimeWrapper = buildMessageQueryWrapper(conversationId);
        firstSendTimeWrapper.select("SUBSTRING_INDEX(GROUP_CONCAT(send_time ORDER BY send_time DESC), ',', -1) " +
                "as send_time");
        ImMessage imMessageFirstSendTime = imMessageService.getOne(firstSendTimeWrapper);
        if (imMessageFirstSendTime != null) {
            servantMessageVO.setFirstSendTime(imMessageFirstSendTime.getSendTime());
        } else {
            servantMessageVO.setFirstSendTime(null);
        }
        List<ConsultHistoryListVO.ServantMessageVO> servantMessageVOList = new ArrayList<>();
        servantMessageVOList.add(servantMessageVO);
        return servantMessageVOList;
    }

    private QueryWrapper<ImMessage> buildMessageQueryWrapper(Integer conversationId) {
        QueryWrapper<ImMessage> servantMessageQueryWrapper = new QueryWrapper<>();
        servantMessageQueryWrapper.eq("conversation_id", conversationId);
        servantMessageQueryWrapper.eq("type", 2);
        servantMessageQueryWrapper.eq("status", 1);
        servantMessageQueryWrapper.orderByAsc("send_time");
        servantMessageQueryWrapper.last("limit 1");
        return servantMessageQueryWrapper;
    }
}
