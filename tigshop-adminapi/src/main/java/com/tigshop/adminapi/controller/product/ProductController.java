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

package com.tigshop.adminapi.controller.product;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldProductDTO;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.param.product.ProductEditParam;
import com.tigshop.bean.param.product.ProductSaveParam;
import com.tigshop.bean.param.product.VendorProductImportParam;
import com.tigshop.bean.vo.product.ProductConfigVO;
import com.tigshop.bean.vo.product.ProductShippingTplListVO;
import com.tigshop.common.properties.TigshopProperties;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.authority.AdminLogService;
import com.tigshop.service.product.ProductEsService;
import com.tigshop.service.product.ProductService;
import com.tigshop.service.setting.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.PRODUCT_FIELDS;

/**
 * 商品控制器
 *
 * @author Tigshop团队
 * @create 2024年11月21日 14:30
 */
@RestController
@RequestMapping("/adminapi/product/product")
@Tag(name = "商品")
@PreAuthorize("@pms.hasPermission('productManage')")
@Validated
public class ProductController {
    @Resource
    private ProductService productService;

    @Resource
    private AdminLogService adminLogService;

    @Resource
    private ConfigService configService;
    @Resource
    private TigshopProperties tigshopProperties;

    @Resource
    private ProductEsService productEsService;

    @GetMapping("/list")
    @Operation(summary = "商品列表")
    public Page<ProductListResDTO> list(ProductListDTO listDTO) {
        Integer headerShopId = HeaderUtils.getShopId();
        if (headerShopId != null && headerShopId > 0) {
            listDTO.setShopId(headerShopId);
        }
        return productService.list(listDTO);
    }

    @GetMapping("/getWaitingCheckedCount")
    @Operation(summary = "待审核商品数量")
    public Long getWaitingCheckedCount() {
        return productService.getWaitingCheckedCount();
    }

    @GetMapping("/detail")
    @Operation(summary = "商品详情")
    public ProductDetailDTO detail(@RequestParam Integer id) {
        return productService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建商品")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public String create(@Valid @RequestBody ProductSaveParam param) {
        Integer headerShopId = HeaderUtils.getShopId();
        if (headerShopId != null && headerShopId > 0) {
            param.setShopId(headerShopId);
        }
        //新增日志
        adminLogService.createByLogInfo(StrUtil.format("创建商品:{}", param.getProductName()));
        return productService.create(param);
    }

    @PostMapping("/update")
    @Operation(summary = "更新商品")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public String update(@RequestBody @Validated ProductEditParam param) {
        Integer headerShopId = HeaderUtils.getShopId();
        if (headerShopId != null && headerShopId > 0) {
            param.setShopId(headerShopId);
        }
        boolean update = productService.update(param);
        if (update){
            adminLogService.createByLogInfo(StrUtil.format("更新商品:{}", param.getProductName()));
            Integer shopId = HeaderUtils.getShopId();
            if (shopId != null && shopId > 0) {
                String shopProductNeedCheck = configService.getConfigByCode("shopProductNeedCheck").getBizVal();
                if (tigshopProperties.getIsO2o() == 1 && productService.getById(param.getProductId()).getShopId() == 0
                        && AdminTypeEnum.fromCode(HeaderUtils.getAdminType()) != AdminTypeEnum.ADMIN) {
                    return "商品管理更新成功";
                }
                if ("1".equals(shopProductNeedCheck)) {
                    return "商品已修改，等待管理员审核";
                }
            }
            return "商品管理更新成功";
        }else{
            return "商品管理更新失败";
        }
    }

    @Operation(summary = "商品配置")
    @GetMapping("/config")
    public ProductConfigVO config(@RequestParam(required = false) Integer shopId) {
        return productService.config(shopId);
    }

    @GetMapping("shippingTplList")
    @Operation(summary = "运费模板")
    public List<ProductShippingTplListVO> shippingTplList() {
        return productService.getShippingTplListByShopId();
    }

    @PostMapping("getParticiple")
    @Operation(summary = "更新关键词")
    public String getParticiple(@RequestBody ProductKeyDTO dto) {
        return productService.getParticiple(dto);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public String updateField(@RequestBody UpdateFieldProductDTO updateField) {
        return productService.updateFieldProduct(updateField, PRODUCT_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(description = "批量操作", summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public void batchOperation(@RequestBody @Validated ProductBatchOperationDTO dto) {
        productService.batchOperation(dto);
    }

    @PostMapping("/recycle")
    @Operation(description = "产品回收", summary = "产品回收")
    public boolean recycle(@RequestBody @Validated ProductRecycleDTO dto) {
        return productService.recycle(dto);
    }

    @PostMapping("/copy")
    @Operation(description = "产品复制", summary = "产品复制")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public String copy(@RequestBody @Validated ProductCopyDTO dto) {
        return productService.copy(dto);
    }

    @PostMapping("/audit")
    @Operation(description = "产品审核", summary = "产品审核")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public boolean audit(@RequestBody @Validated ProductAuditDTO dto) {
        return productService.audit(dto);
    }

    @PostMapping("/auditAgain")
    @Operation(description = "产品再次审核", summary = "产品再次审核")
    @PreAuthorize("@pms.hasPermission('productModifyManage')")
    public boolean auditAgain(@RequestBody @Validated ProductAuditDTO dto) {
        return productService.auditAgain(dto.getId());
    }

    @Operation(description = "供应商商品导入", summary = "供应商商品导入")
    @PostMapping("/vendorProductImport")
    public void vendorProductImport(@RequestBody @Validated VendorProductImportParam param) {
        productService.vendorProductImport(param);
    }

    @Operation(description = "获取供应商最大价格", summary = "获取供应商最大价格")
    @GetMapping("/getVendorMaxPrice")
    public BigDecimal getVendorMaxPrice(@RequestParam("vendorProductSkuId") Integer vendorProductSkuId) {
        return productService.getVendorMaxPrice(vendorProductSkuId);
    }

    @PostMapping("/initAllToEs")
    @Operation(description = "初始化所有商品到ES", summary = "初始化所有商品到ES")
    public void initAllToEs() {
        productEsService.syncAllProductsStream(1000);
    }

}
