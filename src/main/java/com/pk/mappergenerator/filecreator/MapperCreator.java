package com.pk.mappergenerator.filecreator;

import com.pk.mappergenerator.config.ModelConfig;
import com.pk.mappergenerator.util.Const;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class MapperCreator extends TemplateCreator {

    private ModelConfig modelConfig;

    @Override
    public String getTargetPath() {
        StringBuilder sb = new StringBuilder();
        String packagePath = modelConfig.getMapperPackage().replace(".", "/");
        sb.append(modelConfig.getAbsolutePath());
        sb.append(SRC_PATH);
        sb.append(packagePath);
        sb.append("/");
        sb.append(modelConfig.getPojoName() + "Mapper");
        sb.append(Const.JAVA_SUFFIX);
        return sb.toString();
    }

    /**
     * 不覆盖
     * @param
     * @return void
     * @Author: pengke
     * @Date: 2020年06月17日
     **/
    @Override
    public void update() throws IOException {
    }

    @Override
    public String replaceExpression(String content) {
        // 替换所有表达式中的值
        content = super.replaceExpression(content, Const.PACKAGE_PATH, modelConfig.getMapperPackage());
        content = super.replaceExpression(content, Const.MODEL_PATH, modelConfig.getPojoPackage() + "." + modelConfig.getPojoName());
        content = super.replaceExpression(content, Const.CLASS_NAME, modelConfig.getPojoName());

        return content;
    }

    @Override
    public String getTemplateName() {
        return "Mapper.txt";
    }

}
