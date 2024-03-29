package com.iyundao.base.utils;

import com.iyundao.base.BaseEntity;
import com.iyundao.base.annotation.Excel;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @ClassName: ExcelUtils
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/6/25 15:26
 * @Description: 工具类 - poi
 * @Version: V2.0
 */
public class ExcelUtils {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0");// 格式化 number为整

    private static final DecimalFormat DECIMAL_FORMAT_PERCENT = new DecimalFormat("##.00%");//格式化分比格式，后面不足2位的用0补齐

    private static final FastDateFormat FAST_DATE_FORMAT = FastDateFormat.getInstance("yyyy/MM/dd");

    private static final DecimalFormat DECIMAL_FORMAT_NUMBER  = new DecimalFormat("0.00E000"); //格式化科学计数器

    private static final Pattern POINTS_PATTERN = Pattern.compile("0.0+_*[^/s]+"); //小数匹配

    /**
     * 模板生成
     * @return
     */
    public static HSSFWorkbook createWorkBook(Class<? extends BaseEntity> cls) {
        // 创建excel工作簿
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        // 设置列宽
        Field[] fields = cls.getDeclaredFields();
        List<Excel> excels = new ArrayList<>();
        for (Field field : fields) {
            Excel annotation = field.getAnnotation(Excel.class);
            if (annotation != null) {
                excels.add(annotation);
            }
        }
        for(int i=0;i<excels.size();i++){
            sheet.setColumnWidth((short) i, (short) (50*60));
        }

        // 创建第一行，并设置其单元格格式
        HSSFRow row = sheet.createRow((short) 0);
        row.setHeight((short)500);
        // 单元格格式(用于列名)
        HSSFCellStyle cs = wb.createCellStyle();
        HSSFFont f = wb.createFont();
        f.setFontName("宋体");
        f.setFontHeightInPoints((short) 10);
        f.setBold(true);
        cs.setFont(f);
        cs.setAlignment(HorizontalAlignment.CENTER);// 水平居中
        cs.setVerticalAlignment(VerticalAlignment.CENTER);// 垂直居中
        cs.setLocked(true);
        cs.setWrapText(true);//自动换行
        //设置列名
        int index = 0;
        for (Excel excel : excels) {
            HSSFCell cell = row.createCell(excel.sort() == 0 ? index++ : excel.sort());
            cell.setCellValue(excel.name());
            cell.setCellStyle(cs);
        }
        return wb;
    }

    /**
     * 生成下载
     * @param fileName
     * @param response
     * @throws IOException
     */
    public static void downloadWorkBook(String fileName,
                                        Class<? extends BaseEntity> cls,
                                        HttpServletResponse response) throws IOException{
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            createWorkBook(cls).write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] content = os.toByteArray();
        InputStream is = new ByteArrayInputStream(content);
        // 设置response参数
        response.reset();
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename="+ new String((fileName + ".xls").getBytes(), "iso-8859-1"));
        ServletOutputStream out = response.getOutputStream();
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(out);
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (final IOException e) {
            throw e;
        } finally {
            if (bis != null)
                bis.close();
            if (bos != null)
                bos.close();
        }
    }

    /**
     * 创建工作簿
     * @param file
     * @return
     */
    public static Workbook getWorkBook(MultipartFile file) {
        //获得文件名
        String fileName = file.getOriginalFilename();
        //创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {
            //获取excel文件的io流
            InputStream is = file.getInputStream();
            //根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if(fileName.endsWith("xls")){
                //2003
                workbook = new HSSFWorkbook(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    /**
     * 获取excel数据 将之转换成bean
     *
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> readExcel(MultipartFile file, Class<T> cls) {
        List<T> dataList = new LinkedList<T>();
        Workbook workbook = null;
        try {
            workbook = getWorkBook(file);
            Map<String, List<Field>> classMap = new HashMap<>();
            Field[] fields = cls.getDeclaredFields();
            for (Field field : fields) {
                Excel annotation = field.getAnnotation(Excel.class);
                if (annotation != null) {
                    String value = annotation.name();
                    if (!classMap.containsKey(value)) {
                        classMap.put(value, new ArrayList<>());
                    }
                    field.setAccessible(true);
                    classMap.get(value).add(field);
                }
            }
            Map<Integer, List<Field>> reflectionMap = new HashMap<Integer, List<Field>>();
            int sheetsNumber = workbook.getNumberOfSheets();
            for (int n = 0; n < sheetsNumber; n++) {
                Sheet sheet = workbook.getSheetAt(n);
                for (int j = sheet.getRow(0).getFirstCellNum(); j < sheet.getRow(0).getLastCellNum(); j++) { //首行提取注解
                    Object cellValue = getCellValue(sheet.getRow(0).getCell(j));
                    if (classMap.containsKey(cellValue)) {
                        reflectionMap.put(j, classMap.get(cellValue));
                    }
                }
                Row row = null;
                Cell cell = null;
                for (int i = sheet.getFirstRowNum() + 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    row = sheet.getRow(i);
                    T t = cls.newInstance();
                    for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                        cell = row.getCell(j);
                        if (reflectionMap.containsKey(j)) {
                            Object cellValue = getCellValue(cell);
                            List<Field> fieldList = reflectionMap.get(j);
                            for (Field field : fieldList) {
                                try {
                                    switch (field.getType().getSimpleName()) {
                                        case  "String":
                                             field.set(t, cellValue);
                                             break;
                                        case  "int":
                                             field.setInt(t, Integer.parseInt(cellValue + ""));
                                             break;
                                        default :
                                             field.set(t, null);
                                             break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    dataList.add(t);
                }
            }
            file.getInputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataList;
    }


    public static Object getCellValue(Cell cell){
        Object value = null;
        if (cell == null) {
            return null;
        } 
        switch (cell.getCellTypeEnum()) {
            case _NONE:
                break;
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                if(DateUtil.isCellDateFormatted(cell)){ //日期
                    value = FAST_DATE_FORMAT.format(DateUtil.getJavaDate(cell.getNumericCellValue()));//统一转成 yyyy/MM/dd
                } else if("@".equals(cell.getCellStyle().getDataFormatString())
                        || "General".equals(cell.getCellStyle().getDataFormatString())
                        || "0_ ".equals(cell.getCellStyle().getDataFormatString())){
                    //文本  or 常规 or 整型数值
                    value = DECIMAL_FORMAT.format(cell.getNumericCellValue());
                } else if(POINTS_PATTERN.matcher(cell.getCellStyle().getDataFormatString()).matches()){ //正则匹配小数类型
                    value = cell.getNumericCellValue();  //直接显示
                } else if("0.00E+00".equals(cell.getCellStyle().getDataFormatString())){//科学计数
                    value = cell.getNumericCellValue();	//待完善
                    value = DECIMAL_FORMAT_NUMBER.format(value);
                } else if("0.00%".equals(cell.getCellStyle().getDataFormatString())){//百分比
                    value = cell.getNumericCellValue(); //待完善
                    value = DECIMAL_FORMAT_PERCENT.format(value);
                } else if("# ?/?".equals(cell.getCellStyle().getDataFormatString())){//分数
                    value = cell.getNumericCellValue(); ////待完善
                } else { //货币
                    value = cell.getNumericCellValue();
                    value = DecimalFormat.getCurrencyInstance().format(value);
                }
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case BLANK:
                //value = ",";
                break;
            default:
                value = cell.toString();
        }
        return value;
    }

    /**
     * 检测文件是否符合excel
     */
    public static boolean checkFile(MultipartFile file) {
        //判断文件是否存在
        if(null == file){
            return false;
        }
        //获得文件名
        String fileName = file.getOriginalFilename();
        //判断文件是否是excel文件
        if(!fileName.endsWith("xls")){
            return false;
        }
        return true;
    }
}
