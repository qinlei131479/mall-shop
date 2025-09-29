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

package com.tigshop.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.AftersalesLogCreateDTO;
import com.tigshop.bean.dto.order.AftersalesLogListDTO;
import com.tigshop.bean.dto.order.AftersalesLogUpdateDTO;
import com.tigshop.bean.dto.order.ReturnPicDTO;
import com.tigshop.bean.model.order.AftersalesLog;
import com.tigshop.bean.vo.common.ListResVO;
import com.tigshop.bean.vo.order.AftersalesLogVO;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.order.AftersalesLogMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.order.AftersalesLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 售后记录服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class AftersalesLogServiceImpl extends BaseServiceImpl<AftersalesLogMapper, AftersalesLog> implements AftersalesLogService {
    @Override
    public ListResVO<AftersalesLogVO, AftersalesLogListDTO> list(AftersalesLogListDTO listDTO) {
        // 分页
        Page<AftersalesLog> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<AftersalesLog> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        /*Integer isShow = listDTO.getIsShow();
        if (isShow != null && (isShow.equals(IsShowType.SHOW.getCode()) || isShow.equals(IsShowType.NOT_SHOW.getCode()))) {
            queryWrapper.eq("is_show", isShow);
        }*/

        // 搜索关键字
        /*String keyword = listDTO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("aftersales_log", keyword);
        }*/
        // 执行查询
        Page<AftersalesLog> aftersalesLogPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<AftersalesLog> aftersalesLogPageRecords = aftersalesLogPage.getRecords();
        // 转换为VO
        List<AftersalesLogVO> aftersalesLogVOList = aftersalesLogPageRecords.stream()
                .map(aftersalesLog -> {
                    AftersalesLogVO aftersalesLogVO = new AftersalesLogVO();
                    BeanUtils.copyProperties(aftersalesLog, aftersalesLogVO);
                    return aftersalesLogVO;
                }).toList();
        return new ListResVO<>(aftersalesLogVOList, listDTO, aftersalesLogPage.getTotal());
    }

    @Override
    public AftersalesLogVO detail(Integer id) {
        if (id != null) {
            AftersalesLog aftersalesLog = this.getById(id);
            AftersalesLogVO aftersalesLogVO = new AftersalesLogVO();
            BeanUtils.copyProperties(aftersalesLog, aftersalesLogVO);
            return aftersalesLogVO;
        }
        return new AftersalesLogVO();
    }

    @Override
    public boolean create(AftersalesLogCreateDTO createDTO) {
        if (createDTO != null) {
            AftersalesLog aftersalesLog = new AftersalesLog();
            BeanUtils.copyProperties(createDTO, aftersalesLog);
            return this.save(aftersalesLog);
        }
        return false;
    }

    @Override
    public boolean update(AftersalesLogUpdateDTO updateDTO) {
        if (updateDTO != null) {
            AftersalesLog aftersalesLog = new AftersalesLog();
            BeanUtils.copyProperties(updateDTO, aftersalesLog);
            return this.updateById(aftersalesLog);
        }
        return false;
    }

    @Override
    public boolean addAftersaleLog(Integer aftersalesId, String logInfo) {
        AftersalesLog aftersalesLog = new AftersalesLog();
        aftersalesLog.setAftersaleId(aftersalesId);
        aftersalesLog.setLogInfo(logInfo);
        if (SecurityUtils.isAdminUser()) {
            aftersalesLog.setAdminName(SecurityUtils.getCurrentUsername());
        } else {
            aftersalesLog.setUserName(SecurityUtils.getCurrentUsername());
        }
        aftersalesLog.setAddTime(StringUtils.getCurrentTime().toString());
        aftersalesLog.setShopId(HeaderUtils.getShopId());
        aftersalesLog.setVendorId(HeaderUtils.getVendorId());
        return this.save(aftersalesLog);
    }

    @Override
    public List<AftersalesLogVO> getDetailLog(Integer id) {
        LambdaQueryWrapper<AftersalesLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AftersalesLog::getAftersaleId, id);
        queryWrapper.orderByAsc(AftersalesLog::getLogId);
        List<AftersalesLog> afterSalesLogs = this.list(queryWrapper);
        return getList(afterSalesLogs);
    }

    public List<AftersalesLogVO> getList(List<AftersalesLog> afterSalesLogs) {
        return afterSalesLogs.stream().map(aftersalesLog -> {
            AftersalesLogVO aftersalesLogVO = new AftersalesLogVO();
            BeanUtils.copyProperties(aftersalesLog, aftersalesLogVO);
            if (aftersalesLog.getReturnPic() != null && !"[]".equals(aftersalesLog.getReturnPic())) {
                aftersalesLogVO.setReturnPic(JsonUtil.jsonToList(aftersalesLog.getReturnPic(), ReturnPicDTO.class));
            } else {
                aftersalesLogVO.setReturnPic(new ArrayList<>());
            }
            aftersalesLogVO.setAddTime(TigUtils.handelTime(Long.parseLong(aftersalesLog.getAddTime())));
            return aftersalesLogVO;
        }).toList();
    }
}
