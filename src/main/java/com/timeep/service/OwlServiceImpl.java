package com.timeep.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.timeep.dao.OwlRepository;
import com.timeep.po.Owl;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Li
 **/
@Service
public class OwlServiceImpl implements OwlService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private OwlRepository owlRepository;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查找知识点
     *
     * @return
     */
    @Override
    public List<String> findZhiShiDian() {
        List<Owl> owlList = owlRepository.findByPropertyAndObjectContaining("type", "知识点");
        List<String> list = new ArrayList<>();
        HashSet set = new HashSet();
        for (Owl owl : owlList) {
            //判断是否重复
            if (set.add(owl.getSubject()))
                list.add(owl.getSubject());
        }
        return list;
    }

    /**
     * 首次加载数据
     *
     * @param subject
     * @param is
     * @return
     */
    @Override
    public HashMap<String, String> findFirst(String subject, Boolean is) {
        //从redis中取出数据
        List<Owl> data = getData();
        List<Owl> owlList = new ArrayList<>();
        for (Owl owl : data) {
            //查询关于初中数学的知识点
            if (owl.getProperty().equals("type") && owl.getObject().startsWith("MathK")) {
                owlList.add(owl);
            }
        }
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        HashSet<String> hashSet = new HashSet<>();
        note.append("[{" + "name:'" + subject + "',des:'" + subject + "',symbolSize:60,category:0" + "},");
        link.append("[");
        hashSet.add(subject);
        for (Owl owl : owlList) {
            hashSet.add(owl.getSubject());
            note.append("{" + "name:'" + owl.getSubject() + "',des:'" + owl.getSubject() + "',symbolSize:60,category:1" + "},");
            link.append("{" + "source:'" + owl.getObject() + "',target:'" + owl.getSubject() + "',name:'属于'" + ",lineStyle: {normal: { curveness: 0.1 }}},");
        }
        LinkedHashSet<String> count = new LinkedHashSet<>();
        count.addAll(hashSet);
        while (!count.isEmpty()) {
            String next = count.iterator().next();
            count.remove(next);
            List<Owl> owls = new ArrayList<>();
            for (Owl owl : data) {
                //查询当前知识点的并列知识点
                if (owl.getProperty().equals("isSiblingof") && owl.getSubject().equals(next)) {
                    owls.add(owl);
                }
            }
            if (!owls.isEmpty()) {
                for (Owl owl : owls) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                        count.add(owl.getObject());
                        note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "},");
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'并列知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}},");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'并列知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}},");
                    }
                }
            }
        }
        LinkedHashSet<String> count2 = new LinkedHashSet<>();
        count2.addAll(hashSet);
        while (!count2.isEmpty()) {
            String next = count2.iterator().next();
            count2.remove(next);
            List<Owl> owls = new ArrayList<>();
            for (Owl owl : data) {
                //查询当前知识点的后序知识点
                if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(next))
                    owls.add(owl);
            }
            if (!owls.isEmpty()) {
                for (Owl owl : owls) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                        count2.add(owl.getObject());
                        note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "},");
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}},");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}},");
                    }
                }
            }
        }
        LinkedHashSet<String> count3 = new LinkedHashSet<>();
        count3.addAll(hashSet);
        while (!count3.isEmpty()) {
            String next = count3.iterator().next();
            count3.remove(next);
            List<Owl> owls = new ArrayList<>();
            for (Owl owl : data) {
                //查询当前知识点的前序知识点
                if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(next))
                    owls.add(owl);
            }
            if (!owls.isEmpty()) {
                for (Owl owl : owls) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                        count3.add(owl.getObject());
                        note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "},");
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}},");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}},");
                    }
                }
            }
        }
        if (is) {
            LinkedHashSet<String> count4 = new LinkedHashSet<>();
            count4.addAll(hashSet);
            while (!count4.isEmpty()) {
                String next = count4.iterator().next();
                count4.remove(next);
                List<Owl> owls = new ArrayList<>();
                for (Owl owl : data) {
                    //查询当前知识点的前序知识点
                    if (owl.getProperty().equals("relatedBook") && owl.getSubject().equals(next))
                        owls.add(owl);
                }
                if (!owls.isEmpty()) {
                    for (Owl owl : owls) {
                        //判断subject和Object是否相同，和是否为最后一个
                        if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                            count4.add(owl.getObject());
                            note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:3" + "},");
                            link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'relatedBook'" + ",lineStyle: {normal: { curveness: 0.1 }}},");
                        } else if (!owl.getSubject().equals(owl.getObject())) {
                            link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'relatedBook'" + ",lineStyle: {normal: { curveness: 0.1 }}},");
                        }

                        List<Owl> owls2 = new ArrayList<>();
                        for (Owl o : data) {
                            //查询当前知识点的前序知识点
                            if (o.getProperty().equals("type") && o.getSubject().equals(o.getObject()))
                                owls2.add(o);
                        }
                        if (!owls2.isEmpty()) {
                            for (Owl owl1 : owls2) {
                                //添加版本信息结点
                                if (owl1.getObject().startsWith("MathBook") && hashSet.add(owl1.getObject())) {
                                    note.append("{" + "name:'" + owl1.getObject() + "',des:'版本：" + owl1.getObject() + "',symbolSize:40,category:4" + "},");
                                    link.append("{" + "source:'" + owl1.getSubject() + "',target:'" + owl1.getObject() + "',name:'属于'" + "},");
                                } else if (owl1.getObject().startsWith("MathBook")) {
                                    link.append("{" + "source:'" + owl1.getSubject() + "',target:'" + owl1.getObject() + "',name:'属于'" + "},");
                                }
                            }
                        }
                    }
                }
            }
        }

        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        } else {
            note.append("]");
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.substring(0, link.length() - 1));
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }
        String a = note.substring(0, note.length());
        String b = link.substring(0, link.length());
        if (a.equals("]")) {
            a = "[{name:'" + subject + ":无',des:'" + subject + "',symbolSize:60,category:0}]";
        }
        hashMap.put("NOTE", a);
        hashMap.put("LINK", b);

        return hashMap;
    }

    @Override
    public HashMap<String, String> findIsSiblingOf(String subject) {
        List<Owl> owlList = owlRepository.findByPropertyAndSubject("isSiblingof", subject);
//        List<Owl> owlList1=new ArrayList<>();
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        if (!owlList.isEmpty()) {
            note.append("[{" + "name:'" + owlList.get(0).getSubject() + "',des:'" + owlList.get(0).getSubject() + "',symbolSize:60,category:0" + "},");
//        note.append("[");
            link.append("[");
            for (int i = 0; i < owlList.size(); i++) {
                Owl owl = owlList.get(i);
                if (!owl.getSubject().equals(owl.getObject()) && i != owlList.size()) {
                    note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "},");
                    link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'并列知识点'" + "},");
                } else if (!owl.getSubject().equals(owl.getObject())) {
                    note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "}");
                    link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'并列知识点'" + "}");
                }
            }
            note.append("]");
            link.append("]");
        } else {
            note.append("[{name:'" + subject + ":无并列知识点',des:'" + subject + "',symbolSize:70,category:0}]");
            link.append("[{}]");
        }

        String a = note.substring(0, note.length());
        String b = link.substring(0, link.length());

        hashMap.put("NOTE", a);
        hashMap.put("LINK", b);
        return hashMap;
    }

    @Override
    public HashMap<String, String> findHasPostK(String subject) {
        List<Owl> owlList = owlRepository.findByPropertyAndSubject("hasPostK", subject);
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        if (!owlList.isEmpty()) {
            note.append("[{" + "name:'" + owlList.get(0).getSubject() + "',des:'" + owlList.get(0).getSubject() + "',symbolSize:60,category:0" + "},");
//        note.append("[");
            link.append("[");
            for (int i = 0; i < owlList.size(); i++) {
                Owl owl = owlList.get(i);
                //查询不能为空
                if (!owl.getObject().isEmpty() && hashSet.add(owl.getObject())) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject()) && i != owlList.size() - 1) {
                        note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "},");
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + "},");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "}");
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + "}");
                    }
                }
            }
            LinkedHashSet<String> count = new LinkedHashSet<>(hashSet);
//            Iterator iterator = hashSet.iterator();
//            for (int i = 0; i < hashSet.size(); i++) {
            while (!count.isEmpty()) {
                String next = count.iterator().next();
                count.remove(next);
                List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPostK", next);
                if (!owls.isEmpty()) {
                    for (Owl owl : owls) {
                        //查询不能为空
                        if (!owl.getObject().isEmpty() && hashSet.add(owl.getObject())) {
                            count.add(owl.getObject());
                            //判断subject和Object是否相同，和是否为最后一个
                            if (!owl.getSubject().equals(owl.getObject())) {
                                note.append(",{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "}");
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + "}");
                            }
                        }
                    }
                }
            }
            note.append("]");
            link.append("]");
        } else {
            note.append("[{name:'" + subject + ":无后继知识点',des:'" + subject + "',symbolSize:70,category:0}]");
            link.append("[{}]");
        }
        String a = note.substring(0, note.length());
        String b = link.substring(0, link.length());

        hashMap.put("NOTE", a);
        hashMap.put("LINK", b);

        return hashMap;
    }

    @Override
    public HashMap<String, String> findHasPreK(String subject) {
        List<Owl> owlList = owlRepository.findByPropertyAndSubject("hasPreK", subject);
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        if (!owlList.isEmpty()) {
            note.append("[{" + "name:'" + owlList.get(0).getSubject() + "',des:'" + owlList.get(0).getSubject() + "',symbolSize:60,category:0" + "},");
//        note.append("[");
            link.append("[");
            for (int i = 0; i < owlList.size(); i++) {
                Owl owl = owlList.get(i);
                //查询不能为空
                if (!owl.getObject().isEmpty() && hashSet.add(owl.getObject())) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject()) && i != owlList.size() - 1) {
                        note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "},");
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + "},");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "}");
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + "}");
                    }
                }
            }
            LinkedHashSet<String> count = new LinkedHashSet<>();
            count.addAll(hashSet);
            while (!count.isEmpty()) {
                String next = count.iterator().next();
                count.remove(next);
                List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPreK", next);
                if (!owls.isEmpty()) {
                    for (int j = 0; j < owls.size(); j++) {
                        Owl owl = owls.get(j);
                        //查询不能为空
                        if (!owl.getObject().isEmpty() && hashSet.add(owl.getObject())) {
                            count.add(owl.getObject());
                            //判断subject和Object是否相同，和是否为最后一个
                            if (!owl.getSubject().equals(owl.getObject())) {
                                note.append(",{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "}");
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + "}");
                            }
                        }
                    }
                }
            }
            note.append("]");
            link.append("]");
        } else {
            note.append("[{name:'" + subject + ":无前序知识点',des:'" + subject + "',symbolSize:70,category:0}]");
            link.append("[{}]");
        }
        String a = note.substring(0, note.length());
        String b = link.substring(0, link.length());

        hashMap.put("NOTE", a);
        hashMap.put("LINK", b);

        return hashMap;
    }

    @Override
    public HashMap<String, String> findAll(String subject) {
        /*isSiblingof知识点*/
        List<Owl> isSiblingofOwlList = owlRepository.findByPropertyAndSubject("isSiblingof", subject);
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        HashSet<String> hashSet = new HashSet<>();
        note.append("[{" + "name:'" + subject + "',des:'" + subject + "',symbolSize:60,category:0" + "},");
        link.append("[");
        hashSet.add(subject);
        if (!isSiblingofOwlList.isEmpty()) {
//        note.append("[");
            for (int i = 0; i < isSiblingofOwlList.size(); i++) {
                Owl owl = isSiblingofOwlList.get(i);
                if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                    if (i != isSiblingofOwlList.size() - 1) {
                        note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "},");
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'并列知识点'" + "},");
                    } else {
                        note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "}");
                        link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'并列知识点'" + "}");
                    }
                }
            }
        }
        if (note.toString().endsWith(",")) {
            note.delete(note.length() - 1, note.length());
        }
        if (link.toString().endsWith(",")) {
            link.delete(link.length() - 1, link.length());
        }
        /*查询hasPostK*/
        List<Owl> hasPostKOwlList = owlRepository.findByPropertyAndSubject("hasPostK", subject);
        HashSet<String> hasPostSet = new HashSet<>();
        if (!hasPostKOwlList.isEmpty()) {
            for (Owl owl : hasPostKOwlList) {
                //查询不能为空
                if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                        note.append(",{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:2" + "}");
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.2}}}");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.2 }}}");
                    }
                }
            }
            LinkedHashSet<String> count = new LinkedHashSet<>();
            count.addAll(hashSet);
            while (!count.isEmpty()) {
                String next = count.iterator().next();
                count.remove(next);
                List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPostK", next);
                if (!owls.isEmpty()) {
                    for (Owl owl : owls) {
                        //查询不能为空
                        if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                            //判断subject和Object是否相同，和是否为最后一个
                            if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                count.add(owl.getObject());
                                note.append(",{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:2" + "}");
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.2 }}}");
                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.2 }}}");
                            }
                        }
                    }

                }
            }
        }
        /*hasPreK查询*/
        List<Owl> hasPreKOwlList = owlRepository.findByPropertyAndSubject("hasPreK", subject);
        HashSet<String> hasPreKSet = new HashSet<>();
        if (!hasPreKOwlList.isEmpty()) {
            for (Owl owl : hasPreKOwlList) {
                //查询不能为空
                if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                        note.append(",{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:2" + "}");
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.2 }}}");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.2 }}}");
                    }
                }
            }
            LinkedHashSet<String> count = new LinkedHashSet<>();
            count.addAll(hashSet);
            while (!count.isEmpty()) {
                String next = count.iterator().next();
                count.remove(next);
                List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPreK", next);
                if (!owls.isEmpty()) {
                    for (Owl owl : owls) {
                        //查询不能为空
                        if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                            //判断subject和Object是否相同，和是否为最后一个
                            if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                count.add(owl.getObject());
                                note.append(",{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:2" + "}");
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.2 }}}");
                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.2 }}}");
                            }
                        }
                    }
                }

            }
        }
        /*test*/
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.addAll(hashSet);
        while (!linkedHashSet.isEmpty()) {
            String next = linkedHashSet.iterator().next();
            linkedHashSet.remove(next);
            List<Owl> owlList = owlRepository.findByPropertyAndSubject("relatedBook", next);
            if (!owlList.isEmpty()) {
                for (Owl owl : owlList) {
                    //添加查询出来的知识点，relatedBook关系
                    if (hashSet.add(owl.getObject())) {
                        note.append(",{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:40,category:3" + "}");
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'relatedBook关系'" + "}");
                    }
                    List<Owl> owls = owlRepository.findByPropertyAndSubject("type", owl.getObject());
                    if (!owls.isEmpty()) {
                        for (Owl owl1 : owls) {
                            //添加版本信息结点
                            if (owl1.getObject().startsWith("MathBook") && hashSet.add(owl1.getObject())) {
                                note.append(",{" + "name:'" + owl1.getObject() + "',des:'版本：" + owl1.getObject() + "',symbolSize:40,category:4" + "}");
                                link.append(",{" + "source:'" + owl1.getSubject() + "',target:'" + owl1.getObject() + "',name:'属于'" + "}");
                            } else if (owl1.getObject().startsWith("MathBook")) {
                                link.append(",{" + "source:'" + owl1.getSubject() + "',target:'" + owl1.getObject() + "',name:'属于'" + "}");
                            }
                        }
                    }
                }
            }
        }
        /**/

        if (link.toString().startsWith("[,")) {
            StringBuilder link2 = new StringBuilder();

            link2.append("[" + link.substring(2, link.length()));
            link = link2;
        }

        if (link.length() == 0) {
            StringBuilder note1 = new StringBuilder();
            StringBuilder link1 = new StringBuilder();
            note1.append("[{name:'" + subject + ":独立知识点',des:'" + subject + "',symbolSize:70,category:0}]");
            link1.append("[{}]");
            String a = note1.substring(0, note1.length());
            String b = link1.substring(0, link1.length());

            hashMap.put("NOTE", a);
            hashMap.put("LINK", b);

            return hashMap;
        } else {
            note.append("]");
            link.append("]");
        }
        String a = note.substring(0, note.length());
        String b = link.substring(0, link.length());

        hashMap.put("NOTE", a);
        hashMap.put("LINK", b);

        return hashMap;
    }

    @Override
    public HashMap<String, String> findK(String subject) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        List<Owl> owlList = owlRepository.findByPropertyAndSubject("relatedBook", subject);
        link.append("[");
        if (!owlList.isEmpty()) {
            note.append("[{" + "name:'" + subject + "',des:'" + subject + "',symbolSize:60,category:0" + "},");
            hashSet.add(subject);
            for (Owl owl : owlList) {
                //添加查询出来的知识点，relatedBook关系
                if (hashSet.add(owl.getObject())) {
                    note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:40,category:3" + "},");
                    link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'relatedBook关系'" + "},");
                }
                List<Owl> owls = owlRepository.findByPropertyAndSubject("type", owl.getObject());
                if (!owls.isEmpty()) {
                    for (Owl owl1 : owls) {
                        //添加版本信息结点
                        if (owl1.getObject().startsWith("MathBook") && hashSet.add(owl1.getObject())) {
                            note.append("{" + "name:'" + owl1.getObject() + "',des:'版本：" + owl1.getObject() + "',symbolSize:40,category:4" + "},");
                            link.append("{" + "source:'" + owl1.getSubject() + "',target:'" + owl1.getObject() + "',name:'属于'" + "},");
                        } else if (owl1.getObject().startsWith("MathBook")) {
                            link.append("{" + "source:'" + owl1.getSubject() + "',target:'" + owl1.getObject() + "',name:'属于'" + "},");
                        }
                    }
                }
            }
        }

        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        } else {
            note.append("]");
        }

        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link, 0, link.length() - 1);
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }
        String a = note.substring(0, note.length());
        String b = link.substring(0, link.length());
        if (a.equals("]")) {
            a = "[{name:'" + subject + ":无',des:'" + subject + "',symbolSize:60,category:0}]";
        }
        hashMap.put("NOTE", a);
        hashMap.put("LINK", b);

        return hashMap;
    }


    /*查询教育属性*/
    @Override
    public HashMap<String, String> findEducationProperty(String subject) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        note.append("[{" + "name:'" + subject + "',des:'" + subject + "',symbolSize:60,category:0" + "},");
        link.append("[");
        List<Owl> owlList1 = owlRepository.findByPropertyAndSubject("refertoSubject", subject);
        if (!owlList1.isEmpty()) {
            for (Owl owl : owlList1) {
                note.append("{" + "name:'" + owl.getObject() + "',des:'学科：" + owl.getObject() + "',symbolSize:60,category:1" + "},");
                link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'属于'" + "},");
            }
        }
        List<Owl> owlList2 = owlRepository.findByPropertyAndSubject("refertoPeriod", subject);
        if (!owlList2.isEmpty()) {
            for (Owl owl : owlList2) {
                note.append("{" + "name:'" + owl.getObject() + "',des:'学制：" + owl.getObject() + "',symbolSize:60,category:1" + "},");
                link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'属于'" + "},");
                if (owl.getObject().equals("初中学段")) {
                    note.append("{" + "name:'初一',des:'学制：初一',symbolSize:60,category:1" + "},");
                    note.append("{" + "name:'初二',des:'学制：初二',symbolSize:60,category:1" + "},");
                    note.append("{" + "name:'初三',des:'学制：初三',symbolSize:60,category:1" + "},");
                    link.append("{" + "source:'" + owl.getObject() + "',target:'初一',name:'属于'" + "},");
                    link.append("{" + "source:'" + owl.getObject() + "',target:'初二',name:'属于'" + "},");
                    link.append("{" + "source:'" + owl.getObject() + "',target:'初三',name:'属于'" + "},");

                }
            }
        }
        List<Owl> owlList3 = owlRepository.findByPropertyAndSubject("refertoBookVersion", subject);
        if (!owlList3.isEmpty()) {
            for (Owl owl : owlList3) {
                note.append("{" + "name:'" + owl.getObject() + "',des:'版本：" + owl.getObject() + "',symbolSize:60,category:1" + "},");
                link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'属于'" + "},");
            }
        }
        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        } else {
            note.append("]");
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link, 0, link.length() - 1);
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }
        String a = note.substring(0, note.length());
        String b = link.substring(0, link.length());
        hashMap.put("NOTE", a);
        hashMap.put("LINK", b);
        return hashMap;

    }


    /*_________________________________________________*/

    /*查询所有的教育属性*/
    @Override
    public StringBuilder findAllEducationProperty(String subject) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"Thing\",\"des\":\"Thing\",\"symbolSize\":60,\"category\":0},");
        link.append("[");
        List<Owl> owlList1 = owlRepository.findByPropertyAndObject("subClassOf", "教育基础属性");
        hashSet.add("教育基础属性");
        if (!owlList1.isEmpty()) {
            for (Owl owl : owlList1) {
                if (hashSet.add(owl.getSubject())) {
                    if (owl.getSubject().equals("高中") || owl.getSubject().equals("十二年一贯") || owl.getSubject().equals("初中") || owl.getSubject().equals("小学") || owl.getSubject().equals("完全中学") || owl.getSubject().equals("九年一贯")) {
                        note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":40,\"category\":2},");
                    } else {
                        note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":1" + "},");
                    }
                    if (!owl.getSubject().equals("高中") && !owl.getSubject().equals("十二年一贯") && !owl.getSubject().equals("初中") && !owl.getSubject().equals("小学") && !owl.getSubject().equals("完全中学") && !owl.getSubject().equals("九年一贯")) {
                        link.append("{\"source\":\"Thing\",\"target\":\"" + owl.getSubject() + "\",\"name\":\" \"" + "},");
                    }
                    //拿着教育基础属性下的类去查
                    List<Owl> owlList2 = owlRepository.findByPropertyAndObject("type", owl.getSubject());
                    if (!owlList2.isEmpty()) {
                        for (Owl owl2 : owlList2) {
                            if (hashSet.add(owl2.getSubject())) {
                                note.append("{\"name\":\"" + owl2.getSubject() + "\",\"des\":\"" + owl2.getSubject() + "\",\"symbolSize\":40,\"category\":2" + "},");
                                link.append("{\"source\":\"" + owl2.getObject() + "\",\"target\":\"" + owl2.getSubject() + "\",\"name\":\" \"" + "},");
                            } else {
                                link.append("{\"source\":\"" + owl2.getObject() + "\",\"target\":\"" + owl2.getSubject() + "\",\"name\":\" \"" + "},");
                            }
                        }
                    }
                }
            }
        }

        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link, 0, link.length() - 1);
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }
        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;
    }


    /*查询教材体系*/
    @Override
    public StringBuilder findTextbookSystem(String subject) {
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":60,\"category\":0},");
        link.append("[");
        List<Owl> owlList = owlRepository.findByPropertyAndObject("type", subject);
        if (!owlList.isEmpty()) {
            for (Owl owl : owlList) {
                if (hashSet.add(owl.getSubject())) {
                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "'\",\"symbolSize\":60,\"category\":1},");
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"},");
                }
            }
        }
        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString(), 0, link.length() - 1);
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }
        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;
    }

    /*查询所有的教材体系*/
    @Override
    public StringBuilder findAllTextbookSystem(String query) {
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"" + query + "\",\"des\":\"" + query + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> owlListAll = owlRepository.findByPropertyAndObjectContaining("type", "Book");
        if (!owlListAll.isEmpty()) {
            for (Owl owl : owlListAll) {
                if (hashSet.add(owl.getObject())) {
                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":60,\"category\":1" + "},");
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + query + "\",\"name\":\"属于\"" + "},");
                }
                if (hashSet.add(owl.getSubject())) {
                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":2},");
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"},");
                }
            }
        }
        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link, 0, link.length() - 1);
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }
        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;
    }

    /*再次查询教材体系（定位展开）*/
    @Override
    public StringBuilder TextbookSystem(String subject) {
        String[] strings = subject.split(",");
        subject = strings[0];
        String zj = strings[1];
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> owlList = owlRepository.findByPropertyAndObject("type", subject);
        if (!owlList.isEmpty()) {
            for (Owl owl : owlList) {
                if (hashSet.add(owl.getSubject())) {
                    if (!owl.getSubject().equals(zj)) {
                        note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":1" + "},");
                    } else {
                        note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":0" + "},");
                    }
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"" + "},");
                }
            }
        }

        List<Owl> owlList1 = owlRepository.findByPropertyAndObject("relatedBook", zj);
        if (!owlList1.isEmpty()) {
            for (Owl owl : owlList1) {
                if (hashSet.add(owl.getSubject())) {
                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":2" + "},");
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"拥有知识点\"" + "},");
                } else {
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"拥有知识点\"" + "},");
                }
            }
        }

        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link, 0, link.length() - 1);
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }
        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;
    }

    //查询知识点体系
    @Override
    public StringBuilder findKnowledgePointSystem(String query) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        HashSet<String> hashSet = new HashSet<>();
        String subject = null;
        String[] knowledge = query.split("@");
        if (knowledge.length > 1) {
            subject = knowledge[1];
            hashSet.add(subject);
            note.append("[{\"name\":\"" + knowledge[1] + "\",\"des\":\"" + knowledge[1] + "\",\"symbolSize\":60,\"category\":0" + "},");
            link.append("[");
            List<Owl> isSiblingofOwlList = owlRepository.findByPropertyAndSubject("isSiblingof", subject);
            if (!isSiblingofOwlList.isEmpty()) {
                for (Owl owl : isSiblingofOwlList) {
                    if (hashSet.add(owl.getObject())) {
                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
                    } else {
                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
                    }
                }
            }

            /*查询hasPostK*/
            List<Owl> hasPostKOwlList = owlRepository.findByPropertyAndSubject("hasPostK", subject);
            HashSet<String> hasPostSet = new HashSet<>();
            if (!hasPostKOwlList.isEmpty()) {
                for (Owl owl : hasPostKOwlList) {
                    //查询不能为空
                    if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                        //判断subject和Object是否相同，和是否为最后一个
                        if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                        } else if (!owl.getSubject().equals(owl.getObject())) {
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                        }
                    }
                }
                LinkedHashSet<String> count = new LinkedHashSet<>();
                count.addAll(hasPostSet);
                while (!count.isEmpty()) {
                    String next = count.iterator().next();
                    count.remove(next);
                    List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPostK", next);
                    if (!owls.isEmpty()) {
                        for (Owl owl : owls) {
                            //查询不能为空
                            if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                                //判断subject和Object是否相同，和是否为最后一个
                                if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                    count.add(owl.getObject());
                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");

                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");

                                }
                            }
                        }
                    }
                }
            }

            /*hasPreK查询*/
            List<Owl> hasPreKOwlList = owlRepository.findByPropertyAndSubject("hasPreK", subject);
            HashSet<String> hasPreKSet = new HashSet<>();
            if (!hasPreKOwlList.isEmpty()) {
                for (Owl owl : hasPreKOwlList) {
                    //查询不能为空
                    if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                        //判断subject和Object是否相同，和是否为最后一个
                        if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");

                        } else if (!owl.getSubject().equals(owl.getObject())) {
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");

                        }
                    }
                }
                LinkedHashSet<String> count = new LinkedHashSet<>();
                count.addAll(hasPreKSet);
                while (!count.isEmpty()) {
                    String next = count.iterator().next();
                    count.remove(next);
                    List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPreK", next);
                    if (!owls.isEmpty()) {
                        for (Owl owl : owls) {
                            //查询不能为空
                            if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                                //判断subject和Object是否相同，和是否为最后一个
                                if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                    count.add(owl.getObject());
                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");

                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");

                                }
                            }
                        }
                    }
                }
            }
            //relatedBook关系
            LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
            linkedHashSet.addAll(hashSet);
            while (!linkedHashSet.isEmpty()) {
                String next = linkedHashSet.iterator().next();
                linkedHashSet.remove(next);
                List<Owl> owlList = owlRepository.findByPropertyAndSubject("relatedBook", next);
                if (!owlList.isEmpty()) {
                    for (Owl owl : owlList) {
                        //添加查询出来的知识点，relatedBook关系
                        if (hashSet.add(owl.getObject())) {
                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                        } else {
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                        }
                        List<Owl> owls = owlRepository.findByPropertyAndSubject("type", owl.getObject());
                        if (!owls.isEmpty()) {
                            for (Owl owl1 : owls) {
                                //添加版本信息结点
                                if (owl1.getObject().startsWith("MathBook") && hashSet.add(owl1.getObject())) {
                                    note.append("{\"name\":\"" + owl1.getObject() + "\",\"des\":\"版本:" + owl1.getObject() + "\",\"symbolSize\":40,\"category\":3" + "},");
                                    link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                } else if (owl1.getObject().startsWith("MathBook")) {
                                    link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                }
                            }
                        }
                    }
                }
            }
            if (note.toString().endsWith(",")) {
                StringBuilder note1 = new StringBuilder();
                note1.append(note, 0, note.length() - 1);
                note1.append("]");
                note = note1;
            }
            if (link.toString().endsWith(",")) {
                StringBuilder link1 = new StringBuilder();
                link1.append(link, 0, link.length() - 1);
                link1.append("]");
                link = link1;
            } else {
                link.append("]");
            }

            StringBuilder result = new StringBuilder();
            result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
            return result;
        }

        note.append("[{\"name\":\"" + query + "\",\"des\":\"" + query + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> first = owlRepository.findByPropertyAndObject("equivalentClass", query + "知识点");
        if (!first.isEmpty()) {
            for (Owl owl : first) {
                subject = owl.getSubject();
            }
        }
        List<Owl> all = owlRepository.findByPropertyAndObject("type", subject);
        if (!all.isEmpty()) {
            for (Owl owlAll : all) {
                /*isSiblingof知识点*/
                subject = owlAll.getSubject();
                List<Owl> isSiblingofOwlList = owlRepository.findByPropertyAndSubject("isSiblingof", subject);
                if (hashSet.add(subject)) {
                    note.append("{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":50,\"category\":1" + "},");
                    link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");
                }
                if (!isSiblingofOwlList.isEmpty()) {
                    for (Owl owl : isSiblingofOwlList) {
                        if (hashSet.add(owl.getObject())) {
                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
                            link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");
                        } else {
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
                            link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                        }
                    }
                }

                /*查询hasPostK*/
                List<Owl> hasPostKOwlList = owlRepository.findByPropertyAndSubject("hasPostK", subject);
                HashSet<String> hasPostSet = new HashSet<>();
                if (!hasPostKOwlList.isEmpty()) {
                    for (Owl owl : hasPostKOwlList) {
                        //查询不能为空
                        if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                            //判断subject和Object是否相同，和是否为最后一个
                            if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                            }
                        }
                    }
                    LinkedHashSet<String> count = new LinkedHashSet<>();
                    count.addAll(hasPostSet);
                    while (!count.isEmpty()) {
                        String next = count.iterator().next();
                        count.remove(next);
                        List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPostK", next);
                        if (!owls.isEmpty()) {
                            for (Owl owl : owls) {
                                //查询不能为空
                                if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                                    //判断subject和Object是否相同，和是否为最后一个
                                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                        count.add(owl.getObject());
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                        link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                                    } else if (!owl.getSubject().equals(owl.getObject())) {
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                        link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                                    }
                                }
                            }
                        }
                    }
                }

                /*hasPreK查询*/
                List<Owl> hasPreKOwlList = owlRepository.findByPropertyAndSubject("hasPreK", subject);
                HashSet<String> hasPreKSet = new HashSet<>();
                if (!hasPreKOwlList.isEmpty()) {
                    for (Owl owl : hasPreKOwlList) {
                        //查询不能为空
                        if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                            //判断subject和Object是否相同，和是否为最后一个
                            if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                            }
                        }
                    }
                    LinkedHashSet<String> count = new LinkedHashSet<>();
                    count.addAll(hasPreKSet);
                    while (!count.isEmpty()) {
                        String next = count.iterator().next();
                        count.remove(next);
                        List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPreK", next);
                        if (!owls.isEmpty()) {
                            for (Owl owl : owls) {
                                //查询不能为空
                                if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                                    //判断subject和Object是否相同，和是否为最后一个
                                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                        count.add(owl.getObject());
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                        link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                                    } else if (!owl.getSubject().equals(owl.getObject())) {
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                        link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                                    }
                                }
                            }
                        }
                    }
                }

            }
            //relatedBook关系
            LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
            linkedHashSet.addAll(hashSet);
            while (!linkedHashSet.isEmpty()) {
                String next = linkedHashSet.iterator().next();
                linkedHashSet.remove(next);
                List<Owl> owlList = owlRepository.findByPropertyAndSubject("relatedBook", next);
                if (!owlList.isEmpty()) {
                    for (Owl owl : owlList) {
                        //添加查询出来的知识点，relatedBook关系
                        if (hashSet.add(owl.getObject())) {
                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                        } else {
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                        }
                        List<Owl> owls = owlRepository.findByPropertyAndSubject("type", owl.getObject());
                        if (!owls.isEmpty()) {
                            for (Owl owl1 : owls) {
                                //添加版本信息结点
                                if (owl1.getObject().startsWith("MathBook") && hashSet.add(owl1.getObject())) {
                                    note.append("{\"name\":\"" + owl1.getObject() + "\",\"des\":\"版本:" + owl1.getObject() + "\",\"symbolSize\":40,\"category\":3" + "},");
                                    link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                } else if (owl1.getObject().startsWith("MathBook")) {
                                    link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                }
                            }
                        }
                    }
                }
            }

        }


        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link, 0, link.length() - 1);
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }

        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;
    }

    /*查询所有的知识点体系(新)*/
    @Override
    public StringBuilder findAllKnowledgePointSystem(String subject) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        HashSet<String> hashSet = new HashSet<>();
        note.append("[{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
//        先查出数学知识点体系有哪个几个
        List<Owl> first = owlRepository.findByPropertyAndSubjectContaining("subClassOf", "数学知识点");
        if (!first.isEmpty()) {
            for (Owl o : first) {
                if (o.getObject().startsWith("MathK") && hashSet.add(o.getObject())) {
//                   //增加版本数据
                    List<Owl> all = owlRepository.findByPropertyAndObject("type", o.getObject());
                    note.append("{\"name\":\"" + o.getSubject() + "\",\"des\":\"" + o.getSubject() + "\",\"symbolSize\":50,\"category\":0},");
                    link.append("{\"source\":\"" + subject + "\",\"target\":\"" + o.getSubject() + "\"},");
                    if (!all.isEmpty()) {
                        for (Owl owlAll : all) {

//                            isSiblingof知识点
                            subject = owlAll.getSubject();
                            List<Owl> isSiblingofOwlList = owlRepository.findByPropertyAndSubject("isSiblingof", subject);
                            if (hashSet.add(subject)) {
                                note.append("{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":50,\"category\":1" + "},");
                                link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + subject + "\"},");

                            }
                            if (!isSiblingofOwlList.isEmpty()) {
                                for (Owl owl : isSiblingofOwlList) {
                                    if (hashSet.add(owl.getObject())) {
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
                                        link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                    } else {
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
                                        link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                    }
                                }
                            }

//                            查询hasPostK
                            List<Owl> hasPostKOwlList = owlRepository.findByPropertyAndSubject("hasPostK", subject);
                            HashSet<String> hasPostSet = new HashSet<>();
                            if (!hasPostKOwlList.isEmpty()) {
                                for (Owl owl : hasPostKOwlList) {
                                    //查询不能为空
                                    if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                                        //判断subject和Object是否相同，和是否为最后一个
                                        if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                            link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                        } else if (!owl.getSubject().equals(owl.getObject())) {
                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                            link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                        }
                                    }
                                }
                                LinkedHashSet<String> count = new LinkedHashSet<>();
                                count.addAll(hasPostSet);
                                while (!count.isEmpty()) {
                                    String next = count.iterator().next();
                                    count.remove(next);
                                    List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPostK", next);
                                    if (!owls.isEmpty()) {
                                        for (Owl owl : owls) {
                                            //查询不能为空
                                            if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                                                //判断subject和Object是否相同，和是否为最后一个
                                                if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                                    count.add(owl.getObject());
                                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                                    link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": {\" curveness\": 0.2 }}},");
                                                    link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            //hasPreK查询
                            List<Owl> hasPreKOwlList = owlRepository.findByPropertyAndSubject("hasPreK", subject);
                            HashSet<String> hasPreKSet = new HashSet<>();
                            if (!hasPreKOwlList.isEmpty()) {
                                for (Owl owl : hasPreKOwlList) {
                                    //查询不能为空
                                    if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                                        //判断subject和Object是否相同，和是否为最后一个
                                        if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                            link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                        } else if (!owl.getSubject().equals(owl.getObject())) {
                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                            link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                        }
                                    }
                                }
                                LinkedHashSet<String> count = new LinkedHashSet<>();
                                count.addAll(hasPreKSet);
                                while (!count.isEmpty()) {
                                    String next = count.iterator().next();
                                    count.remove(next);
                                    List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPreK", next);
                                    if (!owls.isEmpty()) {
                                        for (Owl owl : owls) {
                                            //查询不能为空
                                            if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                                                //判断subject和Object是否相同，和是否为最后一个
                                                if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                                    count.add(owl.getObject());
                                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                                    link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                                    link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                                }
                                            }
                                        }
                                    }
                                }
                            }

//                            查询参考知识点
                            List<Owl> hasRefKList = owlRepository.findByPropertyAndSubject("hasRefK", subject);
                            if (!hasRefKList.isEmpty()) {
                                for (Owl owl : hasRefKList) {
                                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des:\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"参考知识点\"" + "},");
                                    }
                                }
                            }

                        }
                        //relatedBook关系
                        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
                        linkedHashSet.addAll(hashSet);
                        while (!linkedHashSet.isEmpty()) {
                            String next = linkedHashSet.iterator().next();
                            linkedHashSet.remove(next);
                            List<Owl> owlList = owlRepository.findByPropertyAndSubject("relatedBook", next);
                            if (!owlList.isEmpty()) {
                                for (Owl owl : owlList) {
                                    //添加查询出来的知识点，relatedBook关系
                                    if (hashSet.add(owl.getObject())) {
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    } else {
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    }
                                    List<Owl> owls = owlRepository.findByPropertyAndSubject("type", owl.getObject());
                                    if (!owls.isEmpty()) {
                                        for (Owl owl1 : owls) {
                                            //添加版本信息结点
                                            if (owl1.getObject().startsWith("MathBook") && hashSet.add(owl1.getObject())) {
                                                note.append("{\"name\":\"" + owl1.getObject() + "\",\"des\":\"版本：" + owl1.getObject() + "\",\"symbolSize\":40,\"category\":3" + "},");
                                                link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                            } else if (owl1.getObject().startsWith("MathBook")) {
                                                link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }


        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note = note1;
            note.append("]");
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link, 0, link.length() - 1);
            link = link1;
            link.append("]");
        } else {
            link.append("]");
        }


       /* String a = note.substring(0, note.length());
        String b = link.substring(0, link.length());

        hashMap.put("NOTE", a);
        hashMap.put("LINK", b);*/
        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;
    }

    //查询知识图谱
    @Override
    public StringBuilder findKnowledgeGraph(String subject) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"Thing\",\"des\":\"Thing\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> owlList1 = owlRepository.findByPropertyAndObject("subClassOf", "教育基础属性");
        hashSet.add("教育基础属性");
        note.append("{\"name\":\"教育基础属性\",\"des\":\"教育基础属性\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("{\"source\":\"Thing\",\"target\":\"教育基础属性\"},");
        if (!owlList1.isEmpty()) {
            for (Owl owl : owlList1) {
                if (hashSet.add(owl.getSubject())) {
                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":1" + "},");
                    link.append("{\"source\":\"教育基础属性\",\"target\":\"" + owl.getSubject() + "\",\"name\":\" \"" + "},");
                    //拿着教育基础属性下的类去查
                    List<Owl> owlList2 = owlRepository.findByPropertyAndObject("type", owl.getSubject());
                    if (!owlList2.isEmpty()) {
                        for (Owl owl2 : owlList2) {
                            if (hashSet.add(owl2.getSubject())) {
                                note.append("{\"name\":\"" + owl2.getSubject() + "\",\"des\":\"" + owl2.getSubject() + "\",\"symbolSize\":40,\"category\":1" + "},");
                                link.append("{\"source\":\"" + owl2.getObject() + "\",\"target\":\"" + owl2.getSubject() + "\",\"name\":\" \"" + "},");
                            } else {
                                link.append("{\"source\":\"" + owl2.getObject() + "\",\"target\":\"" + owl2.getSubject() + "\",\"name\":\" \"" + "},");
                            }
                        }
                    }
                }
            }
        }

        //所有教材体系
        note.append("{\"name\":\"所有教材体系\",\"des\":\"所有教材体系\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("{\"source\":\"Thing\",\"target\":\"所有教材体系\"},");
        List<Owl> owlListAll = owlRepository.findByPropertyAndObjectContaining("type", "Book");
        if (!owlListAll.isEmpty()) {
            for (Owl owl : owlListAll) {
                if (hashSet.add(owl.getObject())) {
                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":60,\"category\":3" + "},");
                    link.append("{\"source\":\"所有教材体系\",\"target\":\"" + owl.getObject() + "\",\"name\":\"属于\"" + "},");
                }
                if (hashSet.add(owl.getSubject())) {
                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":2" + "},");
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"" + "},");
                } else {
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"" + "},");
                }
            }
        }

        /********************************/
        /*所有的知识点体系*/

        note.append("{\"name\":\"所有的知识点体系\",\"des\":\"所有的知识点体系\",\"symbolSize\":50,\"category\":0" + "},");
        link.append("{\"source\":\"Thing\",\"target\":\"所有的知识点体系\"},");
        List<Owl> first = owlRepository.findByPropertyAndSubjectContaining("subClassOf", "数学知识点");
        if (!first.isEmpty()) {
            for (Owl o : first) {
                if (o.getObject().startsWith("MathK") && hashSet.add(o.getObject())) {
                    note.append("{\"name\":\"" + o.getObject() + "\",\"des\":\"" + o.getObject() + "\",\"symbolSize\":50,\"category\":3" + "},");
                    link.append("{\"source\":\"所有的知识点体系\",\"target\":\"" + o.getObject() + "\"},");
//                   //增加版本数据
                    List<Owl> all = owlRepository.findByPropertyAndObject("type", o.getObject());
                    if (!all.isEmpty()) {
                        for (Owl owlAll : all) {
                            /*isSiblingof知识点*/
                            subject = owlAll.getSubject();
                            List<Owl> isSiblingofOwlList = owlRepository.findByPropertyAndSubject("isSiblingof", subject);
                            if (hashSet.add(subject)) {
                                note.append("{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":50,\"category\":4" + "},");
                                link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + subject + "\"},");
                            } else {
                                link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + subject + "\"},");
                            }
                            if (!isSiblingofOwlList.isEmpty()) {
                                for (Owl owl : isSiblingofOwlList) {
                                    if (hashSet.add(owl.getObject())) {
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
                                        link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                    } else {
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
                                        link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                    }
                                }
                            }

                            /*查询hasPostK*/
                            List<Owl> hasPostKOwlList = owlRepository.findByPropertyAndSubject("hasPostK", subject);
                            HashSet<String> hasPostSet = new HashSet<>();
                            if (!hasPostKOwlList.isEmpty()) {
                                for (Owl owl : hasPostKOwlList) {
                                    //查询不能为空
                                    if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                                        //判断subject和Object是否相同，和是否已存在
                                        if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                            link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                        } else if (!owl.getSubject().equals(owl.getObject())) {
                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
                                            link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                        }
                                    }
                                }
                                LinkedHashSet<String> count = new LinkedHashSet<>();
                                count.addAll(hasPostSet);
                                while (!count.isEmpty()) {
                                    String next = count.iterator().next();
                                    count.remove(next);
                                    List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPostK", next);
                                    if (!owls.isEmpty()) {
                                        for (Owl owl : owls) {
                                            //查询不能为空
                                            if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                                                //判断subject和Object是否相同，和是否为最后一个
                                                if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                                    count.add(owl.getObject());
                                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                                    link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                                    link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            /*hasPreK查询*/
                            List<Owl> hasPreKOwlList = owlRepository.findByPropertyAndSubject("hasPreK", subject);
                            HashSet<String> hasPreKSet = new HashSet<>();
                            if (!hasPreKOwlList.isEmpty()) {
                                for (Owl owl : hasPreKOwlList) {
                                    //查询不能为空
                                    if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                                        //判断subject和Object是否相同，和是否为最后一个
                                        if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                            link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                        } else if (!owl.getSubject().equals(owl.getObject())) {
                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                            link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                        }
                                    }
                                }
                                LinkedHashSet<String> count = new LinkedHashSet<>();
                                count.addAll(hasPreKSet);
                                while (!count.isEmpty()) {
                                    String next = count.iterator().next();
                                    count.remove(next);
                                    List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPreK", next);
                                    if (!owls.isEmpty()) {
                                        for (Owl owl : owls) {
                                            //查询不能为空
                                            if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                                                //判断subject和Object是否相同，和是否为最后一个
                                                if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                                    count.add(owl.getObject());
                                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                                    link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
                                                    link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            /*查询参考知识点*/
                            List<Owl> hasRefKList = owlRepository.findByPropertyAndSubject("hasRefK", subject);
                            if (!hasRefKList.isEmpty()) {
                                for (Owl owl : hasRefKList) {
                                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"参考知识点\"" + "},");
                                    }
                                }
                            }

                        }
                        //relatedBook关系
                        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
                        linkedHashSet.addAll(hashSet);
                        while (!linkedHashSet.isEmpty()) {
                            String next = linkedHashSet.iterator().next();
                            linkedHashSet.remove(next);
                            List<Owl> owlList = owlRepository.findByPropertyAndSubject("relatedBook", next);
                            if (!owlList.isEmpty()) {
                                for (Owl owl : owlList) {
                                    //添加查询出来的知识点，relatedBook关系
                                    if (hashSet.add(owl.getObject())) {
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    } else {
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    }
                                    List<Owl> owls = owlRepository.findByPropertyAndSubject("type", owl.getObject());
                                    if (!owls.isEmpty()) {
                                        for (Owl owl1 : owls) {
                                            //添加版本信息结点
                                            if (owl1.getObject().startsWith("MathBook") && hashSet.add(owl1.getObject())) {
                                                note.append("{\"name\":\"" + owl1.getObject() + "\",\"des\":\"版本:" + owl1.getObject() + "\",\"symbolSize\":40,\"category\":3" + "},");
                                                link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                            } else if (owl1.getObject().startsWith("MathBook")) {
                                                link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link, 0, link.length() - 1);
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }

        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;

    }

    /**
     * 查询前后序和参考知识点
     *
     * @param subject
     * @param number
     * @return
     */
    @Override
    public StringBuilder findIsSiblingOfAndRefK(String subject, int number) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("{\"search\":\"" + subject + "\",");
        /*查询hasPostK*/
        List<Owl> hasPostKOwlList = owlRepository.findByPropertyAndSubject("hasPostK", subject);
        HashSet<String> hasPostSet = new HashSet<>();
        int countPost = 0;
        List<String> postList = new ArrayList<>();
        if (!hasPostKOwlList.isEmpty()) {
            for (Owl owl : hasPostKOwlList) {
                //查询不能为空
                if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject())) {
                        postList.add(owl.getObject());
                        countPost++;
                        if (countPost >= number) {
                            break;
                        }
                    }
                }
            }
            LinkedHashSet<String> count = new LinkedHashSet<>();
            count.addAll(hasPostSet);
            if (countPost < number) {
                loop:
                while (!count.isEmpty()) {
                    String next = count.iterator().next();
                    count.remove(next);
                    List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPostK", next);
                    if (!owls.isEmpty()) {
                        //控制当前查询的层数
                        countPost++;
                        if (countPost > number) {
                            break loop;
                        }
                        for (Owl owl : owls) {
                            //查询不能为空
                            if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                                //判断subject和Object是否相同，和是否为最后一个
                                if (!owl.getSubject().equals(owl.getObject())) {
                                    count.add(owl.getObject());
                                    postList.add(owl.getObject());
                                }
                            }
                        }
                    }
                }
            }
        }

        /*hasPreK查询*/
        List<Owl> hasPreKOwlList = owlRepository.findByPropertyAndSubject("hasPreK", subject);
        HashSet<String> hasPreKSet = new HashSet<>();
        int countPreK = 0;
        List<String> preList = new ArrayList<>();
        if (!hasPreKOwlList.isEmpty()) {
            for (Owl owl : hasPreKOwlList) {
                //查询不能为空
                if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject())) {
                        preList.add(owl.getObject());
                        countPreK++;
                        if (countPreK >= number) {
                            break;
                        }
                    }
                }
            }
            LinkedHashSet<String> count = new LinkedHashSet<>();
            count.addAll(hasPreKSet);
            if (countPreK < number) {
                loop:
                while (!count.isEmpty()) {
                    String next = count.iterator().next();
                    count.remove(next);
                    List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPreK", next);
                    if (!owls.isEmpty()) {
                        //控制查询层级
                        countPreK++;
                        if (countPreK > number) {
                            break loop;
                        }
                        for (Owl owl : owls) {
                            //查询不能为空
                            if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                                //判断subject和Object是否相同，和是否为最后一个
                                if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                    count.add(owl.getObject());
                                    preList.add(owl.getObject());
                                }
                            }
                        }
                    }
                }
            }
        }


        /*查询参考知识点*/
        List<Owl> hasRefKList = owlRepository.findByPropertyAndSubject("hasRefK", subject);
        List<String> refList = new ArrayList<>();
        if (!hasRefKList.isEmpty()) {
            for (Owl owl : hasRefKList) {
                if (!owl.getSubject().equals(owl.getObject())) {
                    refList.add(owl.getObject());
                }
            }
        }
        String pre = preList.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
        String post = postList.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
        String ref = refList.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));

        note.append("\"qx\":[" + pre + "],\"hx\":[" + post + "],\"ck\":[" + ref + "]}");

        return note;
    }

    /**
     * 查询所有的教材体系
     *
     * @param query
     * @return
     */
    @Override
    public StringBuilder findAllTextbookSystemAndEducationProperty(String query) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"" + query + "\",\"des\":\"" + query + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> owlListAll = owlRepository.findByPropertyAndObjectContaining("type", "Book");
        if (!owlListAll.isEmpty()) {
            for (Owl owl : owlListAll) {
                if (hashSet.add(owl.getObject())) {
                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":60,\"category\":1" + "},");
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + query + "\",\"name\":\"属于\"" + "},");
                }
                if (hashSet.add(owl.getSubject())) {
                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":2},");
                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"},");
                }
                List<Owl> relations = owlRepository.findByPropertyContainingAndSubject("referto", owl.getSubject());
                if (!relations.isEmpty()) {
                    for (Owl relationOwl : relations) {
                        if (hashSet.add(relationOwl.getObject())) {
                            note.append("{\"name\":\"" + relationOwl.getObject() + "\",\"des\":\"" + relationOwl.getObject() + "\",\"symbolSize\":60,\"category\":3" + "},");
                            link.append("{\"source\":\"" + relationOwl.getSubject() + "\",\"target\":\"" + relationOwl.getObject() + "\",\"name\":\"教育属性\"" + "},");
                        } else {
                            link.append("{\"source\":\"" + relationOwl.getSubject() + "\",\"target\":\"" + relationOwl.getObject() + "\",\"name\":\"教育属性\"" + "},");
                        }
                    }
                }
            }
        }
        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note, 0, note.length() - 1);
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link, 0, link.length() - 1);
            link1.append("]");
            link = link1;
        } else {
            link.append("]");
        }
        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;
    }

    /**
     * @return 返回是否推理成功
     */
    @Override
    public Boolean reasoning() {
        //创建模型
        Model m = ModelFactory.createDefaultModel();
        Resource configuration = m.createResource();
        configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
        //推理规则文件加载
        configuration.addProperty(ReasonerVocabulary.PROPruleSet, "mathv4.1.rules");
        // 创建这样一个推理机的实例
        Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
        //本体文件加载
        Model data = FileManager.get().loadModel("/opt/wangjie/owlapi/owlapi/src/main/resources/jena/mathv4.1.owl");
        //
        InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
        StmtIterator i = infmodel.listStatements();

        int count = 1;
        List<Owl> owlList = new ArrayList<>();
        while (i.hasNext()) {
            Owl owl = new Owl();
            Statement stmt = i.nextStatement();
            System.out.println("subject:" + stmt.getSubject() + "***property:" + stmt.getPredicate() + "***object:" + stmt.getObject());
            count++;

            //处理数据subject
            String subject = stmt.getSubject().toString();
            if (subject != null && stmt.getSubject().toString().split("#").length > 1) {
                subject = stmt.getSubject().toString().split("#")[1];
                owl.setSubject(subject);
            } else {
                continue;
            }
            System.out.println(subject);

            //取数据property
            String property = stmt.getPredicate().getLocalName();
            owl.setProperty(property);
            System.out.println(property);

            //截取object字段
            String object = stmt.getObject().toString();
            if (object != null && stmt.getObject().toString().split("#").length > 1) {
                object = stmt.getObject().toString().split("#")[1];
                owl.setObject(object);
            } else {
                continue;

            }
            System.out.println(object);
            //如果全是英文跳出循环
            if (object.matches("[a-zA-Z]+") && subject.matches("[a-zA-Z]+")) {
                count--;
                continue;
            }

            owlList.add(owl);

        }
        String str = JSON.toJSON(owlList).toString();
        System.out.println(str);
        System.out.println("推理结束" + count);
        //库里的数据
        List<Owl> all = owlRepository.findAll();
        //需要更新的
        List<Owl> update = new ArrayList<>();
        for (Owl owl : owlList) {
            for (Owl owl1 : all) {
                //判断是否在库里
                if (!owl1.getObject().equals(owl.getObject()) && !owl1.getSubject().equals(owl.getSubject()) && !owl1.getProperty().equals(owl.getProperty())) {
                    update.add(owl);
                }
            }
        }
        owlRepository.saveAll(update);
        return true;
    }

    public List<Owl> getData() {
        if (!redisTemplate.hasKey("owl")) {
            logger.info("redis中数据不存在");
            List<Owl> all = owlRepository.findAll();
            redisTemplate.opsForValue().set("owl", JSON.toJSON(all).toString());
            logger.info("重新载入数据成功");
        }
        logger.info("读取redis数据");
        String owlString = (String) redisTemplate.opsForValue().get("owl");
        JSONArray objects = JSON.parseArray(owlString);
        List<Owl> owls = new ArrayList<>();
        for (Object object : objects) {
            JSONObject jsonObject = (JSONObject) object;
            Owl owl = JSONObject.toJavaObject(jsonObject, Owl.class);
            owls.add(owl);
        }
        return owls;
    }

}


