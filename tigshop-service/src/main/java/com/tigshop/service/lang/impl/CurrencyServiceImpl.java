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

package com.tigshop.service.lang.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.lang.CurrencyDTO;
import com.tigshop.bean.dto.lang.CurrencyListDTO;
import com.tigshop.bean.model.lang.Currency;
import com.tigshop.bean.vo.setting.CurrencyVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.lang.CurrencyMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.lang.CurrencyService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 货币管理接口实现类
 *
 * @author Tigshop团队
 * @create 2024年12月30日 16:43
 */
@Service
public class CurrencyServiceImpl extends BaseServiceImpl<CurrencyMapper, Currency> implements CurrencyService {
    @Override
    public Page<CurrencyVO> list(CurrencyListDTO dto) {
        Page<Currency> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Currency> queryWrapper = new LambdaQueryWrapper<>();
        String name = dto.getName();
        if (StrUtil.isNotEmpty(name)) {
            queryWrapper.like(Currency::getName, name);
        }
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());
        List<CurrencyVO> list = this.list(page, queryWrapper).stream().map(item -> {
            CurrencyVO vo = new CurrencyVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList();
        return PageUtil.convertPage(page, list);
    }

    @Override
    public boolean create(CurrencyDTO dto) {
        if (dto != null) {
            Currency currency = new Currency();
            BeanUtils.copyProperties(dto, currency);
            return this.save(currency);
        }
        return false;
    }

    @Override
    public boolean update(CurrencyDTO dto) {
        if (dto.getIsDefault() == 1) {
            // 更新列表中所有的默认为 0
            this.update(new LambdaUpdateWrapper<Currency>().eq(Currency::getIsDefault, 1).set(Currency::getIsDefault, 0));
        }
        if (dto != null) {
            Currency currency = new Currency();
            BeanUtils.copyProperties(dto, currency);
            return this.updateById(currency);
        }
        return false;
    }

    @Override
    public CurrencyVO detail(Integer id) {
        if (id != null) {
            Currency currency = this.getById(id);
            CurrencyVO currencyVO = new CurrencyVO();
            BeanUtils.copyProperties(currency, currencyVO);
            return currencyVO;
        }
        return null;
    }

    @Override
    public List<CurrencyVO> getCurrency(){
        List<Currency> currency = this.list();
        return currency.stream().map(item -> {
            CurrencyVO vo = new CurrencyVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList();
    }
}
