package com.tigshop.service.o2o;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.model.o2o.Tip;
import com.tigshop.bean.param.o2o.TipCreateParam;
import com.tigshop.bean.param.o2o.TipGroupQueryPage;
import com.tigshop.bean.param.o2o.TipUpdateParam;
import com.tigshop.bean.vo.o2o.TipListVO;
import com.tigshop.service.common.BaseService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * @author Admin
 * @description 针对表【tip_group(门店标签组)】的数据库操作Service
 * @createDate 2025-08-29 12:07:49
 */
public interface TipService extends BaseService<Tip> {

    Page<TipListVO> list(TipGroupQueryPage listDTO);

    TipListVO detail(Integer id);

    void create(@Valid TipCreateParam createDTO);

    void update(@Valid TipUpdateParam updateDTO);

    List<TipListVO> selectAllTip(String keyword);
}
