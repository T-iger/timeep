package com.timeep.controller;

import com.timeep.service.OwlService;
import com.timeep.service.TempService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    @Autowired
    private TempService tempService;

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

    @GetMapping("5")
    public String p5(Model model) {
        return "indextest5";
    }

    @GetMapping("4")
    public String p4(Model model) {
        return "indextest4";
    }

    @GetMapping("3")
    public String p3(Model model) {
        return "indextest3";
    }

    @GetMapping("2")
    public String p2(Model model) {
        return "indextest2";
    }

    @GetMapping("1")
    public String p1(Model model) {
        return "indextest1";
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
        return ResponseEntity.ok(owlService.findKnowledgeGraph("所有"));
//        return ResponseEntity.ok(owlService.findHasPreK(find));
    }

    @PostMapping("hasAllEducationProperty")
    public ResponseEntity<?> findAllEducationProperty(Model model, @RequestParam("find") String find) {
        return ResponseEntity.ok(owlService.findAllEducationProperty("所有的教育属性"));
    }

    @PostMapping("hasAllSection")
    public ResponseEntity<?> findAllSection(Model model, @RequestParam("find") String find) {
        return ResponseEntity.ok(owlService.findAllKnowledgePointSystem("所有的教材体系"));
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


    @PostMapping("KnowledgeGraph")
    public ResponseEntity<?> KnowledgeGraph(Model model) {
        return ResponseEntity.ok(owlService.findKnowledgeGraph("Thing"));
    }

    @PostMapping("EducationProperty")
    public ResponseEntity<?> EducationProperty(Model model) {
        return ResponseEntity.ok(owlService.findAllEducationProperty("Thing"));
    }

    @PostMapping("TextbookSystem")
    public ResponseEntity<?> TextbookSystem(Model model) {
        return ResponseEntity.ok(owlService.findAllTextbookSystem("Thing"));
    }

    @PostMapping("findTextbookSystem")
    public ResponseEntity<?> findTextbookSystem(Model model) {
        return ResponseEntity.ok(owlService.findTextbookSystem("MathBookHK2014Chuzhong"));
    }

    @PostMapping("KnowledgePointSystem")
    public ResponseEntity<?> KnowledgePointSystem(Model model) {
        return ResponseEntity.ok(owlService.findAllKnowledgePointSystem("Thing"));
    }


    /*-----------以下为出版社所需接口、传输数据为post方式、数据为form-data格式----------------*/
    /*定位查询教材体系
     * @Param X (为教材版本,章节)
     * */
    @PostMapping("RETextbookSystem")
    public ResponseEntity<?> RETextbookSystem(Model model, @RequestParam("X") String X) {
        return ResponseEntity.ok(owlService.TextbookSystem(X));
    }

    /*数据查询接口*/
    @PostMapping("search")
    public ResponseEntity<?> Search(@RequestParam("flag") int flag,//查询类型
                                    @RequestParam(value = "relation", required = false) String relation,//查询关系
                                    @RequestParam(value = "query", required = false) String query//被查询数据
    ) {
        if (flag == 1 && "all".equals(relation)) {//教育属性
            //query=初中数学人教版
            return ResponseEntity.ok(owlService.findAllEducationProperty("Thing"));
        } else if (flag == 2) {//教材体系
            // query=MathBookHK2014Chuzhong
            if ("all".equals(relation)) {
                return ResponseEntity.ok(owlService.findAllTextbookSystem("Thing"));
            } else if ("single".equals(relation)) {
                return ResponseEntity.ok(owlService.findTextbookSystem(query));
            } else {
                return ResponseEntity.ok(false);
            }
        } else if (flag == 3) {//知识点体系
            if ("all".equals(relation) && query != "") {
                return ResponseEntity.ok(owlService.findKnowledgePointSystem(query));
            } else if ("all".equals(relation) && query == "") {
                return ResponseEntity.ok(owlService.findAllKnowledgePointSystem("Thing"));
            } else {
                return ResponseEntity.ok(false);
            }
        } else if (flag == 4) {//知识图谱
            //query= 知识点
            if ("all".equals(relation)) {
                return ResponseEntity.ok(owlService.findKnowledgeGraph(query));
            } else {
                return ResponseEntity.ok(false);
            }
        } else {
            return ResponseEntity.ok(false);
        }
    }

    /*知识图谱版本更新*/
    @PostMapping("update")
    public ResponseEntity<?> update(@RequestParam(value = "time", required = false) Date time, @RequestParam("do") int d, @RequestParam("version") String version) {
        long startTime = System.currentTimeMillis();
        boolean b = tempService.update();
        long finishTime = System.currentTimeMillis();
        System.out.println("总耗时:" + (finishTime - startTime) / 1000);
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
        HashMap<String, String> map = new HashMap<>();
        map.put("time", df.format(new Date()));
        map.put("version", version);
        if (b) {
            map.put("do", "1");
            return ResponseEntity.ok(map);
        } else {
            map.put("do", "0");
            return ResponseEntity.ok(map);
        }
    }

    /*列表查询的数据（知识点的前后序，拥有层数控制）*/
    @PostMapping("findlist")
    public ResponseEntity<?> findList(@RequestParam("knowledge") String knowledge, @RequestParam("number") int number) {
        return ResponseEntity.ok(owlService.findIsSiblingOfAndRefK(knowledge, number));
    }

   /* @GetMapping("/upload")
    public String load(Model model) throws IOException {
        model.addAttribute("allVersion",versionService.findAll());

        return "indextest6";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "上传失败，请选择文件";
        }
        String fileName = file.getOriginalFilename();
        String filePath = "D:\\IDEA\\timeep\\src\\main\\resources\\upload\\";
        File dest = new File(filePath + fileName);
        try {
            file.transferTo(dest);
            return "上传成功";
        } catch (IOException e) {
        }
        return "上传失败！";
    }*/


}
