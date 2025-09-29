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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.AccountCreateDTO;
import com.tigshop.bean.dto.shop.AccountListDTO;
import com.tigshop.bean.dto.shop.AccountUpdateDTO;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.shop.Account;
import com.tigshop.bean.model.shop.Shop;
import com.tigshop.bean.vo.merchant.MerchantVO;
import com.tigshop.bean.vo.shop.AccountVO;
import com.tigshop.bean.vo.shop.ShopAccountLogVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.shop.AccountMapper;
import com.tigshop.service.authority.AdminUserService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.merchant.MerchantAccountService;
import com.tigshop.service.merchant.MerchantService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.shop.AccountService;
import com.tigshop.service.shop.ShopService;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.tigshop.common.utils.SecurityUtils.getCurrentAdminId;

/**
 * 店铺资金变化服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class AccountServiceImpl extends BaseServiceImpl<AccountMapper, Account> implements AccountService {
    @Resource
    ShopService shopService;
    @Resource
    OrderService orderService;
    @Resource
    AdminUserService adminUserService;
    @Resource
    MerchantAccountService merchantAccountService;
    @Resource
    MerchantService merchantService;

    @Override
    public AccountVO index(AccountListDTO listDTO) {
        Integer shopId = getShopId();
        Integer adminId = getCurrentAdminId();
        AccountVO accountVO = new AccountVO();
        BigDecimal sumShopMoney = shopService.getSumShopMoney(shopId);
        BigDecimal sumFrozenMoney = shopService.getSumFrozenMoney(shopId);
        BigDecimal sumOrderUnSettlementAmount = orderService.getOrderUnSettlementAmount(shopId);
        //查询该adminId下的merchant_id
        AdminUser adminUser = adminUserService.getById(adminId);
        Integer merchantId = adminUser.getMerchantId();
        Integer count = merchantAccountService.getCardCount(merchantId, shopId);
        if (merchantId > 0) {
            MerchantVO merchantVo = merchantService.getMerchantVO(merchantId);
            accountVO.setMerchant(merchantVo);
        } else {
            accountVO.setMerchant(null);
        }

        accountVO.setShopMoney(sumShopMoney);
        accountVO.setFrozenMoney(sumFrozenMoney);
        accountVO.setUnSettlementMoney(sumOrderUnSettlementAmount);
        accountVO.setCardCount(count);
        return accountVO;
    }

    @Override
    public AccountVO detail(Integer id) {
        if (id != null) {
            Account account = this.getById(id);
            AccountVO accountVO = new AccountVO();
            BeanUtils.copyProperties(account, accountVO);
            return accountVO;
        }
        return null;
    }

    @Override
    public boolean create(AccountCreateDTO createDTO) {
        if (createDTO != null) {
            Account account = new Account();
            BeanUtils.copyProperties(createDTO, account);
            return this.save(account);
        }
        return false;
    }

    @Override
    public boolean update(AccountUpdateDTO updateDTO) {
        if (updateDTO != null) {
            Account account = new Account();
            BeanUtils.copyProperties(updateDTO, account);
            return this.updateById(account);
        }
        return false;
    }

    @Override
    public Page<ShopAccountLogVO> list(AccountListDTO listDTO) {
        // 分页
        Page<Account> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Account> queryWrapper = new LambdaQueryWrapper<>();
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            List<Shop> shop = shopService.getShopByShopTitle(keyword);
            List<Integer> shopIds = shop.stream().
                    map(Shop::getShopId).
                    toList();
            if(shopIds.isEmpty()){
                return null;
            }
            queryWrapper.in(Account::getShopId, shopIds);
        }

        if (listDTO.getShopId() != null) {
            queryWrapper.eq(Account::getShopId, listDTO.getShopId());
        } else {
            if (getShopId() != 0) {
                queryWrapper.eq(Account::getShopId, getShopId());
            }
        }
        //时间查询
        String startTime = listDTO.getStartTime();
        String endTime = listDTO.getEndTime();
        if(!StringUtil.isNullOrEmpty(startTime)){
            startTime = startTime + " 00:00:00";
            long addTimeStartTimestamp = DateUtil.parse(startTime).getTime() / 1000;
            queryWrapper.ge(Account::getAddTime, addTimeStartTimestamp);
        }
        if(!StringUtil.isNullOrEmpty(endTime)){
            endTime = endTime + " 23:59:59";
            long addTimeEndTimestamp = DateUtil.parse(endTime).getTime() / 1000;
            queryWrapper.le(Account::getAddTime, addTimeEndTimestamp);
        }
        // 执行查询
        Page<Account> accountPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Account> accountPageRecords = accountPage.getRecords();
        // 转换为VO
        List<ShopAccountLogVO> shopAccountLogVOList = accountPageRecords.stream()
                .map(account -> {
                    ShopAccountLogVO shopAccountLogVO = new ShopAccountLogVO();
                    BeanUtils.copyProperties(account, shopAccountLogVO);
                    long timestampInSeconds = account.getAddTime();
                    Date date = new Date(timestampInSeconds * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    shopAccountLogVO.setAddTime(sdf.format(date));
                    shopAccountLogVO.setShopTitle(Optional.ofNullable(shopService.getById(account.getShopId())).map(Shop::getShopTitle).orElse(""));
                    shopAccountLogVO.setUnSettlementOrder(orderService.getOrderUnSettlementAmount(account.getShopId()));
                    shopAccountLogVO.setChangeAmount(account.getShopMoney());
                    /*if (account.getIsNew() == 0) {
                        shopAccountLogVO.setChangeAmount(account.getShopMoney());
                    } else {
                        BigDecimal changeAmount = account.getNewShopMoney().subtract(account.getShopMoney()).setScale(2, RoundingMode.HALF_UP);
                        if (changeAmount.compareTo(BigDecimal.ZERO) <= 0) {
                            changeAmount = BigDecimal.ZERO;
                        }
                        shopAccountLogVO.setChangeAmount(changeAmount);
                    }*/
                    return shopAccountLogVO;
                }).toList();
        shopAccountLogVOList.forEach(shopAccountLogVO ->
                shopAccountLogVO.setShopMoney(shopAccountLogVO.getNewShopMoney().subtract(shopAccountLogVO.getShopMoney())));
        return PageUtil.convertPage(accountPage, shopAccountLogVOList);
    }

    @Override
    public void refundWithDrawLog(Integer shopId, BigDecimal amount) {

        Shop shop = this.changeShopAccount(shopId, amount, 2);
        BigDecimal newShopMoney = shop.getShopMoney().add(amount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newFrozenMoney = shop.getFrozenMoney().subtract(amount).setScale(2, RoundingMode.HALF_UP);

        Account accountLog = new Account();
        accountLog.setShopMoney(shop.getShopMoney());
        accountLog.setFrozenMoney(shop.getFrozenMoney());
        accountLog.setNewShopMoney(newShopMoney);
        accountLog.setNewFrozenMoney(newFrozenMoney);
        accountLog.setShopId(shopId);
        accountLog.setType(1);
        accountLog.setAddTime(StringUtils.getCurrentTime());

        this.save(accountLog);
    }

    @Override
    public void addWithDrawLog(Integer shopId, BigDecimal amount) {
        Shop oldShop = shopService.getById(shopId);
        Shop shop = this.changeShopAccount(shopId, amount, 1);
        Account accountLog = new Account();
        accountLog.setShopMoney(oldShop.getShopMoney());
        accountLog.setFrozenMoney(oldShop.getFrozenMoney());
        accountLog.setNewShopMoney(shop.getShopMoney());
        accountLog.setNewFrozenMoney(shop.getFrozenMoney());
        accountLog.setShopId(shopId);
        accountLog.setAddTime(StringUtils.getCurrentTime());
        // 1.店铺资金增加 2提现
        accountLog.setType(2);
        // 1是新版本
        accountLog.setIsNew(1);
        this.save(accountLog);
    }

    @Override
    public void completeWithDrawLog(Integer shopId, BigDecimal amount) {
        Shop oldShop = shopService.getById(shopId);
        Shop shop = this.changeShopAccount(shopId, amount, 3);
        Account accountLog = new Account();
        accountLog.setShopMoney(oldShop.getShopMoney());
        accountLog.setFrozenMoney(oldShop.getFrozenMoney());
        accountLog.setNewShopMoney(shop.getShopMoney());
        accountLog.setNewFrozenMoney(shop.getFrozenMoney());
        accountLog.setShopId(shopId);
        accountLog.setType(1);
        this.save(accountLog);
    }

    public Shop changeShopAccount(Integer shopId, BigDecimal amount, int type) {

        Shop shop = shopService.getById(shopId);
        LambdaUpdateChainWrapper<Shop> wrapper = shopService.lambdaUpdate().eq(Shop::getShopId, shopId);
        if (shop == null) {
            throw new RuntimeException("店铺不存在");
        }
        switch (type) {
            case 1:
                wrapper.setDecrBy(Shop::getShopMoney, amount)
                        .setIncrBy(Shop::getFrozenMoney, amount);
                break;
            case 2:
                wrapper.setIncrBy(Shop::getShopMoney, amount)
                        .setDecrBy(Shop::getFrozenMoney, amount);
                break;
            case 3:
                wrapper.setDecrBy(Shop::getFrozenMoney, amount);
                break;
            case 4:
                wrapper.setIncrBy(Shop::getShopMoney, amount);
                break;
            default:
                break;
        }
        wrapper.update();
        return shop;
    }

}
