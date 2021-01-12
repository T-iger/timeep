package com.timeep.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.timeep.common.BuildComm;
import com.timeep.common.MainComm;
import com.timeep.common.Type;
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

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Li
 **/
@Service
public class MainServiceImpl implements MainService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private OwlRepository owlRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private BuildComm buildComm;
    @Autowired
    private MainComm mainComm;

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
                    note.append(buildComm.Build_Book_Note(owl.getSubject()));
                    link.append(buildComm.Build_Book_Link(owl.getObject(), owl.getSubject()));
//                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "'\",\"symbolSize\":60,\"category\":1},");
//                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"},");
                }
            }
        }
        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note.toString(), 0, note.length() - 1);
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
                    note.append(buildComm.Build_Book_Note(owl.getObject()));
                    link.append(buildComm.Build_Book_Link(owl.getObject(), query));
//                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":60,\"category\":1" + "},");
//                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + query + "\",\"name\":\"属于\"" + "},");
                }
                if (hashSet.add(owl.getSubject())) {
                    note.append(buildComm.Build_Book_Note(owl.getSubject()));
                    link.append(buildComm.Build_Book_Link(owl.getObject(), owl.getSubject()));
//                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":2},");
//                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"},");
                }
            }
        }
        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note.toString(), 0, note.length() - 1);
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
        List<Owl> owlList1 = new ArrayList<>();
        for (Owl owl : data) {
            if (owl.getProperty().equals("type") && owl.getObject().equals(subject)) {
                owlList.add(owl);
            }
            if (owl.getProperty().equals("relatedBook") && owl.getObject().equals(zj)) {
                owlList1.add(owl);
            }
        }
        if (!owlList.isEmpty()) {
            for (Owl owl : owlList) {
                if (hashSet.add(owl.getSubject())) {
                    if (!owl.getSubject().equals(zj)) {
                        note.append(buildComm.Build_Book_Note(owl.getSubject()));
//                        note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":1" + "},");
                    } else {
                        note.append(buildComm.Build_Book_Note(owl.getSubject()));
//                        note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":0" + "},");
                    }
                    link.append(buildComm.Build_Book_Link(owl.getObject(), owl.getSubject()));
//                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"" + "},");
                }
            }
        }

        if (!owlList1.isEmpty()) {
            for (Owl owl : owlList1) {
                if (hashSet.add(owl.getSubject())) {
                    note.append(buildComm.Build_Book_Note(owl.getSubject()));
                    link.append(buildComm.Build_Book_Link(owl.getObject(), owl.getSubject()));
//                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":2" + "},");
//                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"拥有知识点\"" + "},");
                } else {
                    link.append(buildComm.Build_Book_Link(owl.getObject(), owl.getSubject()));
//                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"拥有知识点\"" + "},");
                }
            }
        }

        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note.toString(), 0, note.length() - 1);
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
        // 此时有@，即查询该教材体系下的知识点
        if (knowledge.length > 1) {
            subject = knowledge[1];
            hashSet.add(subject);
            note.append("[{\"name\":\"" + knowledge[1] + "\",\"des\":\"" + knowledge[1] + "\",\"symbolSize\":60,\"category\":0" + "},");
            link.append("[");
            List<Owl> isSiblingofOwlList = new ArrayList<>();
            List<Owl> hasPostKOwlList = new ArrayList<>();
            List<Owl> hasPreKOwlList = new ArrayList<>();
            List<Owl> hasRefKOwlList = new ArrayList<>();
            // 标识是否存在，0不存在，1存在
            Integer exist = 0;
            for (Owl owl : data) {
                // isSiblingof
                if (owl.getProperty().equals("isSiblingof") && owl.getSubject().equals(subject)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
                    isSiblingofOwlList.add(owl);
                }
                // hasPostK
                if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(subject)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
                    hasPostKOwlList.add(owl);
                }
                //hasPreK查询
                if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(subject)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
                    hasPreKOwlList.add(owl);
                }

                if (owl.getSubject().equals(subject)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd()) || owl.getObject().equals(subject)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
                    exist++;
                }
            }
            if (exist == 0) {
                StringBuilder none = new StringBuilder();
                StringBuilder result = new StringBuilder();
                none.append("[{\"name\":\"无:" + knowledge[1] + "知识点("+knowledge[0]+")\",\"des\":\"" + knowledge[1] + "\",\"symbolSize\":60,\"category\":0" + "}]");
                result.append("{\"NOTE\":" + none + ",\"LINK\":[]}");
                return result;
            }
            // 兄弟知识点
            if (!isSiblingofOwlList.isEmpty()) {
                for (Owl owl : isSiblingofOwlList) {
                    if (hashSet.add(owl.getObject())) {
                        note.append(buildComm.Build_XD_Note(owl.getObject()));
//                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + ",\"lineStyle\": {\"normal\": {\"color\":\"" + Type.ISSIBLINGOF_COLOR + "\" }}},");
                        link.append(buildComm.Build_XD_Link(owl.getSubject(), owl.getObject()));
                    } else {
                        link.append(buildComm.Build_XD_Link(owl.getSubject(), owl.getObject()));
//                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + ",\"lineStyle\": {\"normal\": {\"color\":\"" + Type.ISSIBLINGOF_COLOR + "\" }}},");
                    }
                }
            }


            /*查询hasPostK*/
            HashSet<String> hasPostSet = new HashSet<>();
            if (!hasPostKOwlList.isEmpty()) {
                for (Owl owl : hasPostKOwlList) {
                    //查询不能为空
                    if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                        //判断subject和Object是否相同，和是否为最后一个
                        if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                            note.append(buildComm.Build_HX_Note(owl.getObject()));
                            link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
//                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPOSTK_COLOR + "\"}}},");
                        } else if (!owl.getSubject().equals(owl.getObject())) {
                            link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPOSTK_COLOR + "\" }}},");
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
                        if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(next)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
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
                                    note.append(buildComm.Build_HX_Note(owl.getObject()));
                                    link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
//                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 ,\"color\":\"" + Type.HASPOSTK_COLOR + "\" }}},");

                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                    link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
//                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 ,\"color\":\"" + Type.HASPOSTK_COLOR + "\" }}},");
                                }
                            }
                        }
                    }
                }
            }

            /*hasPreK查询*/
            HashSet<String> hasPreKSet = new HashSet<>();
            if (!hasPreKOwlList.isEmpty()) {
                for (Owl owl : hasPreKOwlList) {
                    //查询不能为空
                    if (!owl.getObject().isEmpty() && hasPreKSet.add(owl.getObject())) {
                        //判断subject和Object是否相同，和是否为最后一个
                        if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                            note.append(buildComm.Build_QX_Note(owl.getObject()));
                            link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
//                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.3,\"color\":\"" + Type.HASPREK_COLOR + "\" }}},");
                        } else if (!owl.getSubject().equals(owl.getObject())) {
                            link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.3,\"color\":\"" + Type.HASPREK_COLOR + "\" }}},");

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
                        if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(next)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
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
                                    note.append(buildComm.Build_QX_Note(owl.getObject()));
                                    link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
//                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.3,\"color\":\"" + Type.HASPREK_COLOR + "\" }}},");
                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                    link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
//                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.3,\"color\":\"" + Type.HASPREK_COLOR + "\" }}},");

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
                            note.append(buildComm.Build_Book_Note(owl.getObject()));
                            link.append(buildComm.Build_Book_Link(owl.getSubject(), owl.getObject()));
//                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":" + getSubject(owl.getObject()) + "},");
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.RELATEDBOOK_COLOR + "\" }}},");
                        } else {
                            link.append(buildComm.Build_Book_Link(owl.getSubject(), owl.getObject()));
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.RELATEDBOOK_COLOR + "\" }}},");
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
                                    note.append(buildComm.Build_Book_Note(owl1.getObject()));
                                    link.append(buildComm.Build_Book_Link(owl1.getSubject(), owl1.getObject()));
//                                    note.append("{\"name\":\"" + owl1.getObject() + "\",\"des\":\"版本:" + owl1.getObject() + "\",\"symbolSize\":40,\"category\":" + Type.BOOK_TYPE + "},");
//                                    link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.RELATEDBOOK_COLOR + "\" }}},");
                                } else if (owl1.getObject().startsWith("MathBook")) {
                                    link.append(buildComm.Build_Book_Link(owl1.getSubject(), owl1.getObject()));
//                                    link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.RELATEDBOOK_COLOR + "\" }}},");
                                }
                            }
                        }
                    }
                }
            }
            //hasRefKOwlList
            for (String s : hashSet) {
                for (Owl owl : data) {
                    if (owl.getProperty().equals("hasRefK") && owl.getSubject().equals(s)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
                        hasRefKOwlList.add(owl);
                    }
                }
            }
            // 参考知识点
            if (!hasRefKOwlList.isEmpty()) {
                for (Owl owl : hasRefKOwlList) {
                    if (hashSet.add(owl.getObject())) {
                        note.append(buildComm.Build_CK_Note(owl.getObject()));
                        link.append(buildComm.Build_CK_Link(owl.getSubject(), owl.getObject()));
//                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"参考知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.3 ,\"color\":\"" + Type.HASREFK_COLOR + "\"}}},");
                    } else {
                        link.append(buildComm.Build_CK_Link(owl.getSubject(), owl.getObject()));
//                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"参考知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.3 ,\"color\":\"" + Type.HASREFK_COLOR + "\"}}},");
                    }
                }
            }


            if (note.toString().endsWith(",")) {
                StringBuilder note1 = new StringBuilder();
                note1.append(note.toString(), 0, note.length() - 1);
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

        note.append("[{\"name\":\"" + query + "\",\"des\":\"" + query + "\",\"symbolSize\":60,\"category\":0" + "},");
        link.append("[");
        List<Owl> first = new ArrayList<>();
        List<Owl> all = new ArrayList<>();
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("equivalentClass") && owl.getSubject().equals(query + "知识点") && owl.getObject().indexOf("K") != -1) {
                first.add(owl);
            }
        }
        if (!first.isEmpty()) {
            for (Owl owl : first) {
                subject = owl.getObject();
            }
        }

        for (Owl owl : data) {
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
                    if (owl.getProperty().equals("isSiblingof") && owl.getSubject().equals(subject)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
                        isSiblingofOwlList.add(owl);
                    }
                }
                if (hashSet.add(subject)) {
                    note.append(buildComm.Build_PT_Note(subject));
//                    note.append("{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":50,\"category\":"+getSubject(subject) + "},");
                    link.append(buildComm.Build_PT_Link(query, subject));
//                    link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");
                }
                if (!isSiblingofOwlList.isEmpty()) {
                    for (Owl owl : isSiblingofOwlList) {
                        if (hashSet.add(owl.getObject())) {
                            note.append(buildComm.Build_XD_Note(owl.getObject()));
//                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":"+getSubject(owl.getObject())+ "},");
                            link.append(buildComm.Build_XD_Link(owl.getSubject(), owl.getObject()));
                            link.append(buildComm.Build_PT_Link(query, subject));
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 ,\"color\":\""+Type.ISSIBLINGOF_COLOR+"\"}}},");
//                            link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");
                        } else {
                            link.append(buildComm.Build_XD_Link(owl.getSubject(), owl.getObject()));
                            link.append(buildComm.Build_PT_Link(query, subject));
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 ,\"color\":\"" + Type.ISSIBLINGOF_COLOR + "\"}}},");
//                            link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                        }
                    }
                }
                /*查询hasPostK*/
                List<Owl> hasPostKOwlList = new ArrayList<>();
                for (Owl owl : data) {
                    //
                    if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(subject)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
                        hasPostKOwlList.add(owl);
                    }
                }
                /*HashMap<String, StringBuilder> postKMap = mainComm.getPostK(hasPostKOwlList, hashSet, data, query, subject);
                note.append(postKMap.get("note"));
                link.append(postKMap.get("link"));*/

                HashSet<String> hasPostSet = new HashSet<>();
                if (!hasPostKOwlList.isEmpty()) {
                    for (Owl owl : hasPostKOwlList) {
                        //查询不能为空
                        if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                            //判断subject和Object是否相同，和是否为最后一个
                            if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                                note.append(buildComm.Build_HX_Note(owl.getObject()));
                                link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                link.append(buildComm.Build_PT_Link(query, subject));
//                                note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                                link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPOSTK_COLOR + "\"}}},");
//                                link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                link.append(buildComm.Build_PT_Link(query, subject));
//                                link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPOSTK_COLOR + "\"}}},");
//                                link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

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
                            if (owl.getProperty().equals("hasPostK") && owl.getSubject().equals(next)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
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
                                        note.append(buildComm.Build_HX_Note(owl.getObject()));
                                        link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                        link.append(buildComm.Build_PT_Link(query, subject));
//                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPOSTK_COLOR + "\"}}},");
//                                        link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                                    } else if (!owl.getSubject().equals(owl.getObject())) {
                                        link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                        link.append(buildComm.Build_PT_Link(query, subject));
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPOSTK_COLOR + "\"}}},");
//                                        link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

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
                    if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(subject)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
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
                                note.append(buildComm.Build_QX_Note(owl.getObject()));
                                link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                link.append(buildComm.Build_PT_Link(query, subject));
//                                note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                                link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPREK_COLOR + "\" }}},");
//                                link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                link.append(buildComm.Build_PT_Link(query, subject));
//                                link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPREK_COLOR + "\" }}},");
//                                link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

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
                            if (owl.getProperty().equals("hasPreK") && owl.getSubject().equals(next)&&owl.getXd()!=null&& knowledge[0].contains(owl.getXd())) {
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
                                        note.append(buildComm.Build_QX_Note(owl.getObject()));
                                        link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                        link.append(buildComm.Build_PT_Link(query, subject));
//                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + getSubject(owl.getObject()) + "},");
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPREK_COLOR + "\" }}},");
//                                        link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

                                    } else if (!owl.getSubject().equals(owl.getObject())) {
                                        link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                        link.append(buildComm.Build_PT_Link(query, subject));
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\"" + Type.HASPREK_COLOR + "\" }}},");
//                                        link.append("{\"source\":\"" + query + "\",\"target\":\"" + subject + "\"},");

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
                            note.append(buildComm.Build_Book_Note(owl.getObject()));
                            link.append(buildComm.Build_Book_Link(owl.getSubject(), owl.getObject()));
//                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":" + getSubject(owl.getObject()) + "},");
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.RELATEDBOOK_COLOR + "\" }}},");

                        } else {
                            link.append(buildComm.Build_Book_Link(owl.getSubject(), owl.getObject()));
//                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.RELATEDBOOK_COLOR + "\" }}},");
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
                                    note.append(buildComm.Build_Book_Note(owl1.getObject()));
                                    link.append(buildComm.Build_Book_Link(owl1.getSubject(), owl1.getObject()));
//                                    note.append("{\"name\":\"" + owl1.getObject() + "\",\"des\":\"版本:" + owl1.getObject() + "\",\"symbolSize\":40,\"category\":" + Type.BOOK_TYPE + "},");
//                                    link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.RELATEDBOOK_COLOR + "\" }}},");
                                } else if (owl1.getObject().startsWith("MathBook")) {
                                    link.append(buildComm.Build_Book_Link(owl1.getSubject(), owl1.getObject()));
//                                    link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.RELATEDBOOK_COLOR + "\" }}},");
                                }
                            }
                        }
                    }
                }
            }
        }

        if (note.toString().endsWith(",")) {
            StringBuilder note1 = new StringBuilder();
            note1.append(note.toString(), 0, note.length() - 1);
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
                                note.append(buildComm.Build_PT_Note(subject));
                                link.append(buildComm.Build_PT_Link(o.getSubject(), subject));
//                                note.append("{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":50,\"category\":1" + "},");
//                                link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + subject + "\"},");
                            }
                            if (!isSiblingofOwlList.isEmpty()) {
                                for (Owl owl : isSiblingofOwlList) {
                                    if (hashSet.add(owl.getObject())) {
                                        note.append(buildComm.Build_XD_Note(owl.getObject()));
                                        link.append(buildComm.Build_XD_Link(owl.getSubject(), owl.getObject()));
                                        link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
//                                        link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                    } else {
                                        link.append(buildComm.Build_XD_Link(owl.getSubject(), owl.getObject()));
                                        link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
//                                        link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
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
                                            note.append(buildComm.Build_HX_Note(owl.getObject()));
                                            link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                            link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
//                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                            link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                        } else if (!owl.getSubject().equals(owl.getObject())) {
                                            link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                            link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
//                                            link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
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
                                                    note.append(buildComm.Build_HX_Note(owl.getObject()));
                                                    link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                                    link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
//                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                                    link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                                    link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                                    link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": {\" curveness\": 0.2 }}},");
//                                                    link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
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
                                            note.append(buildComm.Build_QX_Note(owl.getObject()));
                                            link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                            link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
//                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                            link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                        } else if (!owl.getSubject().equals(owl.getObject())) {
                                            link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                            link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
//                                            link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

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
                                                    note.append(buildComm.Build_QX_Note(owl.getObject()));
                                                    link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                                    link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":1" + "},");
//                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                                    link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                                    link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                                    link.append(buildComm.Build_PT_Link(o.getSubject(), owl.getObject()));
//                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
//                                                    link.append("{\"source\":\"" + o.getSubject() + "\",\"target\":\"" + owl.getObject() + "\"},");

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
                                        note.append(buildComm.Build_CK_Note(owl.getObject()));
                                        link.append(buildComm.Build_CK_Link(owl.getSubject(), owl.getObject()));
//                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"参考知识点\"" + "},");
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
                                        note.append(buildComm.Build_Book_Note(owl.getObject()));
                                        link.append(buildComm.Build_Book_Link(owl.getSubject(), owl.getObject()));
//                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    } else {
                                        link.append(buildComm.Build_Book_Link(owl.getSubject(), owl.getObject()));
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
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
                                                note.append(buildComm.Build_Book_Note(owl1.getObject()));
                                                link.append(buildComm.Build_Book_Link(owl1.getSubject(), owl1.getObject()));
//                                                note.append("{\"name\":\"" + owl1.getObject() + "\",\"des\":\"版本：" + owl1.getObject() + "\",\"symbolSize\":40,\"category\":3" + "},");
//                                                link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                            } else if (owl1.getObject().startsWith("MathBook")) {
                                                link.append(buildComm.Build_Book_Link(owl1.getSubject(), owl1.getObject()));
//                                                link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
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
            note1.append(note.toString(), 0, note.length() - 1);
            note = note1;
            note.append("]");
        }
        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString(), 0, link.length() - 1);
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
        Integer exist = 0;
        for (Owl owl : data) {
            //
            if (owl.getProperty().equals("subClassOf") && owl.getObject().equals("教育基础属性")) {
                owlList1.add(owl);
            }
            if (owl.getSubject().equals(subject) || owl.getObject().equals(subject)) {
                exist++;
            }
        }
        if (exist == 0) {
            StringBuilder none = new StringBuilder();
            StringBuilder result = new StringBuilder();
            none.append("[{\"name\":\"无:" + subject + "知识点\",\"des\":\"" + subject + "\",\"symbolSize\":60,\"category\":0" + "}]");
            result.append("{\"NOTE\":" + none + ",\"LINK\":[]}");
            return result;
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
                                note.append(buildComm.Build_Education_Note(owl2.getSubject()));
                                link.append(buildComm.Build_Education_Link(owl2.getObject(), owl2.getSubject()));
//                                note.append("{\"name\":\"" + owl2.getSubject() + "\",\"des\":\"" + owl2.getSubject() + "\",\"symbolSize\":40,\"category\":1" + "},");
//                                link.append("{\"source\":\"" + owl2.getObject() + "\",\"target\":\"" + owl2.getSubject() + "\",\"name\":\" \"" + "},");
                            } else {
                                link.append(buildComm.Build_Education_Link(owl2.getObject(), owl2.getSubject()));
//                                link.append("{\"source\":\"" + owl2.getObject() + "\",\"target\":\"" + owl2.getSubject() + "\",\"name\":\" \"" + "},");
                            }
                        }
                    }
                }
            }
        }

        //所有教材体系
//        note.append(buildComm.Build_Book_Note("所有教材体系"));
        note.append("{\"name\":\"所有教材体系\",\"des\":\"所有教材体系\",\"symbolSize\":60,\"category\":0" + "},");
//        link.append(buildComm.Build_Book_Link("Thing","所有教材体系"));
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
                    note.append(buildComm.Build_Book_Note(owl.getObject()));
                    link.append(buildComm.Build_Book_Link("所有教材体系", owl.getObject()));
//                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":60,\"category\":3" + "},");
//                    link.append("{\"source\":\"所有教材体系\",\"target\":\"" + owl.getObject() + "\",\"name\":\"属于\"" + "},");
                }
                if (hashSet.add(owl.getSubject())) {
                    note.append(buildComm.Build_Book_Note(owl.getSubject()));
                    link.append(buildComm.Build_Book_Link(owl.getObject(), owl.getSubject()));
//                    note.append("{\"name\":\"" + owl.getSubject() + "\",\"des\":\"" + owl.getSubject() + "\",\"symbolSize\":60,\"category\":2" + "},");
//                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"" + "},");
                } else {
                    link.append(buildComm.Build_Book_Link(owl.getObject(), owl.getSubject()));
//                    link.append("{\"source\":\"" + owl.getObject() + "\",\"target\":\"" + owl.getSubject() + "\",\"name\":\"属于\"" + "},");
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
                    note.append(buildComm.Build_Book_Note(o.getObject()));
//                    note.append("{\"name\":\"" + o.getObject() + "\",\"des\":\"" + o.getObject() + "\",\"symbolSize\":50,\"category\":3" + "},");
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
                                note.append(buildComm.Build_XD_Note(subject));
                                link.append(buildComm.Build_PT_Link(o.getObject(), subject));
//                                note.append("{\"name\":\"" + subject + "\",\"des\":\"" + subject + "\",\"symbolSize\":50,\"category\":4" + "},");
//                                link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + subject + "\"},");
                            } else {
                                link.append(buildComm.Build_PT_Link(o.getObject(), subject));
//                                link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + subject + "\"},");
                            }
                            if (!isSiblingofOwlList.isEmpty()) {
                                for (Owl owl : isSiblingofOwlList) {
                                    if (hashSet.add(owl.getObject())) {
                                        note.append(buildComm.Build_XD_Note(owl.getObject()));
                                        link.append(buildComm.Build_XD_Link(owl.getSubject(), owl.getObject()));
                                        link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
//                                        link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                    } else {
                                        link.append(buildComm.Build_XD_Link(owl.getSubject(), owl.getObject()));
                                        link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + "},");
//                                        link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
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
                                            note.append(buildComm.Build_HX_Note(owl.getObject()));
                                            link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                            link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
//                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                            link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
                                        } else if (!owl.getSubject().equals(owl.getObject())) {
                                            link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                            link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2 }}},");
//                                            link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
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
                                                    note.append(buildComm.Build_HX_Note(owl.getObject()));
                                                    link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                                    link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
//                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                                    link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                                    link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                                    link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"后继知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                                    link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

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
                                            note.append(buildComm.Build_QX_Note(owl.getObject()));
                                            link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                            link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                            note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
//                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                            link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                        } else if (!owl.getSubject().equals(owl.getObject())) {
                                            link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                            link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                            link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                            link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

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
                                                    note.append(buildComm.Build_QX_Note(owl.getObject()));
                                                    link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                                    link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
//                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                                    link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");

                                                } else if (!owl.getSubject().equals(owl.getObject())) {
                                                    link.append(buildComm.Build_QX_Link(owl.getSubject(), owl.getObject()));
                                                    link.append(buildComm.Build_PT_Link(o.getObject(), owl.getObject()));
//                                                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"前序知识点\",\"lineStyle\": {\"normal\": { \"curveness\": 0.2}}},");
//                                                    link.append("{\"source\":\"" + o.getObject() + "\",\"target\":\"" + owl.getObject() + "\"},");
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
                                        note.append(buildComm.Build_CK_Note(owl.getObject()));
                                        link.append(buildComm.Build_CK_Link(owl.getSubject(), owl.getObject()));
//                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":4" + "},");
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"参考知识点\"" + "},");
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
                                        note.append(buildComm.Build_Book_Note(owl.getObject()));
                                        link.append(buildComm.Build_Book_Link(owl.getSubject(), owl.getObject()));
//                                        note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":40,\"category\":2" + "},");
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
                                    } else {
                                        link.append(buildComm.Build_Book_Link(owl.getSubject(), owl.getObject()));
//                                        link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"relatedBook关系\"" + "},");
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
                                                note.append(buildComm.Build_Book_Note(owl1.getObject()));
                                                link.append(buildComm.Build_Book_Link(owl1.getSubject(), owl1.getObject()));
//                                                note.append("{\"name\":\"" + owl1.getObject() + "\",\"des\":\"版本:" + owl1.getObject() + "\",\"symbolSize\":40,\"category\":3" + "},");
//                                                link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
                                            } else if (owl1.getObject().startsWith("MathBook")) {
                                                link.append(buildComm.Build_Book_Link(owl1.getSubject(), owl1.getObject()));
//                                                link.append("{\"source\":\"" + owl1.getSubject() + "\",\"target\":\"" + owl1.getObject() + "\",\"name\":\"属于\"" + "},");
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
            note1.append(note.toString(), 0, note.length() - 1);
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
            note1.append(note.toString(), 0, note.length() - 1);
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
//            Model data = FileManager.get().loadModel("/opt/owl/owlapi/src/main/resources/jena/mathv4.2.owl");
            Model data = FileManager.get().loadModel("C:/Users/88551/Desktop/mathv4.2.owl");
            //
            InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
            StmtIterator i = infmodel.listStatements();

            int count = 1;
            List<Owl> owlList = new ArrayList<>();
            List<String> gaoZhong = new ArrayList<>();
            List<String> chuZhong = new ArrayList<>();
            List<String> xiaoXue = new ArrayList<>();
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
                //如果全是英文跳出循环,前后一样去除(去除冗余数据)
                if (object.matches("[a-zA-Z]+") && subject.matches("[a-zA-Z]+")||owl.getSubject().equals(owl.getObject())) {
                    count--;
                    continue;
                }
                if ("MathKXiaoxue".equals(owl.getObject()) && "type".equals(owl.getProperty())) {
                    xiaoXue.add(owl.getSubject());
                }
                if ("MathKChuzhong".equals(owl.getObject()) && "type".equals(owl.getProperty())) {
                    chuZhong.add(owl.getSubject());
                }
                if ("MathKGaozhong".equals(owl.getObject()) && "type".equals(owl.getProperty())) {
                    gaoZhong.add(owl.getSubject());
                }
                owlList.add(owl);
            }
            /**
             * TODO
             * 1、遍历推理出来的数据
             * 2、比对这个知识点属于什么学段
             * 3、赋值
             * */
            owlList.forEach(x -> {
                if (xiaoXue.contains(x.getObject()) && xiaoXue.contains(x.getSubject())) {
                    x.setXd("小学");
                } else if (chuZhong.contains(x.getObject()) && chuZhong.contains(x.getSubject())) {
                    x.setXd("初中");
                } else if (gaoZhong.contains(x.getObject()) && gaoZhong.contains(x.getSubject())) {
                    x.setXd("高中");
                }
            });

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
                        if (owl1.getXd()==null&&owl.getXd()==null||owl1.getXd()!=null&&owl1.getXd().equals(owl.getXd())){
                            add = false;
                        }
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
                        if (owl1.getXd()==null&&owl.getXd()==null||owl1.getXd()!=null&&owl1.getXd().equals(owl.getXd())){
                            delete = false;
                        }
                    }
                }
                if (delete) {
                    countDel++;
                    del.add(owl);
                }
            }

            if (update.size() > 0) {
                logger.info("更新成功，本次更新了：" + sum + "条数据--" + update.toString());
                owlRepository.saveAll(update);

            }
            if (del.size() > 0) {
                logger.info("本次删除了：" + countDel + "条数据--" + del.toString());
                owlRepository.deleteAll(del);
            }
            logger.info("开始清除所有的key");
            Set<String> keys = redisTemplate.keys("*");
            redisTemplate.delete(keys);
            logger.info("刷新redis数据");
            redisTemplate.opsForValue().set("owl", JSON.toJSON(owlList).toString());
            //刷新学科知识点
            setSubject();
            logger.info("redis数据刷新成功");
            logger.info("redis当前数据量:" + owlList.size());
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
            logger.info("redis当前数据量:" + all.size());
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
        logger.info("redis当前数据量:" + owls.size());
        return owls;
    }

    /**
     * 获取学科的对应的知识点
     *
     * @param subject 学科名称
     */
    @Override
    public Integer getSubject(String subject) {
        try {
            String su = (String) redisTemplate.opsForValue().get(subject);
            if (su.equals(Type.HIGH_SCHOOL)) {
                return Type.HIGH_SCHOOL_TYPE;
            }
            if (su.equals(Type.JUNIOR_HIGH_SCHOOL)) {
                return Type.JUNIOR_HIGH_SCHOOL_TYPE;
            }
            if (su.equals(Type.PRIMARY_SCHOOL)) {
                return Type.PRIMARY_SCHOOL_TYPE;
            }
            if (su.startsWith(Type.BOOK)) {
                return Type.BOOK_TYPE;
            }
        } catch (Exception e) {
            logger.error("读取redis数据异常");
            return Type.NO_TYPE;
        }
        //不是以上定义的返回   未知:9
        return Type.NO_TYPE;
    }

    private Boolean setSubject() {
        logger.info("开始存储所有学科对应的");
        //读取redis中所有的数据
        List<Owl> allData = getData();
        //存储学科和知识点的键值对
        List<Owl> o1 = new ArrayList<>();
        HashSet set = new HashSet();
        for (Owl owl : allData) {
            if (owl.getObject().startsWith("MathK") && owl.getProperty().equals("type")) {
                set.add(owl.getObject());
                o1.add(owl);
            }
        }
        for (Owl owl : o1) {
            redisTemplate.opsForValue().set(owl.getSubject(), owl.getObject());
        }
        logger.info("所有学科知识点存储完毕");
        logger.info("共" + o1.size() + "个知识点");
        return true;
    }

    @PostConstruct
    public void start() {
        logger.info("开始清除所有的key");
        Set<String> keys = redisTemplate.keys("*");
        redisTemplate.delete(keys);
        logger.info("数据初始化,刷新redis数据");
        List<Owl> all = owlRepository.findAll();
        redisTemplate.opsForValue().set("owl", JSON.toJSON(all).toString());
        getData();
        setSubject();
    }

}


