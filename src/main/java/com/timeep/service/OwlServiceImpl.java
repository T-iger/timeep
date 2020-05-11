package com.timeep.service;

import com.timeep.dao.OwlRepository;
import com.timeep.po.Owl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public HashMap<String, StringBuilder> findIsSiblingOf(String subject) {
        List<Owl> owlList = owlRepository.findByPropertyAndSubject("isSiblingof", subject);
//        List<Owl> owlList1=new ArrayList<>();
        HashMap<String, StringBuilder> hashMap = new HashMap<>();
     /*   List<String> note=new ArrayList<>();
        List<String> link=new ArrayList<>();*/
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
//        note.append("[{" + "name:'" + owlList.get(0).getSubject() + "',des:'" + owlList.get(0).getSubject() + "',symbolSize:50,category:1" + "}");
        note.append("[");
        link.append("[");
        for (int i = 0; i < owlList.size(); i++) {
            Owl owl = owlList.get(i);
            if (!owl.getSubject().equals(owl.getObject()) && i != owlList.size()) {
                note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + ",},");
                link.append("{" + "source:'" + owl.getSubject() + "','target:'" + owl.getObject() + "',name:'isSiblingof'" + "},");
            } else {
                note.append("{" + "name:'" + owl.getObject() + "',des:'" + owl.getObject() + "',symbolSize:50,category:1" + ",}");
                link.append("{" + "source:'" + owl.getSubject() + "',target:'" + owl.getObject() + "',name:'isSiblingof'" + "}");
            }
        }
        note.append("]");
        link.append("]");
        hashMap.put("NOTE", note);
        hashMap.put("LINK", link);
        return hashMap;
    }

    @Override
    public HashMap<String, String> findHasPostK(String object) {
        return null;
    }

    @Override
    public HashMap<String, String> findHasPreK(String object) {
        return null;
    }
}
