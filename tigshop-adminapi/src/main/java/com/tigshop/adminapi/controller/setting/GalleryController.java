// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.adminapi.controller.setting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.setting.GalleryDTO;
import com.tigshop.bean.dto.setting.GalleryListDTO;
import com.tigshop.bean.dto.setting.GalleryUpdateDTO;
import com.tigshop.service.setting.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 相册控制器
 *
 * @author Jayce
 * @create 2024年11月12日 16:35
 */
@RestController
@RequestMapping("/adminapi/setting/gallery")
@Tag(name = "相册管理", description = "相册管理")
@PreAuthorize("@pms.hasPermission('galleryManage')")
@Validated
public class GalleryController {
    @Resource
    private GalleryService galleryService;

    @GetMapping("/list")
    @Operation(summary = "相册列表")
    public Page<GalleryDTO> list(GalleryListDTO dto) {
        return galleryService.list(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "相册详情")
    public GalleryDTO detail(@RequestParam Integer id) {
        return galleryService.detail(id);
    }

    @PostMapping("/del")
    @Operation(summary = "删除相册")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage')")
    public void del(@RequestBody OperateDTO operate) {
        galleryService.del(operate.getId());
    }

    @PostMapping("/create")
    @Operation(summary = "添加相册")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage')")
    public void create(@RequestBody GalleryDTO dto) {
        galleryService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "修改相册")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage')")
    public void update(@RequestBody GalleryUpdateDTO dto) {
        galleryService.update(dto);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作相册")
    @PreAuthorize("@pms.hasPermission('galleryModifyManage')")
    public void batch(@RequestBody BatchDTO dto) {
        galleryService.batch(dto);
    }
}
