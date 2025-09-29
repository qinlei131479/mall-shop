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
package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.promotion.WechatLiveListDTO;
import com.tigshop.bean.dto.promotion.WechatLiveUpdateDTO;
import com.tigshop.bean.vo.promotion.WechatLiveVO;
import com.tigshop.service.promotion.WechatLiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 微信直播控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/wechatLive")
@Tag(name = "微信直播", description = "微信直播功能")
@PreAuthorize("@pms.hasPermission('wechatLiveManage')")
@Validated
public class WechatLiveController {
    @Resource
    private WechatLiveService wechatLiveService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<WechatLiveVO> list(WechatLiveListDTO listDTO) {
        return wechatLiveService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public WechatLiveVO detail(@RequestParam Integer id) {
        return wechatLiveService.detail(id);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('wechatLiveModifyManage')")
    public void update(@Valid @RequestBody WechatLiveUpdateDTO updateDTO) {
        wechatLiveService.update(updateDTO);
    }

    @GetMapping("/refresh")
    @Operation(summary = "刷新直播间信息")
    public void refresh() {
        wechatLiveService.refresh();
    }
}
