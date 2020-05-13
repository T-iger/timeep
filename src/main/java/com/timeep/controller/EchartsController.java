package com.timeep.controller;

import com.timeep.service.OwlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
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
    @PostMapping("/isSiblingof")
    public ResponseEntity<?> findIsSiblingOf(Model model, @RequestParam("find")String find){

        return ResponseEntity.ok(owlService.findIsSiblingOf(find));
    }

    @PostMapping("/hasPostK")
    public ResponseEntity<?> findHasPostK(Model model, @RequestParam("find")String find){

        return ResponseEntity.ok(owlService.findHasPostK(find));
    }
    @PostMapping("/hasPreK")
    public ResponseEntity<?> findHasPreK(Model model, @RequestParam("find")String find){

        return ResponseEntity.ok(owlService.findHasPreK(find));
    }
    @PostMapping("/all")
    public ResponseEntity<?> all(Model model, @RequestParam("find")String find){

        return ResponseEntity.ok(owlService.findAll(find));
    }


}
