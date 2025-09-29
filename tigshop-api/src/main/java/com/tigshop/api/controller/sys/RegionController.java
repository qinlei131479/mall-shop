package com.tigshop.api.controller.sys;

import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.vo.sys.CityRegionVO;
import com.tigshop.bean.vo.sys.RegionVO;
import com.tigshop.service.o2o.MapPositioningService;
import com.tigshop.service.setting.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地区控制器
 *
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@RequestMapping("/api/sys/region")
@Tag(name = "地区")
@Validated
public class RegionController {

    @Resource
    private RegionService regionService;
    @Resource
    private MapPositioningService mapPositioningService;

    @GetMapping("/getRegion")
    @Operation(summary = "地区列表")
    public List<List<Region>> getRegion(@RequestParam(value = "regionIds", required = false) String regionIds) {
        return regionService.getRegionByIds(regionIds);
    }

    @GetMapping("/getProvinceList")
    @Operation(summary = "省份列表")
    public List<Region> getProvinceList() {
        return regionService.getProvinceList();
    }

    @GetMapping("/getUserRegion")
    @Operation(summary = "用户地区列表")
    public Region getUserRegion() {
        return regionService.getUserRegion();
    }

    @GetMapping("/getCityRegion")
    @Operation(summary = "用户地区列表")
    public CityRegionVO getCityRegion() {
        return regionService.getCityRegion();
    }

    @GetMapping("/getCityListByCityName")
    @Operation(summary = "用户地区列表")
    public List<Region> getCityListByCityName(String cityName) {
        return regionService.getCityListByCityName(cityName);
    }

    @GetMapping("/getLocationByIp")
    @Operation(summary = "通过ip获取城市定位")
    public RegionVO getLocationByIp(@RequestParam(value = "ip", required = false) String ip) {
        return mapPositioningService.getLocationByIp(ip);
    }

    // 地理编码（地址 -> 经纬度）
    @GetMapping("/getLocationByLocation")
    @Operation(summary = "通过gps获取城市定位")
    public RegionVO getLocationByLocation(@RequestParam("location") String location) {
        return mapPositioningService.getLocationByLocation(location);
    }

    @GetMapping("/getRegionListByRegionId")
    @Operation(summary = "获取子集通过RegionId")
    public List<Region> getRegionListByRegionId(@RequestParam(value = "regionId") Integer regionId) {
        return regionService.getRegionByParentId(regionId);
    }
}
