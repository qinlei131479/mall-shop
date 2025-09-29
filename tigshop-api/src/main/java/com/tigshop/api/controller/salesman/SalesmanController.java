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
package com.tigshop.api.controller.salesman;

import com.tigshop.bean.vo.salesman.SalesmanUserDetailVO;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.service.salesman.SalesmanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分销员控制器
 *
 * @author kidd
 * @create 2025/6/23
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/salesman/salesman")
@Tag(name = "分销员控制器", description = "分销员控制器")
public class SalesmanController {

    private final SalesmanService salesmanService;

    @Operation(summary = "获取详情")
    @GetMapping("/detail")
    public SalesmanUserDetailVO detail() {
        return salesmanService.detailForApi();
    }

    @GetMapping("/userInfo")
    @Operation(summary = "获取分销员用户基础信息")
    public UserBaseVO getUserInfo(@RequestParam(name = "salesmanId") Integer salesmanId) {
        return salesmanService.getSalesman(salesmanId).getBaseUserInfo();
    }

}
