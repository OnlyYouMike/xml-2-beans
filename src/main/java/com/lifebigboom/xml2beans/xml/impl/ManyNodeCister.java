package com.lifebigboom.xml2beans.xml.impl;


import com.lifebigboom.xml2beans.xml.AbstractCister;
import com.lifebigboom.xml2beans.xml.Cister;
import com.lifebigboom.xml2beans.xml.Node;
import com.lifebigboom.xml2beans.xml.XmlAider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: zj
 * @create: 2019-05-14 18:42
 **/
public class ManyNodeCister extends AbstractCister implements Cister {


    private Map<String,Map<String,Object>> ALL_NODE_SUB_NODES = new HashMap<>();

    @Override
    public void initData() {

        if (MapUtils.isEmpty(ALL_SINGLE_NODE_MAP)){
            initListNode();
            return;
        }
        for (Map.Entry<String,Node> entry : ALL_SINGLE_NODE_MAP.entrySet()){

            String key = entry.getKey();
            List<Node> subNodes = entry.getValue().getSubNodes();
            if (CollectionUtils.isEmpty(subNodes)){
                continue;
            }
            Map<String, Object> paramMap = new HashMap<>();
            for (Node node : subNodes){
                paramMap.put(node.getName(),node.getValue());
            }
            ALL_NODE_SUB_NODES.put(key,paramMap);
        }
        initListNode();
    }

    @Override
    public <T> T buildInstance(Class<T> clazz) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        T instance = clazz.newInstance();
        buildCore(clazz,null,instance);
        return instance;
    }

    private void buildCore(Class clazz,XmlAider xmlAider,Object instance) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        if (null == xmlAider){
            xmlAider = (XmlAider)clazz.getAnnotation(XmlAider.class);
        }

        if (null == xmlAider) throw new NullPointerException();

        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field field : declaredFields){
            field.setAccessible(true);
            switch (validator(field)){
                case BASIC:
                    Object o = buildBasicField(field, xmlAider);
                    field.set(instance,o);
                    break;
                case LIST:
                    field.set(instance,buildListField(field));
                    break;
                case OBJECT:
                    buildObjectField(field,instance);
                    break;
            }

        }
    }

    private void buildObjectField(Field field,Object obj) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        XmlAider fieldAnnotation = field.getAnnotation(XmlAider.class);

        Object instance = field.getType().newInstance();

        Class<?> aClass = field.getType();

        buildCore(aClass,fieldAnnotation,instance);

        field.set(obj,instance);

    }

    private Object buildBasicField(Field field,XmlAider xmlAider){

        XmlAider fieldAnnotation = field.getAnnotation(XmlAider.class);
        String nodeName = field.getName().toUpperCase();
        if (null != fieldAnnotation && !fieldAnnotation.getAttribute()){
            nodeName = fieldAnnotation.nodeName().toUpperCase();
        }else if (null != fieldAnnotation && fieldAnnotation.getAttribute()){
            return takeAttribute(fieldAnnotation);
        }

        String currentNodeName = xmlAider.nodeName();
        String upperNodeName = xmlAider.upperLevelName();
        if (StringUtils.isBlank(xmlAider.nodeName()) || StringUtils.isBlank(xmlAider.upperLevelName()))
            throw new NullPointerException();

        Map<String, Object> map = ALL_NODE_SUB_NODES.get(joinNodePath(currentNodeName.toUpperCase(), upperNodeName.toUpperCase()));

        if (MapUtils.isEmpty(map)){
            return null;
        }
        return map.get(nodeName);
    }



}
