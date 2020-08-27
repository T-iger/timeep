package com.timeep.vo;

/**
 * @Description:
 * @Author: lh
 * @Date 2020/8/20
 * @Version: 1.0
 **/
public class Note {
    /**name 结点名称*/
    private String name;
    /**des 结点描述*/
    private String des;
    /**symbolSize 结点大小*/
    private Integer symbolSize;
    /**category 结点颜色(类型)*/
    private Integer category;

    public Note(String name, String des, Integer symbolSize, Integer category) {
        this.name = name;
        this.des = des;
        this.symbolSize = symbolSize;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public Integer getSymbolSize() {
        return symbolSize;
    }

    public void setSymbolSize(Integer symbolSize) {
        this.symbolSize = symbolSize;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Note{" +
                "name='" + name + '\'' +
                ", des='" + des + '\'' +
                ", symbolSize='" + symbolSize + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
