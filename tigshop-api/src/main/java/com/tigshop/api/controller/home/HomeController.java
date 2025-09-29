// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.api.controller.home;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.promotion.coupon.CouponPageQuery;
import com.tigshop.bean.dto.setting.ConfigCustomerDTO;
import com.tigshop.bean.model.setting.FriendLinks;
import com.tigshop.bean.vo.decorate.DecorateDiscreteVO;
import com.tigshop.bean.vo.decorate.DecorateModuleListVO;
import com.tigshop.bean.vo.promotion.CouponClientVO;
import com.tigshop.bean.vo.promotion.SeckillListVO;
import com.tigshop.service.decorate.DecorateDiscreteService;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.promotion.CouponService;
import com.tigshop.service.promotion.SeckillService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.FriendLinksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页控制器
 *
 * @author Tigshop团队
 * @create 2025年01月20日 15:34
 */
@RestController
@RequestMapping("/api/home/home")
@Tag(name = "首页")
public class HomeController {
    @Resource
    private FriendLinksService friendLinksService;

    @Resource
    private DecorateDiscreteService decorateDiscreteService;

    @Resource
    private DecorateService decorateService;

    @Resource
    private ConfigService configService;

    @Resource
    private SeckillService seckillService;

    @Resource
    private CouponService couponService;

    @GetMapping("/friendLinks")
    @Operation(summary = "友情链接")
    public List<FriendLinks> friendLinks() {
        return friendLinksService.list();
    }

    @GetMapping("/mobileNav")
    @Operation(summary = "移动端导航")
    public DecorateDiscreteVO mobileNav(@RequestParam(value = "decorateSn", defaultValue = "mobileNav", required = false) String decorateSn) {
        return decorateDiscreteService.detail(decorateSn);
    }

    @GetMapping("/index")
    @Operation(summary = "首页")
    public DecorateModuleListVO index(@RequestParam(value = "previewId", required = false) Integer previewId) {
        DecorateModuleListVO decorate;
        if (previewId != null && previewId > 0) {
            // 预览
            decorate = decorateService.getAppPreviewDecorate(previewId);
        } else {
            // 获取首页发布版
            decorate = decorateService.getAppHomeDecorate();
        }
        return decorate;
    }

    @GetMapping("/pcIndex")
    @Operation(summary = "PC端首页")
    public DecorateModuleListVO pcIndex(@RequestParam("previewId") Integer previewId) {
        DecorateModuleListVO decorate;
        if (previewId != null && previewId > 0) {
            // 预览
            decorate = decorateService.getPcPreviewDecorate(previewId);
        } else {
            // 获取首页发布版
            decorate = decorateService.getPcHomeDecorate();
        }
        return decorate;
    }

    @GetMapping("/getCustomerServiceConfig")
    @Operation(summary = "获取前台客户配置")
    public ConfigCustomerDTO getConfigCustomer() {
        return configService.getConfigCustomer();
    }

    @GetMapping("/getSeckill")
    @Operation(summary = "获取秒杀")
    public Page<SeckillListVO> getSeckill(@RequestParam(value = "size", defaultValue = "15", required = false) Integer size,
                                          @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                                          @RequestParam(value = "unStarted", defaultValue = "0", required = false) Integer unStarted) {
        return seckillService.getSeckillProductList(size, page, unStarted);
    }

    @GetMapping("/getCoupon")
    @Operation(summary = "首页优惠券")
    public List<CouponClientVO> getCoupon(CouponPageQuery pageQuery) {
        pageQuery.setIsShow(1);
        pageQuery.setValidDate(1);
        pageQuery.setReceiveFlag(1);
        return couponService.getList(pageQuery).getRecords();
    }

    @GetMapping("/memberDecorate")
    @Operation(summary = "个人中心")
    public Object memberDecorate(@RequestParam(value = "decorateSn", defaultValue = "memberDecorate") String decorateSn) {
        return decorateDiscreteService.detail(decorateSn);
    }

    @GetMapping("/getRecommend")
    @Operation(summary = "首页")
    public Object getRecommend(@RequestParam(name = "decorateId", defaultValue = "0") int decorateId,
                               @RequestParam(name = "moduleIndex") String moduleIndex,
                               @RequestParam(name = "page", defaultValue = "1") int page,
                               @RequestParam(name = "previewId", defaultValue = "0") int previewId) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("size", 10);

        Object result = null;
        if (previewId > 0) {
            result = decorateService.getPreviewDecorateModuleData(decorateId, moduleIndex, params);
        } else {
            result = decorateService.getDecorateModuleData(decorateId, moduleIndex, params);
        }

        return result;
    }
}