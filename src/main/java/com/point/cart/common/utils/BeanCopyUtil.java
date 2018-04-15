package com.point.cart.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  类拷贝
 */
public class BeanCopyUtil {
    public static <T> T copyBean(T target,T source,String... fields){
        try {
            Class targetClass = target.getClass();
            Class sourceClass = source.getClass();
            // 如果用户没有指定属性则取目标数据的全部包括父类的属性
            if(fields == null || fields.length == 0){
                List<Field> allFields = getAllFields(targetClass);
                fields = getTargetFieldName(allFields);
            }

            if(fields != null && fields.length > 0){
                for (int i = 0,length = fields.length; i < length; i++) {
                    String field = fields[i];
                    // 源数据取值
                    String getMethodName = getMethodName(field);
                    Method getMethod = null;
                    try {
                        getMethod = sourceClass.getMethod(getMethodName);
                        if (getMethod != null){
                            Class returnType = getMethod.getReturnType();
                            Object value = getMethod.invoke(source);
                            if (null != value){
                                // 目标数据源设值
                                String setMethodName = setMethodName(field);
                                Method setMethod = targetClass.getMethod(setMethodName, returnType);
                                setMethod.invoke(target,value);
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

    /**
     * 获取目标对象的所有属性
     * @param listField
     * @return
     */
    private static String[] getTargetFieldName(List<Field> listField){
        String[] fieldArr = new String[listField.size()];
        if (listField.size() > 0){
            for (int i = 0,length = listField.size(); i < length; i++) {
                fieldArr[i] = listField.get(i).getName();
            }
        }

        return  fieldArr;
    }

    /**
     * 递归获取所有属性
     * @param clazz
     * @return
     */
    private static List<Field> getAllFields(Class clazz){
        List<Field> list = new ArrayList<Field>();
        if (clazz != null){
            Field[] declaredFields = clazz.getDeclaredFields();
            if (declaredFields != null && declaredFields.length > 0){
                list.addAll(Arrays.asList(declaredFields));
            }

            Class superclass = clazz.getSuperclass();
            List<Field> allFields = getAllFields(superclass);
            if (allFields.size() > 0) list.addAll(allFields);
        }
        return list;
    }

    private static String getMethodName(String field){
        return "get" + field.substring(0,1).toUpperCase() + field.substring(1);
    }

    private static String setMethodName(String field){
        return "set" + field.substring(0,1).toUpperCase() + field.substring(1);
    }

    // mvn clean package打包不能有多个main函数
    /*public static void main(String[] args) {
        StudentModel target = new StudentModel();
        target.setId(23);
        System.out.println(JSON.toJSONString(target));

        StudentModel source = new StudentModel();
        source.setName("邓伟");
        source.setAge(25);
        System.out.println(JSON.toJSONString(source));

        StudentModel result = copyBean(target, source, new String[]{"name", "age"});
        System.out.println(JSON.toJSONString(result));
    }*/
}
