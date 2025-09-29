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

package com.tigshop.service.setting.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.setting.RegionCreateDTO;
import com.tigshop.bean.dto.setting.RegionListDTO;
import com.tigshop.bean.enums.setting.SettingsEnum;
import com.tigshop.bean.model.setting.ConfigPO;
import com.tigshop.bean.model.setting.Region;
import com.tigshop.bean.vo.setting.RegionListVO;
import com.tigshop.bean.vo.sys.CityRegionVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.mapper.setting.RegionMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.setting.ConfigService;
import com.tigshop.service.setting.RegionService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.tigshop.common.constant.Constants.CITY_PINYING_LIST;
import static com.tigshop.common.constant.ExceptionConstants.PARAM_ERROR;
import static com.tigshop.common.utils.Ip2RegionUtil.getRegionByIp;

/**
 * 地区列表服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月11日 14:39
 */
@Service
public class RegionServiceImpl extends BaseServiceImpl<RegionMapper, Region> implements RegionService {

    @Resource
    private ConfigService configService;
    @Resource
    private RedisCache redisCache;

    @Override
    public List<Region> getRegionByParentId(Integer parentId) {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        if (parentId == null) {
            throw new GlobalException(PARAM_ERROR);
        }
        queryWrapper.eq(Region::getParentId, parentId);
        return this.list(queryWrapper);
    }

    @Override
    public List<String> getRegionNamesByRegionIds(List<Integer> regionIds) {
        if (regionIds == null || regionIds.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Region::getRegionId, regionIds);
        List<Region> regions = this.list(queryWrapper);
        return regions.stream().map(Region::getRegionName).toList();
    }

    @Override
    public List<Tree<Integer>> getAllRegionTree() {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.gt(Region::getLevel, 0);
        List<Region> regionsList = this.list(queryWrapper);
        List<Tree<Integer>> regionTree = getRegionTree(regionsList);
        return regionTree;
    }

    /**
     * 获取地区列表<树形结构>
     *
     * @param regionList 地区列表
     * @return List<Tree < Integer>>
     */
    private List<Tree<Integer>> getRegionTree(List<Region> regionList) {
        if (regionList.isEmpty()) {
            return Collections.emptyList();
        }

        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("regionId");
        config.setParentIdKey("parentId");
        config.setNameKey("regionName");
        config.setDeep(8);

        return TreeUtil.build(regionList, 0, config, (object, treeNode) -> {
            treeNode.putExtra("regionId", object.getRegionId());
            treeNode.putExtra("parentId", object.getParentId());
            treeNode.putExtra("regionName", object.getRegionName());
        });
    }

    @Override
    public Page<RegionListVO> list(RegionListDTO dto) {
        // 分页查询
        Page<Region> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        Integer parentId = dto.getParentId();
        if (parentId != null && parentId >= 0) {
            queryWrapper.eq(Region::getParentId, parentId);
        }
        Integer regionId = dto.getRegionId();
        if (regionId != null && regionId >= 0) {
            queryWrapper.eq(Region::getRegionId, regionId);
        }
        // 构建排序条件
        buildSortOrder(page, dto.getSortField(),
                dto.getSortOrder());
        // 构建查询条件
        Page<Region> regionPage = this.page(page, queryWrapper);
        List<Region> regions = regionPage.getRecords();
        // 将查询结果转为 VO 返回
        List<RegionListVO> regionListVOList = regions.stream().map(region -> {
            RegionListVO regionListVO = new RegionListVO();
            BeanUtils.copyProperties(region, regionListVO);
            return regionListVO;
        }).toList();
        return PageUtil.convertPage(regionPage, regionListVOList);
    }

    @Override
    public boolean create(RegionCreateDTO dto) {
        Region region = new Region();
        BeanUtils.copyProperties(dto, region);
        region.setFirstWord(PinyinUtil.getPinyin(region.getRegionName()).substring(0, 1).toUpperCase());
        return this.save(region);
    }

    @Override
    public List<List<Region>> getRegionByIds(String regionIds) {
        List<List<Region>> regionList = new ArrayList<>();
        //string转json
        ConfigPO defaultCountryCode = configService.getConfigByCode(SettingsEnum.DEFAULT_COUNTRY.getBizCode());
        Integer defaultCountry = Integer.valueOf(defaultCountryCode.getBizVal());

        List<Integer> regionIdsList;
        if (regionIds != null) {
            regionIdsList = Arrays.stream(regionIds.split(",")).map(Integer::parseInt).toList();
            Region parentRegion = this.getById(regionIdsList.getFirst());
            List<Integer> parentList = new ArrayList<>();
            if (parentRegion != null) {
                parentList.add(parentRegion.getParentId());
                parentList.addAll(regionIdsList);
                regionIdsList = parentList;
            }

        } else {
            regionIdsList = new ArrayList<>();
            regionIdsList.add(defaultCountry);
        }
        if (!regionIdsList.isEmpty()) {
            for (Integer integer : regionIdsList) {
                regionList.add(this.list(new LambdaQueryWrapper<Region>().eq(Region::getParentId, integer)));
            }
        }
        return regionList;
    }

    @Override
    public List<Region> getProvinceList() {
        LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Region::getLevel, 2)
                .select(Region::getRegionId, Region::getRegionName);
        return this.list(queryWrapper).stream().peek(region -> {
            String regionName = region.getRegionName();
            region.setRegionName(regionName.replace("省", "").replace("市", "").replace("自治区", ""));
        }).toList();
    }

    @Override
    public Region getUserRegion() {
        try {
            String region = getRegionByIp();
            region = StrUtil.isBlank(region) ? "北京" : region;
            LambdaQueryWrapper<Region> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(Region::getRegionName, region);
            return this.list(queryWrapper).getFirst();
        } catch (Exception e) {
            return this.list().getFirst();
        }
    }

    @Override
    public CityRegionVO getCityRegion() {
        Object cacheObject = redisCache.getCacheObject(CITY_PINYING_LIST);
        if (cacheObject != null) {
            return JSONUtil.toBean(cacheObject.toString(), CityRegionVO.class);
        }
        List<Region> hot = list(new LambdaQueryWrapper<Region>()
                .eq(Region::getLevel, 3)
                .eq(Region::getIsHot, 1)
        );
        List<Region> list = list(new LambdaQueryWrapper<Region>()
                .eq(Region::getLevel, 3)
                .orderByAsc(Region::getFirstWord)
                .orderByAsc(Region::getRegionId)
        );
        CityRegionVO cityRegionVO = new CityRegionVO();
        cityRegionVO.setHot(hot);
        cityRegionVO.setCity(list);
        redisCache.setCacheObject(CITY_PINYING_LIST, JSONUtil.toJsonStr(cityRegionVO));
        return cityRegionVO;
    }

    @Override
    public List<Region> getCityListByCityName(String cityName) {
        return list(new LambdaQueryWrapper<Region>()
                .like(Region::getRegionName, cityName)
                .eq(Region::getLevel, 3)
        );
    }
}
