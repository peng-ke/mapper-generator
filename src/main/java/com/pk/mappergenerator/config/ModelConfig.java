package com.pk.mappergenerator.config;


import lombok.Data;

@Data
public class ModelConfig {
    private String absolutePath;
    private String tableName;
    private String pojoPackage;
    private String pojoName;
    private String mapperPackage;
    private String xmlPath;
//    private String servicePackage;
//    private String serviceImplPackage;
}
