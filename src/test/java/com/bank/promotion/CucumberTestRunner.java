package com.bank.promotion;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber 測試執行器
 * 配置 BDD 測試場景執行環境和報告生成
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/simple-test.feature")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.bank.promotion.bdd")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = 
    "pretty," +
    "html:build/reports/cucumber-html," +
    "json:build/reports/cucumber-json/cucumber.json," +
    "junit:build/reports/cucumber-junit/junit.xml")
@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")
@ConfigurationParameter(key = Constants.JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, value = "long")
@ConfigurationParameter(key = Constants.OBJECT_FACTORY_PROPERTY_NAME, value = "io.cucumber.spring.SpringFactory")
public class CucumberTestRunner {
}