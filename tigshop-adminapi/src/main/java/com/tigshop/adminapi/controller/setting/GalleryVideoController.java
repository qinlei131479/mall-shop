package com.tigshop.adminapi.controller.setting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.setting.GalleryListDTO;
import com.tigshop.bean.dto.setting.GalleryVideoCreateDTO;
import com.tigshop.bean.dto.setting.GalleryVideoUpdateDTO;
import com.tigshop.bean.vo.setting.GalleryVideoListVO;
import com.tigshop.bean.vo.setting.GalleryVideoVO;
import com.tigshop.service.video.GalleryVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺资金变化控制器
 *
 * @author wzh
 */
@RestController
@RequestMapping("/adminapi/setting/galleryVideo")
@Validated
@Tag(name = "视频相册后台管理接口", description = "视频相册后台管理接口")
@PreAuthorize("@pms.hasPermission('galleryVideoManage')")
@AllArgsConstructor
public class GalleryVideoController {

    private final GalleryVideoService galleryVideoService;

    @GetMapping("/getAllCategory")
    @Operation(summary = "视频相册树形列表")
    public List<GalleryVideoListVO> treeList() {
        // 调用galleryVideoService的treeList方法，返回视频相册树形列表
        return galleryVideoService.treeList();
    }


    @GetMapping("/list")
    @Operation(summary = "视频相册列表")
    public Page<GalleryVideoVO> list(GalleryListDTO dto) {
        return galleryVideoService.list(dto);
    }

    @GetMapping("/detail")
    @Operation(summary = "视频相册详情")
    public GalleryVideoVO detail(@RequestParam Integer id) {
        return galleryVideoService.detail(id);
    }

    @PostMapping("/del")
    @Operation(summary = "删除视频相册")
    public void del(@RequestBody OperateDTO operate) {
        galleryVideoService.del(operate.getId());
    }

    @PostMapping("/create")
    @Operation(summary = "添加视频相册")
    public Integer create(@RequestBody @Valid GalleryVideoCreateDTO dto) {
        return galleryVideoService.create(dto);
    }

    @PostMapping("/update")
    @Operation(summary = "修改视频相册")
    public Integer update(@RequestBody @Valid GalleryVideoUpdateDTO dto) {
        return galleryVideoService.update(dto);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作相册")
    public void batch(@RequestBody BatchDTO dto) {
        galleryVideoService.batch(dto);
    }


}
