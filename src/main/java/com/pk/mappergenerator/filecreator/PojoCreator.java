package com.pk.mappergenerator.filecreator;

import com.pk.mappergenerator.config.ModelConfig;
import com.pk.mappergenerator.core.TableInfo;
import com.pk.mappergenerator.util.Const;
import java.io.IOException;
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
        String fields = tableInfo.getFieldsDeclareInfo();
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
}
