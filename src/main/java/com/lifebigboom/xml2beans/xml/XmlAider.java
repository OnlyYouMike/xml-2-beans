package com.lifebigboom.xml2beans.xml;

import com.lifebigboom.xml2beans.xml.enums.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: zj
 * @create: 2019-05-14 18:17
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.TYPE})
public @interface XmlAider {

    /**
     * 节点名称
     * @return
     */
    String nodeName();

    /**
     * 上一级
     * @return
     */
    String upperLevelName() default "";

    /**
     * 上上级
     * @return
     */
    String upper2LevelName () default "";

    /**
     * 字段类型
     * @return
     */
    FieldType fieldType() default FieldType.BASIC;

}
