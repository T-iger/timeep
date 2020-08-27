package com.timeep.Jena;

import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.ReasonerVocabulary;


public class ReasonerRule111 {
    public static void main(String[] args) {
        //创建模型
        Model m = ModelFactory.createDefaultModel();
        Resource configuration = m.createResource();
        configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
        //推理规则文件加载
        configuration.addProperty(ReasonerVocabulary.PROPruleSet, "C:/Users/88551/Desktop/mathv4.1.rules");
        // 创建这样一个推理机的实例
        Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);
        //本体文件加载
        Model data = FileManager.get().loadModel("file:C:/Users/88551/Desktop/mathv4.1.owl");
        //
        InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
        StmtIterator i = infmodel.listStatements();

        int count = 1;
        while (i.hasNext()) {
            Statement stmt = i.nextStatement();
            System.out.println("subject:" + stmt.getSubject() + "***property:" + stmt.getPredicate() + "***object:" + stmt.getObject());
            count++;

            //处理数据subject
            String subject = stmt.getSubject().toString();
            if (subject!=null&&stmt.getSubject().toString().split("#").length>1){
                    subject = stmt.getSubject().toString().split("#")[1];
            }else{
                continue ;
            }
            System.out.println(subject);

            //取数据property
            String property= stmt.getPredicate().getLocalName();
            System.out.println(property);
            //截取object字段
            String object = stmt.getObject().toString();
            if (object!=null&&stmt.getObject().toString().split("#").length>1){
                    object = stmt.getObject().toString().split("#")[1];
            }else{
                continue ;
            }
            System.out.println(object);
        }
        System.out.println("推理结束" + count);
    }

}
