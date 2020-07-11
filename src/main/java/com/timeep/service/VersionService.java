package com.timeep.service;

import com.timeep.po.Version;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Li
 **/

public interface VersionService {
    List<Version> findAll();
    Version save(Version version);
}
