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

package com.tigshop.service.authority.impl;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.SuppliersDTO;
import com.tigshop.bean.dto.authority.SuppliersListDTO;
import com.tigshop.bean.enums.common.IsShowType;
import com.tigshop.bean.model.authority.Suppliers;
import com.tigshop.bean.vo.authority.SuppliersVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.authority.SuppliersMapper;
import com.tigshop.service.authority.SuppliersService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tigshop.common.constant.authority.SuppliersConstants.SUPPLIERS_NAME_EXIST;

/**
 * 供应商管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class SuppliersServiceImpl extends BaseServiceImpl<SuppliersMapper, Suppliers> implements SuppliersService {
    @Override
    public Page<SuppliersVO> list(SuppliersListDTO listDTO) {
        // 分页
        Page<Suppliers> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Suppliers> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq(Suppliers::getIsShow, isShow);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(Suppliers::getSuppliersName, keyword);
        }
        // 执行查询
        Page<Suppliers> suppliersPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Suppliers> suppliersPageRecords = suppliersPage.getRecords();
        // 转换为VO
        List<SuppliersVO> suppliersVOList = suppliersPageRecords.stream()
                .map(suppliers -> {
                    SuppliersVO suppliersVO = new SuppliersVO();
                    BeanUtils.copyProperties(suppliers, suppliersVO);
                    return suppliersVO;
                }).toList();
        return PageUtil.convertPage(suppliersPage,suppliersVOList);
    }

    @Override
    public SuppliersVO detail(Integer id) {
        if (id != null) {
            Suppliers suppliers = this.getById(id);
            SuppliersVO suppliersVO = new SuppliersVO();
            BeanUtils.copyProperties(suppliers, suppliersVO);
            //插入地区集合
            List<Integer> regionsArr = new ArrayList<>(Arrays.asList(
                    suppliersVO.getCountry(),
                    suppliersVO.getProvince(),
                    suppliersVO.getCity(),
                    suppliersVO.getDistrict()
            ));
            suppliersVO.setRegions(regionsArr);

            return suppliersVO;
        }
        return null;
    }

    /**
     * 获取地区
     * @param dto SuppliersDTO
     */
    public void getRegions(SuppliersDTO dto) {
        Integer[] regions = dto.getRegions();
        // 取出数组元素
        if (ArrayUtil.isNotEmpty(regions)) {
            dto.setCountry(regions[0]);
            dto.setProvince(regions[1]);
            dto.setCity(regions[2]);
            dto.setDistrict(regions[3]);
        }
    }

    /**
     * 校验是否存在
     * @param id 供应商id
     * @param dto SuppliersDTO
     */
    public void checkUnique(Integer id,SuppliersDTO dto) {
        String suppliersName = dto.getSuppliersName();
        if (this.count(new LambdaQueryWrapper<Suppliers>()
                .eq(Suppliers::getSuppliersName, suppliersName)
                .ne(Suppliers::getSuppliersId, id)
                .select(Suppliers::getSuppliersName)) > 0) {
            throw new GlobalException(SUPPLIERS_NAME_EXIST);
        }
    }

    @Override
    public boolean create(SuppliersDTO createDTO) {
        if (createDTO != null) {
            Suppliers suppliers = new Suppliers();

            Integer shopId = getShopId();
            createDTO.setShopId(shopId);
            checkUnique(0, createDTO);

            getRegions(createDTO);

            BeanUtils.copyProperties(createDTO, suppliers);
            return this.save(suppliers);
        }
        return false;
    }

    @Override
    public boolean update(SuppliersDTO updateDTO) {
        if (updateDTO != null) {
            Suppliers suppliers = new Suppliers();
            checkUnique(updateDTO.getSuppliersId(), updateDTO);
            getRegions(updateDTO);
            BeanUtils.copyProperties(updateDTO, suppliers);
            return this.updateById(suppliers);
        }
        return false;
    }
}
