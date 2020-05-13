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
        if (!isSiblingofOwlList.isEmpty()) {
            hashSet.add(subject);
//        note.append("[");
            link.append("[");
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
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.3 }}}");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + ",lineStyle: {normal: { curveness: 0.3 }}}");
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
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + "}");
                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'后继知识点'" + "}");
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
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + "}");
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + "}");
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
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + "}");
                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append(",{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'前序知识点'" + "}");
                            }
                        }
                    }
                }

            }
        }
/*        if(note.toString().endsWith(",")){
            StringBuilder note1=new StringBuilder();
            StringBuilder link1=new StringBuilder();
            note1.append("[{name:'" + subject + ":独立知识点',des:'" + subject + "',symbolSize:70,category:0}]");
            link1.append("[{}]");
            String a = note1.substring(0, note1.length());
            String b = link1.substring(0, link1.length());

            hashMap.put("NOTE", a);
            hashMap.put("LINK", b);

            return hashMap;
        }else {
            note.append("]");
            link.append("]");
        }*/
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
}
