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

package com.tigshop.service.setting.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.LogisticsCompanyCreateDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyListDTO;
import com.tigshop.bean.dto.setting.LogisticsCompanyUpdateDTO;
import com.tigshop.bean.enums.common.IsShowType;
import com.tigshop.bean.model.setting.LogisticsCompany;
import com.tigshop.bean.vo.setting.LogisticsCompanyVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.setting.LogisticsCompanyMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.LogisticsCompanyService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配送方式配置信息服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class LogisticsCompanyServiceImpl extends BaseServiceImpl<LogisticsCompanyMapper, LogisticsCompany> implements LogisticsCompanyService {
    @Resource
    private AdminLogService adminLogService;

    @Override
    public Page<LogisticsCompanyVO> list(LogisticsCompanyListDTO listDTO) {
        // 分页
        Page<LogisticsCompany> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<LogisticsCompany> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer shopId = listDTO.getShopId();
        if (IsShowType.fromTypeCode(shopId) != null) {
            queryWrapper.eq(LogisticsCompany::getShopId, shopId);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(LogisticsCompany::getLogisticsName, keyword);
        }
        // 执行查询
        Page<LogisticsCompany> logisticsCompanyPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<LogisticsCompany> logisticsCompanyPageRecords = logisticsCompanyPage.getRecords();
        // 转换为VO
        List<LogisticsCompanyVO> logisticsCompanyVOList = logisticsCompanyPageRecords.stream()
                .map(logisticsCompany -> {
                    LogisticsCompanyVO logisticsCompanyVO = new LogisticsCompanyVO();
                    BeanUtils.copyProperties(logisticsCompany, logisticsCompanyVO);
                    return logisticsCompanyVO;
                }).toList();
        return PageUtil.convertPage(logisticsCompanyPage, logisticsCompanyVOList);
    }

    @Override
    public LogisticsCompanyVO detail(Integer id) {
        if (id != null) {
            LogisticsCompany logisticsCompany = this.getById(id);
            LogisticsCompanyVO logisticsCompanyVO = new LogisticsCompanyVO();
            BeanUtils.copyProperties(logisticsCompany, logisticsCompanyVO);
            return logisticsCompanyVO;
        }
        return null;
    }

    @Override
    public boolean create(LogisticsCompanyCreateDTO createDTO) {
        if (createDTO != null) {
            LogisticsCompany logisticsCompany = new LogisticsCompany();
            BeanUtils.copyProperties(createDTO, logisticsCompany);
            logisticsCompany.setShopId(getShopId());
            adminLogService.createByLogInfo("新增物流公司:"+ createDTO.getLogisticsName());
            return this.save(logisticsCompany);
        }
        return false;
    }

    @Override
    public boolean update(LogisticsCompanyUpdateDTO updateDTO) {
        if (updateDTO != null) {
            LogisticsCompany logisticsCompany = new LogisticsCompany();
            BeanUtils.copyProperties(updateDTO, logisticsCompany);
            LogisticsCompany oldLogisticsCompany = this.getById(updateDTO.getId());
            logisticsCompany.setLogisticsId(updateDTO.getId());
            adminLogService.createByLogInfo("更新物流公司:"+ oldLogisticsCompany.getLogisticsName());
            return this.updateById(logisticsCompany);
        }
        return false;
    }

    @Override
    public LogisticsCompany getLogisticsCompanyById(Integer id) {
        if (id != null) {
            return this.getById(id);
        }
        return  null;
    }
}
