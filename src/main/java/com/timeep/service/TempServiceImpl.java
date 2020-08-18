package com.timeep.service;

import com.timeep.dao.OwlRepository;
import com.timeep.dao.TempRepository;
import com.timeep.po.Owl;
import com.timeep.po.Temp;
import com.timeep.util.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Li
 **/
@Service
public class TempServiceImpl implements TempService {

    @Autowired
    private TempRepository tempRepository;
    @Autowired
    private OwlRepository owlRepository;

    @Override
    public boolean update() {
        tempRepository.deleteAll();
        boolean b = Update.doUpdate();
        if (b){
            owlRepository.deleteAll();
            List<Temp> all =tempRepository.findAll();
            List<Owl> owlList=new ArrayList<>();
            for (Temp temp : all) {
                Owl owl=new Owl();
                owl.setId(temp.getId());
                owl.setObject(temp.getObject());
                owl.setProperty(temp.getProperty());
                owl.setSubject(temp.getSubject());
                owlList.add(owl);
            }
            owlRepository.saveAll(owlList);
            return true;
        }else{
            return false;
        }
    }
}
