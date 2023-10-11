package com.field.utils.fieldutils.utils;

import net.redhogs.cronparser.CronExpressionDescriptor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.text.ParseException;
import java.util.Locale;

/**
 * @author Field
 * @date 2021-07-27 17:13
 **/
public class FieldBaseUtil {

    /**
     * Activiti 的 反序列化二进制
     */
//    public static Object activitiBytesToObject(byte[] bytes){
//        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//        try {
//            ObjectInputStream ois = new ObjectInputStream(bais) {
//                @Override
//                protected Class<?> resolveClass(ObjectStreamClass desc){
//                    return ReflectUtil.loadClass(desc.getName());
//                }
//            };
//            return ois.readObject();
//        } catch (Exception ignored) {
//        } finally {
//            IoUtil.closeSilently(bais);
//        }
//        return null;
//    }

    /**
     * cron 转 中文
     * @param text 文本
     * @return 处理结果
     */
    public static String cronToString(String text) throws ParseException {
        return CronExpressionDescriptor.getDescription(text, Locale.CHINESE);
    }

    /**
     * 清除富文本中指定的标签，以防止安全问题
     * @param payload 输入的文本
     * @return 处理结果
     */
    public static String safeCleanHtml(String payload){
        //String payload = "aaa<script>alert(1)</script>bbb<img src=\"http://.cc/a.jpg\" class=\"s\" onerror=\"alert(1)\"><a href=\"http://.cc/test\">sss</a>";
        Safelist safelist = Safelist.basicWithImages().addAttributes(":all", "class");
        return Jsoup.clean(payload, safelist);
    }

}
