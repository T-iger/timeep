package com.timeep.service;

import com.timeep.dao.VersionRepository;
import com.timeep.po.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Li
 **/
@Service
public class VersionServiceImpl implements VersionService {
    @Autowired
    private  VersionRepository versionRepository;

    @Override
    public List<Version> findAll() {
        List<Version> all = versionRepository.findAll();
        return all;
    }

    @Override
    public Version save(Version version) {
        return versionRepository.save(version);
    }
}
