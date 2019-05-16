package com.lifebigboom.xml2beans.xml.impl;


import com.lifebigboom.xml2beans.xml.AbstractCister;
import com.lifebigboom.xml2beans.xml.Cister;
import com.lifebigboom.xml2beans.xml.Node;
import com.lifebigboom.xml2beans.xml.XmlAider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author: zj
 * @create: 2019-05-14 18:42
 **/
public class SingleNodeCister extends AbstractCister implements Cister {

    private Map<String,Object> SINGLE_NODE_MAP = new HashMap<>();

    private Map<String,Map<String,Object>> UPPER_LEVEL_SAME_NODE_PATH_MAP = new HashMap<>();

    private Map<String,Object> UPPER_LEVEL_DIFF_NODE_MAP = new HashMap<>();

    @Override
    public void initData() {

        Map<String, Node> filterDuplicateNodes = new HashMap<>();

        /**
         *  上级节点相同的数据集合
         */
        Set<String> upperLevelSameDuplicateNameSet = new HashSet<>();
        List<Node> upperLevelSameNodeName  = new ArrayList<>();

        /**
         * 上级节点不同的数据集合
         */
        Set<String> upperLevelDiffDuplicateNameSet = new HashSet<>();
        List<Node> upperLevelDiffNodeName  = new ArrayList<>();

        for (Map.Entry<String,Node> map : ALL_SINGLE_NODE_MAP.entrySet()){

            List<Node> value = map.getValue().getSubNodes();
            if (CollectionUtils.isEmpty(value)){
                continue;
            }
            for (Node node : value){
                if (!filterDuplicateNodes.containsKey(node.getName())){
                    filterDuplicateNodes.put(node.getName(),node);
                    continue;
                }

                String name = node.getParentNode().getName();
                String name1 = filterDuplicateNodes.get(node.getName()).getParentNode().getName();
                if (StringUtils.equals(name,name1)){
                    upperLevelSameNodeName.add(node);
                    upperLevelSameDuplicateNameSet.add(node.getName());
                }else {
                    upperLevelDiffNodeName.add(node);
                    upperLevelDiffDuplicateNameSet.add(node.getName());
                }
            }

        }

        /**
         * 处理好上级相同和上级不同的相同节点名处理好
         */
        deDuplicate(upperLevelDiffDuplicateNameSet,filterDuplicateNodes,upperLevelDiffNodeName);
        deDuplicate(upperLevelSameDuplicateNameSet,filterDuplicateNodes,upperLevelSameNodeName);


        /**
         * 将不同节点名的node组装好
         */
        for (Map.Entry<String,Node> entry : filterDuplicateNodes.entrySet()){
            Node value = entry.getValue();
            SINGLE_NODE_MAP.put(value.getName().toUpperCase(),value.getValue());
        }
        if (CollectionUtils.isNotEmpty(upperLevelSameNodeName)){
            for (Node node : upperLevelSameNodeName){
                String superiorPath = node.getParentNode().getSuperiorPath();
                Map<String, Object> map = UPPER_LEVEL_SAME_NODE_PATH_MAP.get(superiorPath);
                if (MapUtils.isEmpty(map)){
                    map = new HashMap<>();
                }
                map.put(node.getName(),node.getValue());
                UPPER_LEVEL_SAME_NODE_PATH_MAP.put(superiorPath,map);
            }
        }
        if(CollectionUtils.isNotEmpty(upperLevelDiffNodeName)){
            for (Node node : upperLevelDiffNodeName){
                String superiorPath = node.getSuperiorPath();
                UPPER_LEVEL_DIFF_NODE_MAP.put(superiorPath,node.getValue());
            }
        }
        initListNode();
    }

    private void deDuplicate(Set<String> duplicateNameSet,Map<String, Node> filterDuplicateNodes,List<Node> sameNodeName){

        if (CollectionUtils.isEmpty(duplicateNameSet) || MapUtils.isEmpty(filterDuplicateNodes)){
            return;
        }
        for (String duplicateName : duplicateNameSet ){
            Node node = filterDuplicateNodes.get(duplicateName);
            if (null == node)continue;
            sameNodeName.add(node);
            filterDuplicateNodes.remove(duplicateName);
        }

    }


    @Override
    public <T> T buildInstance(Class<T> clazz) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Field[] declaredFields = clazz.getDeclaredFields();
        T instance = clazz.newInstance();
        for (Field field : declaredFields){
            field.setAccessible(true);
            switch (validator(field)){
                case BASIC:
                    buildBasicField(field,instance);
                    break;
                case LIST:
                     field.set(instance,buildListField(field));
                    break;
            }
        }
        return instance;
    }


    private void buildBasicField(Field field,Object obj) throws IllegalAccessException {
        XmlAider annotation = field.getAnnotation(XmlAider.class);

        String nodeName = field.getName().toUpperCase();
        Object value  = SINGLE_NODE_MAP.get(nodeName);
        if (null != annotation && !annotation.getAttribute()){
            value = getValue(annotation,nodeName);
        }else if (null != annotation && annotation.getAttribute()){
            value = takeAttribute(annotation);
        }
        field.set(obj,value);
    }

    private Object getValue(XmlAider annotation,String fileName){
        String upperLevelName = annotation.upperLevelName();
        String upper2LevelName = annotation.upper2LevelName();
        String nodeName = annotation.nodeName();
        if (StringUtils.isNotBlank(upperLevelName) && StringUtils.isNotBlank(upper2LevelName)){
            Map<String, Object> map = UPPER_LEVEL_SAME_NODE_PATH_MAP.get(joinNodePath(upperLevelName, upper2LevelName));
            if (MapUtils.isEmpty(map)){
                return null;
            }
            if (StringUtils.isNotBlank(nodeName)){
                fileName = nodeName;
            }
            return map.get(fileName.toUpperCase());
        }

        if (StringUtils.isNotBlank(upperLevelName)){
            if (StringUtils.isNotBlank(nodeName)){
                fileName = nodeName;
            }
            Object o = UPPER_LEVEL_DIFF_NODE_MAP.get(joinNodePath(fileName, annotation.upperLevelName()));
            return o;
        }
        return null;
    }

}
