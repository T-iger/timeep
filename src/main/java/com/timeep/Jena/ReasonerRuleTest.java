package com.timeep.Jena;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.ReasonerVocabulary;

public class ReasonerRuleTest {
	public static void main(String[] args) {

		Connection con = null;
		PreparedStatement pstm = null;
		Model m = ModelFactory.createDefaultModel();
		Resource configuration = m.createResource();
		configuration.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
		configuration.addProperty(ReasonerVocabulary.PROPruleSet, "C:/Users/88551/Desktop/mathv4.1.rules");

		// Create an instance of such a reasoner
		Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(configuration);

		// Load test data
		// Model data = FileManager.get().loadModel("file:expert/demodata.ttl");
		Model data = FileManager.get().loadModel("file:C:/Users/88551/Desktop/mathv4.2.owl");
		InfModel infmodel = ModelFactory.createInfModel(reasoner, data);

		// Query for all things related to "a" by "p"
//		Property p = data.getProperty(demoURI, "p");
//		Resource a = data.getResource(demoURI + "a");
		// StmtIterator i = infmodel.listStatements(a , p, (RDFNode)null);
		StmtIterator i = infmodel.listStatements();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/ontologyir?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=UTC";
//			String url = "jdbc:mysql://47.96.231.239:3306/ontologyir?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=UTC";
			String user = "root";
			String password = "root";
			con = DriverManager.getConnection(url, user, password);
			int count=1;
			while (i.hasNext()) {
				// System.out.println(" - " + PrintUtil.print(i.nextStatement()));
				org.apache.jena.rdf.model.Statement stmt = i.nextStatement();
				System.out.println(
						"<" + stmt.getSubject() + "> <" + stmt.getPredicate() + "> <" + stmt.getObject() + "> .");

				String sql = "insert into tb_owl(id,subject,property,object) value(?,?,?,?)";
				pstm = con.prepareStatement(sql);

				pstm.setLong(1,Long.valueOf(count));
				count++;

				/*
				 * 截取数据库中subject字段
				 */

				String s = stmt.getSubject().toString();
				if (s.contains("#")) {
					String s1 = s.substring(0, s.indexOf("#"));
					String s2 = s.substring(s1.length() + 1, s.length());
					pstm.setString(2, s2);
				} else {
					pstm.setString(2, s);
				}

				pstm.setString(3, stmt.getPredicate().getLocalName());

				/*
				 * 截取object字段
				 * 
				 */
				String str = stmt.getObject().toString();
				if (str.contains("#")) {
					String str1 = str.substring(0, str.indexOf("#"));
					String str2 = str.substring(str1.length() + 1, str.length());
					pstm.setString(4, str2);
				} else {
					pstm.setString(4, str);
				}
				int row = pstm.executeUpdate();
				// System.out.println("新增数据为:" + row + "条");
			}
			System.out.println("推理结束"+count);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstm != null) {
				try {
					pstm.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
