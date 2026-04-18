package com.vuthevy1209.springmail.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "emails")
public class MailElasticSearch {

    @Id
    private String id; // email ID

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Keyword)
    private String threadId;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String subject;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String snippet;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String sender;

    @Field(type = FieldType.Keyword)
    private String senderEmail;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String receiver;

    @Field(type = FieldType.Keyword)
    private String receiverEmail;

    @Field(type = FieldType.Keyword)
    private Set<String> labelIds;

    @Field(type = FieldType.Date)
    private Long timestamp;
}
