package com.timeep.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.timeep.dao.OwlRepository;
import com.timeep.po.Owl;
import com.timeep.vo.Link;
import com.timeep.vo.Note;
import com.timeep.vo.SearchResult;
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
public class MainServiceImpl implements MainService {
    @Autowired
    private OwlRepository owlRepository;
    @Autowired
    private RedisTemplate redisTemplate;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 查询所有的教育属性
     *
     * @param subject 查询内容
     * @return 数据
     */
    @Override
    public StringBuilder findAllEducationProperty(String subject) {
        //获取数据
        List<Owl> data = getData();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        SearchResult searchResult = new SearchResult();
        List<Note> noteList = new ArrayList<>();
        List<Link> linkList = new ArrayList<>();
        noteList.add(new Note("Thing", "Thing", 60, 0));
        List<Owl> owlList1 = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("subClassOf") && owl.getObject().equals("教育基础属性")) {
                owlList1.add(owl);
            }
        }
        hashSet.add("教育基础属性");
        if (!owlList1.isEmpty()) {
            for (Owl owl : owlList1) {
                if (hashSet.add(owl.getSubject())) {
                    if ("高中".equals(owl.getSubject()) || "十二年一贯".equals(owl.getSubject()) || "初中".equals(owl.getSubject()) || "小学".equals(owl.getSubject()) || "完全中学".equals(owl.getSubject()) || "九年一贯".equals(owl.getSubject())) {
                        noteList.add(new Note(owl.getSubject(), owl.getSubject(), 40, 2));
                    } else {
                        noteList.add(new Note(owl.getSubject(), owl.getSubject(), 60, 1));
                    }
                    if (!"高中".equals(owl.getSubject()) && !"十二年一贯".equals(owl.getSubject()) && !"初中".equals(owl.getSubject()) && !"小学".equals(owl.getSubject()) && !"完全中学".equals(owl.getSubject()) && !"九年一贯".equals(owl.getSubject())) {
                        linkList.add(new Link("Thing", owl.getSubject(), " "));
                    }
                    //拿着教育基础属性下的类去查
                    List<Owl> owlList2 = new ArrayList<>();
                    for (Owl o : data) {
                        //拿着教育基础属性下的类去查
                        if (o.getProperty().equals("type") && o.getObject().equals(owl.getSubject())) {
                            owlList2.add(o);
                        }
                    }
                    if (!owlList2.isEmpty()) {
                        for (Owl owl2 : owlList2) {
                            if (hashSet.add(owl2.getSubject())) {
                                noteList.add(new Note(owl2.getSubject(), owl2.getSubject(), 40, 2));
                                linkList.add(new Link(owl2.getObject(), owl2.getSubject(), " "));
                            } else {
                                linkList.add(new Link(owl2.getObject(), owl2.getSubject(), " "));
                            }
                        }
                    }
                }
            }
        }
        String note = JSON.toJSON(noteList).toString();
        String link = JSON.toJSON(linkList).toString();

        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;
    }

    /**
     * 查询教材体系
     *
     * @param subject 查询内容
     * @return 数据
     */
    @Override
    public StringBuilder findTextbookSystem(String subject) {
        //从redis中获取数据
        List<Owl> data = getData();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":60,\"category\":0},");
        link.append("[");
        List<Owl> owlList = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("type") && owl.getObject().equals(subject)) {
                owlList.add(owl);
            }
        }
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
            note1.append(note.toString().substring(0, note.length() - 1));
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString().substring(0, link.length() - 1));
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
     * 查询所有的教材体系
     *
     * @param query 查询内容
     * @return 数据
     */
    @Override
    public StringBuilder findAllTextbookSystem(String query) {
        List<Owl> data = getData();
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"" + query + "\",\"des\":\"" + query + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> owlListAll = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("type") && owl.getObject().indexOf("Book") != -1) {
                owlListAll.add(owl);
            }
        }
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
            note1.append(note.toString().substring(0, note.length() - 1));
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString().substring(0, link.length() - 1));
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
     * 再次查询教材体系（定位展开）
     *
     * @param subject 查询内容
     * @return 数据
     */
    @Override
    public StringBuilder TextbookSystem(String subject) {
        List<Owl> data = getData();
        String[] strings = subject.split(",");
        subject = strings[0];
        String zj = strings[1];
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> owlList = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("type") && owl.getObject().equals(subject)) {
                owlList.add(owl);
            }
        }
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

        List<Owl> owlList1 = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("relatedBook") && owl.getObject().equals(zj)) {
                owlList1.add(owl);
            }
        }
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
            note1.append(note.toString().substring(0, note.length() - 1));
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString().substring(0, link.length() - 1));
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
     * 查询知识点体系
     *
     * @param query 查询内容
     * @return 数据
     */
    @Override
    public StringBuilder findKnowledgePointSystem(String query) {
        List<Owl> data = getData();
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
            List<Owl> isSiblingofOwlList = new ArrayList<>();
            for (Owl owl : data) {
                //
                if (owl.getProperty().equals("isSiblingof") && owl.getSubject().equals(subject)) {
                    isSiblingofOwlList.add(owl);
                }
            }
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
            List<Owl> hasPostKOwlList = new ArrayList<>();
            for (Owl owl : data) {
                //
                if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(subject)) {
                    hasPostKOwlList.add(owl);
                }
            }
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
                    List<Owl> owls = new ArrayList<>();
                    for (Owl owl : data) {
                        //
                        if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(next)) {
                            owls.add(owl);
                        }
                    }
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
            List<Owl> hasPreKOwlList = new ArrayList<>();
            for (Owl owl : data) {
                //hasPreK查询
                if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(subject)) {
                    hasPreKOwlList.add(owl);
                }
            }
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
                    List<Owl> owls = new ArrayList<>();
                    for (Owl owl : data) {
                        //遍历前序知识点
                        if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(next)) {
                            owls.add(owl);
                        }
                    }
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
                List<Owl> owlList = new ArrayList<>();
                for (Owl owl : data) {
                    //查询relatedBook关系
                    if (owl.getProperty().equals("relatedBook") && owl.getSubject().equals(next)) {
                        owlList.add(owl);
                    }
                }
                if (!owlList.isEmpty()) {
                    for (Owl owl : owlList) {
                        //添加查询出来的知识点，relatedBook关系
                        if (hashSet.add(owl.getObject())) {
                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                        } else {
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                        }
                        List<Owl> owls = new ArrayList<>();
                        for (Owl owl1 : data) {
                            //查询relatedBook关系
                            if (owl1.getProperty().equals("type") && owl1.getSubject().equals(owl.getObject())) {
                                owls.add(owl1);
                            }
                        }
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
                note1.append(note.toString().substring(0, note.length() - 1));
                note1.append("]");
                note = note1;
            }
            if (link.toString().endsWith(",")) {
                StringBuilder link1 = new StringBuilder();
                link1.append(link.toString().substring(0, link.length() - 1));
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
        List<Owl> first = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("equivalentClass") && owl.getObject().equals(query + "知识点")) {
                first.add(owl);
            }
        }
        if (!first.isEmpty()) {
            for (Owl owl : first) {
                subject = owl.getSubject();
            }
        }
        List<Owl> all = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("type") && owl.getObject().equals(subject)) {
                all.add(owl);
            }
        }
        if (!all.isEmpty()) {
            for (Owl owlAll : all) {
                /*isSiblingof知识点*/
                subject = owlAll.getSubject();
                List<Owl> isSiblingofOwlList = new ArrayList<>();
                for (Owl owl : data) {
                    //
                    if (owl.getProperty().equals("isSiblingof") && owl.getSubject().equals(subject)) {
                        isSiblingofOwlList.add(owl);
                    }
                }
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
                List<Owl> hasPostKOwlList = new ArrayList<>();
                for (Owl owl : data) {
                    //
                    if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(subject)) {
                        hasPostKOwlList.add(owl);
                    }
                }
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
                        List<Owl> owls = new ArrayList<>();
                        for (Owl owl : data) {
                            //
                            if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(next)) {
                                owls.add(owl);
                            }
                        }
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
                List<Owl> hasPreKOwlList = new ArrayList<>();
                for (Owl owl : data) {
                    //
                    if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(subject)) {
                        hasPreKOwlList.add(owl);
                    }
                }
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
                        List<Owl> owls = new ArrayList<>();
                        for (Owl owl : data) {
                            //
                            if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(next)) {
                                owls.add(owl);
                            }
                        }
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
                List<Owl> owlList = new ArrayList<>();
                for (Owl owl : data) {
                    //
                    if (owl.getProperty().equals("relatedBook") && owl.getSubject().equals(next)) {
                        owlList.add(owl);
                    }
                }
                if (!owlList.isEmpty()) {
                    for (Owl owl : owlList) {
                        //添加查询出来的知识点，relatedBook关系
                        if (hashSet.add(owl.getObject())) {
                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                        } else {
                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                        }
                        List<Owl> owls = new ArrayList<>();
                        for (Owl owl1 : data) {
                            //
                            if (owl1.getProperty().equals("type") && owl1.getSubject().equals(owl.getObject())) {
                                owls.add(owl1);
                            }
                        }
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
            note1.append(note.toString().substring(0, note.length() - 1));
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString().substring(0, link.length() - 1));
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
     * 查询所有的知识点体系(新)
     *
     * @param subject 查询内容
     * @return 数据
     */
    @Override
    public StringBuilder findAllKnowledgePointSystem(String subject) {
        List<Owl> data = getData();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        HashSet<String> hashSet = new HashSet<>();
        note.append("[{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
//        先查出数学知识点体系有哪个几个
        List<Owl> first = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("subClassOf") && owl.getSubject().indexOf("数学知识点") != -1) {
                first.add(owl);
            }
        }
        if (!first.isEmpty()) {
            for (Owl o : first) {
                if (o.getObject().startsWith("MathK") && hashSet.add(o.getObject())) {
//                   //增加版本数据
                    List<Owl> all = new ArrayList<>();
                    for (Owl owl : data) {
                        //
                        if (owl.getProperty().equals("type") && owl.getObject().equals(o.getObject())) {
                            all.add(owl);
                        }
                    }
                    note.append("{\"name\":\"" + o.getSubject() + "\",\"des\":\"" + o.getSubject() + "\",\"symbolSize\":50,\"category\":0},");
                    link.append("{\"source\":\"" + subject + "\",\"target\":\"" + o.getSubject() + "\"},");
                    if (!all.isEmpty()) {
                        for (Owl owlAll : all) {

//                            isSiblingof知识点
                            subject = owlAll.getSubject();
                            List<Owl> isSiblingofOwlList = new ArrayList<>();
                            for (Owl owl : data) {
                                //
                                if (owl.getProperty().equals("isSiblingof") && owl.getSubject().equals(subject)) {
                                    isSiblingofOwlList.add(owl);
                                }
                            }
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
                            List<Owl> hasPostKOwlList = new ArrayList<>();
                            for (Owl owl : data) {
                                //
                                if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(subject)) {
                                    hasPostKOwlList.add(owl);
                                }
                            }
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
                                    List<Owl> owls = new ArrayList<>();
                                    for (Owl owl : data) {
                                        //
                                        if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(next)) {
                                            owls.add(owl);
                                        }
                                    }
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
                            List<Owl> hasPreKOwlList = new ArrayList<>();
                            for (Owl owl : data) {
                                //
                                if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(subject)) {
                                    hasPreKOwlList.add(owl);
                                }
                            }
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
                                    List<Owl> owls = new ArrayList<>();
                                    for (Owl owl : data) {
                                        //
                                        if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(next)) {
                                            owls.add(owl);
                                        }
                                    }
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
                            List<Owl> hasRefKList = new ArrayList<>();
                            for (Owl owl : data) {
                                //
                                if (owl.getProperty().equals("hasRefK") && owl.getSubject().equals(subject)) {
                                    hasRefKList.add(owl);
                                }
                            }
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
                            List<Owl> owlList = new ArrayList<>();
                            for (Owl owl : data) {
                                //
                                if (owl.getProperty().equals("relatedBook") && owl.getSubject().equals(next)) {
                                    owlList.add(owl);
                                }
                            }
                            if (!owlList.isEmpty()) {
                                for (Owl owl : owlList) {
                                    //添加查询出来的知识点，relatedBook关系
                                    if (hashSet.add(owl.getObject())) {
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    } else {
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    }
                                    List<Owl> owls = new ArrayList<>();
                                    for (Owl owl1 : data) {
                                        //
                                        if (owl1.getProperty().equals("type") && owl1.getSubject().equals(owl.getObject())) {
                                            owls.add(owl1);
                                        }
                                    }
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
            note1.append(note.toString().substring(0, note.length() - 1));
            note = note1;
            note.append("]");
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString().substring(0, link.length() - 1));
            link = link1;
            link.append("]");
        } else {
            link.append("]");
        }

        StringBuilder result = new StringBuilder();
        result.append("{\"NOTE\":" + note + ",\"LINK\":" + link + "}");
        return result;
    }

    /**
     * 查询知识图谱
     *
     * @param subject 查询内容
     * @return 数据
     */
    @Override
    public StringBuilder findKnowledgeGraph(String subject) {
        List<Owl> data = getData();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"Thing\",\"des\":\"Thing\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> owlList1 = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("subClassOf") && owl.getObject().equals("教育基础属性")) {
                owlList1.add(owl);
            }
        }
        hashSet.add("教育基础属性");
        note.append("{\"name\":\"教育基础属性\",\"des\":\"教育基础属性\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("{\"source\":\"Thing\",\"target\":\"教育基础属性\"},");
        if (!owlList1.isEmpty()) {
            for (Owl owl : owlList1) {
                if (hashSet.add(owl.getSubject())) {
                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":1" + "},");
                    link.append("{\"source\":\"教育基础属性\",\"target\":\"" + owl.getSubject() + "\",\"name\":\" \"" + "},");
                    //拿着教育基础属性下的类去查
                    List<Owl> owlList2 = new ArrayList<>();
                    for (Owl owl1 : data) {
                        //
                        if (owl1.getProperty().equals("type") && owl1.getObject().equals(owl.getSubject())) {
                            owlList2.add(owl1);
                        }
                    }
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
        List<Owl> owlListAll = new ArrayList<>();
        for (Owl owl1 : data) {
            //
            if (owl1.getProperty().equals("type") && owl1.getObject().indexOf("Book") != -1) {
                owlListAll.add(owl1);
            }
        }
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
        List<Owl> first = new ArrayList<>();
        for (Owl owl1 : data) {
            //
            if (owl1.getProperty().equals("subClassOf") && owl1.getSubject().indexOf("数学知识点") != -1) {
                first.add(owl1);
            }
        }
        if (!first.isEmpty()) {
            for (Owl o : first) {
                if (o.getObject().startsWith("MathK") && hashSet.add(o.getObject())) {
                    note.append("{\"name\":\"" + o.getObject() + "\",\"des\":\"" + o.getObject() + "\",\"symbolSize\":50,\"category\":3" + "},");
                    link.append("{\"source\":\"所有的知识点体系\",\"target\":\"" + o.getObject() + "\"},");
//                   //增加版本数据
                    List<Owl> all = new ArrayList<>();
                    for (Owl owl1 : data) {
                        //
                        if (owl1.getProperty().equals("type") && owl1.getObject().equals(o.getObject())) {
                            all.add(owl1);
                        }
                    }
                    if (!all.isEmpty()) {
                        for (Owl owlAll : all) {
                            /*isSiblingof知识点*/
                            subject = owlAll.getSubject();
                            List<Owl> isSiblingofOwlList = new ArrayList<>();
                            for (Owl owl1 : data) {
                                //
                                if (owl1.getProperty().equals("isSiblingof") && owl1.getSubject().equals(subject)) {
                                    isSiblingofOwlList.add(owl1);
                                }
                            }
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
                            List<Owl> hasPostKOwlList = new ArrayList<>();
                            for (Owl owl1 : data) {
                                //
                                if (owl1.getProperty().equals("hasPostK") && owl1.getSubject().equals(subject)) {
                                    hasPostKOwlList.add(owl1);
                                }
                            }
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
                                    List<Owl> owls = new ArrayList<>();
                                    for (Owl owl1 : data) {
                                        //
                                        if (owl1.getProperty().equals("hasPostK") && owl1.getSubject().equals(next)) {
                                            owls.add(owl1);
                                        }
                                    }
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
                            List<Owl> hasPreKOwlList = new ArrayList<>();
                            for (Owl owl1 : data) {
                                //
                                if (owl1.getProperty().equals("hasPreK") && owl1.getSubject().equals(subject)) {
                                    hasPreKOwlList.add(owl1);
                                }
                            }
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
                                    List<Owl> owls = new ArrayList<>();
                                    for (Owl owl1 : data) {
                                        //
                                        if (owl1.getProperty().equals("hasPreK") && owl1.getSubject().equals(next)) {
                                            owls.add(owl1);
                                        }
                                    }
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
                            List<Owl> hasRefKList = new ArrayList<>();
                            for (Owl owl1 : data) {
                                //
                                if (owl1.getProperty().equals("hasRefK") && owl1.getSubject().equals(subject)) {
                                    hasRefKList.add(owl1);
                                }
                            }
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
                            List<Owl> owlList = new ArrayList<>();
                            for (Owl owl1 : data) {
                                //
                                if (owl1.getProperty().equals("relatedBook") && owl1.getSubject().equals(next)) {
                                    owlList.add(owl1);
                                }
                            }
                            if (!owlList.isEmpty()) {
                                for (Owl owl : owlList) {
                                    //添加查询出来的知识点，relatedBook关系
                                    if (hashSet.add(owl.getObject())) {
                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    } else {
                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    }
                                    List<Owl> owls = new ArrayList<>();
                                    for (Owl owl1 : data) {
                                        //
                                        if (owl1.getProperty().equals("type") && owl1.getSubject().equals(owl.getObject())) {
                                            owls.add(owl1);
                                        }
                                    }
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
            note1.append(note.toString().substring(0, note.length() - 1));
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString().substring(0, link.length() - 1));
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
     * @param subject 查询内容
     * @param number  层数
     * @return 数据
     */
    @Override
    public StringBuilder findIsSiblingOfAndRefK(String subject, int number) {
        List<Owl> data = getData();
        StringBuilder note = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("{\"search\":\"" + subject + "\",");
        /*查询hasPostK*/
        List<Owl> hasPostKOwlList = new ArrayList<>();
        for (Owl owl1 : data) {
            //
            if (owl1.getProperty().equals("hasPostK") && owl1.getSubject().equals(subject)) {
                hasPostKOwlList.add(owl1);
            }
        }
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
                    List<Owl> owls = new ArrayList<>();
                    for (Owl owl1 : data) {
                        //
                        if (owl1.getProperty().equals("hasPostK") && owl1.getSubject().equals(next)) {
                            owls.add(owl1);
                        }
                    }
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
        List<Owl> hasPreKOwlList = new ArrayList<>();
        for (Owl owl1 : data) {
            //
            if (owl1.getProperty().equals("hasPreK") && owl1.getSubject().equals(subject)) {
                hasPreKOwlList.add(owl1);
            }
        }
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
                    List<Owl> owls = new ArrayList<>();
                    for (Owl owl1 : data) {
                        //
                        if (owl1.getProperty().equals("hasPreK") && owl1.getSubject().equals(next)) {
                            owls.add(owl1);
                        }
                    }
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
        List<Owl> hasRefKList = new ArrayList<>();
        for (Owl owl1 : data) {
            //
            if (owl1.getProperty().equals("hasRefK") && owl1.getSubject().equals(subject)) {
                hasRefKList.add(owl1);
            }
        }
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
     * @param query 查询内容
     * @return 数据
     */
    @Override
    public StringBuilder findAllTextbookSystemAndEducationProperty(String query) {
        List<Owl> data = getData();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>();
        note.append("[{\"name\":\"" + query + "\",\"des\":\"" + query + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> owlListAll = new ArrayList<>();
        for (Owl owl1 : data) {
            //
            if (owl1.getProperty().equals("type") && owl1.getObject().indexOf("Book") != -1) {
                owlListAll.add(owl1);
            }
        }
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
                List<Owl> relations = new ArrayList<>();
                for (Owl owl1 : data) {
                    //
                    if (owl1.getProperty().indexOf("referto") != -1 && owl1.getSubject().equals(owl.getSubject())) {
                        relations.add(owl1);
                    }
                }
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
            note1.append(note.toString().substring(0, note.length() - 1));
            note1.append("]");
            note = note1;
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString().substring(0, link.length() - 1));
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
     * 执行本体推理并更新
     *
     * @return 返回是否推理成功
     */
    @Override
    public Boolean reasoning() {
        logger.info("开始执行推理");
        //创建模型
        Model m = ModelFactory.createDefaultModel();
        Resource configuration = m.createResource();
        configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
        //推理规则文件加载
        try {
            configuration.addProperty(ReasonerVocabulary.PROPruleSet, "mathv4.1.rules");
            // 创建这样一个推理机的实例
            Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
            //本体文件加载
//            Model data = FileManager.get().loadModel("/opt/wangjie/owlapi/owlapi/src/main/resources/jena/mathv4.2.owl");
            Model data = FileManager.get().loadModel("/opt/owlapi/src/main/resources/jena/mathv4.2.owl");
//            Model data = FileManager.get().loadModel("C:/Users/88551/Desktop/mathv4.2.owl");
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
            Integer sum = 0;
            for (Owl owl : owlList) {
                Boolean add = true;
                for (Owl owl1 : all) {
                    //判断是否在库里
                    if (owl1.getObject().equals(owl.getObject()) && owl1.getSubject().equals(owl.getSubject()) && owl1.getProperty().equals(owl.getProperty())) {
                        add = false;
                    }
                }
                if (add) {
                    sum++;
                    update.add(owl);
                }
            }
            //删除
            List<Owl> del = new ArrayList<>();
            Integer countDel = 0;
            for (Owl owl : all) {
                Boolean delete = true;
                for (Owl owl1 : owlList) {
                    //判断是否在库里
                    if (owl1.getObject().equals(owl.getObject()) && owl1.getSubject().equals(owl.getSubject()) && owl1.getProperty().equals(owl.getProperty())) {
                        delete = false;
                    }
                }
                if (delete) {
                    countDel++;
                    del.add(owl);
                }
            }

            if (update.size() > 0) {
                logger.info("更新成功，本次更新了：" + sum + "条数据--"+update.toString());
                owlRepository.saveAll(update);

            }
            if (del.size()>0){
                logger.info("本次删除了："+countDel+"条数据--"+del.toString());
                owlRepository.deleteAll(del);
            }
            logger.info("刷新redis数据");
            redisTemplate.delete("owl");
            redisTemplate.opsForValue().set("owl",JSON.toJSON(owlList).toString());
            logger.info("redis数据刷新成功");
            logger.info("redis当前数据量:"+owlList.size());
        } catch (Exception e) {
            logger.debug("出错了：" + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 从redis中取数据
     *
     * @return 数据
     */
    public List<Owl> getData() {
        if (!redisTemplate.hasKey("owl")) {
            logger.info("redis中数据不存在");
            List<Owl> all = owlRepository.findAll();
            redisTemplate.opsForValue().set("owl", JSON.toJSON(all).toString());
            logger.info("重新载入数据成功");
            logger.info("redis当前数据量:"+all.size());
        }
        logger.info("读取redis数据成功");
        String owlString = (String) redisTemplate.opsForValue().get("owl");
        JSONArray objects = JSON.parseArray(owlString);
        List<Owl> owls = new ArrayList<>();
        for (Object object : objects) {
            JSONObject jsonObject = (JSONObject) object;
            Owl owl = JSONObject.toJavaObject(jsonObject, Owl.class);
            owls.add(owl);
        }
        logger.info("redis当前数据量:"+owls.size());
        return owls;
    }

}


