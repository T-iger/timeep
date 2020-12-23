package com.timeep.common;

/**
 * @Description: 学科枚举，用来控制结点颜色
 * @Author: lh
 * @Date 2020/11/15
 * @Version: 1.0
 **/
public class Type {
    /**中心点*/
    public static final Integer CENTER_TYPE = 0;
    /**小学*/
    public static final String PRIMARY_SCHOOL ="MathKXiaoxue";
    /**初中*/
    public static final String JUNIOR_HIGH_SCHOOL = "MathKChuzhong";
    /**高中*/
    public static final String HIGH_SCHOOL =  "MathKGaozhong";
    /**高中*/
    public static final String BOOK =  "MathBook";
    /**小学*/
    public static final Integer PRIMARY_SCHOOL_TYPE = 1;
    /**初中*/
    public static final Integer JUNIOR_HIGH_SCHOOL_TYPE = 2;
    /**高中*/
    public static final Integer HIGH_SCHOOL_TYPE = 3;
    /**书本*/
    public static final Integer BOOK_TYPE = 4;
    /**教育*/
    public static final Integer EDUCATION_TYPE = 5;
    /**未知*/
    public static final Integer NO_TYPE = 6;

    /**兄弟的颜色 红*/
    public static final String ISSIBLINGOF_COLOR = "#FF0000";
    /**后续颜色  橙*/
    public static final String HASPOSTK_COLOR = "#FF7F00";
    /**前序颜色   绿*/
    public static final String HASPREK_COLOR = "#0000FF";
    /**relatedBook  蓝*/
    public static final String RELATEDBOOK_COLOR = "#0000FF";
    /**参考的颜色    紫*/
    public static final String HASREFK_COLOR = "#8B00FF";
    /**教育的颜色    棕褐色 #DB9370*/
    public static final String EDUCATION_COLOR = "#DB9370";



}
