package com.timeep.common;

import com.timeep.po.Owl;
import com.timeep.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Description:
 * @Author: lh
 * @Date 2020/11/16
 * @Version: 1.0
 **/
@Component
public class MainComm {
    @Autowired
    private MainService mainService;
    @Autowired
    private BuildComm buildComm;


    /**
     * 处理后续知识点
     *
     * @return
     */
    public HashMap<String, StringBuilder> getPostK(List<Owl> hasPostKOwlList,
                                                   HashSet<String> hashSet,
                                                   List<Owl> data,
                                                   String query,
                                                   String subject) {
        HashMap<String, StringBuilder> map = new HashMap<>();
        HashSet<String> hasPostSet = new HashSet<>();
        StringBuilder note = new StringBuilder();
        StringBuilder link = new StringBuilder();
        if (!hasPostKOwlList.isEmpty()) {
            for (Owl owl : hasPostKOwlList) {
                //查询不能为空
                if (!owl.getObject().isEmpty() && hasPostSet.add(owl.getObject())) {
                    //判断subject和Object是否相同，和是否为最后一个
                    if (!owl.getSubject().equals(owl.getObject()) && hashSet.add(owl.getObject())) {
                        note.append(buildComm.Build_HX_Note(owl.getObject()));
                        link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                        if (query != null) {
                            link.append(buildComm.Build_PT_Link(query, subject));
                        }
                    } else if (!owl.getSubject().equals(owl.getObject())) {
                        link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                        if (query != null) {
                            link.append(buildComm.Build_PT_Link(query, subject));
                        }
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
                                if (query != null) {
                                    link.append(buildComm.Build_PT_Link(query, subject));
                                }
                            } else if (!owl.getSubject().equals(owl.getObject())) {
                                link.append(buildComm.Build_HX_Link(owl.getSubject(), owl.getObject()));
                                if (query != null) {
                                    link.append(buildComm.Build_PT_Link(query, subject));
                                }
                            }
                        }
                    }
                }
            }
        }
        map.put("note", note);
        map.put("link", link);
        return map;
    }

    public HashMap<String , StringBuilder> getIsSiblingOf(List<Owl> isSiblingofOwlList,
                                                         HashSet<String> hashSet){
        StringBuilder note=new StringBuilder();
        StringBuilder link=new StringBuilder();
        HashMap<String, StringBuilder> map = new HashMap<>();
        if (!isSiblingofOwlList.isEmpty()) {
            for (Owl owl : isSiblingofOwlList) {
                if (hashSet.add(owl.getObject())) {
                    note.append("{\"name\":\"" + owl.getObject() + "\",\"des\":\"" + owl.getObject() + "\",\"symbolSize\":50,\"category\":" + mainService.getSubject(owl.getObject()) + "},");
                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + ",\"lineStyle\": {\"normal\": {\"color\":\"" + Type.ISSIBLINGOF_COLOR + "\" }}},");
                } else {
                    link.append("{\"source\":\"" + owl.getSubject() + "\",\"target\":\"" + owl.getObject() + "\",\"name\":\"并列知识点\"" + ",\"lineStyle\": {\"normal\": {\"color\":\"" + Type.ISSIBLINGOF_COLOR + "\" }}},");
                }
            }
        }
        map.put("note", note);
        map.put("link", link);
        return map;
    }

}
