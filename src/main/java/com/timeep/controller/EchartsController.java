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

    @GetMapping("timeep")
    public String hello(Model model) {
        List<String> zhiShiDian = owlService.findZhiShiDian();
        model.addAttribute("zhishidianList", zhiShiDian);
        return "index";
    }

    @GetMapping("test")
    public String hello2(Model model) {
        List<String> zhiShiDian = owlService.findZhiShiDian();
        model.addAttribute("zhishidianList", zhiShiDian);
        return "index2";
    }

    @PostMapping("first")
    public ResponseEntity<?> first(Model model, @RequestParam("find") String find, @RequestParam("is") Boolean is) {
        return ResponseEntity.ok(owlService.findFirst(find, is));
    }

    @PostMapping("isSiblingof")
    public ResponseEntity<?> findIsSiblingOf(Model model, @RequestParam("find") String find) {

        return ResponseEntity.ok(owlService.findIsSiblingOf(find));
    }

    @PostMapping("hasPostK")
    public ResponseEntity<?> findHasPostK(Model model, @RequestParam("find") String find) {

        return ResponseEntity.ok(owlService.findHasPostK(find));
    }

    @PostMapping("hasPreK")
    public ResponseEntity<?> findHasPreK(Model model, @RequestParam("find") String find) {
        return ResponseEntity.ok(owlService.findHasPreK(find));
    }

    @PostMapping("all")
    public ResponseEntity<?> all(Model model, @RequestParam("find") String find) {

        return ResponseEntity.ok(owlService.findAll(find));
    }

    /*知识点和章节的关系
    relateBook 知识点 查章节 查类名称
    relatedK 章节 查知识点*/
    @PostMapping("relatedBook")
    public ResponseEntity<?> relatedBook(Model model, @RequestParam("find") String find) {
        System.out.println();
        return ResponseEntity.ok(owlService.findK(find));
    }

    @PostMapping("search")
    public ResponseEntity<?> Search(@RequestParam("flag") int flag,//查询类型
                                    @RequestParam(value = "relation", required = false) String relation,//查询关系
                                    @RequestParam("query") String query//被查询数据
    ) {
        if (flag == 1) {//教育属性
            return ResponseEntity.ok(owlService.findEducationProperty(query));
        } else if (flag == 2) {//教材体系
            return ResponseEntity.ok(owlService.findSection(query));
        } else if (flag == 3) {//知识点体系
            if ("并列关系".equals(relation)) {
                return ResponseEntity.ok(owlService.findIsSiblingOf(query));
            } else if ("前序关系".equals(relation)) {
                return ResponseEntity.ok(owlService.findHasPreK(query));
            } else if ("后继关系".equals(relation)) {
                return ResponseEntity.ok(owlService.findHasPostK(query));
            } else if ("relatedBook".equals(relation)) {
                return ResponseEntity.ok(owlService.findK(query));
            } else {
                return ResponseEntity.ok(false);
            }
        } else if (flag == 4) {//知识图谱
            if ("所有关系".equals(relation)) {
                return ResponseEntity.ok(owlService.findAll(query));
            } else {
                return ResponseEntity.ok(false);
            }
        } else if (flag == 5) {//所有
            return ResponseEntity.ok(owlService.findFirst("MathKChuzhong", true));
        } else {
            return ResponseEntity.ok(false);
        }
    }
}
