package com.lifebigboom.xml2beans.xml;

import com.lifebigboom.xml2beans.xml.enums.Xml2BeanType;
import com.lifebigboom.xml2beans.xml.impl.ManyNodeCister;
import com.lifebigboom.xml2beans.xml.impl.SingleNodeCister;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;

/**
 * @author: zj
 * @create: 2019-05-14 15:08
 **/
public class XmlFactory {

    private Cister cister;


    public XmlFactory(Xml2BeanType xml2BeanType){

        switch (xml2BeanType){
            case MANY:
                cister = new ManyNodeCister();
                break;
            case SINGLE:
                cister = new SingleNodeCister();
                break;
                default:
                    throw new NullPointerException();
        }

    }


    public <T> T conciseInstance(String xml,Class<T> clazz) throws DocumentException, IllegalAccessException, ClassNotFoundException, InstantiationException {

        if (StringUtils.isBlank(xml) || null == clazz) throw new NullPointerException();

        try {
            Document document = DocumentHelper.parseText(xml);
            Element rootElement = document.getRootElement();
            decomposeNodes(rootElement,null);
            cister.initData();
            return  cister.buildInstance(clazz);
        }catch (Exception e){
            throw e;
        }finally {
            cister = null;
        }
    }




    public XmlFactory initCister(String xml) throws DocumentException {

        Document document = DocumentHelper.parseText(xml);
        Element rootElement = document.getRootElement();
        if (null == rootElement) throw new NullPointerException();
        decomposeNodes(rootElement,null);
        cister.initData();
        return this;
    }


    public <T> T instance2Bean(Class<T> clazz) throws IllegalAccessException, ClassNotFoundException, InstantiationException {

        try {
            if (null == clazz) throw new NullPointerException();

            T instance = cister.buildInstance(clazz);

            return instance;
        }catch (Exception e){
           throw e;
        }
        finally {
            cister = null;
        }
    }


    /**
     * 获取所有节点
     * @param rootElement
     */
    private void decomposeNodes(Element rootElement,Node parentNode){
        List<Element> elements = rootElement.elements();
        if (CollectionUtils.isEmpty(elements)){
            return;
        }
        //添加父节点到容器
        String name = rootElement.getName();
        String text = rootElement.getText();
        Node rootNode = new Node(parentNode, name, text);
        cister.addNode(rootNode);
        List<Element> subElements = rootElement.elements();
        for (Element element : subElements){
            List<Element> subElements2 = element.elements();
            if (CollectionUtils.isEmpty(subElements2)){
                Node subNode = new Node(rootNode, element.getName(), element.getText());
                cister.addNode2(rootNode,subNode);
            }else {
                decomposeNodes(element,rootNode);
            }
        }
    }




}
