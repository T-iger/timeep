package com.timeep.controller;

import com.timeep.service.OwlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.List;

/**
 * @author Li
 **/
@Controller
@RequestMapping("/")
public class EchartsController {
    @Autowired
    private OwlService owlService;
    @GetMapping("/")
    public String hello(Model model){
        List<String> zhiShiDian = owlService.findZhiShiDian();
        model.addAttribute("zhishidianList",zhiShiDian);
        return "/index";
    }
    @PostMapping("/xiongdi")
    public ResponseEntity<?> hello2(Model model){
        System.out.println(owlService.findIsSiblingOf("中心对称图形").get("NOTE"));
        return ResponseEntity.ok(owlService.findIsSiblingOf("中心对称图形"));
    }

}
