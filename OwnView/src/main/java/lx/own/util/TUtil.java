package lx.own.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.ParameterizedType;

/**
 * <p>反射、泛型相关的工具</p><br/>
 *
 * @author Lx
 * @date 15/05/2017
 */

public class TUtil {
    /**
     * 利用反射获取本类泛型对象的方法
     *
     * @param object 本类对象(必须具有已经声明的泛型实现)
     * @param index  泛型的索引
     * @return 泛型的实例
     */
    @SuppressWarnings(value = {"unchecked"})
    @Nullable
    public static <T> T getT(@NonNull Object object, @NonNull int index) {
        try {
            return ((Class<T>) ((ParameterizedType) (object.getClass()
                    .getGenericSuperclass())).getActualTypeArguments()[index])
                    .newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过全类名获取Class对象的方法
     *
     * @param className 全类名
     * @return Class对象
     */
    @Nullable
    public static Class<?> forName(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
