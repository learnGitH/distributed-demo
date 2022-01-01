package com.haibin.mongdb;

import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;

import static com.mongodb.client.model.Filters.eq;

public class QuickStart {

    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create("mongodb://192.168.159.178:27017");
        //获取数据库
        MongoDatabase database = mongoClient.getDatabase("productdb");
        //获取集合
        MongoCollection<Document> productdesc = database.getCollection("productdesc");

        Document doc = new Document("name","MongoDB")
                .append("type","database")
                .append("count",1)
                .append("versions", Arrays.asList("v3.2","v3.0","v2.6"))
                .append("info",new Document("x",203).append("y",102));

     //   productdesc.insertOne(doc);

        Bson eq = eq("name","MongoDB");
        FindIterable<Document> find = productdesc.find(eq);
        Document first=find.first();
        System.out.println(first);
    }

}




