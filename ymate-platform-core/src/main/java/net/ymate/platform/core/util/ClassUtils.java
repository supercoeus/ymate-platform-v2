/*
 * Copyright 2007-2107 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.platform.core.util;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.thoughtworks.paranamer.AdaptiveParanamer;
import net.ymate.platform.core.lang.PairObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * <p>
 * ClassUtils
 * </p>
 * <p>
 * 类操作相关工具；
 * </p>
 *
 * @author 刘镇 (suninformation@163.com)
 * @version 0.0.0
 *          <table style="border:1px solid gray;">
 *          <tr>
 *          <th width="100px">版本号</th><th width="100px">动作</th><th
 *          width="100px">修改人</th><th width="100px">修改时间</th>
 *          </tr>
 *          <!-- 以 Table 方式书写修改历史 -->
 *          <tr>
 *          <td>0.0.0</td>
 *          <td>创建类</td>
 *          <td>刘镇</td>
 *          <td>2012-12-5下午6:41:23</td>
 *          </tr>
 *          </table>
 */
public class ClassUtils {

    private static final Log _LOG = LogFactory.getLog(ClassUtils.class);

    private static InnerClassLoader _INNER_CLASS_LOADER = new InnerClassLoader(new URL[]{}, ClassUtils.class.getClassLoader());

    public static class InnerClassLoader extends URLClassLoader {

        public InnerClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }

    }

    /**
     * @return 返回默认类加载器对象
     */
    public static ClassLoader getDefaultClassLoader() {
        return _INNER_CLASS_LOADER;
    }

    /**
     * 获得指定名称、限定接口的实现类
     *
     * @param <T>
     * @param className      实现类名
     * @param interfaceClass 限制接口名
     * @param callingClass
     * @return 如果可以得到并且限定于指定实现，那么返回实例，否则为空
     */
    @SuppressWarnings("unchecked")
    public static <T> T impl(String className, Class<T> interfaceClass, Class<?> callingClass) {
        if (StringUtils.isNotBlank(className)) {
            try {
                Class<?> implClass = loadClass(className, callingClass);
                if (interfaceClass == null || interfaceClass.isAssignableFrom(implClass)) {
                    return (T) implClass.newInstance();
                }
            } catch (Exception e) {
                _LOG.warn("", RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T impl(Class<?> implClass, Class<T> interfaceClass) {
        if (implClass != null) {
            if (interfaceClass == null || interfaceClass.isAssignableFrom(implClass)) {
                try {
                    return (T) implClass.newInstance();
                } catch (Exception e) {
                    _LOG.warn("", RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return null;
    }

    /**
     * @param className
     * @param callingClass
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(String className, Class<?> callingClass) throws ClassNotFoundException {
        Class<?> _targetClass = null;
        try {
            _targetClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                _targetClass = Class.forName(className, false, ClassUtils.class.getClassLoader());
            } catch (ClassNotFoundException ex) {
                try {
                    _targetClass = _INNER_CLASS_LOADER.loadClass(className);
                } catch (ClassNotFoundException exc) {
                    _targetClass = callingClass.getClassLoader().loadClass(className);
                }
            }
        }
        return _targetClass;
    }

    /**
     * 判断类clazz是否是superClass类的子类对象
     *
     * @param clazz
     * @param superClass
     * @return
     */
    public static boolean isSubclassOf(Class<?> clazz, Class<?> superClass) {
        boolean _flag = false;
        do {
            Class<?> cc = clazz.getSuperclass();
            if (cc != null) {
                if (cc.equals(superClass)) {
                    _flag = true;
                    break;
                } else {
                    clazz = clazz.getSuperclass();
                }
            } else {
                break;
            }
        } while ((clazz != null && clazz != Object.class));
        return _flag;
    }

    /**
     * @param clazz          目标对象
     * @param interfaceClass 接口类型
     * @return 判断clazz类中是否实现了interfaceClass接口
     */
    public static boolean isInterfaceOf(Class<?> clazz, Class<?> interfaceClass) {
        boolean _flag = false;
        do {
            for (Class<?> cc : clazz.getInterfaces()) {
                if (cc.equals(interfaceClass)) {
                    _flag = true;
                }
            }
            clazz = clazz.getSuperclass();
        } while (!_flag && (clazz != null && clazz != Object.class));
        return _flag;
    }

    /**
     * @param target          目标对象，即可以是Field对象、Method对象或是Class对象
     * @param annotationClass 注解类对象
     * @return 判断target对象是否存在annotationClass注解
     */
    public static boolean isAnnotationOf(Object target, Class<? extends Annotation> annotationClass) {
        if (target instanceof Field) {
            if (((Field) target).isAnnotationPresent(annotationClass)) {
                return true;
            }
        } else if (target instanceof Method) {
            if (((Method) target).isAnnotationPresent(annotationClass)) {
                return true;
            }
        } else if (target instanceof Class) {
            if (((Class<?>) target).isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param clazz 类型
     * @return 返回类中实现的接口名称集合
     */
    public static String[] getInterfaceNames(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        List<String> names = new ArrayList<String>();
        for (Class<?> i : interfaces) {
            names.add(i.getName());
        }
        return names.toArray(new String[names.size()]);
    }

    /**
     * @param clazz 类对象
     * @return 获取泛型的数据类型集合，注：不适用于泛型嵌套, 即泛型里若包含泛型则返回此泛型的RawType类型
     */
    public static List<Class<?>> getParameterizedTypes(Class<?> clazz) {
        List<Class<?>> _clazzs = new ArrayList<Class<?>>();
        Type _types = clazz.getGenericSuperclass();
        if (ParameterizedType.class.isAssignableFrom(_types.getClass())) {
            for (Type _type : ((ParameterizedType) _types).getActualTypeArguments()) {
                if (ParameterizedType.class.isAssignableFrom(_type.getClass())) {
                    _clazzs.add((Class<?>) ((ParameterizedType) _type).getRawType());
                } else {
                    _clazzs.add((Class<?>) _type);
                }
            }
        } else {
            _clazzs.add((Class<?>) _types);
        }
        return _clazzs;
    }

    /**
     * 获取clazz指定的类对象所有的Field对象（若包含其父类对象，直至其父类为空）
     *
     * @param clazz  目标类
     * @param parent 是否包含其父类对象
     * @return Field对象集合
     */
    public static List<Field> getFields(Class<?> clazz, boolean parent) {
        List<Field> fieldList = new ArrayList<Field>();
        Class<?> clazzin = clazz;
        do {
            if (clazzin == null) {
                break;
            }
            fieldList.addAll(Arrays.asList(clazzin.getDeclaredFields()));
            if (parent) {
                clazzin = clazzin.getSuperclass();
            } else {
                clazzin = null;
            }
        } while (true);
        return fieldList;
    }

    /**
     * @param <A>
     * @param clazz
     * @param annotationClazz
     * @param onlyFirst
     * @return 获取clazz类中成员声明的annotationClazz注解
     */
    public static <A extends Annotation> List<PairObject<Field, A>> getFieldAnnotations(Class<?> clazz, Class<A> annotationClazz, boolean onlyFirst) {
        List<PairObject<Field, A>> _annotations = new ArrayList<PairObject<Field, A>>();
        for (Field _field : ClassUtils.getFields(clazz, true)) {
            A _annotation = _field.getAnnotation(annotationClazz);
            if (_annotation != null) {
                _annotations.add(new PairObject<Field, A>(_field, _annotation));
                if (onlyFirst) {
                    break;
                }
            }
        }
        return _annotations;
    }

    /**
     * @param method
     * @return 获取方法的参数名
     */
    public static String[] getMethodParamNames(final Method method) {
        return new AdaptiveParanamer().lookupParameterNames(method);
    }

    /**
     * @param clazz 数组类型
     * @return 返回数组元素类型
     */
    public static Class<?> getArrayClassType(Class<?> clazz) {
        try {
            return Class.forName(StringUtils.substringBetween(clazz.getName(), "[L", ";"));
        } catch (ClassNotFoundException e) {
            _LOG.warn("", RuntimeUtils.unwrapThrow(e));
        }
        return null;
    }

    /**
     * @param clazz 目标类型
     * @return 创建一个类对象实例，包裹它并赋予其简单对象属性操作能力，可能返回空
     */
    public static <T> ClassBeanWrapper<T> wrapper(Class<T> clazz) {
        try {
            return wrapper(clazz.newInstance());
        } catch (Exception e) {
            _LOG.warn("", RuntimeUtils.unwrapThrow(e));
        }
        return null;
    }

    /**
     * @param target 目标类对象
     * @return 包裹它并赋予其简单对象属性操作能力，可能返回空
     */
    public static <T> ClassBeanWrapper<T> wrapper(T target) {
        return new ClassBeanWrapper<T>(target);
    }

    /**
     * <p>
     * ClassBeanWrapper
     * </p>
     * <p>
     * 类对象包裹器，赋予对象简单的属性操作能力；
     * </p>
     *
     * @author 刘镇 (suninformation@163.com)
     * @version 0.0.0
     *          <table style="border:1px solid gray;">
     *          <tr>
     *          <th width="100px">版本号</th><th width="100px">动作</th><th
     *          width="100px">修改人</th><th width="100px">修改时间</th>
     *          </tr>
     *          <!-- 以 Table 方式书写修改历史 -->
     *          <tr>
     *          <td>0.0.0</td>
     *          <td>创建类</td>
     *          <td>刘镇</td>
     *          <td>2012-12-23上午12:46:50</td>
     *          </tr>
     *          </table>
     */
    public static class ClassBeanWrapper<T> {

        protected static Map<Class<?>, MethodAccess> __methodCache = new WeakHashMap<Class<?>, MethodAccess>();

        private T target;

        private Map<String, Field> _fields;

        private MethodAccess methodAccess;

        /**
         * 构造器
         *
         * @param target
         */
        protected ClassBeanWrapper(T target) {
            this.target = target;
            this._fields = new HashMap<String, Field>();
            for (Field _field : getFields(target.getClass(), true)/*target.getClass().getDeclaredFields()*/) {
                if (Modifier.isStatic(_field.getModifiers())) {
                    continue;
                }
                this._fields.put(_field.getName(), _field);
            }
            //
            this.methodAccess = __methodCache.get(target.getClass());
            if (this.methodAccess == null) {
                this.methodAccess = MethodAccess.get(target.getClass());
                __methodCache.put(target.getClass(), this.methodAccess);
            }
        }

        public T getTarget() {
            return target;
        }

        public Set<String> getFieldNames() {
            return _fields.keySet();
        }

        public Annotation[] getFieldAnnotations(String fieldName) {
            return getField(fieldName).getAnnotations();
        }

        public Field getField(String fieldName) {
            return _fields.get(StringUtils.uncapitalize(fieldName));
        }

        public Class<?> getFieldType(String fieldName) {
            return getField(fieldName).getType();
        }

        public ClassBeanWrapper<T> setValue(String fieldName, Object value) {
            methodAccess.invoke(this.target, "set" + StringUtils.capitalize(fieldName), value);
            return this;
        }

        public Object getValue(String fieldName) {
            return methodAccess.invoke(this.target, "get" + StringUtils.capitalize(fieldName));
        }

        /**
         * 拷贝当前对象的成员属性值到dist对象
         *
         * @param dist
         * @param <D>
         * @return
         */
        public <D> D copy(D dist) {
            ClassBeanWrapper<D> _wrapDist = wrapper(dist);
            for (String _fieldName : getFieldNames()) {
                if (_wrapDist.getFieldNames().contains(_fieldName)) {
                    try {
                        _wrapDist.setValue(_fieldName, getValue(_fieldName));
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
            return _wrapDist.getTarget();
        }

    }

}