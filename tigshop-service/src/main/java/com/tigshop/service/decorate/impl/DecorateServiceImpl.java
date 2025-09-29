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

package com.tigshop.service.decorate.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.tigshop.bean.dto.decorate.*;
import com.tigshop.bean.enums.decorate.DecorateType;
import com.tigshop.bean.model.decorate.Decorate;
import com.tigshop.bean.model.decorate.MobileCatNav;
import com.tigshop.bean.model.lang.Locales;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.vo.decorate.DecorateModuleListVO;
import com.tigshop.bean.vo.decorate.DecorateVO;
import com.tigshop.bean.vo.decorate.MobileCatNavVO;
import com.tigshop.common.constant.decorate.InitDecorateDataConstants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.JsonUtil;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.StringUtils;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.decorate.DecorateMapper;
import com.tigshop.mapper.lang.LocalesMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.decorate.MobileCatNavService;
import com.tigshop.service.product.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.tigshop.common.constant.ExceptionConstants.PARAM_ERROR;
import static com.tigshop.common.constant.decorate.DecorateConstants.TEMPLATE_NOT_EXIST;

/**
 * 页面管理服务实现类
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:05
 */
@Service
public class DecorateServiceImpl extends BaseServiceImpl<DecorateMapper, Decorate> implements DecorateService {
    @Resource
    private TranslatePackageImpl translatePackage;
    @Resource
    private MobileCatNavService mobileCatNavService;
    @Resource
    private CategoryService categoryService;

    @Resource
    private LocalesMapper localesMapper;

    @Override
    public Page<DecorateVO> list(DecorateListDTO listDTO) {
        // 分页
        Page<Decorate> page = new Page<>(listDTO.getPage(), listDTO.getSize());
        // 构造查询构造器
        LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Decorate::getShopId, HeaderUtils.getShopId());
        if (listDTO.getParentId() != null && listDTO.getParentId() > -1) {
            queryWrapper.eq(Decorate::getParentId, listDTO.getParentId());
        }
        // 排序字段
        buildSortOrder(page, listDTO.getSortField(), listDTO.getSortOrder());

        // 是否显示
        Integer decorateType = listDTO.getDecorateType();
        if (decorateType != null && DecorateType.isValidCode(decorateType)) {
            queryWrapper.eq(Decorate::getDecorateType, decorateType);
        }

        // 搜索关键字
        String keyword = listDTO.getKeyword();
        if (StrUtil.isNotEmpty(keyword)) {
            queryWrapper.like(Decorate::getDecorateTitle, keyword);
        }
        // 执行查询
        Page<Decorate> decoratePage = this.page(page, queryWrapper);
        // 获取查询结果
        List<Decorate> decoratePageRecords = decoratePage.getRecords();

        // 转换为VO
        List<DecorateVO> decorateVOList = decoratePageRecords.stream()
                .map(decorate -> {
                    DecorateVO decorateVO = new DecorateVO();
                    BeanUtils.copyProperties(decorate, decorateVO);

                    DateTime date = DateUtil.date(decorate.getUpdateTime() * 1000);
                    decorateVO.setUpdateTime(DateUtil.format(date, "yyyy-MM-dd HH:mm:ss"));

                    //获得parentId为自己的子集
                    List<Decorate> childList = this.list(new LambdaQueryWrapper<Decorate>().eq(Decorate::getParentId, decorate.getDecorateId()));
                    if (childList != null && !childList.isEmpty()) {
                        List<DecorateVO.ChildrenVo> children = childList.stream().map(childDecorate -> {
                            DecorateVO.ChildrenVo childrenVo = new DecorateVO.ChildrenVo();
                            childrenVo.setDecorateId(childDecorate.getDecorateId());
                            childrenVo.setLocaleId(childDecorate.getLocaleId());
                            Locales locales = localesMapper.selectById(childDecorate.getLocaleId());
                            if (locales != null) {
                                childrenVo.setLanguage(locales.getLanguage());
                            }
                            return childrenVo;
                        }).toList();
                        decorateVO.setChildren(children);
                    }

                    return decorateVO;
                }).toList();
        return PageUtil.convertPage(decoratePage, decorateVOList);
    }

    @Override
    public DecorateVO detail(Integer id, Integer localeId, Integer parentId, Integer decorateType) {
        Decorate decorate;
        if (id == null || id == 0) {
            //根据父级 语言 类型查询一个
            decorate = this.getOne(new LambdaQueryWrapper<Decorate>().eq(Decorate::getParentId, parentId).eq(Decorate::getLocaleId, localeId).eq(Decorate::getDecorateType, decorateType));
        } else {
            decorate = this.getById(id);
        }

        if (decorate == null) {
            return new DecorateVO();
        }
        DecorateVO decorateVO = new DecorateVO();
        BeanUtils.copyProperties(decorate, decorateVO);
        String data = decorate.getData();
        if (StrUtil.isNotEmpty(data)) {
            decorateVO.setData(JsonUtil.fromJson(data, JSONObject.class));
        }
        String draftData = decorate.getDraftData();
        if (StrUtil.isNotEmpty(draftData)) {
            decorateVO.setDraftData(JsonUtil.fromJson(draftData, JSONObject.class));
        }
        //获得parentId为自己的子集
        List<Decorate> childList = this.list(new LambdaQueryWrapper<Decorate>().eq(Decorate::getParentId, decorate.getDecorateId()));
        if (childList != null && !childList.isEmpty()) {
            List<DecorateVO.ChildrenVo> children = childList.stream().map(childDecorate -> {
                DecorateVO.ChildrenVo childrenVo = new DecorateVO.ChildrenVo();
                childrenVo.setDecorateId(childDecorate.getDecorateId());
                childrenVo.setLocaleId(childDecorate.getLocaleId());
                Locales locales = localesMapper.selectById(childDecorate.getLocaleId());
                if (locales != null) {
                    childrenVo.setLanguage(locales.getLanguage());
                }
                return childrenVo;
            }).toList();
            decorateVO.setChildren(children);
        }
        return decorateVO;
    }

    @Override
    public boolean create(DecorateCreateDTO createDTO) {
        if (createDTO != null) {
            Decorate decorate = new Decorate();
            BeanUtils.copyProperties(createDTO, decorate);
            decorate.setShopId(getShopId());
            return this.save(decorate);
        }
        return false;
    }

    @Override
    public boolean update(DecorateUpdateDTO updateDTO) {
        // redisCache.deleteObject(APP_HOME_DECORATE);
        if (updateDTO != null) {
            if (updateDTO.getDecorateId() != null && updateDTO.getDecorateId() > 0) {
                Decorate decorate = new Decorate();
                BeanUtils.copyProperties(updateDTO, decorate);
                return this.updateById(decorate);
            } else {
                Decorate decorate = new Decorate();
                Decorate parentDecorate = this.getById(updateDTO.getParentId());
                decorate.setParentId(updateDTO.getParentId());
                decorate.setDecorateTitle(parentDecorate.getDecorateTitle());
                decorate.setDecorateType(parentDecorate.getDecorateType());
                decorate.setIsHome(0);
                decorate.setShopId(parentDecorate.getShopId());
                decorate.setLocaleId(updateDTO.getLocaleId());
                decorate.setData(updateDTO.getData());
                //创建
                return this.save(decorate);
            }

        }
        return false;
    }

    @Override
    public boolean copy(Integer id) {
        if (id != null) {
            Decorate decorate = this.getById(id);
            decorate.setDecorateId(null);
            decorate.setIsHome(0);
            decorate.setDecorateTitle(decorate.getDecorateTitle() + "-复制");
            decorate.setUpdateTime(System.currentTimeMillis() / 1000);

            return this.save(decorate);
        }
        return false;
    }

    @Override
    public boolean setHome(Integer id) {
        // redisCache.deleteObject(APP_HOME_DECORATE);
        if (id != null) {

            Decorate decorate = this.getById(id);
            //将decorate_type为同类型的is_home设置为0
            this.update(new Decorate() {{
                setIsHome(0);
            }}, new LambdaQueryWrapper<>() {{
                eq(Decorate::getDecorateType, decorate.getDecorateType());
                eq(Decorate::getShopId, HeaderUtils.getShopId());
            }});

            decorate.setIsHome(1);
            return this.updateById(decorate);
        }
        return false;
    }

    @Override
    public boolean saveDecorateToDraft(Integer id, JSONObject data, Integer parentId, Integer localeId) {

        if (id != null) {
            Decorate decorate = this.getById(id);
            decorate.setDraftData(JsonUtil.toJson(data));
            return this.updateById(decorate);
        } else {
            Decorate parentDecorate = this.getById(parentId);
            Decorate decorateOne = this.getOne(new LambdaQueryWrapper<Decorate>().eq(Decorate::getParentId, parentId).eq(Decorate::getLocaleId, localeId).eq(Decorate::getDecorateType, parentDecorate.getDecorateType()));
            if (decorateOne != null) {
                decorateOne.setDraftData(JsonUtil.toJson(data));
                return this.updateById(decorateOne);
            } else {
                Decorate decorate = new Decorate();
                decorate.setParentId(parentId);
                decorate.setDecorateTitle(parentDecorate.getDecorateTitle());
                decorate.setDecorateType(parentDecorate.getDecorateType());
                decorate.setIsHome(0);
                decorate.setShopId(parentDecorate.getShopId());
                decorate.setLocaleId(localeId);
                decorate.setDraftData(JsonUtil.toJson(data));
                return this.save(decorate);
            }
        }
    }

    @Override
    public boolean publishDecorate(Integer id, JSONObject data, Integer parentId, Integer localeId) {
        if (id != null && id > 0) {
            Decorate decorate = this.getById(id);
            decorate.setData(JsonUtil.toJson(data));
            decorate.setStatus(1);
            decorate.setUpdateTime(System.currentTimeMillis() / 1000);
            decorate.setDraftData("");
            return this.updateById(decorate);
        } else {
            Decorate parentDecorate = this.getById(parentId);

            Decorate decorateOne = this.getOne(new LambdaQueryWrapper<Decorate>()
                    .eq(Decorate::getParentId, parentId)
                    .eq(Decorate::getLocaleId, localeId)
                    .eq(Decorate::getDecorateType, parentDecorate.getDecorateType()));
            if (decorateOne != null) {
                decorateOne.setData(JsonUtil.toJson(data));
                return this.updateById(decorateOne);
            } else {
                Decorate decorate = new Decorate();
                decorate.setParentId(parentId);
                decorate.setDecorateTitle(parentDecorate.getDecorateTitle());
                decorate.setDecorateType(parentDecorate.getDecorateType());
                decorate.setIsHome(0);
                decorate.setShopId(parentDecorate.getShopId());
                decorate.setLocaleId(localeId);
                decorate.setData(JsonUtil.toJson(data));
                return this.save(decorate);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initDecorate(Integer shopId) {
        Decorate decorate = new Decorate();
        decorate.setDecorateTitle("默认模板");
        decorate.setData(InitDecorateDataConstants.DECORATE_DATA);
        decorate.setDecorateType(1);
        decorate.setIsHome(1);
        decorate.setShopId(shopId);
        decorate.setStatus(1);
        decorate.setUpdateTime(StringUtils.getCurrentTime());
        this.save(decorate);
    }

    @Override
    public JSONObject getPcIndexDecoratePageConfig(Integer id) {
        if (id != null) {
            Decorate decorate = this.getOne(new LambdaQueryWrapper<Decorate>().eq(Decorate::getDecorateId, id));
            return JsonUtil.fromJson(decorate.getData(), JSONObject.class).getJSONObject("pageModule");
        }
        Decorate decorate = this.getOne(new LambdaQueryWrapper<Decorate>()
                // 自营
                .eq(Decorate::getShopId, 0)
                .eq(Decorate::getDecorateType, DecorateType.H5.getCode())
                // 1是首页，0非首页
                .eq(Decorate::getIsHome, 1)
                // 1发布，0未发布
                .eq(Decorate::getStatus, 1));

        if (decorate == null) {
            return null;
        }
        JSONObject decorateData = JsonUtil.fromJson(decorate.getData(), JSONObject.class);
        return JsonUtil.fromJson(decorateData.get("pageModule").toString(), JSONObject.class);
    }

    @Override
    public JSONObject getShopIndexDecorateData(Integer shopId) {
        LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Decorate::getShopId, shopId);
        queryWrapper.eq(Decorate::getIsHome, 1);
        queryWrapper.eq(Decorate::getStatus, 1);
        queryWrapper.last("limit 1");
        Decorate decorate = this.getOne(queryWrapper);
        if (decorate == null) {
            throw new GlobalException(translatePackage.translate(TEMPLATE_NOT_EXIST));
        }

        JSONObject decorateData = JSONUtil.parseObj(decorate.getData());
        decorateData.set("decorateId", decorate.getDecorateId());

        return decorateData;
    }

    @Override
    public DecorateModuleListVO getAppHomeDecorate() {
        return getDecorateModule(DecorateType.H5.getCode(), true, 1);
    }

    @Override
    public DecorateModuleListVO getPcHomeDecorate() {
        return getDecorateModule(DecorateType.PC.getCode(), true, 1);
    }

    @Override
    public DecorateModuleListVO getAppPreviewDecorate(Integer decorateId) {
        return getPreviewDecorate(DecorateType.H5.getCode(), decorateId);
    }

    @Override
    public DecorateModuleListVO getPcPreviewDecorate(Integer decorateId) {
        return getPreviewDecorate(DecorateType.PC.getCode(), decorateId);
    }

    @Override
    public DecorateModuleListVO getPreviewDecorate(Integer type, Integer decorateId) {
        if (type == null || decorateId == null) {
            throw new GlobalException(translatePackage.translate(PARAM_ERROR));
        }
        LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Decorate::getDecorateType, type);
        queryWrapper.eq(Decorate::getDecorateId, decorateId);
        Decorate decorate = this.getOne(queryWrapper);
        if (decorate == null) {
            throw new GlobalException(translatePackage.translate(TEMPLATE_NOT_EXIST));
        }
        DecorateModuleListVO decorateModuleListVO = new DecorateModuleListVO();
        decorateModuleListVO.setDecorateId(decorate.getDecorateId());

        JsonNode entries = JsonUtil.fromJson(decorate.getDraftData(), JsonNode.class);
        entries = JsonUtil.convertKeysToCamelCase(entries);
        decorateModuleListVO.setModuleList(JSONUtil.parseArray(entries.get("moduleList").toString()).toList(DecorateModuleListDTO.class));
        decorateModuleListVO.getModuleList().removeIf(module -> module.getIsShow() != null && !module.getIsShow());
        if (entries.has("pageModule") && entries.get("pageModule") != null) {
            decorateModuleListVO.setPageModule(JsonUtil.fromJson(entries.get("pageModule").toString(), Map.class));
        }
        return decorateModuleListVO;
    }

    @Override
    public DecorateModuleListVO getDecorateModule(Integer type, Boolean isHome, Integer status) {

        LambdaQueryWrapper<Decorate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Decorate::getDecorateType, type)
                .eq(Decorate::getShopId, 0)
                .eq(Decorate::getIsHome, isHome)
                .eq(Decorate::getStatus, status)
                .eq(Decorate::getLocaleId, 0)
                .last("limit 1");
        Decorate decorate = this.getOne(queryWrapper);

        if (HeaderUtils.getHeaderValue("X-Locale-Code") != null) {
            Locales locales = localesMapper.selectOne(new LambdaQueryWrapper<Locales>().eq(Locales::getLocaleCode, HeaderUtils.getHeaderValue("X-Locale-Code")));
            if (locales != null && locales.getIsDefault() != 1) {
                Decorate localesDecorate = this.getOne(new LambdaQueryWrapper<Decorate>()
                        .eq(Decorate::getParentId, decorate.getDecorateId())
                        .eq(Decorate::getLocaleId, locales.getId())
                );
                if (localesDecorate != null) {
                    decorate = localesDecorate;
                }
            }
        }
        if (decorate == null) {
            throw new GlobalException(translatePackage.translate(TEMPLATE_NOT_EXIST));
        }

        JsonNode entries = JsonUtil.fromJson(decorate.getData(), JsonNode.class);
        entries = JsonUtil.convertKeysToCamelCase(entries);

        DecorateDataDTO decorateData = JsonUtil.fromJson(entries.toString(), DecorateDataDTO.class);


        //moduleList里过滤掉is_show=false的模块
        if (decorateData != null) {
            decorateData.getModuleList().removeIf(module -> module.getIsShow() != null && !module.getIsShow());
        }

        DecorateModuleListVO decorateModuleListVO = new DecorateModuleListVO();
        decorateModuleListVO.setDecorateId(decorate.getDecorateId());
        decorateModuleListVO.setModuleList(decorateData != null ? decorateData.getModuleList() : null);
        decorateModuleListVO.setPageModule(decorateData != null ? decorateData.getPageModule() : null);
        for (DecorateModuleListDTO decorateModuleListDTO : decorateModuleListVO.getModuleList()) {
            if ("catNav".equals(decorateModuleListDTO.getType())) {
                JSONObject module = JSONUtil.parseObj(decorateModuleListDTO.getModule());
                List<MobileCatNav> list = mobileCatNavService.list(new LambdaQueryWrapper<MobileCatNav>()
                        .eq(MobileCatNav::getIsShow, true)
                        .orderByAsc(MobileCatNav::getSortOrder)
                        .orderByAsc(MobileCatNav::getMobileCatNavId)
                );
                List<MobileCatNavVO> mobileCatNavVOList = list.stream()
                        .map(mobileCatNav -> {
                            MobileCatNavVO mobileCatNavVO = new MobileCatNavVO();
                            BeanUtils.copyProperties(mobileCatNav, mobileCatNavVO);
                            Category category = categoryService.getById(mobileCatNavVO.getCategoryId());
                            if (category != null) {
                                mobileCatNavVO.setCategoryName(category.getCategoryName()); // 将分类名称设置到对象中
                            }
                            return mobileCatNavVO;
                        }).toList();
                module.set("navList", mobileCatNavVOList);
                decorateModuleListDTO.setModule(module);
            }
            if ("specialOffer".equals(decorateModuleListDTO.getType())) {
                JSONObject module = JSONUtil.parseObj(decorateModuleListDTO.getModule());
                JSONObject moduleTitle = module.getJSONObject("moduleTitle");
                JSONArray dailyTime = moduleTitle.getJSONArray("dailyTime");
                List<Long> list = dailyTime.stream().map(o -> Long.parseLong(o.toString())).toList();
                moduleTitle.set("dailyTime", list);
                module.set("moduleTitle", moduleTitle);
                decorateModuleListDTO.setModule(module);
            }
        }
        return decorateModuleListVO;
    }

    @Override
    public JSONObject loadDraftData(Integer id) {
        DecorateVO one = detail(id, null, 0, 0);
        if (one == null) {
            throw new GlobalException("装修不存在");
        }
        return one.getDraftData();
    }

    @Override
    public Map<String, Object> getPreviewDecorateModuleData(int decorateId, String moduleIndex, Map<String, Object> params) {
        Decorate decorate = getById(decorateId);
        if (decorate == null) {
            throw new GlobalException("模板不存在");
        }
        String draftData = decorate.getDraftData();
        JSONObject draftDataJson = JSONUtil.parseObj(draftData);
        JSONArray moduleList = draftDataJson.getJSONArray("moduleList");
        for (int i = 0; i < moduleList.size(); i++) {
            if (Objects.equals(moduleList.getJSONObject(i).getStr("moduleIndex"), moduleIndex)) {
                List<Integer> list = moduleList.getJSONObject(i).getJSONObject("module").getJSONArray("catIds").toList(Integer.class);
                return Map.of("catIds", list, "catList", formatData(moduleList.getJSONObject(i).getStr("type"), list, params, decorate));
            }
        }
        return Map.of();
    }

    @Override
    public Map<String, Object> getDecorateModuleData(int decorateId, String moduleIndex, Map<String, Object> params) {
        Decorate decorate = getById(decorateId);
        if (decorate == null) {
            throw new GlobalException("模板不存在");
        }
        String data = decorate.getData();
        JSONObject dataJson = JSONUtil.parseObj(data);
        JSONArray moduleList = dataJson.getJSONArray("moduleList");
        for (int i = 0; i < moduleList.size(); i++) {
            if (Objects.equals(moduleList.getJSONObject(i).getStr("moduleIndex"), moduleIndex)) {
                List<Integer> list = moduleList.getJSONObject(i).getJSONObject("module").getJSONArray("catIds").toList(Integer.class);
                return Map.of("catIds", list, "catList", formatData(moduleList.getJSONObject(i).getStr("type"), list, params, decorate));
            }
        }
        return Map.of();
    }


    public Object formatData(String type, List<Integer> module, Map<String, Object> params, Decorate decorate) {
        if (ObjectUtil.equals(type, "pcCatWide")) {
            return categoryService.getCategoryByIds(module);
        }
        return List.of();
    }
}
