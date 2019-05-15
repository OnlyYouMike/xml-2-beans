package com.lifebigboom.xml2beans.xml;

/**
 * @author: zj
 * @create: 2019-05-14 15:10
 **/

public interface Cister {


    public void addNode(Node node);

    public void addNode2(Node parent, Node sub);

    public void initData();

    public <T> T buildInstance(Class<T> clazz) throws IllegalAccessException, InstantiationException, ClassNotFoundException;


}
