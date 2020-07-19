package com.timeep.dao;

import com.timeep.po.Temp;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Li
 **/
public interface TempRepository extends JpaRepository<Temp,Long> {
}
