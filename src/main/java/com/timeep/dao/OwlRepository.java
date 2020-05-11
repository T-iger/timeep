package com.timeep.dao;

import com.timeep.po.Owl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Li
 **/
public interface OwlRepository extends JpaRepository<Owl ,Long> {
    List<Owl> findByPropertyAndObject(String property,String object);

    List<Owl> findByPropertyAndSubject(String property,String subject);

}
