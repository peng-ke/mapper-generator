package com.pk.mappergenerator.filecreator;

import com.pk.mappergenerator.config.ModelConfig;
import com.pk.mappergenerator.core.DataInfo;
import com.pk.mappergenerator.core.TableInfo;
import com.pk.mappergenerator.util.Const;
import com.pk.mappergenerator.util.StringUtil;
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
    private static final String INSERT_BATCH_START = "<insert id=\"insertBatch\"";
    private static final String INSERT_BATCH_END = "</insert>";
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
        content = super.replaceExpression(content, Const.RESULT_MAP, this.getResultMap());
        content = super.replaceExpression(content, Const.BASE_FIELD, Const.TAB2 + this.getColumnNames());
        content = super.replaceExpression(content, Const.OTHER_CONDITION, this.getOtherCondition());
        content = super.replaceExpression(content, Const.TABLE_NAME, modelConfig.getTableName());
        content = super.replaceExpression(content, Const.INSERT_STATEMENT, this.getInsertStatement());
        content = super.replaceExpression(content, Const.INSERT_BATCH_STATEMENT, this.getInsertBatchStatement());
        content = super.replaceExpression(content, Const.UPDATE_STATEMENT, this.getUpdateStatement());
        content = super.replaceExpression(content, Const.UPDATE_MAP_MODEL, this.getUpdateMapModel());

        return content;
    }

    @Override
    public String getTemplateName() {
        return "SqlMap.txt";
    }

    private List<Node> getXMLUpdateMappingList(TableInfo tableInfo){
        List<Node> list = new ArrayList<>();
        list.add(new Node(RESULTMAP_START, RESULTMAP_END, this.getResultMap()));
        list.add(new Node(BASE_FIELD_START, BASE_FIELD_END, Const.TAB2 + this.getColumnNames()));
        list.add(new Node(OTHER_CONDITION_START, OTHER_CONDITION_END, this.getOtherCondition()));
        list.add(new Node(INSERT_START, INSERT_END, this.getInsertStatement()));
        list.add(new Node(INSERT_BATCH_START, INSERT_BATCH_END, this.getInsertBatchStatement()));
        list.add(new Node(UPDATE_START, UPDATE_END, this.getUpdateStatement()));
        list.add(new Node(UPDATE4SELECTIVE_START, UPDATE4SELECTIVE_END, this.getUpdateMapModel()));
        return list;
    }

    public String getInsertStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append(Const.TAB2);
        sb.append("insert into ");
        sb.append(tableInfo.getName());
        sb.append("( <include refid=\"base_field\" /> )");
        sb.append(Const.ENDL);
        sb.append(Const.TAB2);
        sb.append("values (");
        sb.append("#{id}, ");
        for (DataInfo dataInfo : tableInfo.getDataInfos()) {
            sb.append("#{");
            sb.append(dataInfo.getFieldName());
            sb.append("}, ");
        }
        StringUtil.deleteLastStr(sb, 2);
        sb.append(")");
        return sb.toString();
    }

    public String getColumnNames() {
        StringBuilder sb = new StringBuilder();
        sb.append(tableInfo.getPrimaryKey());
        sb.append(", ");
        for (DataInfo dataInfo : tableInfo.getDataInfos()) {
            sb.append(dataInfo.getColumnName());
            sb.append(", ");
        }
        StringUtil.deleteLastStr(sb, 2);
        return sb.toString();
    }

    public String getUpdateStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append(Const.TAB2);
        sb.append("update ");
        sb.append(tableInfo.getName());
        sb.append(" set ");
        for (DataInfo dataInfo : tableInfo.getDataInfos()) {
            sb.append(dataInfo.getColumnName());
            sb.append("=#{");
            sb.append(dataInfo.getFieldName());
            sb.append("}, ");
        }
        StringUtil.deleteLastStr(sb, 2);
        sb.append(" where ");
        sb.append(tableInfo.getPrimaryKey());
        sb.append("=#{id}");
        return sb.toString();
    }

    public String getResultMap() {
        StringBuilder sb = new StringBuilder();
        sb.append(Const.TAB2);
        sb.append("<id property=\"id\"");
        sb.append(" column=\"");
        sb.append(tableInfo.getPrimaryKey());
        sb.append("\"");
        sb.append(" jdbcType=\"BIGINT\" />");
        sb.append(Const.ENDL);
        for (DataInfo dataInfo : tableInfo.getDataInfos()) {
            sb.append(Const.TAB2);
            sb.append("<result property=\"");
            sb.append(dataInfo.getFieldName());
            sb.append("\" column=\"");
            sb.append(dataInfo.getColumnName());
            sb.append("\"");
            sb.append(" jdbcType=\"");
            sb.append(dataInfo.getJdbcType());
            sb.append("\"");
            sb.append("/>");
            sb.append(Const.ENDL);
        }
        StringUtil.deleteLastStr(sb, Const.ENDL.length());
        return sb.toString();
    }

    public String getOtherCondition() {
        StringBuilder sb = new StringBuilder();
        for (DataInfo dataInfo : tableInfo.getDataInfos()) {
            sb.append(Const.TAB2);
            sb.append("<if test= \"");
            sb.append(dataInfo.getFieldName());
            sb.append(" != null\">");
            sb.append(" and ");
            sb.append(dataInfo.getColumnName());
            sb.append(" = #{");
            sb.append(dataInfo.getFieldName());
            sb.append("}");
            sb.append("</if>");
            sb.append(Const.ENDL);
        }
        StringUtil.deleteLastStr(sb, Const.ENDL.length());
        return sb.toString();
    }

    public String getUpdateMapModel() {// 动态字段更新
        StringBuilder sb = new StringBuilder();
        sb.append(Const.TAB2);
        sb.append("update ");
        sb.append(tableInfo.getName());
        sb.append(Const.ENDL);
        sb.append(Const.TAB2);
        sb.append("<set>");
        sb.append(Const.ENDL);
        for (DataInfo dataInfo : tableInfo.getDataInfos()) {
            sb.append(Const.TAB3);
            sb.append("<if test=\"");
            sb.append(dataInfo.getFieldName());
            sb.append(" != null \"> ");
            sb.append(Const.ENDL);
            sb.append(Const.TAB4);
            sb.append(dataInfo.getColumnName());
            sb.append(" = #{");
            sb.append(dataInfo.getFieldName());
            sb.append("},");
            sb.append(Const.ENDL);
            sb.append(Const.TAB3);
            sb.append("</if>");
            sb.append(Const.ENDL);
        }
        sb.append(Const.TAB2);
        sb.append("</set>");
        sb.append(Const.ENDL);
        sb.append(Const.TAB2);
        sb.append(" where ");
        sb.append(tableInfo.getPrimaryKey());
        sb.append("=#{id}");
        return sb.toString();
    }

    public String getInsertBatchStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append(Const.TAB2);
        sb.append("insert into ");
        sb.append(tableInfo.getName());
        sb.append("( <include refid=\"base_field\" /> )");
        sb.append(Const.ENDL);
        sb.append(Const.TAB2);
        sb.append("values");
        sb.append(Const.ENDL);
        sb.append(Const.TAB2);
        sb.append("<foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\" >");
        sb.append(Const.ENDL);
        sb.append(Const.TAB3);
        sb.append("(");
        int i = 0;
        for (DataInfo dataInfo : tableInfo.getDataInfos()) {
            sb.append("#{item.");
            sb.append(dataInfo.getFieldName());
            sb.append("}, ");
            if (++i % 5 == 0) {
                sb.append(Const.ENDL);
                sb.append(Const.TAB3);
            }
        }
        StringUtil.deleteLastStr(sb, 2);
        sb.append(")");
        sb.append(Const.ENDL);
        sb.append(Const.TAB2);
        sb.append("</foreach>");
        return sb.toString();
    }
}
