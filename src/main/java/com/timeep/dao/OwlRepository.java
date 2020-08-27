package com.timeep.dao;

import com.timeep.po.Owl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Li
 **/
public interface OwlRepository extends JpaRepository<Owl ,Long> {

    /**
     * 查询object和 Property
     * @param property
     * @param object
     * @return
     */
    @Query()
    List<Owl> findByPropertyAndObject(String property,String object);

    /**
     * 查询subject和 Property
     * @param property
     * @param subject
     * @return
     */
    List<Owl> findByPropertyAndSubject(String property,String subject);

    /**
     * 关于object的模糊查询
     * @param property
     * @param object
     * @return
     */
    List<Owl> findByPropertyAndObjectContaining(String property,String object);

    /**
     * 关于subject的模糊查询
     * @param property
     * @param subject
     * @return
     */
    List<Owl> findByPropertyAndSubjectContaining(String property,String subject);

    /**
     * 模糊查询关系
     * @param property
     * @param subject
     * @return
     */
    List<Owl> findByPropertyContainingAndSubject(String property,String subject);

}
