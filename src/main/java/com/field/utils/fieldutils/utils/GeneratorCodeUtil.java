package com.field.utils.fieldutils.utils;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.fill.Property;

import java.util.Collections;

/**
 * @author Field
 * @date 2023-11-13 20:28
 **/
public class GeneratorCodeUtil {

    public static void generator(String tableName, String lowName) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/ylt_emission?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8",
                        "root", "root")
                .globalConfig(builder -> {
                    builder.author("field") //
                            .outputDir("E:\\service\\src\\main\\java"); // 指定输出目录
//                            .outputDir("E:\\work\\code\\ylt_emission"); // 指定输出目录
                })
                .injectionConfig(builder -> {
                    builder.customMap(Collections.singletonMap("lowName", lowName));
                })
                .packageConfig(builder -> {
                    builder.parent("com")
//                            .moduleName("service")
                            .entity("domain.entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .mapper("mapper")
                            .xml("mapper.xml")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, "E:\\service\\src\\main\\resources\\mapper"))
                            .build();
                })
                .templateConfig(builder -> {
                    builder.disable(TemplateType.ENTITY)
                            .entity("templates/entity.java")
                            .service("templates/service.java")
                            .serviceImpl("templates/serviceImpl.java")
                            .mapper("templates/mapper.java")
                            .xml("templates/mapper.xml")
                            .controller("templates/controller.java")
                            .build();
                })
                .strategyConfig(builder -> {
                    builder.addInclude(tableName)
                            .addTablePrefix("t_", "c_"); // 设置过滤表前缀
                })
                .strategyConfig(builder -> {
                    builder.entityBuilder()
//                            .superClass(BaseEntity.class)
                            .disableSerialVersionUID()
                            .enableFileOverride()
                            .enableChainModel()
                            .enableLombok()
                            .enableRemoveIsPrefix()
                            .enableTableFieldAnnotation()
//                            .enableActiveRecord()
//                            .versionColumnName("version")
                            //.versionPropertyName("version")
                            .logicDeleteColumnName("del_flag")
//                            .logicDeletePropertyName("delFlag")
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel)
                            .addSuperEntityColumns("create_by", "create_time", "update_by", "update_time")
//                            .addIgnoreColumns("age")
                            .addTableFills(new Column("create_time", FieldFill.INSERT))
                            .addTableFills(new Property("updateTime", FieldFill.INSERT_UPDATE))
                            .idType(IdType.AUTO)
                            .formatFileName("%s")
                            .build();
                })
                .strategyConfig(builder -> {
                    builder.controllerBuilder()
//                            .superClass(BaseController.class)
//                            .enableHyphenStyle()
                            .enableRestStyle()
                            .formatFileName("%sController")
                            .build();
                })
                .strategyConfig(builder -> {
                    builder.serviceBuilder()
//                            .superServiceClass(BaseService.class)
//                            .superServiceImplClass(BaseServiceImpl.class)
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")
                            .build();
                })
                .strategyConfig(builder -> {
                    builder.mapperBuilder()
//                            .superClass(BaseMapper.class)
                            .enableMapperAnnotation()
                            .enableBaseResultMap()
//                            .enableBaseColumnList()
//                            .cache(MyMapperCache.class)
                            .formatMapperFileName("%sMapper")
                            .formatXmlFileName("%sMapper")
                            .build();
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }

}
