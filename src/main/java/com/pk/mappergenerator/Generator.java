package com.pk.mappergenerator;

import com.pk.mappergenerator.config.DatabaseConfig;
import com.pk.mappergenerator.config.ModelConfig;
import com.pk.mappergenerator.config.OtherConfig;
import com.pk.mappergenerator.core.TableInfo;
import com.pk.mappergenerator.filecreator.MapperCreator;
import com.pk.mappergenerator.filecreator.PojoCreator;
import com.pk.mappergenerator.filecreator.XmlCreator;
import com.pk.mappergenerator.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class Generator {

    public static void generate(DatabaseConfig databaseConfig, ModelConfig modelConfig, OtherConfig otherConfig) {
        if (StringUtils.isBlank(modelConfig.getPojoName())) {
            modelConfig.setPojoName(StringUtils.capitalize(StringUtil.getFildName(modelConfig.getTableName())));
        }
        if (modelConfig == null) {
            throw new IllegalArgumentException("modelConfig 不能为空！");
        }
        if (otherConfig == null) {
            otherConfig = new OtherConfig();
        }
        // 获取表信息
        TableInfo tableInfo = TableInfo.parseTable(databaseConfig, modelConfig.getTableName(), otherConfig);
        if (tableInfo == null)
            return;
        PojoCreator creator = new PojoCreator(tableInfo, modelConfig);
        MapperCreator mapperCreator = new MapperCreator(modelConfig);
        XmlCreator xmlCreator = new XmlCreator(tableInfo, modelConfig);

        creator.execute();
        mapperCreator.execute();
        xmlCreator.execute();

        log.info("success!");
    }

    public static void generate(DatabaseConfig databaseConfig, ModelConfig modelConfig) {
        OtherConfig otherConfig = new OtherConfig();
        generate(databaseConfig, modelConfig, otherConfig);
    }
}
