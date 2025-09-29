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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.GalleryDTO;
import com.tigshop.bean.dto.setting.GalleryListDTO;
import com.tigshop.bean.dto.setting.GalleryUpdateDTO;
import com.tigshop.bean.model.setting.Gallery;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.mapper.setting.GalleryMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.GalleryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 相册服务实现
 *
 * @author Jayce
 * @create 2024年11月12日 13:48
 */
@Service
public class GalleryServiceImpl extends BaseServiceImpl<GalleryMapper, Gallery> implements GalleryService {
    /**
     * 获取相册列表（分页）
     *
     * @param dto 查询条件和分页信息
     * @return 分页后的相册 DTO 列表
     */
    @Override
    public Page<GalleryDTO> list(GalleryListDTO dto) {
        // 创建分页对象
        Page<Gallery> page = new Page<>(dto.getPage(), 99);
        // 构建查询条件
        LambdaQueryWrapper<Gallery> queryWrapper = buildGalleryQueryWrapper(dto);
        // 执行分页查询
        Page<Gallery> galleryPage = this.page(page, queryWrapper);
        // 将查询结果转为 DTO
        List<GalleryDTO> recordsDTO = galleryPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();
        // 返回分页结果
        return PageUtil.convertPage(galleryPage, recordsDTO);
    }

    /**
     * 构建 Gallery 查询条件
     * @param dto 查询条件和排序信息
     * @return QueryWrapper
     */
    private LambdaQueryWrapper<Gallery> buildGalleryQueryWrapper(GalleryListDTO dto) {
        Page<Gallery> page = new Page<>(dto.getPage(), 10000);

        LambdaQueryWrapper<Gallery> queryWrapper = new LambdaQueryWrapper<>();
        Integer shopId = getShopId();
        queryWrapper.eq(shopId != null, Gallery::getShopId, shopId);

        Integer vendorId = HeaderUtils.getVendorId();
        queryWrapper.eq(vendorId != null, Gallery::getVendorId, vendorId);

        Integer galleryId = dto.getGalleryId();
        // 如果指定了 galleryId，按 parent_id 过滤
        if (galleryId != null && galleryId >= 0) {
            queryWrapper.eq(Gallery::getParentId, galleryId);
        }

        // 排序
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());
        return queryWrapper;
    }

    /**
     * 获取相册详情
     *
     * @param id 相册 ID
     * @return 相册详情 DTO
     */
    @Override
    public GalleryDTO detail(Integer id) {
        // 如果 ID 小于 1，返回空
        if (id < 1) {
            return null;
        }
        // 获取相册实体并转换为 DTO
        Gallery gallery = this.getById(id);
        // 返回相册详情
        return convertToDTO(gallery);
    }

    /**
     * 创建相册
     * @param dto 相册 DTO
     * @return 是否创建成功
     */
    @Override
    public boolean create(GalleryDTO dto) {
        // 将 DTO 转换为实体并保存
        Gallery gallery = convertToBean(dto);
        Integer vendorId = HeaderUtils.getVendorId();
        gallery.setVendorId(vendorId);
        return this.save(gallery);
    }

    /**
     * 更新相册
     * @param dto 相册 DTO
     * @return 是否更新成功
     */
    @Override
    public boolean update(GalleryUpdateDTO dto) {
        // 构建更新条件
        LambdaUpdateWrapper<Gallery> updateWrapper = buildUpdateWrapper(dto);
        // 将 DTO 转换为实体并执行更新
        Gallery gallery = convertToBean(dto);
        return this.update(gallery, updateWrapper);
    }

    /**
     * 构建更新条件（根据 ID）
     * @param dto 相册 DTO
     * @return LambdaUpdateWrapper
     */
    private LambdaUpdateWrapper<Gallery> buildUpdateWrapper(GalleryUpdateDTO dto) {
        LambdaUpdateWrapper<Gallery> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Gallery::getGalleryId, dto.getId());
        return updateWrapper;
    }

    /**
     * 获取指定父级 ID 的所有子相册
     * @param pid 父级相册 ID
     * @return 子相册 DTO 列表
     */
    @Override
    public List<GalleryDTO> getGalleryByPid(Integer pid) {
        LambdaQueryWrapper<Gallery> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Gallery::getParentId, pid);
        List<Gallery> galleryList = this.list(queryWrapper);
        // 将实体列表转换为 DTO 列表
        return galleryList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 将 Gallery 实体转换为 DTO
     * @param gallery Gallery 实体
     * @return GalleryDTO
     */
    private GalleryDTO convertToDTO(Gallery gallery) {
        GalleryDTO dto = new GalleryDTO();
        BeanUtils.copyProperties(gallery, dto);
        return dto;
    }

    /**
     * 将 GalleryDTO 转换为实体
     * @param dto GalleryDTO
     * @return Gallery 实体
     */
    private Gallery convertToBean(GalleryDTO dto) {
        Gallery gallery = new Gallery();
        BeanUtils.copyProperties(dto, gallery);
        gallery.setShopId(getShopId());
        return gallery;
    }
}
