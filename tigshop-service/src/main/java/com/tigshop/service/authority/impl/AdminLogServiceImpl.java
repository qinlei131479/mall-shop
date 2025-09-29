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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.authority.AdminLogDTO;
import com.tigshop.bean.model.authority.AdminLog;
import com.tigshop.bean.model.authority.AdminUser;
import com.tigshop.bean.model.log.RequestLog;
import com.tigshop.bean.model.shop.AdminUserShop;
import com.tigshop.bean.model.vendor.AdminUserVendor;
import com.tigshop.bean.query.adminlog.AdminLogPageQuery;
import com.tigshop.common.utils.*;
import com.tigshop.mapper.authority.AdminLogMapper;
import com.tigshop.mapper.authority.AdminUserMapper;
import com.tigshop.mapper.log.RequestLogMapper;
import com.tigshop.mapper.shop.AdminUserShopMapper;
import com.tigshop.mapper.vendor.AdminUserVendorMapper;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.common.impl.BaseServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tigshop.common.constant.Constants.DATE_FORMAT;

/**
 * 管理员日志
 *
 * @author Jayce
 * @create 2024年10月28日 10:31
 */
@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl extends BaseServiceImpl<AdminLogMapper, AdminLog> implements AdminLogService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserShopMapper adminUserShopMapper;
    private final AdminUserVendorMapper adminUserVendorMapper;
    private final RequestLogMapper requestLogMapper;

    @Override
    public boolean create(AdminLogDTO adminLogDTO) {
        AdminLog adminLog = new AdminLog();
        // 将DTO中的属性复制到AdminLog实体中，以简化创建过程。
        BeanUtils.copyProperties(adminLogDTO, adminLog);
        adminLog.setLogTime(DateUtil.currentSeconds());
        adminLog.setIpAddress(IpUtils.getIpAddr());
        return this.save(adminLog);
    }

    @Override
    public Page<AdminLogDTO> adminLogPage(AdminLogPageQuery query) {

        Page<AdminLog> page = new Page<>(query.getPage(), query.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<AdminLog> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(query.getKeyword())) {
            queryWrapper.like(StrUtil.isNotBlank(query.getKeyword()), AdminLog::getLogInfo, query.getKeyword())
                    .or()
                    .like(StrUtil.isNotBlank(query.getKeyword()), AdminLog::getIpAddress, query.getKeyword());
        }
        if (HeaderUtils.getShopId() != null && HeaderUtils.getShopId() > 0) {
            List<AdminUserShop> adminUsers = adminUserShopMapper.selectList(new LambdaQueryWrapper<AdminUserShop>()
                    .eq(AdminUserShop::getShopId, HeaderUtils.getShopId()));
            List<Integer> adminUserIds = adminUsers.stream().map(AdminUserShop::getAdminId).toList();

            if (CollUtil.isNotEmpty(adminUserIds)) {
                queryWrapper.in(AdminLog::getUserId, adminUserIds);
            } else {
                return PageUtil.convertPage(page, List.of());
            }
        }
        if (HeaderUtils.getVendorId() != null && HeaderUtils.getVendorId() > 0) {
            List<AdminUserVendor> adminUsers = adminUserVendorMapper.selectList(new LambdaQueryWrapper<AdminUserVendor>()
                    .eq(AdminUserVendor::getVendorId, HeaderUtils.getVendorId()));
            List<Integer> adminUserIds = adminUsers.stream().map(AdminUserVendor::getAdminId).toList();

            if (CollUtil.isNotEmpty(adminUserIds)) {
                queryWrapper.in(AdminLog::getUserId, adminUserIds);
            } else {
                return PageUtil.convertPage(page, List.of());
            }
        }

        buildSortOrder(page, query.getSortField(), query.getSortOrder());
        // 执行查询
        Page<AdminLog> resultPage = this.page(page, queryWrapper);

        List<AdminLog> records = resultPage.getRecords();

        if (CollUtil.isEmpty(records)) {
            return PageUtil.convertPage(resultPage, List.of());
        }
        List<AdminLogDTO> resultRecords = getAdminLogListWithUsernames(records);
        return PageUtil.convertPage(resultPage, resultRecords);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createByLogInfo(String logInfo) {
        AdminLog adminLog = new AdminLog();
        adminLog.setLogTime(DateUtil.currentSeconds());
        adminLog.setIpAddress(IpUtils.getIpAddr());
        adminLog.setLogInfo(logInfo);
        adminLog.setUserId(SecurityUtils.getCurrentAdminId());
        return this.save(adminLog);
    }

    /**
     * 将 AdminLog 实体列表转换为带有用户名的 AdminLogDTO 列表
     *
     * @param records 需要转换的 AdminLog 实体列表
     * @return List<AdminLogDTO>
     */
    public List<AdminLogDTO> getAdminLogListWithUsernames(List<AdminLog> records) {
        // 提取 userId 列表
        List<Integer> userIds = records.stream()
                .map(AdminLog::getUserId)
                .distinct()
                .toList();

        // 批量查询 AdminUser 表以获取 userId -> username 映射
        Map<Integer, String> userIdToUsernameMap = adminUserMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(AdminUser::getAdminId, AdminUser::getUsername));

        // 将结果映射到 AdminLogDTO 列表
        return records.stream().map(adminLog -> {
            AdminLogDTO dto = new AdminLogDTO();
            BeanUtils.copyProperties(adminLog, dto);
            DateTime date = DateUtil.date(adminLog.getLogTime() * 1000);
            dto.setLogTime(DateUtil.format(date, DATE_FORMAT));
            // 从映射中获取用户名
            dto.setUsername(userIdToUsernameMap.get(adminLog.getUserId()));
            // 获取请求日志
            Integer requestLogId = adminLog.getRequestLogId();
            if (requestLogId != null){
                RequestLog requestLog = requestLogMapper.selectById(requestLogId);
                dto.setRequestLog(requestLog);
            }
            return dto;
        }).toList();
    }
}
