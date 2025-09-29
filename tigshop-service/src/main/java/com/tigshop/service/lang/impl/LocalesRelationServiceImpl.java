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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.lang.LocalesRelationDTO;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.model.lang.LocalesRelation;
import com.tigshop.bean.vo.lang.LocalesVO;
import com.tigshop.bean.vo.setting.LocalesRelationVO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.lang.LocalesRelationMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.lang.LocalesRelationService;
import com.tigshop.service.lang.LocalesService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 语言关联服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月31日 15:31
 */
@Service
public class LocalesRelationServiceImpl extends BaseServiceImpl<LocalesRelationMapper, LocalesRelation> implements LocalesRelationService {
    @Resource
    private LocalesService localesService;

    @Override
    public Page<LocalesRelationVO> list(BasePage basePage) {
        Page<LocalesRelation> page = new Page<>(basePage.getPage(), basePage.getSize());
        LambdaQueryWrapper<LocalesRelation> queryWrapper = new LambdaQueryWrapper<>();
        buildSortOrder(page, basePage.getSortField(), basePage.getSortOrder());
        List<LocalesRelationVO> list = this.list(page, queryWrapper).stream().map(item -> {
            LocalesRelationVO vo = new LocalesRelationVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).toList();
        return PageUtil.convertPage(page, list);
    }

    @Override
    public LocalesRelationVO detail(Integer id) {
        if (id != null) {
            LocalesRelation localesRelation = this.getById(id);
            LocalesRelationVO localesRelationVO = new LocalesRelationVO();
            BeanUtils.copyProperties(localesRelation, localesRelationVO);
            return localesRelationVO;
        }
        return null;
    }

    @Override
    public boolean create(LocalesRelationDTO dto) {
        if (dto != null) {
            LocalesRelation localesRelation = new LocalesRelation();
            BeanUtils.copyProperties(dto, localesRelation);
            return this.save(localesRelation);
        }
        return false;
    }

    @Override
    public boolean update(LocalesRelationDTO dto) {
        if (dto != null) {
            LocalesRelation localesRelation = new LocalesRelation();
            BeanUtils.copyProperties(dto, localesRelation);
            return this.updateById(localesRelation);
        }
        return false;
    }

    /**
     * 获取默认语言
     *
     * @param code 语言code
     * @return LocalesVO
     */
    @Override
    public LocalesVO getDefaultLocale(String code) {
        if (StrUtil.isNotBlank(code)) {
            LocalesRelation localesRelation = this.getOne(new LambdaQueryWrapper<LocalesRelation>().eq(LocalesRelation::getCode, code));
            if (localesRelation != null) {
                Locales locales = localesService.getById(localesRelation.getLocalesId());
                return localesService.getLocalesVO(locales);
            }
        }
        // 1代表默认,code为空或者没有查到设置语言的时候就会使用这个
        Locales locales = localesService.getOne(new LambdaQueryWrapper<Locales>().eq(Locales::getIsDefault, 1));
        return localesService.getLocalesVO(locales);
    }
}
