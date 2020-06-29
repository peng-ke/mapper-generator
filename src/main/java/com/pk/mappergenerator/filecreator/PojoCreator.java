package com.pk.mappergenerator.filecreator;

import com.pk.mappergenerator.config.ModelConfig;
import com.pk.mappergenerator.core.DataInfo;
import com.pk.mappergenerator.core.TableInfo;
import com.pk.mappergenerator.util.Const;
import com.pk.mappergenerator.util.StringUtil;
import java.io.IOException;
import java.util.LinkedHashSet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class PojoCreator extends TemplateCreator {

    private TableInfo tableInfo;
    private ModelConfig modelConfig;

    @Override
    public void update() throws IOException {
        create();
    }

    @Override
    public String replaceExpression(String content) {
        String fields = this.getFieldsDeclareInfo();
        // 替换所有表达式中的值
        content = super.replaceExpression(content, Const.PACKAGE_PATH, modelConfig.getPojoPackage());
        content = super.replaceExpression(content, Const.CLASS_NAME, modelConfig.getPojoName());
        content = super.replaceExpression(content, Const.FIELDS, fields);

        return content;
    }

    @Override
    public String getTargetPath() {
        String packagePath = modelConfig.getPojoPackage().replace(".", "/");
        StringBuilder sb = new StringBuilder();
        sb.append(modelConfig.getAbsolutePath());
        sb.append(SRC_PATH);
        sb.append(packagePath);
        sb.append("/");
        sb.append(modelConfig.getPojoName());
        sb.append(Const.JAVA_SUFFIX);
        return sb.toString();
    }

    @Override
    public String getTemplateName() {
        return "Model.txt";
    }

    public String getFieldsDeclareInfo() {
        LinkedHashSet<DataInfo> dataInfos = tableInfo.getDataInfos();
        if (dataInfos == null || dataInfos.size() == 0) {
            return "";
        }

        StringBuffer sb = new StringBuffer(Const.TAB);
        sb.append("private Long id;");
        sb.append(Const.ENDL);
        for (DataInfo dataInfo : dataInfos) {
            if (dataInfo.getFieldName().equalsIgnoreCase(Const.ID))
                continue;// id property is in the BaseModel
            if (!StringUtil.isBlank(dataInfo.getComment())) {
                sb.append(Const.TAB);
                sb.append("/*  ");
                sb.append(dataInfo.getComment());
                sb.append("  */");
            }
            sb.append(Const.ENDL);
            sb.append(Const.TAB);
            sb.append("private ");
            sb.append(dataInfo.getJavaType());
            sb.append(" ");
            sb.append(dataInfo.getFieldName());
            sb.append(";");
            sb.append(Const.ENDL);
        }
        return sb.toString();
    }
}
