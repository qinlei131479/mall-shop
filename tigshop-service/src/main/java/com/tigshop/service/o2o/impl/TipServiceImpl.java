package com.tigshop.service.o2o.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.enums.o2o.TipStatusEnum;
import com.tigshop.bean.model.o2o.Tip;
import com.tigshop.bean.param.o2o.TipCreateParam;
import com.tigshop.bean.param.o2o.TipGroupQueryPage;
import com.tigshop.bean.param.o2o.TipUpdateParam;
import com.tigshop.bean.vo.o2o.TipListVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.o2o.TipMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.o2o.TipService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Admin
 * @description 针对表【tip_group(门店标签组)】的数据库操作Service实现
 * @createDate 2025-08-29 12:07:49
 */
@Service
@RequiredArgsConstructor
public class TipServiceImpl extends BaseServiceImpl<TipMapper, Tip>
        implements TipService {

    @Override
    public Page<TipListVO> list(TipGroupQueryPage listDTO) {
        // 分页
        Page<Tip> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Tip> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 搜索关键字
        if (StrUtil.isNotEmpty(listDTO.getKeyword())) {
            queryWrapper.like(Tip::getTipName, listDTO.getKeyword());
        }
        queryWrapper.eq(listDTO.getStatus() != null, Tip::getStatus, listDTO.getStatus());

        // 执行查询
        Page<Tip> areaPage = this.page(page, queryWrapper);

        // 转换为VO
        Page<TipListVO> voPage = new Page<>();
        BeanUtils.copyProperties(areaPage, voPage);

        List<TipListVO> voList = areaPage.getRecords().stream().map(tip -> {
            TipListVO vo = new TipListVO();
            BeanUtils.copyProperties(tip, vo);
            vo.setAddTime(tip.getAddTime().toString());
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public TipListVO detail(Integer id) {
        Tip oldTip = getBaseMapper().selectById(id);
        if (oldTip == null) {
            throw new GlobalException("标签不存在");
        }
        TipListVO tipListVO = new TipListVO();
        BeanUtils.copyProperties(oldTip, tipListVO);
        tipListVO.setAddTime(oldTip.getAddTime().toString());
        return tipListVO;
    }

    @Override
    @Transactional
    public void create(TipCreateParam createDTO) {
        Tip tip = new Tip();
        tip.setTipName(createDTO.getTipName());
        tip.setStatus(createDTO.getStatus());
        tip.setAddTime(StringUtils.getCurrentTime());
        getBaseMapper().insert(tip);
    }

    @Override
    public void update(TipUpdateParam updateDTO) {
        Integer tipId = updateDTO.getTipId();
        Tip oldTip = getBaseMapper().selectById(tipId);
        if (oldTip == null) {
            throw new GlobalException("标签不存在");
        }
        oldTip.setTipName(updateDTO.getTipName());
        oldTip.setStatus(updateDTO.getStatus());
        getBaseMapper().updateById(oldTip);
    }

    @Override
    public List<TipListVO> selectAllTip(String keyword) {
        List<Tip> list = list(new LambdaQueryWrapper<Tip>()
                .eq(Tip::getStatus, TipStatusEnum.ENABLE.getCode())
                .like(ObjectUtil.isNotEmpty(keyword), Tip::getTipName, keyword));
        return list.stream().map(tip -> {
            TipListVO vo = new TipListVO();
            vo.setTipId(tip.getTipId());
            vo.setTipName(tip.getTipName());
            vo.setStatus(tip.getStatus());
            vo.setAddTime(tip.getAddTime().toString());
            return vo;
        }).toList();
    }

}




