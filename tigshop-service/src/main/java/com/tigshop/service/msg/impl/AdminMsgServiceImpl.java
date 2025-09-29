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

package com.tigshop.service.msg.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgCreateDTO;
import com.tigshop.bean.dto.msg.AdminMsgListDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.enums.msg.AdminMsgTypeEnum;
import com.tigshop.bean.model.msg.AdminMsg;
import com.tigshop.bean.vo.msg.AdminMsgTypeListVO;
import com.tigshop.bean.vo.msg.AdminMsgVO;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.msg.AdminMsgMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.msg.AdminMsgService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员消息服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class AdminMsgServiceImpl extends BaseServiceImpl<AdminMsgMapper, AdminMsg> implements AdminMsgService {
    @Override
    public Page<AdminMsgVO> list(AdminMsgListDTO listDTO) {
        // 分页
        Page<AdminMsg> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<AdminMsg> queryWrapper = new LambdaQueryWrapper<>();

        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer msgType = listDTO.getMsgType();
        if (msgType != null) {
            queryWrapper.eq(AdminMsg::getMsgType, msgType);
        }

        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(AdminMsg::getVendorId, getVendorId());
        } else {
            queryWrapper.eq(AdminMsg::getShopId, getShopId());
            queryWrapper.eq(AdminMsg::getVendorId, 0);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(AdminMsg::getTitle, keyword);
        }
        // 执行查询
        Page<AdminMsg> adminMsgPage = this.page(page, queryWrapper);
        // 获取查询结果
        List<AdminMsg> adminMsgPageRecords = adminMsgPage.getRecords();

        // 转换为VO
        List<AdminMsgVO> adminMsgVOList = adminMsgPageRecords.stream()
                .map(adminMsg -> {
                    AdminMsgVO adminMsgVO = new AdminMsgVO();
                    BeanUtils.copyProperties(adminMsg, adminMsgVO);
                    //relatedData 解析json
                    adminMsgVO.setRelatedData(JsonUtil.fromJson(adminMsg.getRelatedData(), JSONObject.class));
                    adminMsgVO.setSendTime(DateUtil.format(DateUtil.date(adminMsg.getSendTime() * 1000), "yyyy-MM-dd HH:mm:ss"));

                    return adminMsgVO;
                }).toList();
        return PageUtil.convertPage(adminMsgPage, adminMsgVOList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createMessage(AdminMsgCreateDTO createDTO) {
        if (createDTO != null) {
            AdminMsg adminMsg = new AdminMsg();
            BeanUtils.copyProperties(createDTO, adminMsg);
            adminMsg.setSendTime(DateUtil.currentSeconds());
            adminMsg.setRelatedData(JsonUtil.toJson(createDTO.getRelatedData()));
            this.save(adminMsg);
        }
    }

    @Override
    public boolean setReaded(Integer msgId) {
        if (msgId == null) {
            return false;
        }
        LambdaUpdateWrapper<AdminMsg> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AdminMsg::getMsgId, msgId);
        updateWrapper.set(AdminMsg::getIsReaded, 1);
        return this.update(updateWrapper);
    }

    @Override
    public boolean setAllReaded() {
        LambdaUpdateWrapper<AdminMsg> updateWrapper = new LambdaUpdateWrapper<>();
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
            updateWrapper.eq(AdminMsg::getVendorId, getVendorId());
        } else {
            updateWrapper.eq(AdminMsg::getShopId, getShopId());
            updateWrapper.eq(AdminMsg::getVendorId, 0);
        }
        updateWrapper.set(AdminMsg::getIsReaded, 1);
        return this.update(updateWrapper);
    }

    @Override
    public Long getUnreadAdminMsgCount() {
        LambdaQueryWrapper<AdminMsg> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(AdminMsg::getVendorId, getVendorId());
        } else {
            queryWrapper.eq(AdminMsg::getShopId, getShopId());
            queryWrapper.eq(AdminMsg::getVendorId, 0);
        }
        queryWrapper.eq(AdminMsg::getIsReaded, 0);
        return this.count(queryWrapper);
    }

    @Override
    public List<AdminMsgTypeListVO> getAdminMsgTypeList() {
        // 获取所有未读消息列表
        LambdaQueryWrapper<AdminMsg> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminMsg::getIsReaded, 0);
        queryWrapper.eq(AdminMsg::getAdminId, 0);
        if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
            queryWrapper.eq(AdminMsg::getVendorId, getVendorId());
        } else {
            queryWrapper.eq(AdminMsg::getShopId, getShopId());
            queryWrapper.eq(AdminMsg::getVendorId, 0);
        }
        queryWrapper.select(AdminMsg::getMsgType);
        List<AdminMsg> adminMsgList = this.list(queryWrapper);

        // 创建一个列表来存储树形结构
        List<AdminMsgTypeListVO> tree = new ArrayList<>();

        // 存储子分类的映射
        Map<Integer, List<AdminMsgTypeListVO.AdminMsgTypeListChildVO>> childMap = new HashMap<>();

        // 存储未读消息数量
        Map<Integer, Integer> unreadCountMap = new HashMap<>();

        // 遍历 AdminMsgType 枚举
        for (AdminMsgTypeEnum type : AdminMsgTypeEnum.values()) {
            if (AdminTypeEnum.fromCode(getAdminType()) == AdminTypeEnum.VENDOR) {
                if (!type.isVendorShow()) {
                    continue;
                }
            } else {
                if (!type.isShopShow()) {
                    continue;
                }
            }
            if (type.getParentId() == 0) {
                // 创建父分类的 VO 对象
                AdminMsgTypeListVO parentVO = new AdminMsgTypeListVO();
                parentVO.setCatId(type.getCatId());
                parentVO.setCatName(type.getCatName());
                parentVO.setUnreadCount(0);
                // 初始化子分类列表
                parentVO.setChild(new ArrayList<>());
                tree.add(parentVO);
            } else {
                // 如果是子分类，存储到 childMap
                AdminMsgTypeListVO.AdminMsgTypeListChildVO childVO = new AdminMsgTypeListVO.AdminMsgTypeListChildVO();
                // 这里使用 catId 作为 msgType
                childVO.setMsgType(type.getCatId());
                childVO.setName(type.getCatName());
                // 初始化子分类未读数量
                childVO.setUnreadCount(0);
                childMap.computeIfAbsent(type.getParentId(), k -> new ArrayList<>()).add(childVO);
            }
        }

        // 统计未读消息数量
        for (AdminMsg adminMsg : adminMsgList) {
            Integer msgType = adminMsg.getMsgType();
            //未读的增加
            unreadCountMap.put(msgType, unreadCountMap.getOrDefault(msgType, 0) + 1);
        }

        // 为每个父分类添加子分类，并更新未读数量
        for (AdminMsgTypeListVO parent : tree) {
            Integer parentId = parent.getCatId();
            if (childMap.containsKey(parentId)) {
                List<AdminMsgTypeListVO.AdminMsgTypeListChildVO> children = childMap.get(parentId);
                parent.setChild(children);
            }
            parent.setUnreadCount(unreadCountMap.getOrDefault(parentId, 0));
        }

        // 添加子分类的未读数量
        for (AdminMsgTypeListVO parent : tree) {
            List<AdminMsgTypeListVO.AdminMsgTypeListChildVO> children = parent.getChild();
            int childUnreadCount = 0;
            if (children != null) {
                for (AdminMsgTypeListVO.AdminMsgTypeListChildVO child : children) {
                    // 使用 msgType 获取未读数量
                    Integer childId = child.getMsgType();
                    Integer count = unreadCountMap.getOrDefault(childId, 0);
                    childUnreadCount += count;
                    child.setUnreadCount(count);
                }
            }
            parent.setUnreadCount(parent.getUnreadCount() + childUnreadCount);
        }

        return tree;
    }

}
