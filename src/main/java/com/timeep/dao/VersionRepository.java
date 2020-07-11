package com.timeep.dao;

import com.timeep.po.Owl;
import com.timeep.po.Version;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Li
 **/
public interface VersionRepository extends JpaRepository<Version,Long> {
}
