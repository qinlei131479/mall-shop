// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.o2o.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.feign.amap.GeoCityResponse;
import com.tigshop.bean.feign.amap.GeoResponse;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.vo.sys.RegionVO;
import com.tigshop.common.utils.IpUtils;
import com.tigshop.feign.AmapFeignClient;
import com.tigshop.service.o2o.MapPositioningService;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

import static com.tigshop.common.utils.AMapSignUtils.generateSignature;

/**
 * @author Tigshop团队
 * @create 2025/8/27 14:35
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MapPositioningServiceImpl implements MapPositioningService {
    private final AmapFeignClient amapFeignClient;
    private final ConfigService configService;
    private final RegionService regionService;

    @Override
    public RegionVO getLocationByIp(String ip) {
        if (ObjectUtil.isEmpty(ip)) {
            ip = IpUtils.getIpAddr();
        }
        log.error("获取定位ip: " + ip);
        String key = configService.getConfigVal(SettingsEnum.AMAP_WEB_KEY);
        String secret = configService.getConfigVal(SettingsEnum.AMAP_WEB_SECRET);
        Map<String, String> params = new TreeMap<>();
        params.put("key", key);
        params.put("ip", ip);
        String sig = null;
        if (ObjectUtil.isNotEmpty(secret)) {
            sig = generateSignature(params, secret);
        }
        String jsonStr = JSONUtil.toJsonStr(amapFeignClient.getLocationByIp(ip, key, sig));
        return getRegion(jsonStr);
    }

    @Override
    public RegionVO getLocationByLocation(String location) {
        String key = configService.getConfigVal(SettingsEnum.AMAP_WEB_KEY);
        String secret = configService.getConfigVal(SettingsEnum.AMAP_WEB_SECRET);
        Map<String, String> params = new TreeMap<>();
        params.put("key", key);
        params.put("location", location);
        String sig = null;
        if (ObjectUtil.isNotEmpty(secret)) {
            sig = generateSignature(params, secret);
        }
        String jsonStr = JSONUtil.toJsonStr(amapFeignClient.regeo(location, key, sig));
        return getRegion(jsonStr);
    }

    private RegionVO getRegion(String jsonStr) {
        log.error("获取定位json: " + jsonStr);
        Region returnRegion = null;
        try {
            GeoResponse geoResponse = JSONUtil.toBean(jsonStr, GeoResponse.class);
            String adcodeSafe = geoResponse.getAdcodeSafe();
            if (ObjectUtil.isNotEmpty(adcodeSafe)) {
                Region region = regionService.getOne(new LambdaQueryWrapper<Region>().eq(Region::getRegionId, adcodeSafe));
                if (region.getLevel() == 3) {
                    returnRegion = region;
                }
                if (region.getLevel() == 4) {
                    returnRegion = regionService.getOne(new LambdaQueryWrapper<Region>().eq(Region::getRegionId, region.getParentId()));
                }
            }
        } catch (Exception e) {
            log.error("获取定位失败", e);
            // 降级处理返回北京市
            returnRegion = regionService.getOne(new LambdaQueryWrapper<Region>().eq(Region::getRegionId, 110100));
        }


        // 获取经纬度
        return getRegionLocation(returnRegion);
    }

    public RegionVO getRegionLocation(Region region) {
        RegionVO regionVO = new RegionVO();
        BeanUtil.copyProperties(region, regionVO);
        String key = configService.getConfigVal(SettingsEnum.AMAP_WEB_KEY);
        String secret = configService.getConfigVal(SettingsEnum.AMAP_WEB_SECRET);
        Map<String, String> params = new TreeMap<>();
        params.put("key", key);
        params.put("keywords", region.getRegionId().toString());
        params.put("subdistrict", "0");
        String sig = null;
        if (ObjectUtil.isNotEmpty(secret)) {
            sig = generateSignature(params, secret);
        }
        String jsonStr = JSONUtil.toJsonStr(amapFeignClient.district(region.getRegionId().toString(), "0", key, sig));
        try {
            GeoCityResponse geoResponse = JSONUtil.toBean(jsonStr, GeoCityResponse.class);
            for (GeoCityResponse.District district : geoResponse.getDistricts()) {
                if (ObjectUtil.equals(district.getAdcode(), region.getRegionId().toString())) {
                    regionVO.setCentor(district.getCenter());
                }
            }
        } catch (Exception e) {
            log.error("获取定位失败", e);
        }
        return regionVO;
    }
}
