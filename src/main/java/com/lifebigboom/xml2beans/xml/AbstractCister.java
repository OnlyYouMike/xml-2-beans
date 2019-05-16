package com.lifebigboom.xml2beans.xml;

import com.lifebigboom.xml2beans.xml.enums.FieldType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lifebigboom.xml2beans.xml.Node.joinChar;


/**
 * @author: zj
 * @create: 2019-05-14 18:41
 **/
public  abstract class AbstractCister {

    /**
     * 存放list节点名称
     */
    protected Map<String,List<Node>> DUPLICATE_NODE_MAP;

    protected Map<String,Node> ALL_SINGLE_NODE_MAP = new HashMap<>();

    /**
     * List 参数
     */
    protected Map<String,List<Map<String,Object>>> DUPLICATE_NODE_PARAM_MAP;

    protected Map<String,Map<String,Object>> ATTRIBUTE_MAP = new HashMap<>();

    protected void initListNode(){
        if (MapUtils.isEmpty(DUPLICATE_NODE_MAP)){
            return;
        }
        DUPLICATE_NODE_PARAM_MAP = new HashMap<>();

        for (Map.Entry<String,List<Node>> entry : DUPLICATE_NODE_MAP.entrySet()){
            List<Node> value = entry.getValue();
            List<Map<String,Object>> objects = new ArrayList<>();
            for (Node node : value){
                objects.add(node.nodes2Map());
            }
            DUPLICATE_NODE_PARAM_MAP.put(entry.getKey(),objects);
        }
        DUPLICATE_NODE_MAP = null;
    }


    public void addNode2(Node parentNode,Node subNode){

        if (MapUtils.isNotEmpty(subNode.getIdValues())){
            ATTRIBUTE_MAP.put(subNode.getName(),subNode.getIdValues());
        }

        parentNode.add2Nodes(subNode);
    }

    public void addNode(Node node){

        String superiorPath = node.getSuperiorPath();

        if (MapUtils.isNotEmpty(node.getIdValues())){
            ATTRIBUTE_MAP.put(node.getName(),node.getIdValues());
        }
        if (!ALL_SINGLE_NODE_MAP.containsKey(superiorPath) && (MapUtils.isEmpty(DUPLICATE_NODE_MAP) || !DUPLICATE_NODE_MAP.containsKey(superiorPath))){
            ALL_SINGLE_NODE_MAP.put(superiorPath,node);
            return;
        }

        if (ALL_SINGLE_NODE_MAP.containsKey(superiorPath)){

            Node listNode1 = ALL_SINGLE_NODE_MAP.get(superiorPath);

            if (MapUtils.isEmpty(DUPLICATE_NODE_MAP)){
                DUPLICATE_NODE_MAP = new HashMap<>();
            }

            List<Node> nodes = new ArrayList<>();
            nodes.add(listNode1);
            nodes.add(node);

            DUPLICATE_NODE_MAP.put(superiorPath,nodes);
            ALL_SINGLE_NODE_MAP.remove(superiorPath);
            return;
        }
        if (DUPLICATE_NODE_MAP.containsKey(superiorPath)){
            List<Node> nodes = DUPLICATE_NODE_MAP.get(superiorPath);
            nodes.add(node);
            return;
        }
    }

    protected FieldType validator(Field field){
        XmlAider annotation = field.getAnnotation(XmlAider.class);
        if (null == annotation || annotation.fieldType() == FieldType.BASIC){
            return FieldType.BASIC;
        }
        return annotation.fieldType();
    }

    protected String joinNodePath (String subNode,String parentNode){
        return parentNode+joinChar+subNode;
    };


    private List<Map<String,Object>> list_param(String nodePathName){

        List<Map<String,Object>> param = null;

        if (!MapUtils.isEmpty(DUPLICATE_NODE_PARAM_MAP)){
            param = DUPLICATE_NODE_PARAM_MAP.get(nodePathName);
        }
        if (CollectionUtils.isEmpty(param)){
            Node node = this.ALL_SINGLE_NODE_MAP.get(nodePathName);
            if (null == node || CollectionUtils.isEmpty(node.getSubNodes())){
                param = null;
            }else {
                param = new ArrayList<>();
                param.add(nodes2Map(node.getSubNodes()));
            }
        }
        return param;
    }

    private Map<String,Object> nodes2Map(List<Node> nodes){

        Map<String, Object> paramMap = new HashMap<>();

        for (Node node :nodes){
            paramMap.put(node.getName().toUpperCase(),node.getValue());
        }
        return paramMap;
    }

    protected Object takeAttribute(XmlAider xmlAider){
        String nodeName = xmlAider.nodeName().toUpperCase();
        Map<String, Object> map = ATTRIBUTE_MAP.get(nodeName);
        if (MapUtils.isNotEmpty(map)){
            return map.get(xmlAider.attribute().toUpperCase());
        }
        return null;
    }


    protected List<Object> buildListField(Field field) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        XmlAider annotation = field.getAnnotation(XmlAider.class);
        if (null == annotation){
            return null;
        }
        ParameterizedType genericType = (ParameterizedType)field.getGenericType();
        Type type = genericType.getActualTypeArguments()[0];

        Class<?> aClass = Class.forName(type.getTypeName());

        String nodePathName = joinNodePath(annotation.nodeName(), annotation.upperLevelName());

        List<Map<String, Object>> list = list_param(nodePathName);

        if (CollectionUtils.isEmpty(list)){
            return null;
        }

        List<Object> objects = new ArrayList<>();

        for (Map<String, Object> param : list){
            Object o = aClass.newInstance();
            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field f :declaredFields ){
                f.setAccessible(true);
                XmlAider annotation1 = f.getAnnotation(XmlAider.class);
                String nodeName = f.getName().toUpperCase();
                if (null != annotation1){
                    nodeName = annotation1.nodeName();
                }
                Object o1 = param.get(nodeName);
                f.set(o,o1);
            }
            objects.add(o);
        }

        return objects;
    }

}
