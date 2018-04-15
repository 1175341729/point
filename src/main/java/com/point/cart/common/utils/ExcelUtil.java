package com.point.cart.common.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * excel导出
 */
public class ExcelUtil {
    private static Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    /**
     * 导出文件
     *
     * @param list   数据集合
     * @param sheetName sheet名称
     * @param title  title
     * @param fields 类属性
     * @param <T>    泛型
     * @return 响应
     */
    public static <T> String exportExcel(List<T> list, String sheetName, String[] title, String[] fields) {
        String path = System.getProperty("user.dir");
        log.info("文件地址：{}", path);
        String filePath = path + File.separator + System.currentTimeMillis() + ".xls";
        FileOutputStream fos = null;
        File file = null;
        try {
            file = new File(filePath);
            fos = new FileOutputStream(file);
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(sheetName);
            HSSFRow row = sheet.createRow(0);
            HSSFCell cell = null;
            // 设置title
            setTitleCellValue(createStyle(wb), row, title);
            // 获取对象
            if (list != null && list.size() > 0 && fields != null && fields.length > 0) {
                for (int i = 0, listLength = list.size(); i < listLength; i++) {
                    T entity = list.get(i);
                    Class<?> clazz = entity.getClass();
                    row = sheet.createRow(i + 1);
                    // 设置cell值
                    for (int j = 0, fieldLength = fields.length; j < fieldLength; j++) {
                        try {
                            cell = row.createCell(j);
                            String field = fields[j];
                            if (StringUtils.isNotBlank(field)) {
                                Method method = clazz.getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1));
                                Object value = method.invoke(entity);
                                cell.setCellValue(value == null ? "" : value.toString());
                            }
                        } catch (Exception e) {
                            log.error("属性不存在！");
                        }
                    }
                }
            }
            wb.write(fos);
            log.info("导出成功");
            // 上传OSS
            OssUtil.putFile(file.getName(), file.getPath());
            log.info("上传OSS成功！");
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (file != null) {
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "/" + file.getName();
    }

    /**
     * 单元格设置值
     *
     * @param row    行
     * @param values 值
     */
    private static void setTitleCellValue(CellStyle cellStyle, HSSFRow row, String... values) {
        if (values != null && values.length > 0) {
            for (int i = 0, length = values.length; i < length; i++) {
                String value = values[i];
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(value);
                if (cellStyle != null) {
                    cell.setCellStyle(cellStyle);
                }
            }
        }
    }

    /**
     * 创建单元格样式
     *
     * @param workbook 工作簿
     * @return 响应
     */
    private static CellStyle createStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setColor(HSSFFont.COLOR_RED);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        return cellStyle;
    }
}
