package com.gorson.springai;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

public class TextReaderDemo {

    public static void main(String[] args) {

        TextReader reader = new TextReader(
                new ClassPathResource("rag/sample.txt")
        );

        List<Document> documents = reader.read();

        for (Document document : documents) {

            System.out.println("Document ID:");
            System.out.println(document.getId());

            System.out.println("Document Content:");
            System.out.println(document.getText());

            System.out.println("Document Metadata:");
            System.out.println(document.getMetadata());
        }
    }
}