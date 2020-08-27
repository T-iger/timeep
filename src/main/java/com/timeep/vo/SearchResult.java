package com.timeep.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: lh
 * @Date 2020/8/21
 * @Version: 1.0
 **/
public class SearchResult {

    private List<Note> NOTE=new ArrayList<>();
    private List<Link> LINK=new ArrayList<>();

    public List<Note> getNOTE() {
        return NOTE;
    }

    public void setNOTE(List<Note> NOTE) {
        this.NOTE = NOTE;
    }

    public List<Link> getLINK() {
        return LINK;
    }

    public void setLINK(List<Link> LINK) {
        this.LINK = LINK;
    }
}
