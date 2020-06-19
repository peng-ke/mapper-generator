package com.pk.mappergenerator.filecreator;

import com.pk.mappergenerator.util.Const;
import com.pk.mappergenerator.util.FileUtil;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public abstract class TemplateCreator {

    protected final static String TEMPLATE_PATH = "template/";
    protected final static String SRC_PATH = "src/main/java/";
    protected final static String RESOURCE_PATH = "src/main/resources/";

    public void execute() {
        File target = new File(getTargetPath());
        try {
            if (!target.exists()) {
                create();
            } else {
                update();
            }
        } catch (IOException e) {
            log.error("文件生成出错, {}", target.getName(), e);
        }
    }

    public void create() throws IOException {
        // 获取要生成的是哪个模板
        String templateName = getTemplateName();
        String template = TEMPLATE_PATH + templateName;
        // 读模板
        String templateContent = FileUtil.readAsString(getClass().getClassLoader().getResource(template).getPath());
        // 替换模板表达式中的内容
        String content = replaceExpression(templateContent);
        // 获取要生成的文件的绝对路径
        String targetPath = this.getTargetPath();
        // 写出生成的文件
        FileUtil.writeString(targetPath, content);
    }


    public String replaceExpression(String str, String key, String value) {
        String newKey = Const.EXPRESSION_LEFT + key + Const.EXPRESSION_RIGHT;
        return StringUtils.replace(str, newKey, value);
    }

    public abstract void update() throws IOException;

    public abstract String getTargetPath();

    public abstract String replaceExpression(String content);

    public abstract String getTemplateName();

}
