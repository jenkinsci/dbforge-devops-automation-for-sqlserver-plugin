package io.jenkins.plugins.Models;

public class RunTestInfo {

  private final String outputReport;
  private String runTests, dgenFile;
  private boolean runEveryTests, generateTestData, isMsTestFormat = false, installFramework = true;

  public RunTestInfo(boolean runEveryTests, String runTests, String outputReport, boolean generateTestData, String dgenFile){

    this.runEveryTests = runEveryTests;
    this.runTests = runTests;
    this.generateTestData=  generateTestData;
    this.dgenFile = dgenFile;
    this.outputReport = outputReport;
  }

  public boolean getInstallFramework() {

    return installFramework;
  }

  public boolean getRunEveryTests() {

    return runEveryTests;
  }

  public String getRunTests() {

    return runTests;
  }

  public String getDgenFile() {

    return dgenFile;
  }

  public boolean getGenerateTestData() {

    return generateTestData;
  }

  public String getOutputReport() {

    return outputReport;
  }

  public String getOutputReportFormat() {

    return isMsTestFormat ? "MsTest" : "JUnit";
  }
}
