package com.timeep.vo;

/**
 * @Description:
 * @Author: lh
 * @Date 2020/8/20
 * @Version: 1.0
 **/
public class Link {
    /**source 源*/
    private String source;

    private String target;

    private String name;

    public Link(String source, String target, String name) {
        this.source = source;
        this.target = target;
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
