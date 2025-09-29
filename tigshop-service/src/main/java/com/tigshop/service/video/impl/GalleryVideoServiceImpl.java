package com.tigshop.service.video.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.GalleryListDTO;
import com.tigshop.bean.dto.setting.GalleryVideoCreateDTO;
import com.tigshop.bean.dto.setting.GalleryVideoUpdateDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.model.video.GalleryVideo;
import com.tigshop.bean.vo.setting.GalleryVideoListVO;
import com.tigshop.bean.vo.setting.GalleryVideoVO;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.mapper.video.GalleryVideoMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.video.GalleryVideoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wzh
 */
@Service
public class GalleryVideoServiceImpl extends BaseServiceImpl<GalleryVideoMapper, GalleryVideo>
        implements GalleryVideoService {

    @Override
    public Page<GalleryVideoVO> list(GalleryListDTO dto) {
        // 创建分页对象
        Page<GalleryVideo> page = new Page<>(dto.getPage(), 99);
        // 构建查询条件
        LambdaQueryWrapper<GalleryVideo> queryWrapper = buildGalleryQueryWrapper(dto);
        // 执行分页查询
        Page<GalleryVideo> galleryPage = this.page(page, queryWrapper);
        // 将查询结果转为 DTO
        List<GalleryVideoVO> recordsDTO = galleryPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();
        // 返回分页结果
        return PageUtil.convertPage(galleryPage, recordsDTO);
    }

    @Override
    public List<GalleryVideoListVO> treeList() {
        LambdaQueryWrapper<GalleryVideo> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.ADMIN == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, 0);
            queryWrapper.eq(GalleryVideo::getVendorId, 0);
        }
        if (AdminTypeEnum.SHOP == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, getShopId());
        }
        if (AdminTypeEnum.VENDOR == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getVendorId, getVendorId());
        }
        queryWrapper.orderByDesc(GalleryVideo::getId);
        List<GalleryVideo> galleryVideoList = list(queryWrapper);

        if (galleryVideoList.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用 Map 缓存所有节点，避免多次遍历
        Map<Integer, GalleryVideoListVO> nodeMap = new HashMap<>();

        // 初始化所有节点到 Map
        galleryVideoList.forEach(gallery -> {
            GalleryVideoListVO vo = BeanUtil.copyProperties(gallery, GalleryVideoListVO.class);
            nodeMap.put(vo.getId(), vo);
        });

        // 构建树形结构
        List<GalleryVideoListVO> rootList = new ArrayList<>();
        nodeMap.forEach((id, node) -> {
            GalleryVideoListVO parent = nodeMap.get(node.getParentId());
            if (node.getParentId() == 0) {
                rootList.add(node);
            } else if (parent != null) {
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);
            }
        });

        return rootList;
    }


    @Override
    public GalleryVideoVO detail(Integer id) {
        // 如果 ID 小于 1，返回空
        if (id < 1) {
            return null;
        }
        // 获取相册实体并转换为 DTO
        GalleryVideo gallery = this.getById(id);
        // 返回相册详情
        return convertToDTO(gallery);
    }

    @Override
    public Integer create(GalleryVideoCreateDTO dto) {
        GalleryVideo gallery = convertToBean(dto);
        this.save(gallery);
        return gallery.getId();
    }

    @Override
    public Integer update(GalleryVideoUpdateDTO dto) {
        GalleryVideo galleryVideo = this.getById(dto.getId());
        Assert.notNull(galleryVideo, "视频相册不存在");

        Long count = this.lambdaQuery()
                .ne(GalleryVideo::getId, dto.getId())
                .eq(GalleryVideo::getParentId, galleryVideo.getParentId())
                .eq(GalleryVideo::getName, dto.getName())
                .count();

        Assert.isTrue(count == 0, "视频相册名称已存在");

        this.lambdaUpdate()
                .eq(GalleryVideo::getId, dto.getId())
                .set(GalleryVideo::getName, dto.getName())
                .set(GalleryVideo::getSort, dto.getSort())
                .update();

        return dto.getId();
    }

    private LambdaQueryWrapper<GalleryVideo> buildGalleryQueryWrapper(GalleryListDTO dto) {
        Page<GalleryVideo> page = new Page<>(dto.getPage(), 10000);
        LambdaQueryWrapper<GalleryVideo> queryWrapper = new LambdaQueryWrapper<>();
        if (AdminTypeEnum.ADMIN == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, 0);
            queryWrapper.eq(GalleryVideo::getVendorId, 0);
        }
        if (AdminTypeEnum.SHOP == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getShopId, getShopId());
        }
        if (AdminTypeEnum.VENDOR == AdminTypeEnum.fromCode(getAdminType())) {
            queryWrapper.eq(GalleryVideo::getVendorId, getVendorId());
        }
        Integer galleryId = dto.getGalleryId();
        // 如果指定了 galleryId，按 parent_id 过滤
        if (galleryId != null && galleryId >= 0) {
            queryWrapper.eq(GalleryVideo::getParentId, galleryId);
        }

        // 排序
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());
        return queryWrapper;
    }


    private GalleryVideoVO convertToDTO(GalleryVideo gallery) {
        GalleryVideoVO dto = new GalleryVideoVO();
        BeanUtils.copyProperties(gallery, dto);
        return dto;
    }


    private GalleryVideo convertToBean(GalleryVideoCreateDTO dto) {
        GalleryVideo gallery = new GalleryVideo();
        BeanUtils.copyProperties(dto, gallery);
        gallery.setShopId(getShopId());
        gallery.setAddUserId(SecurityUtils.getCurrentAdminId());
        gallery.setAddTime(StringUtils.getCurrentTime());
        gallery.setVendorId(getVendorId());
        return gallery;
    }
}




