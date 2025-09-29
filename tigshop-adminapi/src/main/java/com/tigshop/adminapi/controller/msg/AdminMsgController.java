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
package com.tigshop.adminapi.controller.msg;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.msg.AdminMsgListDTO;
import com.tigshop.bean.dto.msg.AdminMsgUpdateDTO;
import com.tigshop.bean.vo.msg.AdminMsgCountVO;
import com.tigshop.bean.vo.msg.AdminMsgTypeListVO;
import com.tigshop.bean.vo.msg.AdminMsgVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.im.ImMessageService;
import com.tigshop.service.msg.AdminMsgService;
import com.tigshop.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.utils.HeaderUtils.getShopId;

/**
 * 管理员消息控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/msg/adminMsg")
@Tag(name = "管理员消息", description = "管理员消息功能")
@Validated
public class AdminMsgController {
    @Resource
    private AdminMsgService adminMsgService;
    @Resource
    private OrderService orderService;
    @Resource
    private ImMessageService imMessageService;

    @GetMapping("/msgCount")
    @Operation(summary = "获取消息数量")
    public AdminMsgCountVO getMsgCount(@RequestParam("startTime") Long startTime) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        return AdminMsgCountVO.builder()
                .imMsgCount(imMessageService.getUnreadImMessageCount(getShopId(), adminId))
                .orderCount(orderService.getNewOrderCount(startTime))
                .unreadMsgCount(adminMsgService.getUnreadAdminMsgCount())
                .build();
    }

    @GetMapping("/msgTypeArr")
    @Operation(summary = "获取消息类别")
    public List<AdminMsgTypeListVO> msgTypeArr() {
        return adminMsgService.getAdminMsgTypeList();
    }

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<AdminMsgVO> list(AdminMsgListDTO listDTO) {
        return adminMsgService.list(listDTO);
    }

    @PostMapping("/setReaded")
    @Operation(summary = "设置为单个已读")
    @PreAuthorize("@pms.hasPermission('adminMsgModifyManage')")
    public void setReaded(@Valid @RequestBody AdminMsgUpdateDTO updateDTO) {
        adminMsgService.setReaded(updateDTO.getMsgId());
    }

    @PostMapping("/setAllReaded")
    @Operation(summary = "设置全部已读")
    @PreAuthorize("@pms.hasPermission('adminMsgModifyManage')")
    public void setAllReaded() {
        adminMsgService.setAllReaded();
    }

}
