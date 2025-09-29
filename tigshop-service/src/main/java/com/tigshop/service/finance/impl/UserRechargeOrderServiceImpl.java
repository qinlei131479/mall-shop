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
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.*;
import com.tigshop.bean.enums.finance.UserRechargeOrderStatus;
import com.tigshop.bean.enums.finance.UserWithDrawApplyStatus;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.wechat.Code2SessionResult;
import com.tigshop.bean.model.finance.Paylog;
import com.tigshop.bean.model.finance.UserRechargeOrder;
import com.tigshop.bean.model.finance.UserWithdrawApply;
import com.tigshop.bean.model.promotion.RechargeSetting;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.finance.ClientUserRechargeOrderVO;
import com.tigshop.bean.vo.finance.UserRechargeOrderPayVO;
import com.tigshop.bean.vo.finance.UserRechargeOrderUserVO;
import com.tigshop.bean.vo.finance.UserRechargeOrderVO;
import com.tigshop.bean.vo.order.OrderVO;
import com.tigshop.bean.vo.order.pay.PayCreateVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.*;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.feign.WechatApiClient;
import com.tigshop.mapper.finance.UserRechargeOrderMapper;
import com.tigshop.mapper.finance.UserWithdrawApplyMapper;
import com.tigshop.mapper.promotion.RechargeSettingMapper;
import com.tigshop.mapper.user.UserMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.finance.PaylogService;
import com.tigshop.service.finance.UserRechargeOrderService;
import com.tigshop.service.pay.PayService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.user.UserBalanceService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.finance.UserRechargeOrderConstants.*;

/**
 * 充值记录服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@RequiredArgsConstructor
@Service
public class UserRechargeOrderServiceImpl extends BaseServiceImpl<UserRechargeOrderMapper, UserRechargeOrder> implements UserRechargeOrderService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserBalanceService userBalanceService;

    @Resource
    private UserWithdrawApplyMapper userWithdrawApplyMapper;

    @Resource
    private RechargeSettingMapper rechargeSettingMapper;

    @Resource
    private PaylogService paylogService;

    @Resource
    @Qualifier("WechatPayService")
    private PayService wechatPayService;

    @Resource
    @Qualifier("AliPayService")
    private PayService aliPayService;

    @Resource
    private TranslatePackageImpl translatePackage;

    private final ConfigService configService;

    private final WechatApiClient wechatApiClient;

    @Override
    public Page<UserRechargeOrderVO> list(UserRechargeOrderListDTO listDTO) {
        // 分页
        Page<UserRechargeOrder> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<UserRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getStatus();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq("is_show", isShow);
        }*/
        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(UserRechargeOrder::getPostscript, keyword);
        }
        // 执行查询
        Page<UserRechargeOrder> userRechargeOrderPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<UserRechargeOrder> userRechargeOrderPageRecords = userRechargeOrderPage.getRecords();

        if (CollUtil.isEmpty(userRechargeOrderPageRecords)) {
            return PageUtil.convertPage(userRechargeOrderPage, List.of());
        }

        // 获取所有 ID
        List<Integer> userIds = userRechargeOrderPageRecords.stream()
                .map(UserRechargeOrder::getUserId)
                .distinct()
                .toList();

        // 调用方法获取会员信息
        List<User> users = userMapper.selectBatchIds(userIds);

        // 将分类信息存入 Map，方便后续查找
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        // 转换为VO
        List<UserRechargeOrderVO> userRechargeOrderVOList = userRechargeOrderPageRecords.stream()
                .map(userRechargeOrder -> {
                    UserRechargeOrderVO userRechargeOrderVO = new UserRechargeOrderVO();
                    BeanUtils.copyProperties(userRechargeOrder, userRechargeOrderVO);

                    User user = userMap.get(userRechargeOrder.getUserId());
                    if (user != null) {
                        UserRechargeOrderUserVO userVO = new UserRechargeOrderUserVO();
                        BeanUtils.copyProperties(user, userVO);
                        userRechargeOrderVO.setUser(userVO);
                    }

                    DateTime date = DateUtil.date(userRechargeOrder.getAddTime() * 1000);
                    userRechargeOrderVO.setAddTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));

                    userRechargeOrderVO.setStatusType(STATUS_TYPE.get(userRechargeOrder.getStatus())); // 将分类名称设置到对象中

                    return userRechargeOrderVO;
                }).toList();
        return PageUtil.convertPage(userRechargeOrderPage, userRechargeOrderVOList);
    }

    @Override
    public UserRechargeOrderVO detail(Integer id) {
        if (id != null) {
            UserRechargeOrder userRechargeOrder = this.getById(id);
            UserRechargeOrderVO userRechargeOrderVO = new UserRechargeOrderVO();
            BeanUtils.copyProperties(userRechargeOrder, userRechargeOrderVO);
            return userRechargeOrderVO;
        }
        return new UserRechargeOrderVO();
    }

    @Override
    public boolean create(UserRechargeOrderCreateDTO createDTO) {
        if (createDTO != null) {
            UserRechargeOrder userRechargeOrder = new UserRechargeOrder();
            BeanUtils.copyProperties(createDTO, userRechargeOrder);
            //假如userRechargeOrder.getAmount() <=0
            if (createDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new GlobalException(USER_RECHARGE_AMOUNT_ERROR);
            }
            userRechargeOrder.setAddTime(DateUtil.currentSeconds());

            return this.save(userRechargeOrder);
        }
        return false;
    }

    @Override
    public boolean update(UserRechargeOrderUpdateDTO updateDTO) {
        if (updateDTO != null) {
            UserRechargeOrder userRechargeOrder = new UserRechargeOrder();
            BeanUtils.copyProperties(updateDTO, userRechargeOrder);

            //如果修改之前的旧 status状态不为0，抛出错误
            if (updateDTO.getStatus() != 0 && this.getById(updateDTO.getOrderId()).getStatus() != 0) {
                throw new GlobalException(USER_RECHARGE_STATUS_ERROR);
            }
            if (updateDTO.getStatus() == 1) {
                if (!userBalanceService.incBalance(userRechargeOrder.getAmount(), userRechargeOrder.getUserId(), "充值订单审核通过增加余额")) {
                    return false;
                }
            }

            return this.updateById(userRechargeOrder);
        }
        return false;
    }

    @Override
    public BigDecimal getUserRechargeSurplusTotal(String startTime, String endTime) {
        LambdaQueryWrapper<UserRechargeOrder> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimeDate = DateUtil.parse(startTime);
            Date endTimeDate = DateUtil.parse(endTime);
            // 将时间设置为当天的结束时间
            endTimeDate = DateUtil.endOfDay(endTimeDate);

            // 添加时间范围的条件，假设 changeTime 是 Unix 时间戳
            queryWrapper.ge(UserRechargeOrder::getAddTime, startTimeDate.getTime() / 1000)
                    .le(UserRechargeOrder::getAddTime, endTimeDate.getTime() / 1000);
        }

        // 查询所有符合条件的余额变动
        queryWrapper.select(UserRechargeOrder::getAmount);
        List<UserRechargeOrder> userRechargeOrder = this.list(queryWrapper);

        // 计算余额变动总和
        return userRechargeOrder.stream()
                .map(UserRechargeOrder::getAmount)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public Page<ClientUserRechargeOrderVO> clientList(UserRechargeOrderListDTO listDTO) {
        // 用户提现申请
        LambdaQueryWrapper<UserWithdrawApply> queryWrapper = new LambdaQueryWrapper<>();
        // 充值记录
        LambdaQueryWrapper<UserRechargeOrder> queryWrapperOrder = new LambdaQueryWrapper<>();
        if (listDTO.getUserId() != null && listDTO.getUserId() > 0) {
            queryWrapper.eq(UserWithdrawApply::getUserId, listDTO.getUserId());
            queryWrapperOrder.eq(UserRechargeOrder::getUserId, listDTO.getUserId());
        }
        if (listDTO.getStatus() != null && listDTO.getStatus() != -1) {
            queryWrapper.eq(UserWithdrawApply::getStatus, listDTO.getStatus());
            queryWrapperOrder.eq(UserRechargeOrder::getStatus, listDTO.getStatus());
        }
        // 查询用户提现申请列表
        List<UserWithdrawApply> userWithdrawApplyList = userWithdrawApplyMapper.selectList(queryWrapper);
        int userWithdrawApplySize = userWithdrawApplyList == null ? 0 : userWithdrawApplyList.size();
        // 查询充值记录列表
        List<UserRechargeOrder> userRechargeOrderList = this.list(queryWrapperOrder);
        int userRechargeOrderSize = userRechargeOrderList == null ? 0 : userRechargeOrderList.size();
        long total = userWithdrawApplySize + userRechargeOrderSize;

        List<ClientUserRechargeOrderVO> clientUserRechargeOrderApplyVOList = new ArrayList<>();
        List<ClientUserRechargeOrderVO> clientUserRechargeOrderVOList = new ArrayList<>();

        if (userWithdrawApplyList != null && !userWithdrawApplyList.isEmpty()) {
            clientUserRechargeOrderApplyVOList = userWithdrawApplyList.stream()
                    .map(userWithdrawApply -> {
                        ClientUserRechargeOrderVO clientUserRechargeOrderVO = new ClientUserRechargeOrderVO();
                        BeanUtils.copyProperties(userWithdrawApply, clientUserRechargeOrderVO);
                        clientUserRechargeOrderVO.setStatusType(UserWithDrawApplyStatus.getDescription(userWithdrawApply.getStatus()));
                        clientUserRechargeOrderVO.setAddTime(TigUtils.handelTime(userWithdrawApply.getAddTime()));
                        clientUserRechargeOrderVO.setType(translatePackage.translate("提现"));
                        clientUserRechargeOrderVO.setAddTimeLong(userWithdrawApply.getAddTime());
                        return clientUserRechargeOrderVO;
                    }).toList();
        }

        if (userRechargeOrderList != null && !userRechargeOrderList.isEmpty()) {
            clientUserRechargeOrderVOList = userRechargeOrderList.stream()
                    .map(userRechargeOrder -> {
                        ClientUserRechargeOrderVO clientUserRechargeOrderVO = new ClientUserRechargeOrderVO();
                        BeanUtils.copyProperties(userRechargeOrder, clientUserRechargeOrderVO);
                        clientUserRechargeOrderVO.setStatusType(UserRechargeOrderStatus.getDescription(userRechargeOrder.getStatus()));
                        clientUserRechargeOrderVO.setAddTime(TigUtils.handelTime(userRechargeOrder.getAddTime()));
                        clientUserRechargeOrderVO.setType(translatePackage.translate("充值"));
                        clientUserRechargeOrderVO.setAddTimeLong(userRechargeOrder.getAddTime());
                        return clientUserRechargeOrderVO;
                    }).toList();
        }
        //将 clientUserRechargeOrderApplyVOList 和 clientUserRechargeOrderVOList 合并成一个 List
        List<ClientUserRechargeOrderVO> clientUserRechargeOrderVoListAll = new ArrayList<>();
        clientUserRechargeOrderVoListAll.addAll(clientUserRechargeOrderApplyVOList);
        clientUserRechargeOrderVoListAll.addAll(clientUserRechargeOrderVOList);

        //将 clientUserRechargeOrderVOListAll 按照 addTimeLong 倒序排序
        clientUserRechargeOrderVoListAll.sort(Comparator.comparing(ClientUserRechargeOrderVO::getAddTimeLong).reversed());

        //根据 listDTO.getPage() 和 listDTO.getSize() 分页
        List<ClientUserRechargeOrderVO> clientUserRechargeOrderVoList = clientUserRechargeOrderVoListAll.stream()
                .skip((long) (listDTO.getPage() - 1) * listDTO.getSize())
                .limit(listDTO.getSize())
                .toList();

        Page<ClientUserRechargeOrderVO> page = new Page<>(listDTO.getPage(), listDTO.getSize(), total);
        page.setRecords(clientUserRechargeOrderVoList);
        return page;
    }

    @Override
    public Integer rechargeOperation(ClientUserRechargeOrderUpdateDTO updateDTO) {
        BigDecimal amount;
        BigDecimal discountMoney;

        if (updateDTO.getAmount() != null) {
            LambdaQueryWrapper<RechargeSetting> queryWrapper = new LambdaQueryWrapper<>();
            //'money', '<=', $amount
            queryWrapper.eq(RechargeSetting::getMoney, updateDTO.getAmount());
            queryWrapper.orderByDesc(RechargeSetting::getMoney);
            queryWrapper.last("limit 1");
            RechargeSetting rechargeSetting = rechargeSettingMapper.selectOne(queryWrapper);
            discountMoney = rechargeSetting != null ? rechargeSetting.getDiscountMoney() : BigDecimal.ZERO;
            amount = updateDTO.getAmount();
        } else {
            LambdaQueryWrapper<RechargeSetting> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(RechargeSetting::getIsShow, 1);
            queryWrapper.eq(RechargeSetting::getRechargeId, updateDTO.getId());
            RechargeSetting rechargeSetting = rechargeSettingMapper.selectOne(queryWrapper);
            if (rechargeSetting == null) {
                throw new GlobalException(translatePackage.translate("充值金额错误"));
            }
            amount = rechargeSetting.getMoney();
            discountMoney = rechargeSetting.getDiscountMoney();
        }

        UserRechargeOrder userRechargeOrder = new UserRechargeOrder();
        userRechargeOrder.setUserId(updateDTO.getUserId());
        userRechargeOrder.setAmount(amount);
        userRechargeOrder.setDiscountMoney(discountMoney);
        userRechargeOrder.setAddTime(StringUtils.getCurrentTime());
        this.save(userRechargeOrder);

        return userRechargeOrder.getOrderId();
    }

    @Override
    public UserRechargeOrderPayVO pay(ClientUserRechargeOrderPayDTO payDTO) {
        UserRechargeOrder userRechargeOrder = this.getById(payDTO.getOrderId());
        if (userRechargeOrder == null) {
            throw new GlobalException(translatePackage.translate("充值申请不存在"));
        }
        if (userRechargeOrder.getStatus() == 1) {
            throw new GlobalException(translatePackage.translate("充值申请已支付"));
        }
        UserRechargeOrderPayVO userRechargeOrderPayVO = new UserRechargeOrderPayVO();
        BeanUtils.copyProperties(userRechargeOrder, userRechargeOrderPayVO);
        userRechargeOrderPayVO.setStatusType(UserRechargeOrderStatus.getDescription(userRechargeOrder.getStatus()));
        userRechargeOrderPayVO.setAddTime(TigUtils.handelTime(userRechargeOrder.getAddTime()));
        userRechargeOrderPayVO.setPaidTime(TigUtils.handelTime(userRechargeOrder.getPaidTime()));
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, userRechargeOrder.getUserId());
        User user = userMapper.selectOne(queryWrapper);
        UserRechargeOrderPayVO.UserVO userVO = new UserRechargeOrderPayVO.UserVO();
        BeanUtils.copyProperties(user, userVO);
        userRechargeOrderPayVO.setUser(userVO);
        return userRechargeOrderPayVO;
    }

    @Override
    public Integer checkStatus(Integer id) {
        if (id == null) {
            throw new GlobalException(translatePackage.translate("参数缺失"));
        }
        UserRechargeOrder userRechargeOrder = this.getById(id);
        if (userRechargeOrder == null) {
            throw new GlobalException(translatePackage.translate("充值申请不存在"));
        }
        return userRechargeOrder.getStatus();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateVO createPay(UserRechargeOrderPayDTO payDTO, Integer userId) {
        if (payDTO.getType() == null || StringUtils.isEmpty(payDTO.getType())) {
            throw new GlobalException(translatePackage.translate("未选择支付方式"));
        }
        String openid = null;
        if (payDTO.getCode() != null) {
            // 获取微信配置
            String appId = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_APP_ID.getBizCode()).getBizVal();
            String appSecret = configService.getConfigByCode(SettingsEnum.WECHAT_MINI_PROGRAM_SECRET.getBizCode()).getBizVal();
            String result = wechatApiClient.code2Session(appId, appSecret, payDTO.getCode(), "authorization_code");
            Code2SessionResult code2SessionResult = JsonUtil.fromJson(result, Code2SessionResult.class);
            // 错误code -1,40029，45011,40226
            if (code2SessionResult.getErrcode() != null && code2SessionResult.getErrcode() != 0) {
                throw new GlobalException(code2SessionResult.getErrmsg());
            }

            openid = code2SessionResult.getOpenid();
        }
        UserRechargeOrder userRechargeOrder = this.getById(payDTO.getId());
        if (userRechargeOrder.getStatus() == 1) {
            throw new GlobalException(translatePackage.translate("订单已支付"));
        }
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderType(1);
        orderVO.setTotalAmount(userRechargeOrder.getAmount());
        orderVO.setUnpaidAmount(userRechargeOrder.getAmount());
        orderVO.setOrderId(userRechargeOrder.getOrderId());
        Paylog paylog = paylogService.creatPayLog(orderVO, payDTO.getType());
        orderVO.setPaySn(paylog.getPaySn());
        // 由于需要签名校验参数所以构建签名的时候这个也必传
        orderVO.setOrderSn(paylog.getPaySn());
        PayCreateVO.PayInfo payResult = new PayCreateVO.PayInfo();

        String clientType = HeaderUtils.getClientType();
        if ("wechat".equals(payDTO.getType())) {
            payResult = wechatPayService.pay(orderVO, openid, clientType);
        }
        if ("alipay".equals(payDTO.getType())) {
            payResult = aliPayService.pay(orderVO, openid, clientType);
        }
        PayCreateVO payCreateVO = new PayCreateVO();
        payCreateVO.setPayInfo(payResult);
        payCreateVO.setOrderId(userRechargeOrder.getOrderId());
        // 支付宝支付这个必传
        payCreateVO.setOrderSn(orderVO.getOrderSn());
        payCreateVO.setOrderAmount(userRechargeOrder.getAmount());
        return payCreateVO;
    }
}
