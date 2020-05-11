package com.timeep.service;

import java.util.HashMap;
import java.util.List;

/**
 * @author Li
 **/
public interface OwlService {

    List<String> findZhiShiDian();//查询知识点名称

    HashMap<String,StringBuilder> findIsSiblingOf(String subject);//并列知识点(兄弟知识点)

    HashMap<String,String> findHasPostK(String object);//后继知识点

    HashMap<String,String> findHasPreK(String object);//前序知识点

   /* HashMap<String,String> findHasPreK(String object);//*/

}
