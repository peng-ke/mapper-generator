package com.pk.mappergenerator.filecreator;

import com.pk.mappergenerator.config.ModelConfig;
import com.pk.mappergenerator.core.TableInfo;
import com.pk.mappergenerator.util.Const;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class XmlCreator extends TemplateCreator {

    public static final String RESULTMAP_START = "<resultMap id=\"result\"";
    public static final String RESULTMAP_END = "</resultMap>";
    public static final String BASE_FIELD_START = "<sql id=\"base_field\">";
    public static final String BASE_FIELD_END = "</sql>";
    public static final String OTHER_CONDITION_START = "<sql id=\"other-condition\">";
    public static final String OTHER_CONDITION_END = "</sql>";
    public static final String INSERT_START = "<insert id=\"insert\"";
    public static final String INSERT_END = "</insert>";
    public static final String UPDATE_START = "<update id=\"update\"";
    public static final String UPDATE_END = "</update>";
    public static final String UPDATE4SELECTIVE_START = "<update id=\"update4Selective\"";
    public static final String UPDATE4SELECTIVE_END = "</update>";

    private TableInfo tableInfo;
    private ModelConfig modelConfig;

    @Data
    @AllArgsConstructor
    class Node {
        private String start;
        private String end;
        private String newContent;
    }

    /**
     * 选择性更新
     **/
    @Override
    public void update(){
        String targetPath = getTargetPath();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(targetPath)))){

            List<Node> replaceNodes = getXMLUpdateMappingList(tableInfo);
            boolean isNeedReplace = false;
            int mappingIndex = 0;
            String line = null;
            List<String> newLineList = new ArrayList<String>();
            while ((line = reader.readLine()) != null) {
                /*
                 *  先判断是否以 start 开头
                 *  如果不是, 判断是否需要替换
                 *  如果不需要,输出原内容
                 *  如果需要,判断是否以 end 开头
                 *  如果不是 什么都不做
                 *  如果是 输出替换内容,输出结尾标签, 下标+1,isNeedReplace=false
                 *  如果是, isNeedReplace=true,输出原内容
                 */
                // 替换完成,输出原内容
                if (mappingIndex == replaceNodes.size()) {
                    newLineList.add(line);// 原内容输出
                    continue;
                }
                Node node = replaceNodes.get(mappingIndex);

                if (line.trim().startsWith(node.getStart())) {
                    isNeedReplace = true;
                    newLineList.add(line);// 原内容输出
                } else {
                    if (isNeedReplace) {
                        if (line.trim().startsWith(node.getEnd())) {
                            newLineList.add(node.getNewContent());
                            newLineList.add(line);// 原内容输出
                            isNeedReplace = false;
                            mappingIndex++;
                        } else {
                            // 什么都不做
                        }
                    } else {
                        newLineList.add(line);// 原内容输出
                    }
                }
            }
            // 输出
            Files.write(Paths.get(targetPath), newLineList);
        } catch (IOException e) {
            log.error("{}：文件更新失败！", targetPath);
        }
    }

    @Override
    public String getTargetPath() {
        StringBuilder sb = new StringBuilder();
        String packagePath = modelConfig.getXmlPath().replace(".", "/");
        sb.append(modelConfig.getAbsolutePath());
        sb.append(RESOURCE_PATH);
        sb.append(packagePath);
        sb.append("/");
        sb.append(modelConfig.getPojoName() + "Mapper");
        sb.append(Const.XML_SUFFIX);
        return sb.toString();
    }

    @Override
    public String replaceExpression(String content) {
        StringBuilder mapperPath = new StringBuilder();
        mapperPath.append(modelConfig.getMapperPackage());
        mapperPath.append(".");
        mapperPath.append(modelConfig.getPojoName());
        mapperPath.append(Const.MAPPER_SUFFIX);
        // 替换所有表达式中的值
        content = super.replaceExpression(content, Const.MAPPER_PATH, mapperPath.toString());
        content = super.replaceExpression(content, Const.MODEL_PATH, modelConfig.getPojoPackage() + "." + modelConfig.getPojoName());
        content = super.replaceExpression(content, Const.RESULT_MAP, tableInfo.getResultMap());
        content = super.replaceExpression(content, Const.BASE_FIELD, Const.TAB2 + tableInfo.getColumnNames());
        content = super.replaceExpression(content, Const.OTHER_CONDITION, tableInfo.getOtherCondition());
        content = super.replaceExpression(content, Const.TABLE_NAME, modelConfig.getTableName());
        content = super.replaceExpression(content, Const.INSERT_STATEMENT, tableInfo.getInsertStatement());
        content = super.replaceExpression(content, Const.UPDATE_STATEMENT, tableInfo.getUpdateStatement());
        content = super.replaceExpression(content, Const.UPDATE_MAP_MODEL, tableInfo.getUpdateMapModel());

        return content;
    }

    @Override
    public String getTemplateName() {
        return "SqlMap.txt";
    }

    private List<Node> getXMLUpdateMappingList(TableInfo tableInfo){
        List<Node> list = new ArrayList<>();
        list.add(new Node(RESULTMAP_START, RESULTMAP_END, tableInfo.getResultMap()));
        list.add(new Node(BASE_FIELD_START, BASE_FIELD_END, Const.TAB2 + tableInfo.getColumnNames()));
        list.add(new Node(OTHER_CONDITION_START, OTHER_CONDITION_END, tableInfo.getOtherCondition()));
        list.add(new Node(INSERT_START, INSERT_END, tableInfo.getInsertStatement()));
        list.add(new Node(UPDATE_START, UPDATE_END, tableInfo.getUpdateStatement()));
        list.add(new Node(UPDATE4SELECTIVE_START, UPDATE4SELECTIVE_END, tableInfo.getUpdateMapModel()));
        return list;
    }
}
