# mepper-generator

### 概述

​		一个生成 mapper 文件的脚手架工具，可以生成`pojo类`、`mapper接口`，`mapperxml`文件。当修改数据库字段后重新生成文件时**不会覆盖**自定义的SQL以及不会生成重复的xml片段。

### 使用

​		使用时只需要创建**DatabaseConfig**、**ModelConfig**、**OtherConfig**三个配置对象。执行**Generator**类的`generate`方法即可。

### 配置类

#### DatabaseConfig

数据库连接配置类

##### 属性说明

| 属性     | 含义         |
| -------- | ------------ |
| url      | 数据库连接   |
| driver   | 数据库驱动   |
| username | 数据库用户名 |
| password | 数据库密码   |

#### ModelConfig

所生成的文件的配置

| 属性          | 含义                                |
| ------------- | ----------------------------------- |
| absolutePath  | 项目的绝对路径                      |
| tableName     | 表明                                |
| pojoPackage   | 实体类的全限定包名                  |
| pojoName      | 实体类名（如果不定义，默认为驼峰）  |
| mapperPackage | mapper接口的全限定包名              |
| xmlPath       | xml文件的路径（resources/下的路径） |

#### OtherConfig

其他配置

| 属性         | 含义                       |
| ------------ | -------------------------- |
| useJava8Time | 是否使用 java8 的时间 API  |
| pojoComment  | 实体类是否生成数据库的注释 |

