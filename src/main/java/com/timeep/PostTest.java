package com.timeep;

/**
 * @author Li
 **/
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


public class PostTest {
    public void doPost() throws ClientProtocolException, IOException {
//1.新建一个客户端对象
        CloseableHttpClient client = HttpClients.createDefault();
//实例化一个post对象
//        HttpPost post = new HttpPost("http://36.7.147.134:18080/search");
//        HttpPost post = new HttpPost("http://47.96.231.239:8080/search");
        HttpPost post = new HttpPost("http://47.96.231.239:8081/demo/deleteNodeInstance");
//        HttpPost post = new HttpPost("http://127.0.0.1:8080/search");
//  使用NameValuePair将发送的参数打包
        List<NameValuePair> list = new ArrayList<NameValuePair>();


//打包
//        list.add(new BasicNameValuePair("flag", "3"));
//        list.add(new BasicNameValuePair("relation", "all"));
//        list.add(new BasicNameValuePair("query", "MathBookHK2014Chuzhong"));//2
        list.add(new BasicNameValuePair("id", "2020-07-03"));
        list.add(new BasicNameValuePair("type", "2"));
        list.add(new BasicNameValuePair("name", "WANGJIE"));
        list.add(new BasicNameValuePair("className", "Person"));

//        list.add(new BasicNameValuePair("query", "MathKChuzhong"));//3
//        list.add(new BasicNameValuePair("query", null));


//使用URLEncodedFormEntity工具类实现一个entity对象,并使用NameValuePair中的数据进行初始化
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(list, Consts.UTF_8);
//将实例化的 entity对象放到post对象的请求体中
        post.setEntity(formEntity);
//建立一个响应对象, 接受客户端执行post后的响应结果
        CloseableHttpResponse response = client.execute(post);
//从实体中提取结果数据
        String result = EntityUtils.toString(response.getEntity());
        System.out.println(result);
    }

    public static void main(String[] args) throws ClientProtocolException, IOException {
        PostTest postDemo = new PostTest();
        postDemo.doPost();
    }
}