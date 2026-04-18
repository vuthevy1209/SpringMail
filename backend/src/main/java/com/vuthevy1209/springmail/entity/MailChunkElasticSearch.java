package com.vuthevy1209.springmail.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "email_chunks")
public class MailChunkElasticSearch {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String mailId;

    @Field(type = FieldType.Keyword)
    private String threadId;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String chunkText;

    @Field(type = FieldType.Integer)
    private Integer chunkIndex;

    @Field(type = FieldType.Dense_Vector, dims = 768)
    private List<Float> contentVector;
    
    @Field(type = FieldType.Keyword)
    private Set<String> labelIds;

    @Field(type = FieldType.Date)
    private Long timestamp;
}
