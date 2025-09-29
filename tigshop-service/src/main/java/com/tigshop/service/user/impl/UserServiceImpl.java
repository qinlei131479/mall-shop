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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.login.*;
import com.tigshop.bean.dto.product.ProductListResDTO;
import com.tigshop.bean.dto.user.*;
import com.tigshop.bean.enums.order.CommentStatus;
import com.tigshop.bean.enums.order.OrderStatusEnum;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.enums.user.AuthorizeTypeEnum;
import com.tigshop.bean.enums.user.ChangTypeEnum;
import com.tigshop.bean.enums.user.FromTag;
import com.tigshop.bean.enums.user.RankGrowthLogTypeEnum;
import com.tigshop.bean.feign.wechat.Code2SessionResult;
import com.tigshop.bean.feign.wechat.GetTokenResult;
import com.tigshop.bean.model.finance.UserBalanceLog;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanCustomer;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.user.*;
import com.tigshop.bean.param.login.BindWechatParam;
import com.tigshop.bean.param.login.CheckEmailParam;
import com.tigshop.bean.param.login.RegisterParam;
import com.tigshop.bean.param.salesman.SalesmanSaveParam;
import com.tigshop.bean.param.user.*;
import com.tigshop.bean.query.user.UserFundDetailPageQuery;
import com.tigshop.bean.vo.config.GalleryPicUploadVO;
import com.tigshop.bean.vo.finance.UserBalanceLogVO;
import com.tigshop.bean.vo.login.LoginWechatEventVO;
import com.tigshop.bean.vo.salesman.SalesmanConfigVO;
import com.tigshop.bean.vo.user.*;
import com.tigshop.common.config.RabbitMQConfig;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.mapper.finance.UserBalanceLogMapper;
import com.tigshop.mapper.order.OrderMapper;
import com.tigshop.mapper.salesman.SalesmanCustomerMapper;
import com.tigshop.mapper.salesman.SalesmanMapper;
import com.tigshop.mapper.user.UserCouponMapper;
import com.tigshop.mapper.user.UserGrowthPointsLogMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.mapper.user.UserPointsLogMapper;
import com.tigshop.security.common.utils.JwtUtil;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.UserBalanceLogService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.salesman.SalesmanConfigService;
import com.tigshop.service.salesman.SalesmanService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.GalleryPicService;
import com.tigshop.service.shop.AdminUserShopService;
import com.tigshop.service.shop.CollectShopService;
import com.tigshop.service.user.*;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.tigshop.common.constant.Constants.ADMIN_TOKEN;
import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;
import static com.tigshop.common.constant.user.UserConstants.*;
import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;
    private final TigshopProperties tigshopProperties;
    private final RedisCache redisCache;
    private final RabbitTemplate rabbitTemplate;

    private final UserRankService userRankService;
    private final UserAddressService userAddressService;
    private final ConfigService configService;
    private final UserPointsLogMapper userPointsLogMapper;
    private final UserBalanceLogMapper userBalanceLogMapper;
    private final UserRankConfigService userRankConfigService;
    private final UserRankLogService userRankLogService;
    private final RankGrowthLogService rankGrowthLogService;
    private final UserCouponMapper userCouponMapper;
    private final UserAuthorizeService userAuthorizeService;
    private final SalesmanMapper salesmanMapper;
    private final SalesmanCustomerMapper salesmanCustomerMapper;
    private final AdminUserShopService adminUserShopService;
    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final ProductService productService;
    private final GalleryPicService galleryPicService;
    private final CollectShopService collectShopService;
    private final UserBalanceLogService userBalanceLogService;
    private final UserGrowthPointsLogMapper userGrowthPointsLogMapper;
    private final WechatApiClient wechatApiClient;
    private final TranslatePackageImpl translatePackage;
    private final WxMpService wxMpService;
    private final WxMpMessageRouter wxMpMessageRouter;
    private final SalesmanService salesmanService;
    private final SalesmanConfigService salesmanConfigService;

    @Override
    public Page<UserDTO> list(UserListDTO dto) {
        Page<User> page = buildSortOrder(dto);

        Page<User> userPage = this.lambdaQuery()
                .and(StrUtil.isNotBlank(dto.getKeyword()),
                        wrapper -> wrapper.like(User::getUsername, dto.getKeyword())
                                .or()
                                .like(User::getEmail, dto.getKeyword())
                                .or()
                                .like(User::getMobile, dto.getKeyword())
                )
                .eq(dto.getFromTag() != null && dto.getFromTag() > 0, User::getFromTag, dto.getFromTag())
                .eq(dto.getRankId() != null && dto.getRankId() > 0, User::getRankId, dto.getRankId())
                .gt(dto.getPointsGt() != null && dto.getPointsGt().compareTo(BigDecimal.ZERO) > 0, User::getPoints, dto.getPointsGt())
                .lt(dto.getPointsLt() != null && dto.getPointsLt().compareTo(BigDecimal.ZERO) > 0, User::getPoints, dto.getPointsLt())
                .eq(dto.getBalance() != null, User::getBalance, dto.getBalance())
                .page(page);
        List<User> users = userPage.getRecords();
        List<UserDTO> userdtoList = users.stream().map(this::convertToDTO).toList();
        return PageUtil.convertPage(userPage, userdtoList);
    }

    public UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        //如果用户头像是空的就用默认头像
        if (StringUtils.isEmpty(user.getAvatar())) {
            //从config里获取默认头像
            userDTO.setAvatar(getUserAvatar(user.getAvatar()));
        }
        userDTO.setRegTime(TigUtils.handelTime(user.getRegTime()));
        userDTO.setFromTagName(FromTag.getFromTagName(user.getFromTag()));
        UserRank userRank = userRankService.getById(user.getRankId());
        if (userRank != null) {
            userDTO.setRankIco(userRank.getRankIco());
            userDTO.setRankName(userRank.getRankName());
        }
        userDTO.setPassword("");

        UserAddress userAddress = userAddressService.lambdaQuery()
                .eq(UserAddress::getUserId, user.getUserId())
                .orderByDesc(UserAddress::getIsDefault)
                .last("limit 1")
                .one();
        if (userAddress != null) {
            UserAddressVO detail = userAddressService.detail(userAddress.getAddressId());
            userDTO.setUserAddress(detail);
        }

        List<Order> orderList = orderService.lambdaQuery()
                .eq(Order::getUserId, user.getUserId())
                .eq(Order::getPayStatus, 2)
                .list();
        if (CollUtil.isNotEmpty(orderList)) {
            userDTO.setOrderCount(orderList.size());
            String orderAmount = orderList.stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .toString();
            userDTO.setOrderAmount(orderAmount);
        }

        return userDTO;
    }

    @Override
    public UserDTO detail(Integer id) {
        if (id == null) {
            throw new GlobalException(translatePackage.translate(SERVICE_ERROR));
        }
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUserId, id));
        return this.convertToDTO(user);
    }

    @Override
    public UserItemVO getUserAndUserRank(Integer id) {
        UserDTO detail = detail(id);
        return new UserItemVO(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batch(BatchDTO batchDTO) {
        UserBatchParam param = (UserBatchParam) batchDTO;
        if ("setRank".equals(param.getType())) {
            List<Integer> ids = param.getIds();
            return this.lambdaUpdate()
                    .set(User::getRankId, param.getRankId())
                    .in(User::getUserId, ids)
                    .update();
        }
        if ("del".equals(batchDTO.getType())) {
            return this.removeByIds(batchDTO.getIds());
        }

        return false;
    }

    @Override
    public boolean create(UserDTO userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setRegTime(StringUtils.getCurrentTime());
        //检查用户名和手机号是否重复
        boolean hasSameUserName = this.checkUserInfo("username", userDTO.getUsername());
        if (hasSameUserName) {
            throw new GlobalException("该用户名已经被使用");
        }

        boolean hasSameMobile = this.checkUserInfo("mobile", userDTO.getMobile());
        if (hasSameMobile) {
            throw new GlobalException("该手机号已被注册");
        }
        // 检查密码是否为空，如果不为空则进行加密
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return this.save(user);
    }

    public boolean checkUserInfo(String type, String value) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if ("username".equals(type)) {
            queryWrapper.eq(User::getUsername, value);
            return this.count(queryWrapper) > 0L;
        }
        if ("mobile".equals(type)) {
            queryWrapper.eq(User::getMobile, value);
            return this.count(queryWrapper) > 0L;
        }
        return false;
    }

    @Override
    public boolean update(UserParam userDTO) {
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        if (userDTO.getPwdConfirm() != null && !Objects.equals(userDTO.getPassword(), userDTO.getPwdConfirm())) {
            throw new GlobalException("两次输入的密码不一致");

        }
        // 检查密码是否为空，如果不为空则进行加密
        String password = user.getPassword().replace(" ", "");
        if (StrUtil.isNotBlank(password)) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            user.setPassword(null);
        }
        if (ObjectUtil.isNotEmpty(userDTO.getEmail())) {
            Long emailCount = this.lambdaQuery().eq(User::getEmail, user.getEmail())
                    .ne(User::getUserId, user.getUserId()).count();
            Assert.isTrue(emailCount == 0, () -> new GlobalException("邮箱已注册"));
        }
        if (ObjectUtil.isNotEmpty(userDTO.getMobile())) {
            Long mobileCount = this.lambdaQuery().eq(User::getMobile, user.getMobile())
                    .ne(User::getUserId, user.getUserId()).count();
            Assert.isTrue(mobileCount == 0, () -> new GlobalException("手机号已注册"));
        }
        return this.updateById(user);
    }

    /**
     * 获取所有用户的余额总和
     *
     * @return BigDecimal
     */
    @Override
    public BigDecimal getUserBalanceTotal() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(User::getBalance);
        List<User> userList = this.list(queryWrapper);
        return userList.stream().map(User::getBalance).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    @Override
    public List<User> getUserByUsernameLike(String keyword) {
        if (StrUtil.isEmpty(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getUsername, keyword);
        return this.list(queryWrapper);
    }

    @Override
    public List<User> getUserByUserInfoLike(String keyword) {
        if (StrUtil.isEmpty(keyword)) {
            return List.of();
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getUsername, keyword)
                .or().like(User::getEmail, keyword)
                .or().like(User::getMobile, keyword);
        return this.list(queryWrapper);
    }

    @Override
    public void wechatServer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WxMpConfigStorage wxMpConfigStorage = wxMpService.getWxMpConfigStorage();


        String signature = request.getParameter("signature");
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            // 消息签名不正确，说明不是公众平台发过来的消息
            response.getWriter().println("非法请求");
            return;
        }

        String echostr = request.getParameter("echostr");
        if (StringUtils.isNotBlank(echostr)) {
            // 说明是一个仅仅用来验证的请求，回显echostr
            response.getWriter().println(echostr);
            return;
        }

        String encryptType = StringUtils.isBlank(request.getParameter("encrypt_type")) ?
                "raw" :
                request.getParameter("encrypt_type");

        WxMpXmlMessage inMessage;

        if ("raw".equals(encryptType)) {
            // 明文传输的消息
            inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
        } else if ("aes".equals(encryptType)) {
            // 是aes加密的消息
            String msgSignature = request.getParameter("msg_signature");
            inMessage = WxMpXmlMessage.fromEncryptedXml(request.getInputStream(), wxMpConfigStorage, timestamp, nonce, msgSignature);
        } else {
            response.getWriter().println("不可识别的加密类型");
            return;
        }
        if (inMessage != null && StrUtil.isNotBlank(inMessage.getEvent())) {
            String openid = inMessage.getFromUser();
            String ticket = inMessage.getTicket();
            String event = inMessage.getEvent();
            if (Arrays.asList("subscribe", "SCAN").contains(event)) {
                if (!ticket.isEmpty() && !openid.isEmpty()) {
                    redisTemplate.opsForValue().set(ticket, openid, 30, TimeUnit.MINUTES);
                }
            }
        }

        WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
        if (outMessage != null) {
            if ("raw".equals(encryptType)) {
                response.getWriter().write(outMessage.toXml());
            } else if ("aes".equals(encryptType)) {
                response.getWriter().write(outMessage.toEncryptedXml(wxMpConfigStorage));
            }
        }
    }

    @Override
    public User getUserByUsername(String username) {
        if (StrUtil.isEmpty(username)) {
            return null;
        }
        List<User> list = this.list(new LambdaQueryWrapper<User>().eq(User::getUsername, username).or().eq(User::getMobile, username));
        if (ObjectUtil.isNotEmpty(list)) {
            return list.getFirst();
        }
        return null;
    }

    @Override
    public User getUserByMobile(String mobile) {
        if (StrUtil.isEmpty(mobile)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile));
    }

    @Override
    public void checkUserCompanyAuth() {
        String isIdentity = configService.getConfigVal(SettingsEnum.IS_IDENTITY);

        if ("1".equals(isIdentity) && tigshopProperties.getIsB2b() == 1) {
            User user = getById(SecurityUtils.getCurrentUserId());
            if (user != null && user.getIsCompanyAuth() == 0) {
                throw new GlobalException("请先完成实名认证");
            }
        }

    }

    @Override
    public Integer getUserReferrerId(Integer currentUserId) {
        if (currentUserId != null) {
            User user = getById(currentUserId);
            if (user != null) {
                return user.getReferrerUserId();
            }
        }
        return 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decPoints(Integer usePoint, Integer currentUserId, String desc) {
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("decPointsUserId::" + currentUserId, uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            try {

                UserPointsLog userPointsLog = new UserPointsLog();
                userPointsLog.setUserId(currentUserId);
                userPointsLog.setPoints(usePoint);
                userPointsLog.setChangeTime(StringUtils.getCurrentTime());
                userPointsLog.setChangeDesc(desc);
                userPointsLog.setChangeType(ChangTypeEnum.DEC_POINTS.getValue());
                LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(User::getUserId, currentUserId).setDecrBy(User::getPoints, usePoint);

                String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
                if (this.update(updateWrapper)) {
                    if (userPointsLogMapper.insert(userPointsLog) < 1) {
                        throw new GlobalException(translatePackage.translate(configVal + "日志操作失败"));
                    }
                } else {
                    throw new GlobalException(translatePackage.translate(configVal + "不足"));
                }
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("decPointsUserId::" + currentUserId, uuid);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incPoints(Integer point, Integer currentUserId, String desc) {
        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("incPointsUserId::" + currentUserId, uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            try {
                UserPointsLog userPointsLog = new UserPointsLog();
                userPointsLog.setUserId(currentUserId);
                userPointsLog.setPoints(point);
                userPointsLog.setChangeTime(StringUtils.getCurrentTime());
                userPointsLog.setChangeDesc(desc);
                userPointsLog.setChangeType(ChangTypeEnum.INC_POINTS.getValue());
                this.lambdaUpdate()
                        .eq(User::getUserId, currentUserId)
                        .setIncrBy(User::getPoints, point)
                        .update();
                userPointsLogMapper.insert(userPointsLog);
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("incPointsUserId::" + currentUserId, uuid);
            }
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void decBalance(BigDecimal useBalance, Integer currentUserId, String desc) {
        User user = this.getById(currentUserId);

        String uuid = UUID.randomUUID().toString();
        boolean lock = redisCache.tryLock("decBalanceUserId::" + currentUserId, uuid, 30, TimeUnit.SECONDS);
        if (lock) {
            try {
                UserBalanceLog userBalanceLog = new UserBalanceLog();
                userBalanceLog.setUserId(currentUserId);
                userBalanceLog.setBeforeBalance(user.getBalance());
                userBalanceLog.setBalance(useBalance);
                userBalanceLog.setAfterBalance(user.getBalance().subtract(useBalance));
                userBalanceLog.setBeforeFrozenBalance(user.getFrozenBalance());
                userBalanceLog.setAfterFrozenBalance(user.getFrozenBalance());
                userBalanceLog.setChangeTime(StringUtils.getCurrentTime());
                userBalanceLog.setChangeDesc(desc);
                userBalanceLog.setChangeType(ChangTypeEnum.DEC_POINTS.getValue());
                LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(User::getUserId, currentUserId).setDecrBy(User::getBalance, useBalance);

                String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
                if (this.update(updateWrapper)) {
                    if (userBalanceLogMapper.insert(userBalanceLog) < 1) {
                        throw new GlobalException(translatePackage.translate("余额日志操作失败"));
                    }
                } else {
                    throw new GlobalException(translatePackage.translate(configVal + "不足"));
                }
            } catch (Exception e) {
                throw new GlobalException(e.getMessage());
            } finally {
                redisCache.unlock("decBalanceUserId::" + currentUserId, uuid);
            }
        }


    }

    @Override
    public UserClientDetailVO clientDetail() {
        Integer userId = getCurrentUserId();
        UserClientDetailVO userClientDetailVO = this.getBaseInfo(userId);
        //查询用户是否授权
        userClientDetailVO.setIsBindWechat(userAuthorizeService.checkUserIsAuthorize(userId, 1));
        Salesman salesman = getSalesmanByUserId(userId);
        UserClientDetailVO.Salesman salesmanVO = new UserClientDetailVO.Salesman();
        if (salesman != null) {
            BeanUtils.copyProperties(salesman, salesmanVO);
            salesmanVO.setAddTime(TigUtils.handelTime(salesman.getAddTime()));
            userClientDetailVO.setSalesman(salesmanVO);
        }
        //查询是否开启签到
        ConfigPO useQiandaoPointConfig = configService.getConfigByCode(SettingsEnum.USE_QIANDAO_POINT.getBizCode());
        userClientDetailVO.setShowSign(Integer.parseInt(useQiandaoPointConfig.getBizVal()));
        userClientDetailVO.setHasShop(adminUserShopService.hasShop(userId));
        String avatar = userClientDetailVO.getAvatar();
        if (StrUtil.isEmpty(avatar)) {
            userClientDetailVO.setAvatar("https://oss.tigshop.com/img/gallery/202403/17111886010Pg60qIrJEzwe3tw06.gif");
        }
        return userClientDetailVO;
    }

    /**
     * 获取用户基本信息
     *
     * @param userId 用户id
     * @return UserClientDetailVO
     */
    public UserClientDetailVO getBaseInfo(Integer userId) {
        UserClientDetailVO userClientDetailVO = new UserClientDetailVO();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(USER_NOT_EXIST);
        }
        UserRankConfig userRankConfig = this.getUserRankConfig();
        BeanUtils.copyProperties(user, userClientDetailVO);
        UserRank userRank = userRankService.getById(user.getRankId());
        if (userRank != null) {
            BeanUtils.copyProperties(userRank, userClientDetailVO);
            userClientDetailVO.setRights(JsonUtil.fromJson(userRank.getRights(), JSONArray.class));
        }
        Map<String, Integer> map = this.refreshRank(userId, userRankConfig);
        userClientDetailVO.setTotalBalance(user.getBalance().add(user.getFrozenBalance()));
        userClientDetailVO.setAvatar(this.getUserAvatar(user.getAvatar()));
        userClientDetailVO.setCoupon(userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId).eq(UserCoupon::getOrderId, 0)
                .ge(UserCoupon::getEndDate, StringUtils.getCurrentTime())
                .le(UserCoupon::getStartDate, StringUtils.getCurrentTime())
        ));
        //获取会员等级有效期
        //从map中获取 rank_expire_time
        userClientDetailVO.setRankExpireTime(map.get("rank_expire_time").toString());
        userClientDetailVO.setGrowth(map.get("growth"));
        UserClientDetailVO.RankConfig rankConfig = new UserClientDetailVO.RankConfig();
        BeanUtils.copyProperties(userRankConfig, rankConfig);
        rankConfig.setData(JsonUtil.fromJson(userRankConfig.getData(), JSONObject.class));
        userClientDetailVO.setRankConfig(rankConfig);
        userClientDetailVO.setDimUsername(getDimUsername(user.getUsername()));
        userClientDetailVO.setGrowthPoints(user.getGrowthPoints());
        return userClientDetailVO;
    }

    /**
     * 获取脱敏用户名
     *
     * @param username 用户名
     * @return String
     */
    public String getDimUsername(String username) {
        if (username == null) {
            return "";
        }
        return username.charAt(0) + "***" + username.substring(username.length() - 1);
    }

    /**
     * 获取会员头像
     *
     * @param avatar String
     * @return String
     */
    public String getUserAvatar(String avatar) {
        if (avatar == null || StringUtils.isEmpty(avatar)) {
            //从配置获取默认头像
            avatar = configService.getConfigByCode(SettingsEnum.DEFAULT_AVATAR.getBizCode()).getBizVal();
        }
        return avatar;
    }

    @Transactional
    @Override
    public boolean updateInformation(UpdateInformationDTO updateInformationDTO) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(USER_NOT_EXIST);
        }
        this.getGrowthPoints(user, 1);
        user.setBirthday(updateInformationDTO.getBirthday());
        user.setNickname(updateInformationDTO.getNickname());
        user.setEmail(updateInformationDTO.getEmail());
        user.setMobile(updateInformationDTO.getMobile());
        user.setWechatImg(updateInformationDTO.getWechatImg());
        boolean updateFlag = this.updateById(user);

        userRankService.addUserGrowthPoints(user.getUserId(), RankGrowthLogTypeEnum.GROWTH_TYPE_INFORMATION);

        return updateFlag;
    }

    @Override
    public MemberCenterVO memberCenter() {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        Assert.notNull(user, () -> new GlobalException(USER_NOT_EXIST));

        MemberCenterVO memberCenterVO = new MemberCenterVO();
        BeanUtils.copyProperties(user, memberCenterVO);
        UserRank userRank = userRankService.getById(user.getRankId());
        if (userRank != null) {
            BeanUtils.copyProperties(userRank, memberCenterVO);
            memberCenterVO.setRights(JsonUtil.fromJson(userRank.getRights(), JSONArray.class));
        }
        memberCenterVO.setDimUsername(getDimUsername(user.getUsername()));
        if (user.getMobileValidated() > 0 && user.getEmailValidated() > 0) {
            memberCenterVO.setSecurityLv(3);
        } else if (user.getMobileValidated() > 0 && user.getEmailValidated() == 0 ||
                user.getMobileValidated() == 0 && user.getEmailValidated() > 0) {
            memberCenterVO.setSecurityLv(2);
        } else {
            memberCenterVO.setSecurityLv(1);
        }
        memberCenterVO.setAwaitPay(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).eq(Order::getOrderStatus, OrderStatusEnum.PENDING.getCode())));
        memberCenterVO.setAwaitShipping(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).eq(Order::getOrderStatus, OrderStatusEnum.CONFIRMED.getCode())));

        Long awaitReceived = orderMapper.selectCount(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getOrderStatus, OrderStatusEnum.PROCESSING.getCode())
        );
        memberCenterVO.setAwaitReceived(awaitReceived);

        Long awaitComment = orderMapper.selectCount(
                Wrappers.lambdaQuery(Order.class)
                        .eq(Order::getUserId, userId)
                        .eq(Order::getCommentStatus, CommentStatus.PENDING.getCode())
                        .eq(Order::getOrderStatus, OrderStatusEnum.COMPLETED.getCode())
        );
        memberCenterVO.setAwaitComment(awaitComment);

        memberCenterVO.setAwaitCoupon(userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId)));
        String avatar = memberCenterVO.getAvatar();
        if (StrUtil.isEmpty(avatar)) {
            memberCenterVO.setAvatar("https://oss.tigshop.com/img/gallery/202403/17111886010Pg60qIrJEzwe3tw06.gif");
        }
        return memberCenterVO;
    }

    @Override
    public void sendMobileCodeByModifyPassword(RegisterSmsDTO dto) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }
        configService.sendSms(dto);
    }

    @Override
    public boolean checkModifyPasswordMobileCode(CheckModifyPasswordDTO checkModifyPasswordDTO) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }
        // 校验验证码
//        String code = (String) redisTemplate.opsForValue().get("modify_password" + user.getMobile());
        Object object = redisTemplate.opsForValue().get("modify_password" + user.getMobile());
        String code = object == null ? null : object.toString();
        if (code == null || !code.equals(checkModifyPasswordDTO.getCode())) {
            throw new GlobalException(translatePackage.translate("短信验证码错误或已过期，请重试"));
        }
        if (checkModifyPasswordDTO.getPassword() == null) {
            throw new GlobalException(translatePackage.translate(NEW_PASSWORD_NOT_EMPTY));
        }

        if (StringUtils.isEmpty(user.getMobile())) {
            throw new GlobalException(translatePackage.translate(MOBILE_NOT_EMPTY));
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getMobile, user.getMobile());
        queryWrapper.ne(User::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException(translatePackage.translate(MOBILE_ALREADY_BIND));
        }
        user.setPassword(passwordEncoder.encode(checkModifyPasswordDTO.getPassword()));
        return this.updateById(user);
    }

    @Transactional
    @Override
    public void modifyMobile(ModifyMobileParam param) {
        Integer modifyMobileCode = (Integer) redisTemplate.opsForValue().get("modify_mobile" + param.getMobile());
        Assert.isTrue(modifyMobileCode != null && param.getCode().equals(modifyMobileCode.toString()), () -> new GlobalException("验证码错误或已过期，请重试"));

        User user = this.getById(SecurityUtils.getCurrentUserId());
        Assert.notNull(user, () -> new GlobalException(translatePackage.translate(USER_NOT_EXIST)));

        // 校验手机号是否存在
        if (ObjectUtil.notEqual(user.getMobile(), param.getMobile())) {
            Long mobileCount = this.lambdaQuery().eq(User::getMobile, param.getMobile()).count();
            Assert.isTrue(mobileCount == 0, () -> new GlobalException("手机号已经被绑定"));

            // 首次绑定，判断是否增加会员成长值
            if (StrUtil.isBlank(user.getMobile())) {
                userRankService.addUserGrowthPoints(user.getUserId(), RankGrowthLogTypeEnum.GROWTH_TYPE_BIND_PHONE);
            }

            user.setMobileValidated(1);
            user.setMobile(param.getMobile());
            this.updateById(user);
        }

    }

    @Override
    public void modifyEmail(ModifyEmailParam param) {
        User user = this.getById(SecurityUtils.getCurrentUserId());
        Assert.notNull(user, () -> new GlobalException(translatePackage.translate(USER_NOT_EXIST)));

        //注册用户
        User emailUser = this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, param.getEmail()));
        if (emailUser != null) {
            throw new GlobalException("邮箱已经被绑定");
        }

        Integer modifyEmailCode = (Integer) redisTemplate.opsForValue().get("modify_email" + param.getEmail());
        Assert.isTrue(modifyEmailCode != null && param.getCode().equals(modifyEmailCode.toString()), () -> new GlobalException("验证码错误或已过期，请重试"));
        user.setEmailValidated(1);
        user.setEmail(param.getEmail());
        this.updateById(user);
    }

    @Override
    public List<ProductListResDTO> historyProduct() {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        Assert.notNull(user, () -> new GlobalException(translatePackage.translate(USER_NOT_EXIST)));

        if (StrUtil.isBlank(user.getHistoryProductIds()) || "[ ]".equals(user.getHistoryProductIds())) {
            return Collections.emptyList();
        }

        List<Integer> historyProductIds = StringUtils.str2IntList(user.getHistoryProductIds());
        return productService.historyProduct(historyProductIds);
    }

    @Override
    public boolean mobileValidate(CheckModifyPasswordDTO checkModifyPasswordDTO) {
        String event = "mobile_validate";
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }
        if (StringUtils.isEmpty(checkModifyPasswordDTO.getMobile())) {
            throw new GlobalException(translatePackage.translate(MOBILE_NOT_EMPTY));
        }
        Object code = redisTemplate.opsForValue().get(event + user.getMobile());
        String mobileCode = code == null ? "" : code.toString();

        if (StrUtil.isEmpty(mobileCode)) {
            throw new GlobalException(translatePackage.translate("验证码为空"));
        }
        if (!mobileCode.equals(checkModifyPasswordDTO.getCode())) {
            throw new GlobalException(translatePackage.translate("短信验证码错误或已过期，请重试"));
        }
        //查看是否已经绑定了手机号
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getMobile, checkModifyPasswordDTO.getMobile());
        queryWrapper.ne(User::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException(translatePackage.translate(MOBILE_ALREADY_BIND));
        }
        return true;
    }

    @Override
    public boolean emailValidateNew(CheckModifyEmailDTO checkModifyEmailDTO) {
        String event = "email_validate";
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }
        if (StringUtils.isEmpty(checkModifyEmailDTO.getEmail())) {
            throw new GlobalException(translatePackage.translate(EMAIL_NOT_EMPTY));
        }
        Object code = redisTemplate.opsForValue().get(event + user.getEmail());
        String emailCode = code == null ? "" : code.toString();

        if (StrUtil.isEmpty(emailCode)) {
            throw new GlobalException(translatePackage.translate("验证码为空"));
        }
        if (!emailCode.equals(checkModifyEmailDTO.getCode())) {
            throw new GlobalException(translatePackage.translate("邮箱验证码错误或已过期，请重试"));
        }
        //查看是否已经绑定了手机号
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, checkModifyEmailDTO.getEmail());
        queryWrapper.ne(User::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException(translatePackage.translate(EMAIL_ALREADY_BIND));
        }
        return true;
    }

    @Override
    public boolean emailValidate(CheckEmailModifyDTO checkEmailModifyDTO) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }
        if (StringUtils.isEmpty(checkEmailModifyDTO.getEmail())) {
            throw new GlobalException(translatePackage.translate(EMAIL_NOT_EMPTY));
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, checkEmailModifyDTO.getEmail());
        queryWrapper.ne(User::getUserId, userId);
        if (this.count(queryWrapper) > 0) {
            throw new GlobalException(translatePackage.translate(EMAIL_ALREADY_BIND));
        }

        if (checkEmailModifyDTO.getType() != null && checkEmailModifyDTO.getType() > 0) {
            user.setEmail(checkEmailModifyDTO.getEmail());
            user.setEmailValidated(1);
            return this.updateById(user);
        }
        return true;
    }

    @Override
    public boolean delHistoryProduct(DelHistoryProductDTO dto) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);

        List<Integer> userHistoryProductIds = StringUtils.str2IntList(user.getHistoryProductIds());

        if (CollUtil.isNotEmpty(dto.getIds())) {
            userHistoryProductIds = userHistoryProductIds.stream().filter(id -> !dto.getIds().contains(id)).toList();
        }

        user.setHistoryProductIds(JsonUtil.toJson(userHistoryProductIds));
        this.updateById(user);
        return true;
    }

    @Override
    public GalleryPicUploadVO uploadImg(MultipartFile file) {
        return galleryPicService.uploadImg(0, file);
    }

    @Override
    public boolean modifyAvatar(MultipartFile file) {
        Integer userId = getCurrentUserId();
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }
        GalleryPicUploadVO galleryPicUploadVO = galleryPicService.uploadImg(0, file);
        user.setAvatar(galleryPicUploadVO.getPicThumb());
        return this.updateById(user);
    }

    @Override
    public Page<CollectShopVO> myCollectShop(CollectShopListDTO collectShopListDTO) {
        return collectShopService.list(collectShopListDTO);
    }

    /**
     * 获取成长值
     *
     * @param user User
     */
    public void getGrowthPoints(User user, Integer type) {
        UserRankConfig userRankConfig = userRankConfigService.getUserRankConfig("grow_config");
        if (userRankConfig != null) {
            Integer growthPoints = 0;
            UserGrowConfig userGrowConfig = JsonUtil.fromJson(userRankConfig.getData(), UserGrowConfig.class);

            switch (type) {
                case 1 -> {
                    if (userGrowConfig.getEvpi() != null) {
                        //判断字符串未空
                        if (StringUtils.isEmpty(user.getNickname()) && StringUtils.isEmpty(user.getBirthday())) {
                            growthPoints = userGrowConfig.getEvpiGrowth();
                        }
                    }
                }
                case 2 -> {
                    //第一次绑定手机号
                    if (userGrowConfig.getBindPhone() != null) {
                        if (StringUtils.isEmpty(user.getMobile())) {
                            growthPoints = userGrowConfig.getBindPhoneGrowth();
                        }
                    }
                }
            }
            if (growthPoints != 0) {
                RankGrowthLog userGrowthLog = new RankGrowthLog();
                userGrowthLog.setUserId(user.getUserId());
                if (type == 1) {
                    userGrowthLog.setType(RankGrowthLogTypeEnum.GROWTH_TYPE_INFORMATION.getCode());
                }
                if (type == 2) {
                    userGrowthLog.setType(RankGrowthLogTypeEnum.GROWTH_TYPE_BIND_PHONE.getCode());
                }
                userGrowthLog.setGrowthPoints(growthPoints);
                userGrowthLog.setChangeType(ChangTypeEnum.INC_POINTS.getValue());
                userGrowthLog.setCreateTime(StringUtils.getCurrentTime());
                rankGrowthLogService.save(userGrowthLog);
            }
        }

    }

    /**
     * 处理用户会员等级
     *
     * @param userId 用户ID
     */
    public Map<String, Integer> refreshRank(Integer userId, UserRankConfig userRankConfig) {

        Map<String, Integer> map = new HashMap<>();
        Integer growth = 0;
        long rankExpireTime = 0L;
        if (userRankConfig != null) {
            UserRankConfigDTO userRankConfigDTO = JsonUtil.fromJson(userRankConfig.getData(), UserRankConfigDTO.class);
            growth = this.getExpireRangeGrowth(userRankConfigDTO, userId);
            User user = this.getById(userId);
            UserRank userRank = userRankService.getById(user.getRankId());
            if (userRank == null) {
                this.handleUserRankLog(userId, user);
            }
            LambdaQueryWrapper<UserRankLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserRankLog::getUserId, userId);
            queryWrapper.orderByDesc(UserRankLog::getId);
            queryWrapper.last("limit 1");
            UserRankLog userRankLog = userRankLogService.getOne(queryWrapper);
            if (userRankLog != null && userRankConfigDTO.getType() == 2) {
                //有时效
                rankExpireTime = userRankLog.getChangeTime() + userRankConfigDTO.getRankAfterMonth() * 30 * 24 * 60 * 60;
                //等级过期后重新定义等级
                if (StringUtils.getCurrentTime() > rankExpireTime) {
                    this.getGrowthByRule(userId);
                    // 重新定义有效期
                    LambdaQueryWrapper<UserRankLog> queryWrapperLog = new LambdaQueryWrapper<>();
                    queryWrapperLog.eq(UserRankLog::getUserId, userId);
                    queryWrapperLog.orderByDesc(UserRankLog::getId);
                    queryWrapperLog.last("limit 1");
                    UserRankLog userRankLogNew = userRankLogService.getOne(queryWrapperLog);
                    rankExpireTime = userRankLogNew.getChangeTime() + userRankConfigDTO.getRankAfterMonth() * 30 * 24 * 60 * 60;
                }
            }
        }
        map.put("growth", growth);
        map.put("rank_expire_time", (int) rankExpireTime);
        return map;
    }

    public void getGrowthByRule(Integer userId) {
        UserRankConfig userRankConfig = this.getUserRankConfig();
        if (userRankConfig != null) {
            UserRankConfigDTO userRankConfigDTO = JsonUtil.fromJson(userRankConfig.getData(), UserRankConfigDTO.class);
            if (userRankConfigDTO != null && userRankConfigDTO.getType() == 2) {
                Integer growth = this.getExpireRangeGrowth(userRankConfigDTO, userId);
                if (userRankConfig.getRankType() == 1) {
                    this.changeUserRank(userId, growth, userRankConfig.getRankType());
                }
            }
        }
    }

    /**
     * 根据成长值重新计算等级  -- 在保级范围内等级不变
     *
     * @param userId   用户ID
     * @param growth   成长值
     * @param rankType 类型
     */
    public void changeUserRank(Integer userId, Integer growth, Integer rankType) {
        User user = this.getById(userId);
        LambdaQueryWrapper<UserRank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRank::getRankType, rankType);
        queryWrapper.le(UserRank::getMinGrowthPoints, growth);
        queryWrapper.orderByDesc(UserRank::getMinGrowthPoints);
        queryWrapper.last("limit 1");
        UserRank userRank = userRankService.getOne(queryWrapper);
        if (userRank != null && !Objects.equals(userRank.getRankId(), user.getRankId())) {
            createUserRankLog(userId, user, userRank);
        }

    }

    private void createUserRankLog(Integer userId, User user, UserRank userRank) {
        user.setRankId(userRank.getRankId());
        boolean isUpdate = this.updateById(user);
        if (isUpdate) {
            UserRankLog userRankLog = new UserRankLog();
            userRankLog.setUserId(userId);
            userRankLog.setRankId(userRank.getRankId());
            userRankLog.setRankType(userRank.getRankType());
            userRankLog.setRankName(userRank.getRankName());
            userRankLog.setChangeTime(StringUtils.getCurrentTime());
            userRankLogService.save(userRankLog);
        }
    }

    //创建user_rank_log
    public void handleUserRankLog(Integer userId, User user) {
        LambdaQueryWrapper<UserRank> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRank::getMinGrowthPoints, 0);
        queryWrapper.orderByAsc(UserRank::getRankId);
        queryWrapper.last("limit 1");
        UserRank userRank = userRankService.getOne(queryWrapper);
        if (userRank != null) {
            createUserRankLog(userId, user, userRank);
        }
    }

    /**
     * 获取用户等级配置
     *
     * @return UserRankConfigDTO
     */
    public UserRankConfig getUserRankConfig() {
        return userRankConfigService.getUserRankConfig("rank_config");
    }

    /**
     * 获取成长值
     *
     * @param userRankConfigDTO 会员配置
     * @return Integer
     */
    public Integer getExpireRangeGrowth(UserRankConfigDTO userRankConfigDTO, Integer userId) {
        if (userRankConfigDTO != null && userRankConfigDTO.getType() == 2) {
            LambdaQueryWrapper<UserRankLog> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserRankLog::getUserId, userId);
            queryWrapper.orderByDesc(UserRankLog::getId);
            queryWrapper.last("limit 1");
            UserRankLog userRankLog = userRankLogService.getOne(queryWrapper);
            QueryWrapper<RankGrowthLog> rankGrowthLogQueryWrapper = new QueryWrapper<>();
            rankGrowthLogQueryWrapper.eq("user_id", userId);
            Long incGrowthQuery = 0L;
            Long decGrowthQuery = 0L;
            if (userRankLog != null) {
                Long rankEndTime = userRankLog.getChangeTime() + userRankConfigDTO.getRankAfterMonth() * 30 * 24 * 60 * 60;
                rankGrowthLogQueryWrapper.le("create_time", rankEndTime);
                rankGrowthLogQueryWrapper.ge("create_time", userRankLog.getChangeTime());
            } else {
                rankGrowthLogQueryWrapper.le("create_time", StringUtils.getCurrentTime());
            }
            rankGrowthLogQueryWrapper.eq("change_type", 1);
            rankGrowthLogQueryWrapper.select("sum(growth_points) as createTime");
            RankGrowthLog rankGrowthLog = rankGrowthLogService.getOne(rankGrowthLogQueryWrapper);
            if (rankGrowthLog != null) {
                incGrowthQuery = rankGrowthLog.getCreateTime();
            }

            rankGrowthLogQueryWrapper.eq("change_type", 2);
            rankGrowthLogQueryWrapper.select("sum(growth_points) as createTime");
            RankGrowthLog rankGrowthLog1 = rankGrowthLogService.getOne(rankGrowthLogQueryWrapper);
            if (rankGrowthLog1 != null) {
                decGrowthQuery = rankGrowthLog1.getCreateTime();
            }
            return Math.max(incGrowthQuery.intValue() - decGrowthQuery.intValue(), 0);
        }
        return 0;
    }

    /**
     * 根据用户ID获取销售员
     *
     * @param userId 用户ID
     * @return Salesman
     */
    public Salesman getSalesmanByUserId(Integer userId) {
        LambdaQueryWrapper<Salesman> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Salesman::getUserId, userId);
        queryWrapper.last("limit 1");
        return salesmanMapper.selectOne(queryWrapper);
    }

    @Transactional
    @Override
    public String register(RegisterParam param) {
        if ("email".equals(param.getRegistType())) {
            return registerByEmail(param);
        } else {
            return registerByMobile(param);
        }
    }

    private String registerByEmail(RegisterParam param) {
        param.emailValidate();
        String email = param.getEmail();

        String redCode = configService.getConfigVal(SettingsEnum.SHOP_REG_CLOSED);
        Assert.isFalse(StrUtil.equals(redCode, "1"), () -> new GlobalException("系统已关闭注册"));


        // 验证验证码 需要加上之前发送验证码时带的 event
        Object emailCode = redisTemplate.opsForValue().get("login" + email);
        Assert.isTrue(param.getEmailCode().equals(String.valueOf(emailCode)), () -> new GlobalException("验证码错误或已过期，请重试"));

        Long emailCount = this.lambdaQuery().eq(User::getEmail, email).count();
        Assert.isTrue(emailCount == 0, () -> new GlobalException("邮箱已注册"));

        User user = param.createEmailUser();
        user.setPassword(passwordEncoder.encode(param.getPassword()));
        this.save(user);

        redisTemplate.delete("login" + email);

        SalesmanSaveParam salesmanSaveParam = new SalesmanSaveParam();
        salesmanSaveParam.setUserId(user.getUserId());
        salesmanSaveParam.setLevel(1);
        if (param.getSalesmanId() != null) {
            Salesman salesman = salesmanMapper.selectById(param.getSalesmanId());
            if (salesman != null) {
                SalesmanCustomer entity = new SalesmanCustomer();
                entity.setSalesmanId(param.getSalesmanId());
                entity.setUserId(user.getUserId());
                entity.setAddTime(System.currentTimeMillis() / 1000);
                salesmanCustomerMapper.insert(entity);
                // 发送拉人消息，用于分销员等级升级
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.DIRECT_EXCHANGE,
                        RabbitMQConfig.SALESMAN_UPDATE_ROUTING_KEY,
                        param.getSalesmanId());
                salesmanSaveParam.setPid(salesman.getUserId());
            }
        }
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        if (salesmanConfig.getRegisterToSalesman() != null && salesmanConfig.getRegisterToSalesman() == 1) {
            salesmanService.create(salesmanSaveParam);
        }
        return jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
    }

    @Nullable
    private String registerByMobile(RegisterParam param) {
        param.mobileValidate();
        String mobile = param.getMobile();

        String redCode = configService.getConfigVal(SettingsEnum.SHOP_REG_CLOSED);
        Assert.isFalse(StrUtil.equals(redCode, "1"), () -> new GlobalException("系统已关闭注册"));

        // 验证验证码 需要加上之前发送验证码时带的 event
        Object mobileCode = redisTemplate.opsForValue().get("login" + mobile);
        Assert.isTrue(param.getMobileCode().equals(String.valueOf(mobileCode)), () -> new GlobalException("短信验证码错误或已过期，请重试"));

        Long mobileCount = this.lambdaQuery().eq(User::getMobile, mobile).count();
        Assert.isTrue(mobileCount == 0, () -> new GlobalException("手机号已注册"));

        User user = param.createMobileUser();
        user.setPassword(passwordEncoder.encode(param.getPassword()));
        this.save(user);

        redisTemplate.delete("login" + mobile);

        SalesmanSaveParam salesmanSaveParam = new SalesmanSaveParam();
        salesmanSaveParam.setUserId(user.getUserId());
        salesmanSaveParam.setLevel(1);
        if (param.getSalesmanId() != null) {
            Salesman salesman = salesmanMapper.selectById(param.getSalesmanId());
            if (salesman != null) {
                SalesmanCustomer entity = new SalesmanCustomer();
                entity.setSalesmanId(param.getSalesmanId());
                entity.setUserId(user.getUserId());
                entity.setAddTime(System.currentTimeMillis() / 1000);
                salesmanCustomerMapper.insert(entity);
                // 发送拉人消息，用于分销员等级升级
                rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.SALESMAN_UPDATE_ROUTING_KEY, param.getSalesmanId());
                salesmanSaveParam.setPid(salesman.getUserId());
            }
        }
        SalesmanConfigVO salesmanConfig = salesmanConfigService.detail("salesmanConfig");
        if (salesmanConfig.getRegisterToSalesman() != null && salesmanConfig.getRegisterToSalesman() == 1) {
            salesmanService.create(salesmanSaveParam);
        }
        return jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
    }

    @Override
    public void loginByPhoneAndRegister(String mobile) {
        User user = User.builder()
                .username(mobile)
                .nickname(StrUtil.format("user_{}", mobile))
                .mobile(mobile)
                .password(passwordEncoder.encode(mobile))
                .regTime(StringUtils.getCurrentTime())
                .mobileValidated(Constants.YES)
                .build();
        this.save(user);
    }

    @Override
    public String checkMobile(CheckMobileDTO dto) {
        String mobile = dto.getMobile();
        String forgetPasswordCode = String.valueOf(redisTemplate.opsForValue().get("forgetPassword" + mobile));
        if (!dto.getCode().equals(forgetPasswordCode)) {
            throw new GlobalException("验证码错误");
        }
        String md5Mobile = SecureUtil.md5(mobile);
        redisTemplate.opsForValue().set(md5Mobile, mobile, 10, TimeUnit.MINUTES);
        return md5Mobile;
    }

    @Override
    public String checkEmail(CheckEmailParam param) {
        String email = param.getEmail();
        String forgetPasswordCode = String.valueOf(redisTemplate.opsForValue().get("forgetPassword" + email));
        if (!param.getCode().equals(forgetPasswordCode)) {
            throw new GlobalException("验证码错误");
        }
        String md5Email = SecureUtil.md5(email);
        redisTemplate.opsForValue().set(md5Email, email, 10, TimeUnit.MINUTES);
        return md5Email;
    }

    @Override
    public boolean modifyPassword(ForgetPasswordDTO dto) {
        String mobileKey = dto.getMobileKey();
        String mobileOrEmail = (String) redisTemplate.opsForValue().get(mobileKey);
        // 判断手机号/邮箱是否过期
        if (StrUtil.isEmpty(mobileOrEmail)) {
            throw new GlobalException("手机号/邮箱已过期，请重新获取");
        }

        // 修改密码
        boolean update = this.update(
                new LambdaUpdateWrapper<User>()
                        .nested(qw -> qw.eq(User::getMobile, mobileOrEmail)
                                .or()
                                .eq(User::getEmail, mobileOrEmail))
                        .set(User::getPassword, passwordEncoder.encode(dto.getPassword())));
        if (update) {
            redisTemplate.delete(mobileKey);
            return true;
        }

        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void fundManagement(UserFundManagementDTO dto) {
        dto.validParam();

        User user = this.getById(dto.getId());
        Assert.notNull(user, () -> new GlobalException("会员不存在"));

        // 处理余额相关日志
        handleBalanceLogs(dto, user);

        // 处理积分相关日志
        handlePointsLogs(dto);

        // 更新用户资金信息
        updateUserFunds(user, dto);

        // 更新用户信息
        this.updateById(user);
    }

    @Override
    public void sendMobileCodeByMobileValidate(RegisterSmsDTO dto, Integer userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }
        dto.setMobile(user.getMobile());
        //发送短信
        configService.sendSms(dto);
    }

    @Override
    public void sendEmailCodeByEmailValidate(RegisterEmailDTO dto, Integer userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }
        dto.setEmail(user.getEmail());
        //发送短信
        configService.sendEmail(dto);
    }

    @Override
    public void sendMobileCodeByModifyMobile(RegisterSmsDTO dto, Integer userId) {
        if (dto.getMobile() == null || StringUtils.isEmpty(dto.getMobile())) {
            throw new GlobalException(translatePackage.translate("手机号不能为空"));
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }

        //发送短信
        configService.sendSms(dto);
    }

    @Override
    public void sendEmailCodeByModifyEmail(RegisterEmailDTO dto, Integer userId) {
        if (dto.getEmail() == null || StringUtils.isEmpty(dto.getEmail())) {
            throw new GlobalException(translatePackage.translate("邮箱不能为空"));
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new GlobalException(translatePackage.translate(USER_NOT_EXIST));
        }

        //发送短信
        configService.sendEmail(dto);
    }

    @Override
    public String bindMobile(BindMobileDTO dto) {
        String mobile = dto.getMobile();
        String sendCode = String.valueOf(redisTemplate.opsForValue().get("login" + mobile));
        if (!dto.getMobileCode().equals(sendCode)) {
            throw new GlobalException("短信验证码错误或已过期，请重试");
        }
        //注册用户
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile));
        if (!ObjectUtil.equals(user, SecurityUtils.getCurrentUserId())) {
            throw new GlobalException("手机号已经被绑定");
        }
        if (user == null) {
            String redCode = configService.getConfigByCode(SettingsEnum.SHOP_REG_CLOSED.getBizCode()).getBizVal();
            if (StrUtil.equals(redCode, "1")) {
                throw new GlobalException("系统已关闭注册");
            }
            user = new User();
            user.setUsername(mobile);
            user.setNickname(StrUtil.format("user_{}", mobile));
            user.setMobile(mobile);
            user.setMobileValidated(1);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setReferrerUserId(dto.getReferrerUserId());
            user.setRegTime(StringUtils.getCurrentTime());
            this.save(user);
        }
        user.setMobileValidated(1);
        this.updateById(user);
        //添加第三方账号信息
        Map<String, Object> openData = dto.getOpenData();
        //用于处理前端多套了一层openData
        if (ObjectUtil.isNotEmpty(openData) && openData.containsKey("openData")) {
            openData = (Map<String, Object>) openData.get("openData");
        }
        if (ObjectUtil.isNotEmpty(openData.get("openid"))) {
            String openid = openData.get("openid") != null ? String.valueOf(openData.get("openid")) : null;
            String unionid = openData.get("unionid") != null ? String.valueOf(openData.get("unionid")) : null;
            openid = StringUtils.trim(openid);
            unionid = StringUtils.trim(unionid);

            // 查询是否已有对应数据
            UserAuthorize entity = userAuthorizeService.getOne(new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getOpenId, openid));
            if (entity == null) {
                entity = new UserAuthorize();
                entity.setUserId(user.getUserId());
                entity.setOpenId(openid);
                entity.setOpenData(JSON.toJSONString(openData));
                entity.setUnionid(unionid);
                entity.setAddTime(StringUtils.getCurrentTime());
                entity.setAuthorizeType(AuthorizeTypeEnum.getAuthorizeType(HeaderUtils.getClientType()));
                userAuthorizeService.save(entity);
            }
        }
        //返回登录token
        return jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
    }

    @Override
    public LoginWechatEventVO wechatEvent(WechatEventDTO dto) {
        Assert.isTrue(StrUtil.isNotBlank(dto.getKey()), () -> new GlobalException("未登录"));
        // 根据 key 从 cache 中拿 openid
        String openid = (String) redisTemplate.opsForValue().get(dto.getKey());
        Assert.isTrue(StrUtil.isNotBlank(openid), () -> new GlobalException("未登录"));

        // 根据 openid 从数据库中查询用户信息
        UserAuthorize userAuthorize = userAuthorizeService.lambdaQuery().eq(UserAuthorize::getOpenId, openid).one();
        if (userAuthorize == null) {
            ConfigPO shopRegClosedConfig = configService.getConfigByCode(SettingsEnum.SHOP_REG_CLOSED.getBizCode());
            int shopRegClosed = Integer.parseInt(shopRegClosedConfig.getBizVal());

            ConfigPO wechatOauthConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_OAUTH.getBizCode());
            int wechatOauth = Integer.parseInt(wechatOauthConfig.getBizVal());

            ConfigPO openWechatRegisterConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_REGISTER.getBizCode());
            int openWechatRegister = Integer.parseInt(openWechatRegisterConfig.getBizVal());

            // 判断是否开启微信注册
            if (Constants.YES == shopRegClosed && Constants.YES == wechatOauth && Constants.YES == openWechatRegister) {
                // 注册用户
                String randomStr = RandomUtil.randomString(3);
                User user = User.builder()
                        .username(StrUtil.format("user_{}", randomStr))
                        .nickname(StrUtil.format("user_{}", randomStr))
                        .password(RandomUtil.randomString(6))
                        .regTime(StringUtils.getCurrentTime())
                        .build();
                this.save(user);

                // 绑定第三方账号信息
                userAuthorize = UserAuthorize.builder()
                        .authorizeType(AuthorizeTypeEnum.WECHAT.getCode())
                        .userId(user.getUserId())
                        .openId(openid)
                        .build();
                userAuthorizeService.save(userAuthorize);
            } else {
                Map<String, Map<String, String>> openData = Map.of("openData", Map.of("openid", openid));
                return LoginWechatEventVO.builder()
                        .type(2)
                        .openData(openData)
                        .build();
            }
        }

        User user = this.getById(userAuthorize.getUserId());
        String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());

        // 将token存入Redis中
        redisCache.setCacheObject(ADMIN_TOKEN + user.getUsername(), token, 30, TimeUnit.DAYS);
        return LoginWechatEventVO.builder()
                .type(1)
                .token(token)
                .build();
    }

    @Override
    public Page<?> userFundDetail(UserFundDetailPageQuery pageQuery) {
        return switch (pageQuery.getFromTag()) {
            case 3 -> getUserGrowthPointsLogVOPage(pageQuery);
            case 4 -> getUserPointsLogVOPage(pageQuery);
            default -> getUserBalanceLogVOPage(pageQuery);
        };
    }

    @Override
    public String getMobile(LoginGetMobileParam param) {
        // 获取小程序用户手机号
        // 获取微信配置
        String appId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode()).getBizVal();
        String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode()).getBizVal();
        // 获取token
        GetTokenResult tokenResult = wechatApiClient.getToken("client_credential", appId, appSecret);
        String accessToken = tokenResult.getAccessToken();
        Assert.notBlank(accessToken, () -> new GlobalException("获取token失败"));

        // 获取小程序用户手机号
        String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + accessToken;

        // 构建请求数据
        JSONObject data = new JSONObject();
        data.put("code", param.getCode());

        // 发送POST请求并获取响应
        String response = HttpUtil.post(url, data.toString());
        JSONObject result = JSON.parseObject(response);

        Integer errcode = result.getInteger("errcode");
        Assert.isTrue(errcode != null && errcode == 0, () -> new GlobalException(result.getString("errmsg")));
        JSONObject phoneInfo = result.getJSONObject("phone_info");

        String purePhoneNumber = phoneInfo.getString("purePhoneNumber");

        // 根据用户手机号注册用户
        User user = this.getUserByMobile(purePhoneNumber);
        if (user == null) {
            ConfigPO shopRegClosedConfig = configService.getConfigByCode(SettingsEnum.SHOP_REG_CLOSED.getBizCode());
            Assert.isTrue("0".equals(shopRegClosedConfig.getBizVal()), () -> new GlobalException("商城已停止注册"));

            ConfigPO wechatOauthConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_OAUTH.getBizCode());
            Assert.isTrue("1".equals(wechatOauthConfig.getBizVal()), () -> new GlobalException("公众号授权登录未开启"));

            ConfigPO openWechatRegisterConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_REGISTER.getBizCode());
            Assert.isTrue("1".equals(openWechatRegisterConfig.getBizVal()), () -> new GlobalException("微信注册未开启"));

            ConfigPO usernamePrefixConfig = configService.getConfigByCode(SettingsEnum.USERNAME_PREFIX.getBizCode());
            String username = usernamePrefixConfig.getBizVal() + purePhoneNumber;
            String password = RandomUtil.randomNumbers(8);

            user = User.builder()
                    .username(username)
                    .mobile(purePhoneNumber)
                    .email("")
                    .password(passwordEncoder.encode(password))
                    .avatar("")
                    .nickname("USER_" + purePhoneNumber)
                    .regTime(StringUtils.getCurrentTime())
                    .referrerUserId(0)
                    .mobileValidated(1)
                    .emailValidated(0)
                    .build();
            this.save(user);
        }
        // 返回登录token
        String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
        // 将token存入Redis中
        redisCache.setCacheObject(ADMIN_TOKEN + user.getUsername(), token, 30, TimeUnit.DAYS);
        return token;
    }

    @Override
    public void updateUserOpenId(LoginUpdateUserOpenIdParam param) {
        // 获取微信配置
        String appId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode()).getBizVal();
        String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode()).getBizVal();
        String result = wechatApiClient.code2Session(appId, appSecret, param.getCode(), "authorization_code");
        Code2SessionResult code2SessionResult = JsonUtil.fromJson(result, Code2SessionResult.class);
        // 错误code -1,40029，45011,40226
        if (code2SessionResult.getErrcode() != null && code2SessionResult.getErrcode() != 0) {
            throw new GlobalException(code2SessionResult.getErrmsg());
        }

        String openid = code2SessionResult.getOpenid();
        if (StrUtil.isNotBlank(openid)) {
            UserAuthorize userAuthorize = userAuthorizeService.lambdaQuery().eq(UserAuthorize::getOpenId, openid).one();
            Integer userId = getCurrentUserId();
            if (userAuthorize != null) {
                userAuthorize.setUserId(userId);
                userAuthorize.setAuthorizeType(AuthorizeTypeEnum.MINI_PROGRAM.getCode());
                userAuthorizeService.updateById(userAuthorize);
            } else {
                userAuthorize = UserAuthorize.builder()
                        .authorizeType(AuthorizeTypeEnum.MINI_PROGRAM.getCode())
                        .userId(userId)
                        .openId(openid)
                        .openData(JsonUtil.toJson(code2SessionResult))
                        .addTime(StringUtils.getCurrentTime())
                        .build();
                userAuthorizeService.save(userAuthorize);
            }
        }

    }

    @Transactional
    @Override
    public WxLoginInfoByCodeVO getWxLoginInfoByCode(String code) throws WxErrorException {
        // 获取微信配置
        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(configService.getConfigVal(SettingsEnum.WECHAT_APP_ID));
        configStorage.setSecret(configService.getConfigVal(SettingsEnum.WECHAT_APP_SECRET));
        configStorage.setToken(configService.getConfigVal(SettingsEnum.WECHAT_SERVER_TOKEN));
        configStorage.setAesKey(configService.getConfigVal(SettingsEnum.WECHAT_SERVER_SECRET));
        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(configStorage);
        // 1. 使用 WxJava 获取 access_token
        WxOAuth2AccessToken accessToken = service.getOAuth2Service().getAccessToken(code);
        String openid = accessToken.getOpenId();

        // 2. 获取用户信息
        WxOAuth2UserInfo userInfo = service.getOAuth2Service().getUserInfo(accessToken, "zh_CN");

        UserAuthorize userAuthorize = userAuthorizeService.lambdaQuery().eq(UserAuthorize::getOpenId, openid).one();

        if (userAuthorize != null) {
            User user = this.getById(userAuthorize.getUserId());
            String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
            // 将token存入Redis中
            redisCache.setCacheObject(ADMIN_TOKEN + user.getUsername(), token, 30, TimeUnit.DAYS);
            return new WxLoginInfoByCodeVO(1, token);
        }

        ConfigPO shopRegClosedConfig = configService.getConfigByCode(SettingsEnum.SHOP_REG_CLOSED.getBizCode());
        Assert.isTrue("0".equals(shopRegClosedConfig.getBizVal()), () -> new GlobalException("商城已停止注册"));

        ConfigPO wechatOauthConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_OAUTH.getBizCode());
        Assert.isTrue("1".equals(wechatOauthConfig.getBizVal()), () -> new GlobalException("公众号授权登录未开启"));

        ConfigPO openWechatRegisterConfig = configService.getConfigByCode(SettingsEnum.OPEN_WECHAT_REGISTER.getBizCode());
        Assert.isTrue("1".equals(openWechatRegisterConfig.getBizVal()), () -> new GlobalException("微信注册未开启"));

        ConfigPO usernamePrefixConfig = configService.getConfigByCode(SettingsEnum.USERNAME_PREFIX.getBizCode());
        String username = usernamePrefixConfig.getBizVal() + RandomUtil.randomNumbers(5);
        String password = RandomUtil.randomNumbers(8);

        User user = User.builder()
                .username(username)
                .mobile("")
                .email("")
                .password(passwordEncoder.encode(password))
                .avatar("")
                .nickname(username)
                .regTime(StringUtils.getCurrentTime())
                .referrerUserId(0)
                .mobileValidated(0)
                .emailValidated(0)
                .build();
        this.save(user);

        userAuthorize = UserAuthorize.builder()
                .authorizeType(AuthorizeTypeEnum.WECHAT.getCode())
                .userId(user.getUserId())
                .openId(openid)
                .openData(JsonUtil.toJson(accessToken))
                .addTime(StringUtils.getCurrentTime())
                .build();
        userAuthorizeService.save(userAuthorize);

        String token = jwtUtil.generateToken(user.getUserId(), false, user.getUsername());
        // 将token存入Redis中
        redisCache.setCacheObject(ADMIN_TOKEN + user.getUsername(), token, 30, TimeUnit.DAYS);
        return new WxLoginInfoByCodeVO(1, token);
    }

    @Override
    public void clientLogout() {
        String username = SecurityUtils.getCurrentUsername();

        // 删除token
        redisTemplate.delete(Constants.ADMIN_TOKEN + username);
    }

    @Override
    public void adminLogout() {
        String username = SecurityUtils.getCurrentUsername();

        // 删除token
        redisTemplate.delete(Constants.ADMIN_TOKEN + username);
    }

    @NotNull
    private Page<UserPointsLogVO> getUserPointsLogVOPage(UserFundDetailPageQuery pageQuery) {
        Page<UserPointsLog> userPointsLogPage = userPointsLogMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()),
                Wrappers.lambdaQuery(UserPointsLog.class)
                        .eq(UserPointsLog::getUserId, pageQuery.getUserId())
                        .orderByDesc(UserPointsLog::getChangeTime)
        );

        Page<UserPointsLogVO> resultPage = new Page<>();
        BeanUtil.copyProperties(userPointsLogPage, resultPage);

        List<UserPointsLog> records = userPointsLogPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return resultPage;
        }

        List<UserPointsLogVO> resultRecords = records.stream()
                .map(UserPointsLogVO::new)
                .toList();
        resultPage.setRecords(resultRecords);

        return resultPage;
    }

    @NotNull
    private Page<UserGrowthPointsLogVO> getUserGrowthPointsLogVOPage(UserFundDetailPageQuery pageQuery) {
        Page<UserGrowthPointsLog> userGrowthPointsLogPage = userGrowthPointsLogMapper.selectPage(new Page<>(pageQuery.getPage(), pageQuery.getSize()),
                Wrappers.lambdaQuery(UserGrowthPointsLog.class)
                        .eq(UserGrowthPointsLog::getUserId, pageQuery.getUserId())
                        .orderByDesc(UserGrowthPointsLog::getChangeTime)
        );

        Page<UserGrowthPointsLogVO> resultPage = new Page<>();
        BeanUtil.copyProperties(userGrowthPointsLogPage, resultPage);

        List<UserGrowthPointsLog> records = userGrowthPointsLogPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return resultPage;
        }

        List<UserGrowthPointsLogVO> resultRecords = records.stream()
                .map(UserGrowthPointsLogVO::new)
                .toList();
        resultPage.setRecords(resultRecords);

        return resultPage;
    }

    @NotNull
    private Page<UserBalanceLogVO> getUserBalanceLogVOPage(UserFundDetailPageQuery pageQuery) {
        User user = this.getById(pageQuery.getUserId());

        Page<UserBalanceLog> userBalanceLogPage = userBalanceLogService.lambdaQuery()
                .and(qw -> {
                    qw.gt(UserBalanceLog::getBalance, 0)
                            .or()
                            .gt(UserBalanceLog::getFrozenBalance, 0);
                })
                .eq(UserBalanceLog::getUserId, pageQuery.getUserId())
                .orderByDesc(UserBalanceLog::getChangeTime)
                .page(new Page<>(pageQuery.getPage(), pageQuery.getSize()));

        Page<UserBalanceLogVO> resultPage = new Page<>();
        BeanUtil.copyProperties(userBalanceLogPage, resultPage);

        List<UserBalanceLog> records = userBalanceLogPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return resultPage;
        }

        List<UserBalanceLogVO> resultRecords = records.stream()
                .map(item -> new UserBalanceLogVO(item, user))
                .toList();
        resultPage.setRecords(resultRecords);

        return resultPage;
    }

    private void handleBalanceLogs(UserFundManagementDTO dto, User user) {
        if (dto.getBalance() != null && dto.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            saveUserBalanceLog(dto, user.getBalance(), dto.getBalance(), dto.getTypeBalance(), user.getFrozenBalance(), null, null);
        }

        if (dto.getFrozenBalance() != null && dto.getFrozenBalance().compareTo(BigDecimal.ZERO) != 0) {
            saveUserBalanceLog(dto, user.getBalance(), null, null, user.getFrozenBalance(), dto.getFrozenBalance(), dto.getTypeFrozenBalance());

            // 冻结余额变动时，生成对应的余额日志
            BigDecimal frozenBalance = dto.getFrozenBalance();
            int type = dto.getTypeFrozenBalance();
            saveUserBalanceLog(dto, user.getBalance(), frozenBalance, type == 1 ? 2 : 1, user.getFrozenBalance(), null, null);
        }
    }

    private void handlePointsLogs(UserFundManagementDTO dto) {
        if (dto.getPoints() != null) {
            saveUserPointsLog(dto, dto.getPoints(), dto.getTypePoints());
        }

        if (dto.getGrowthPoints() != null) {
            saveUserGrowthPointsLog(dto, dto.getGrowthPoints(), dto.getTypeGrowthPoints());
        }
    }

    private void saveUserBalanceLog(UserFundManagementDTO dto, BigDecimal beforeBalance, BigDecimal balance, Integer balanceType, BigDecimal beforeFrozenBalance, BigDecimal frozenBalance, Integer frozenBalanceType) {
        BigDecimal afterBalance = beforeBalance;
        if (balanceType != null) {
            afterBalance = balanceType == 1 ? beforeBalance.add(balance) : beforeBalance.subtract(balance);
        }
        BigDecimal afterFrozenBalance = beforeFrozenBalance;
        if (frozenBalanceType != null) {
            afterFrozenBalance = frozenBalanceType == 1 ? beforeFrozenBalance.add(frozenBalance) : beforeFrozenBalance.subtract(frozenBalance);
        }

        UserBalanceLog log = UserBalanceLog.builder()
                .userId(dto.getId())
                .changeDesc(dto.getChangeDesc())
                .beforeBalance(beforeBalance)
                .balance(balance)
                .afterBalance(afterBalance)
                .frozenBalance(frozenBalance)
                .beforeFrozenBalance(beforeFrozenBalance)
                .afterFrozenBalance(afterFrozenBalance)
                .changeType(balanceType != null ? balanceType : frozenBalanceType)
                .changeTime(StringUtils.getCurrentTime())
                .build();
        userBalanceLogService.save(log);
    }

    private void saveUserPointsLog(UserFundManagementDTO dto, Integer points, Integer type) {
        // 剔除后台添加余额的情况
        if (points != 0) {
            UserPointsLog log = UserPointsLog.builder()
                    .userId(dto.getId())
                    .changeDesc(dto.getChangeDesc())
                    .points(points)
                    .changeType(type)
                    .changeTime(StringUtils.getCurrentTime())
                    .build();
            userPointsLogMapper.insert(log);
        }
    }

    private void saveUserGrowthPointsLog(UserFundManagementDTO dto, Integer growthPoints, Integer type) {
        // 剔除后台添加余额的情况
        if (growthPoints != 0) {
            UserGrowthPointsLog log = UserGrowthPointsLog.builder()
                    .userId(dto.getId())
                    .changeDesc(dto.getChangeDesc())
                    .points(growthPoints)
                    .changeType(type)
                    .changeTime(StringUtils.getCurrentTime())
                    .build();
            userGrowthPointsLogMapper.insert(log);

            // 发送升级消息
            if (tigshopProperties.getIsPro() != 1) {
                return;
            }
            rabbitTemplate.convertAndSend(RabbitMQConfig.DIRECT_EXCHANGE, RabbitMQConfig.VIP_UPDATE_ROUTING_KEY, dto.getId());
        }
    }

    private void updateUserFunds(User user, UserFundManagementDTO dto) {
        if (dto.getBalance() != null) {
            if (dto.getTypeBalance() == 1) {
                BigDecimal balance = user.getBalance().add(dto.getBalance());
                user.setBalance(balance);
            } else {
                Assert.isTrue(user.getBalance().compareTo(dto.getBalance()) >= 0, () -> new GlobalException("用户余额不足"));
                BigDecimal balance = user.getBalance().subtract(dto.getBalance());
                user.setBalance(balance);
            }

        }

        String configVal = configService.getConfigVal(SettingsEnum.INTEGRAL_NAME);
        if (dto.getPoints() != null) {
            if (dto.getTypePoints() == 1) {
                user.setPoints(user.getPoints() + dto.getPoints());
            } else {
                Assert.isTrue(user.getPoints() >= dto.getPoints(), () -> new GlobalException("用户" + configVal + "不足"));
                user.setPoints(user.getPoints() - dto.getPoints());
            }
        }

        if (dto.getGrowthPoints() != null) {
            if (dto.getTypeGrowthPoints() == 1) {
                user.setGrowthPoints(user.getGrowthPoints() + dto.getGrowthPoints());
            } else {
                Assert.isTrue(user.getGrowthPoints() >= dto.getGrowthPoints(), () -> new GlobalException("用户成长积分不足"));
                user.setGrowthPoints(user.getGrowthPoints() - dto.getGrowthPoints());
            }
        }

        if (dto.getFrozenBalance() != null) {
            if (dto.getTypeFrozenBalance() == 1) {
                user.setFrozenBalance(user.getFrozenBalance().add(dto.getFrozenBalance()));
                user.setBalance(user.getBalance().subtract(dto.getFrozenBalance()));
            } else {
                user.setFrozenBalance(user.getFrozenBalance().subtract(dto.getFrozenBalance()));
                user.setBalance(user.getBalance().add(dto.getFrozenBalance()));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeUser(int userId) {
        if (userId > 0) {
            this.update(Wrappers.lambdaUpdate(User.class).set(User::getStatus, 0).eq(User::getUserId, userId));
        }
    }

    @Override
    @SneakyThrows
    public void bindWechat(BindWechatParam param) {
        String code = param.getCode();
        Assert.isTrue(StringUtils.isNotBlank(code), () -> new GlobalException("code不能为空"));

        UserAuthorize userAuthorize = userAuthorizeService.getOne(
                new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getUserId, getCurrentUserId()));
        //检测用户是否有授权，有则不进行授权
        if (userAuthorize != null) {
            Assert.isTrue(userAuthorize.getOpenId() == null, () -> new GlobalException("该账号已绑定微信公众号"));
        }

        // 获取微信配置
        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(configService.getConfigVal(SettingsEnum.WECHAT_APP_ID));
        configStorage.setSecret(configService.getConfigVal(SettingsEnum.WECHAT_APP_SECRET));
        configStorage.setToken(configService.getConfigVal(SettingsEnum.WECHAT_SERVER_TOKEN));
        configStorage.setAesKey(configService.getConfigVal(SettingsEnum.WECHAT_SERVER_SECRET));
        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(configStorage);
        // 使用 WxJava 获取 access_token
        WxOAuth2AccessToken accessToken = service.getOAuth2Service().getAccessToken(code);
        String openId = accessToken.getOpenId();
        Assert.notNull(openId, () -> new GlobalException("该账号异常"));

        UserAuthorize userAuthorizeByOpenId = userAuthorizeService.getOne(
                new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getOpenId, openId));

        if (userAuthorizeByOpenId != null) {
            Integer userId = userAuthorizeByOpenId.getUserId();
            Assert.isTrue(userId != null && userId.equals(getCurrentUserId()),
                    () -> new GlobalException("该微信号已绑定其他账号，请解绑后再重试"));
        }

        userAuthorize = UserAuthorize.builder()
                .authorizeType(AuthorizeTypeEnum.WECHAT.getCode())
                .userId(getCurrentUserId())
                .openId(openId)
                .openData(JsonUtil.toJson(accessToken))
                .addTime(StringUtils.getCurrentTime())
                .build();
        userAuthorizeService.save(userAuthorize);
    }

    @Override
    public void unbindWechat() {
        UserAuthorize userAuthorize = userAuthorizeService.getOne(
                new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getUserId, getCurrentUserId()));
        String openId = userAuthorize.getOpenId();
        Assert.notNull(openId, () -> new GlobalException("该账号未绑定微信公众号"));
        userAuthorizeService.remove(new LambdaQueryWrapper<UserAuthorize>().eq(UserAuthorize::getOpenId, openId));
    }
}

