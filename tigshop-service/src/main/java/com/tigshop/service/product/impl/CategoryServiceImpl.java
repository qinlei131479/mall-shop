// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.service.product.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.product.CategoryDTO;
import com.tigshop.bean.dto.product.CategoryListDTO;
import com.tigshop.bean.dto.product.MoveCatDTO;
import com.tigshop.bean.model.product.Category;
import com.tigshop.bean.model.product.Product;
import com.tigshop.common.constant.Constants;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.PageUtil;
import com.tigshop.common.utils.RedisCache;
import com.tigshop.common.utils.ThreadLocalUtil;
import com.tigshop.core.TranslatePackageImpl;
import com.tigshop.mapper.product.CategoryMapper;
import com.tigshop.mapper.product.ProductMapper;
import com.tigshop.service.common.impl.BaseServiceImpl;
import com.tigshop.service.product.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 商品分类接口实现类
 *
 * @author Tigshop团队
 * @create 2024年10月31日 17:00
 */
@Service
public class CategoryServiceImpl extends BaseServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;
    private final TranslatePackageImpl translatePackage;
    @Resource
    private RedisCache redisCache;

    public CategoryServiceImpl(ProductMapper productMapper, TranslatePackageImpl translatePackage) {
        super();
        this.productMapper = productMapper;
        this.translatePackage = translatePackage;
    }

    @Override
    public Page<CategoryDTO> list(CategoryListDTO dto) {
        // 分页参数
        Page<Category> page = new Page<>(dto.getPage(), dto.getSize());

        // 创建查询构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        // 获取parentId，并检查是否为空。
        Integer parentId = dto.getParentId();

        if (parentId != null && parentId >= 0) {
            queryWrapper.eq(Category::getParentId, parentId);
        }

        // 排序字段
        buildSortOrder(page, dto.getSortField(), dto.getSortOrder());

        // 分页查询
        Page<Category> categoryPage = this.page(page, queryWrapper);

        // 转换记录为 DTO
        List<CategoryDTO> recordsDTO = categoryPage.getRecords().stream()
                .map(this::convertToDTO)
                .toList();

        return PageUtil.convertPage(categoryPage, recordsDTO);
    }

    /**
     * 将 Category 对象转换为 CategoryDTO 对象
     *
     * @param category Category 对象
     * @return CategoryDTO
     */
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        dto.setHasChildren(getCountById(category.getCategoryId()));
        return dto;
    }

    /**
     * 根据分类 ID 获取分类数量
     *
     * @param categoryId 分类 ID
     * @return Long
     */
    public Long getCountById(Integer categoryId) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, categoryId);
        return this.count(queryWrapper);
    }

    /**
     * 根据 parentId 查询父级分类名称
     *
     * @param parentId 父级分类 ID
     * @return String
     */
    @Override
    public String getParentName(Integer parentId) {
        return Optional.ofNullable(this.getById(parentId))
                .map(Category::getCategoryName)
                .orElse(null);
    }

    @Override
    public CategoryDTO detail(Integer id) {
        Category category = this.getById(id);
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(category, dto);
        return dto;
    }

    @Override
    public void create(CategoryDTO dto) {
        Category category = convertToEntity(dto);
        save(category);

        // 清除缓存 多语言版本
        Collection<String> defaultKeys = redisCache.keys("categoryTree::category::*::" + 0);
        redisCache.deleteObject(defaultKeys);
        // 清除缓存 非语言版本
        redisCache.deleteObject("categoryTree::category::" + 0);
    }

    @Override
    public void update(CategoryDTO dto) {
        // 清除缓存 多语言版本
        Collection<String> keys = redisCache.keys("categoryTree::category::*::" + dto.getCategoryId());
        Collection<String> defaultKeys = redisCache.keys("categoryTree::category::*::" + 0);
        if (keys != null) {
            redisCache.deleteObject(keys);
            redisCache.deleteObject(defaultKeys);
        }
        // 清除缓存 非语言版本
        redisCache.deleteObject("categoryTree::category::" + dto.getCategoryId());
        redisCache.deleteObject("categoryTree::category::" + 0);
        updateById(convertToEntity(dto));
    }

    @Override
    public boolean updateField(UpdateFieldDTO updateFieldDTO, String[] allowFields) {

        // 清除缓存 多语言版本
        Collection<String> keys = redisCache.keys("categoryTree::category::*::" + updateFieldDTO.getId());
        Collection<String> defaultKeys = redisCache.keys("categoryTree::category::*::" + 0);
        if (keys != null) {
            redisCache.deleteObject(keys);
            redisCache.deleteObject(defaultKeys);
        }
        // 清除缓存 非语言版本
        redisCache.deleteObject("categoryTree::category::" + updateFieldDTO.getId());
        redisCache.deleteObject("categoryTree::category::" + 0);

        return super.updateField(updateFieldDTO, allowFields);
    }

    /**
     * 转换为实体对象
     *
     * @param dto 数据传输对象
     * @return Category
     */
    private Category convertToEntity(CategoryDTO dto) {
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        return category;
    }

    @Override
    public List<Tree<Integer>> getAllCategory() {
        List<Category> categories = this.lambdaQuery().eq(Category::getIsShow, Constants.YES).list();

        return convertTreeList(categories);
    }

    @Override
    public List<Tree<Integer>> getChildrenCategoryTreeById(Integer categoryId) {
        String key;
        if (StrUtil.isNotBlank(ThreadLocalUtil.getValue())) {
            key = "categoryTree::category::" + ThreadLocalUtil.getValue() + "::" + categoryId;
        } else {
            key = "categoryTree::category::" + categoryId;
        }

        List<Tree<Integer>> cacheObject = redisCache.getCacheObject(key);
        if (cacheObject != null) {
            return cacheObject;
        }

        // 获取所有分类的树结构
        List<Tree<Integer>> trees = convertTreeList(this.lambdaQuery().eq(Category::getIsShow, Constants.YES).list());
        if (categoryId == 0) {
            redisCache.setCacheObject(key, trees);
            return trees;
        }
        // 打印SQL执行时间
        for (Tree<Integer> tree : trees) {
            // 如果当前分类 ID 与传入的 ID 相等，则返回该分类的子分类
            if (tree.getId().equals(categoryId)) {
                redisCache.setCacheObject(key, tree.getChildren());
                if (tree.getChildren() == null) {
                    return List.of();
                }
                return tree.getChildren();
            }
        }
        // 如果没有找到匹配的分类，则返回null
        redisCache.setCacheObject(key, null);
        return List.of();
    }

    /**
     * 转换为树形结构
     *
     * @param categoryList 分类列表
     * @return List<Tree < String>>
     */
    public List<Tree<Integer>> convertTreeList(List<Category> categoryList) {
        if (categoryList.isEmpty()) {
            return Collections.emptyList();
        }

        TreeNodeConfig config = new TreeNodeConfig();
        config.setIdKey("categoryId");
        config.setParentIdKey("parentId");
        config.setNameKey("categoryName");
        config.setWeightKey("sortOrder");
        config.setDeep(4);

        return TreeUtil.build(categoryList, 0, config, (object, treeNode) -> {
            treeNode.setId(object.getCategoryId());
            treeNode.setParentId(object.getParentId());
            treeNode.setName(translatePackage.translate(object.getCategoryName()));
            treeNode.setWeight(object.getSortOrder());
            treeNode.putExtra("isShow", object.getIsShow());
            treeNode.putExtra("categoryPic", object.getCategoryPic());
        });
    }

    @Override
    public List<Category> getHotCategory() {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getIsHot, 1)
                .orderByDesc(Category::getSortOrder)
                .last("limit 20");
        return this.list(queryWrapper);
    }

    @Override
    public List<Integer> getCategoryAllChildIds(Integer categoryId) {
        List<Category> categoryList = new ArrayList<>();
        Integer id = categoryId;
        while (id != 0) {
            // 先查出父级是当前id的分类
            Category category = this.getById(id);
            if (category != null) {
                id = category.getParentId();
                categoryList.add(category);
            } else {
                id = 0;
            }
            if (categoryList.size() > 4) {
                break;
            }
        }
        List<Integer> categoryIdList = categoryList.stream().map(Category::getCategoryId).toList();
        if (categoryIdList.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = new ArrayList<>();
        //包含自身
        ids.add(categoryId);
        List<Tree<Integer>> childrenCategoryTreeById = getChildrenCategoryTreeById(categoryIdList.getLast());
        if (CollUtil.isNotEmpty(childrenCategoryTreeById)) {
            if (!categoryId.equals(categoryIdList.getLast())) {
                childrenCategoryTreeById = childrenCategoryTreeById.stream().filter(treeNode -> treeNode.get("categoryId").equals(categoryId)).toList();
            }
            for (Tree<Integer> treeNode : childrenCategoryTreeById) {
                collectCategoryIds(treeNode, ids, 0);
            }
        }
        return ids.stream().distinct().toList();
    }

    /**
     * 递归收集 categoryId
     */
    private static void collectCategoryIds(Tree<Integer> treeNode, List<Integer> categoryIds, int depth) {
        // 递归终止条件
        if (depth > 4) {
            return;
        }
        categoryIds.add((Integer) treeNode.get("categoryId"));
        List<Tree<Integer>> children = treeNode.getChildren();
        if (children != null && !children.isEmpty()) {
            for (Tree<Integer> child : children) {
                collectCategoryIds(child, categoryIds, depth + 1);
            }
        }
    }

    @Override
    public List<Category> getCategoryByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Category::getCategoryId, ids);
        return this.list(queryWrapper);
    }

    @Override
    public boolean moveCat(MoveCatDTO dto) {
        if (dto.getId() == null || dto.getTargetCatId() == null) {
            throw new GlobalException("参数错误");
        }
        productMapper.update(new Product(), new LambdaUpdateWrapper<Product>()
                .eq(Product::getCategoryId, dto.getId())
                .set(Product::getCategoryId, dto.getTargetCatId()));
        return true;
    }

    @Override
    public Integer getCategoryIds(String[] categoryNames, Integer isAutoCat) {
        int categoryId = 0;
        int parentId = 0;
        for (String categoryName : categoryNames) {
            Category category = this.lambdaQuery().eq(Category::getCategoryName, categoryName).one();
            if (category == null && isAutoCat == 1) {
                category = Category.builder()
                        .categoryName(categoryName)
                        .parentId(parentId)
                        .build();
                this.save(category);
                categoryId = category.getCategoryId();
                parentId = category.getCategoryId();
            } else {
                categoryId = category == null ? 0 : category.getCategoryId();
            }
        }

        return categoryId;
    }

    @Override
    public List<Category> getCategoryByParentId(Integer parentId, int size) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getParentId, parentId)
                .eq(Category::getIsShow, 1)
                .orderByAsc(Category::getSortOrder)
                .last("limit " + size);
        return this.list(queryWrapper);
    }


    // 把整个树 flatten 成 Map，便于通过 ID 查找节点
    private static void buildTreeMap(Tree<Integer> tree, Map<Integer, Tree<Integer>> map) {
        map.put(tree.getId(), tree);
        if (tree.getChildren() != null) {
            for (Tree<Integer> child : tree.getChildren()) {
                buildTreeMap(child, map);
            }
        }
    }

    private static void collectAllIdsSafe(Tree<Integer> node, List<Integer> list, Set<Integer> visited) {
        if (!visited.add(node.getId())) {
            return; // 已经访问过，说明有环，终止
        }
        list.add(node.getId());
        if (node.getChildren() != null) {
            for (Tree<Integer> child : node.getChildren()) {
                collectAllIdsSafe(child, list, visited);
            }
        }
    }

    @Override
    public List<Integer> getRelatedCategoryIds(Integer categoryId) {
        List<Tree<Integer>> trees = getAllCategory();
        // 构建一个 ID -> Tree 映射，方便查找
        Map<Integer, Tree<Integer>> treeMap = new HashMap<>();
        for (Tree<Integer> tree : trees) {
            buildTreeMap(tree, treeMap);
        }

        // 1. 向上找到祖先节点
        Tree<Integer> current = treeMap.get(categoryId);
        if (current == null) return new ArrayList<>();

        Set<Integer> visitedUp = new HashSet<>();
        Tree<Integer> ancestor = current;
        while (ancestor.getParentId() != null && !ancestor.getParentId().equals(0)) {
            if (!visitedUp.add(ancestor.getId())) {
                // 出现环，强制终止
                break;
            }
            Tree<Integer> parent = treeMap.get(ancestor.getParentId());
            if (parent == null) break;
            ancestor = parent;
        }

        // 2. 从祖先节点开始向下递归收集所有子节点的 ID
        List<Integer> result = new ArrayList<>();
        collectAllIdsSafe(ancestor, result, new HashSet<>());

        return result;
    }

}
