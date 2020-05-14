package com.timeep.service;

import com.timeep.dao.OwlRepository;
import com.timeep.po.Owl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Li
 **/
@Service
public class OwlServiceImpl implements OwlService {
    @Autowired
    private OwlRepository owlRepository;

    @Override
    public List<String> findZhiShiDian() {
        List<Owl> owlList = owlRepository.findByPropertyAndObject("type", "MathKChuzhong");
        List<String> list = new ArrayList<>();
        for (Owl owl : owlList) {
            list.add(owl.getSubject());
        }
        return list;
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
            LinkedHashSet<String> count = new LinkedHashSet<>();
            count.addAll(hashSet);
//            Iterator iterator = hashSet.iterator();
//            for (int i = 0; i < hashSet.size(); i++) {
            while (!count.isEmpty()) {
                String next = count.iterator().next();
                count.remove(next);
                List<Owl> owls = owlRepository.findByPropertyAndSubject("hasPostK", next);
                if (!owls.isEmpty()) {
                    for (int j = 0; j < owls.size(); j++) {
                        Owl owl = owls.get(j);
                        //查询不能为空
                        if (!owl.getObject().isEmpty() && hashSet.add(owl.getObject())) {
                            count.add(owl.getObject());
                            //判断subject和Object是否相同，和是否为最后一个
                            if (!owl.getSubject().equals(owl.getObject())) {
                                note.append(",{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "}");
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + "}");
                            } /*else {
                                    note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "}");
                                    link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + "}");
                                }*/
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
//            Iterator iterator = hashSet.iterator();
//            for (int i = 0; i < hashSet.size(); i++) {
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
                            }/* else {
                                    note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + "}");
                                    link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + "}");
                                }*/
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
        if (!isSiblingofOwlList.isEmpty()) {
            hashSet.add(subject);
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
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}}");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}}");
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
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}}");
                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}}");
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
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}}");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}}");
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
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}}");
                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + ",lineStyle: {normal: { curveness: 0.1 }}}");
                            }
                        }
                    }
                }

            }
        }
        if (link.toString().startsWith("[,")) {
            StringBuilder link2 = new StringBuilder();

            link2.append("[" + link.toString().substring(2, link.length()));
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
            note1.append(note.toString().substring(0, note.length() - 1));
            note1.append("]");
            note=note1;
        }else{
            note.append("]");
        }

        if (link.toString().endsWith(",")) {
            StringBuilder link1 = new StringBuilder();
            link1.append(link.toString().substring(0, link.length() - 1));
            link1.append("]");
            link=link1;
        } else {
            link.append("]");
        }
        String a = note.substring(0, note.length());
        String b = link.substring(0, link.length());
        if(a.equals("]")){
            a="[{name:'"+subject+":无',des:'"+subject+"',symbolSize:60,category:0}]";
        }
        hashMap.put("NOTE", a);
        hashMap.put("LINK", b);

        return hashMap;
    }
}
