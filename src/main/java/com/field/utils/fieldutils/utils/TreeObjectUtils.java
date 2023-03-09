package com.field.utils.fieldutils.utils;



import org.springframework.util.CollectionUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 数组快速建树
 */
public class TreeObjectUtils {

    /**
     * @param list 数组
     * @param idName id字段名
     * @param parentIdName 父id字段名
     * @param childName 子数组名称
     * @param levelName 级别字段名 必须要有
     */
    public static <T> List<T> buildTree(List<T> list, String idName, String parentIdName, String childName, String levelName) {

        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        //等级集合,从高到底。如：1，2，3
        Set<Integer> levelNumSet = list.stream()
                .map(e -> (Integer) getName(levelName, e, null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Integer levelMin = levelNumSet.stream().min(Comparator.comparingInt(e -> e)).orElse(1);
        Integer levelMax = levelNumSet.stream().max(Comparator.comparingInt(e -> e)).orElse(1);

        List<Integer> level = IntStream.range(levelMin, levelMax + 1).boxed().collect(Collectors.toList());
        // key：id value：索引
        Map<Object, Integer> map = new HashMap<>(list.size());
        int levelSize = level.size();
        //key：级别 value：这个级别的数组
        Map<Object, List<T>> levelMap = new HashMap<>(levelSize);
        for (T t : list) {
            Object key = getName(idName, t, null);
            Object mapLevel = getName(levelName, t, null);
            List<T> levelList = levelMap.get(mapLevel);
            int index;
            if (levelList == null) {
                levelList = new ArrayList<>();
                index = 0;
                levelList.add(t);
                levelMap.put(mapLevel, levelList);
            } else {
                index = levelList.size();
                levelList.add(t);
                levelMap.put(mapLevel, levelList);
            }
            map.put(key, index);
        }
        // 开始构建树
        for (int i = levelSize - 1; i > 0; i--) {
            List<T> levelList = levelMap.get(level.get(i));
            //获取上一级对象
            List<T> parentList = levelMap.get(level.get(i - 1));
            for (T unit : levelList) {
                //获取父级id
                Object key = getName(parentIdName, unit, null);
                //获取索引id
                Integer integer = map.get(key);
                // 通过ID寻找父节点
                T t = parentList.get(integer);
                if (t == null){
                    continue;
                }
                // 拿出父节点的子集
                @SuppressWarnings("unchecked")
                List<T> childList = (List<T>) getName(childName, t, null);
                if (childList == null) {
                    childList = new ArrayList<>();
                }
                childList.add(unit);
                //将新的子集设置回去
                getName(childName, t, childList);
            }
        }
        return levelMap.get(level.get(0));
    }

    /**
     * 通过反射调用方法使用Get和Set
     *
     * @param name 方法名
     * @param obj  类
     * @param arg  参数
     */
    private static Object getName(String name, Object obj, Object arg) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(name, obj.getClass());
            //获取get方法
            Method getMethod = pd.getReadMethod();
            //获取set方法
            Method setMethod = pd.getWriteMethod();
            Object rtn = null;
            if (arg == null) {
                rtn = getMethod.invoke(obj);
            } else {
                setMethod.invoke(obj, arg);
            }
            return rtn;
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}