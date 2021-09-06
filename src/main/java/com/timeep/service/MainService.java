package com.timeep.service;

/**
 * @Description: 项目需求service
 * @Author: lh
 * @Date 2020/8/25
 * @Version: 1.0
 **/
public interface MainService {

    /**
     * 查询教材体系
     * @param subject
     * @return
     */
    StringBuilder findTextbookSystem(String subject);


    /**
     * 查询所有的教材体系
     * @param subject
     * @return
     */
    StringBuilder findAllTextbookSystem(String subject);

    /**
     * 再次查询教材体系
     * @param subject
     * @return
     */
    StringBuilder TextbookSystem(String subject);

    /**
     * 查询所有教育属性
     * @param subject
     * @return
     */
    StringBuilder findAllEducationProperty(String subject);

    /**
     * 查询所有的知识点体系
     * @param subject
     * @return
     */
    StringBuilder findAllKnowledgePointSystem(String subject);

    /**
     * 查询知识点体系
     * @param subject
     * @return
     */
    StringBuilder findKnowledgePointSystem(String subject);

    /**
     * 查询知识图谱（在用）
     * @param subject
     * @return
     */
    StringBuilder findKnowledgeGraph(String subject);

    /**
     * 查询前序和后继和参考知识点 （在用）
     * @param subject
     * @param number
     * @return
     */
    StringBuilder findIsSiblingOfAndRefK(String subject, int number);

    /**
     * 再次查询教材体系和教育属性之间的关系 （在用）
     * @param subject
     * @return
     */
    StringBuilder findAllTextbookSystemAndEducationProperty(String subject);

    /**
     * 推理功能 （在用）
     * @return
     */
    Boolean reasoning();

    /**
     * 获取学科 （在用）
     * @param subject
     * @return
     */
    Integer  getSubject(String subject);
}
