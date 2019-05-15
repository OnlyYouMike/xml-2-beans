package com.lifebigboom.xml2beans.xml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: zj
 * @create: 2019-05-14 15:09
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {

    private String superiorPath;

    private String name;

    private String value;

    private String idValue;

    private Node parentNode;

    private List<Node> subNodes;

    public static final String joinChar = ".";

    public Node(Node parNode, String name, String value) {
        if (null == parNode){
            this.superiorPath = name.toUpperCase();
        }else {
            this.superiorPath =  (parNode.getName()+ joinChar + name).toUpperCase();
            this.parentNode = parNode;
        }
        this.name = name.toUpperCase();
        this.value = value;
    }

    public void add2Nodes(Node node){
        if (CollectionUtils.isEmpty(subNodes)){
            this.subNodes = new ArrayList<>();
        }
        this.subNodes.add(node);
    }

    public Map<String,Object> nodes2Map(){
        if (CollectionUtils.isEmpty(this.subNodes)){
            return null;
        }
        Map<String, Object> m = new HashMap<>();
        for (Node node : this.subNodes){
            m.put(node.getName(),node.getValue());
        }
        return m;
    }


}
