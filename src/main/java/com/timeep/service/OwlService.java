package com.timeep.service;

import java.util.HashMap;
import java.util.List;

/**
 * @author Li
 **/
public interface OwlService {

    List<String> findZhiShiDian();//查询知识点名称

    HashMap<String,String > findIsSiblingOf(String subject);//并列知识点(兄弟知识点)

    HashMap<String,String> findHasPostK(String subject);//后继知识点

    HashMap<String,String> findHasPreK(String subject);//前序知识点

    HashMap<String,String> findAll(String subject);//查询所有

    HashMap<String,String> findK(String subject);//查询知识点的章节

    HashMap<String,String> findFirst(String subject,Boolean is);//初始展示

   /* HashMap<String,String> findHasPreK(String object);//*/
   HashMap<String,String> findEducationProperty(String subject);//查询教育属性

   HashMap<String,String> findTextbookSystem(String subject);//查询教材体系



   HashMap<String,String> findAllTextbookSystem(String subject);//查询所有的教材体系

   HashMap<String,String> TextbookSystem(String subject);//再次查询教材体系

   HashMap<String,String> findAllEducationProperty(String subject);//查询所有教育属性

   HashMap<String,String> findAllKnowledgePointSystem(String subject);//查询所有的知识点体系

   HashMap<String,String> findKnowledgePointSystem(String subject);//查询知识点体系

    HashMap<String, String> findKnowledgeGraph(String subject);//查询知识图谱

    HashMap<String, String> findIsSiblingOfAndRefK(String subject,int number);//查询前序和后继和参考知识点


}
