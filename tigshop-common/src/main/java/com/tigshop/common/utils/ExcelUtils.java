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

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.tigshop.common.exception.GlobalException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.tigshop.common.constant.Constants.UTF8;
import static com.tigshop.common.constant.Constants.XLSX;
import static com.tigshop.common.constant.ExceptionConstants.SERVICE_ERROR;

/**
 * excel处理工具类
 *
 * @author Tigshop团队
 * @create 2024年12月17日 10:06
 */
public class ExcelUtils<T> {
    public MultipartFile file;
    public Class<?> clazz;
    public List<T> filterResult;

    public void exportExcelWeb(HttpServletResponse response){
        String fileName = StrUtil.format("{}{}{}", "User-", StringUtils.getCurrentTime(), XLSX);
        // 设置响应头，指定返回的文件类型和文件名
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding(UTF8);
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName);
        try{
            EasyExcel.write(response.getOutputStream(), this.clazz)
                    .sheet("模板")
                    .doWrite(() -> {
                        // 分页查询数据
                        return this.filterResult;
                    });
        } catch (IOException e) {
            throw new GlobalException(SERVICE_ERROR);
        }
    }

    public void exportExcelWebByTitle(HttpServletResponse response, Set<String> includeColumnFiledNames, String fileName, String sheetName) {
        // 设置响应头，指定返回的文件类型和文件名
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding(UTF8);
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName);
        try {
            EasyExcel.write(response.getOutputStream(), this.clazz).includeColumnFieldNames(includeColumnFiledNames)
                    .sheet(sheetName)
                    .doWrite(() -> {
                        // 分页查询数据
                        return this.filterResult;
                    });
        } catch (IOException e){
            throw new GlobalException(SERVICE_ERROR);
        }
    }

    /**
     * 导出excel（文件）
     */
    public void exportExcel() {
        String fileName = StrUtil.format("{}{}{}", "User-", StringUtils.getCurrentTime(), XLSX);
        EasyExcel.write(fileName, this.clazz)
                .sheet("模板")
                .doWrite(() -> {
                    // 分页查询数据
                    return this.filterResult;
                });
    }

    /**
     * 导入excel
     * @return boolean
     */
    public List<T> importExcel() {
        try {
            InputStream inputStream = this.file.getInputStream();
            List<T> lists = new ArrayList<>();
            EasyExcel.read(inputStream, this.clazz, new PageReadListener<T>(lists::addAll))
                    .sheet()
                    .doRead();
            return lists;
        }catch (IOException e){
            throw new GlobalException(SERVICE_ERROR);
        }
    }

    public ExcelUtils(MultipartFile file, Class<?> clazz){
        this.file = file;
        this.clazz = clazz;
    }

    public ExcelUtils(List<T> filterResult, Class<?> clazz){
        this.filterResult = filterResult;
        this.clazz = clazz;
    }
}
