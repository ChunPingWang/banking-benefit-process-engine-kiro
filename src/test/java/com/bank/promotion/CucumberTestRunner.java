package com.bank.promotion;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber 測試執行器
 * 配置 BDD 測試場景執行環境
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.bank.promotion.bdd")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber-reports")
@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")
public class CucumberTestRunner {
}