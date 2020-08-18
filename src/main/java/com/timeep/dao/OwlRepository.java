package com.timeep.dao;

import com.timeep.po.Owl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Li
 **/
public interface OwlRepository extends JpaRepository<Owl ,Long> {

    List<Owl> findByPropertyAndObject(String property,String object);

    /*关于object的模糊查询*/
    List<Owl> findByPropertyAndObjectContaining(String property,String object);

    /*关于subject的模糊查询*/
    List<Owl> findByPropertyAndSubjectContaining(String property,String subject);

    List<Owl> findByPropertyAndSubject(String property,String subject);

    /*模糊查询关系*/
    List<Owl> findByPropertyContainingAndSubject(String property,String subject);



}
