package com.tigshop.adminapi.controller.panel;

import com.tigshop.bean.vo.authority.AuthorityVO;
import com.tigshop.bean.vo.panel.PanelIndexVO;
import com.tigshop.bean.vo.vendor.PanelVendorIndexVO;
import com.tigshop.service.authority.AuthorityService;
import com.tigshop.service.panel.SalesStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 面板控制器
 *
 * @author Tigshop团队
 * @create 2024年12月31日 14:44
 */
@RestController
@RequestMapping("/adminapi/panel/panel")
@Tag(name = "面板控制器")
public class PanelController {

    @Resource
    private SalesStatisticsService salesStatisticsService;

    @Resource
    private AuthorityService authorityService;

    @GetMapping("/index")
    @Operation(summary = "首页面板")
    @PreAuthorize("@pms.hasPermission('consoleManage')")
    public PanelIndexVO index() {
        return salesStatisticsService.getPanelIndex();
    }

    @GetMapping("/vendorIndex")
    @Operation(summary = "首页供应商面板")
    @PreAuthorize("@pms.hasPermission('consoleVendorManage')")
    public PanelVendorIndexVO vendorIndex() {
        return salesStatisticsService.getPanelVendorIndex();
    }

    @GetMapping("/searchMenu")
    @Operation(summary = "一键直达")
    @PreAuthorize("@pms.hasPermission('consoleManage')")
    public List<AuthorityVO> searchMenu(@RequestParam(value = "keyword", required = false) String keyword) {
        return authorityService.getAuthorityByName(keyword);
    }
}
