package com.timeep.common;

import com.timeep.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Author: lh
 * @Date 2020/11/16
 * @Version: 1.0
 **/
@Component
public class BuildComm {

    @Autowired
    private MainService mainService;

    /**
     * 构建兄弟关系结点数据
     *
     * @param name 结点名称
     * @return
     */
    public StringBuilder Build_XD_Note(String name) {
        StringBuilder note = new StringBuilder();
        note.append("{\"name\":\"" + name + "\",\"des\":\"" + name + "\",\"symbolSize\":50,\"category\":" + mainService.getSubject(name) + "},");
        return note;
    }

    /**
     * 构建兄弟关系数据
     *
     * @param source 源
     * @param target 目的
     * @return
     */
    public StringBuilder Build_XD_Link(String source, String target) {
        StringBuilder link = new StringBuilder();
        link.append("{\"source\":\"" + source + "\",\"target\":\"" + target + "\",\"name\":\"并列知识点\"" + ",\"lineStyle\": {\"normal\": {\"color\":\"" + Type.ISSIBLINGOF_COLOR + "\" }}},");
        return link;
    }

    /**
     * 构建前序关系结点数据
     *
     * @param name 结点名称
     * @return
     */
    public StringBuilder Build_QX_Note(String name) {
        StringBuilder note = new StringBuilder();
        note.append("{\"name\":\"" + name + "\",\"des\":\"" + name + "\",\"symbolSize\":50,\"category\":" + mainService.getSubject(name) + "},");
        return note;
    }

    /**
     * 构建前序关系数据
     *
     * @param source 源
     * @param target 目的
     * @return
     */
    public StringBuilder Build_QX_Link(String source, String target) {
        StringBuilder link = new StringBuilder();
        link.append("{\"source\":\"" + source + "\",\"target\":\"" + target + "\",\"name\":\"前序知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.3,\"color\":\""+Type.HASPREK_COLOR+"\" }}},");
        return link;
    }

    /**
     * 构建后续关系结点数据
     *
     * @param name 结点名称
     * @return
     */
    public StringBuilder Build_HX_Note(String name) {
        StringBuilder note = new StringBuilder();
        note.append("{\"name\":\"" + name + "\",\"des\":\"" + name + "\",\"symbolSize\":50,\"category\":" + mainService.getSubject(name)+ "},");
        return note;
    }

    /**
     * 构建后续关系数据
     *
     * @param source 源
     * @param target 目的
     * @return
     */
    public StringBuilder Build_HX_Link(String source, String target) {
        StringBuilder link = new StringBuilder();
        link.append("{\"source\":\"" + source + "\",\"target\":\"" + target + "\",\"name\":\"后继知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.2,\"color\":\""+Type.HASPOSTK_COLOR+"\"}}},");
        return link;
    }

    /**
     * 构建参考结点数据
     *
     * @param name 结点名称
     * @return
     */
    public StringBuilder Build_CK_Note(String name) {
        StringBuilder note = new StringBuilder();
        note.append("{\"name\":\"" + name + "\",\"des\":\"" + name + "\",\"symbolSize\":50,\"category\":" + mainService.getSubject(name) + "},");
        return note;
    }

    /**
     * 构建参考关系数据
     *
     * @param source 源
     * @param target 目的
     * @return
     */
    public StringBuilder Build_CK_Link(String source, String target) {
        StringBuilder link = new StringBuilder();
        link.append("{\"source\":\"" + source + "\",\"target\":\"" + target + "\",\"name\":\"参考知识点\"" + ",\"lineStyle\": {\"normal\": { \"curveness\": 0.3 ,\"color\":\""+Type.HASREFK_COLOR+"\"}}},");
        return link;
    }

    /**
     * 构建普通结点数据
     *
     * @param name 结点名称
     * @return
     */
    public StringBuilder Build_PT_Note(String name) {
        StringBuilder note = new StringBuilder();
        note.append("{\"name\":\"" + name + "\",\"des\":\"" + name + "\",\"symbolSize\":50,\"category\":" + mainService.getSubject(name) + "},");
        return note;
    }

    /**
     * 构建普通关系数据
     *
     * @param source 源
     * @param target 目的
     * @return
     */
    public StringBuilder Build_PT_Link(String source, String target) {
        StringBuilder link = new StringBuilder();
        link.append("{\"source\":\"" + source + "\",\"target\":\"" + target + "\"},");
        return link;
    }

    /**
     * 构建书本章节结点数据
     *
     * @param name 结点名称
     * @return
     */
    public StringBuilder Build_Book_Note(String name) {
        StringBuilder note = new StringBuilder();
        note.append("{\"name\":\"" + name + "\",\"des\":\"" + name + "\",\"symbolSize\":50,\"category\":" + Type.BOOK_TYPE + "},");
        return note;
    }

    /**
     * 构建书本章节关系数据
     *
     * @param source 源
     * @param target 目的
     * @return
     */
    public StringBuilder Build_Book_Link(String source, String target) {
        StringBuilder link = new StringBuilder();
        link.append("{\"source\":\"" + source + "\",\"target\":\"" + target + "\",\"name\":\"属于\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.RELATEDBOOK_COLOR + "\" }}},");
        return link;
    }

    /**
     * 构建教育结点数据
     *
     * @param name 结点名称
     * @return
     */
    public StringBuilder Build_Education_Note(String name) {
        StringBuilder note = new StringBuilder();
        note.append("{\"name\":\"" + name + "\",\"des\":\"" + name + "\",\"symbolSize\":50,\"category\":" + Type.EDUCATION_TYPE+ "},");
        return note;
    }

    /**
     * 构建教育关系数据
     *
     * @param source 源
     * @param target 目的
     * @return
     */
    public StringBuilder Build_Education_Link(String source, String target) {
        StringBuilder link = new StringBuilder();
        link.append("{\"source\":\"" + source + "\",\"target\":\"" + target + "\"" + ",\"lineStyle\": {\"normal\": { \"color\":\"" + Type.EDUCATION_COLOR + "\" }}},");
        return link;
    }
}
