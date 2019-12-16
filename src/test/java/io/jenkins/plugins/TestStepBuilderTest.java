package io.jenkins.plugins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.util.Secret;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestStepBuilderTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  private final String packageId = "id", runTestMode = "runOnly", runTests = "testName";
  private final String serverType = "server", authenticationType = "serverAuthentication";
  private final String server = "srv", database = "db", userName = "su", password = "su";
  private final String dgenFile = "dgenFile.dgen", compareOptions = "compareOptions", filterFile = "filter.scflt";
  private final boolean generateTestData = true;

  @Test
  public void testConfigRoundtrip() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    project.getBuildersList().add(new TestStepBuilder(packageId, serverType, server, database,
      authenticationType, userName, Secret.fromString(password), runTestMode, runTests));
    project = jenkins.configRoundtrip(project);

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
      authenticationType, userName, Secret.fromString(password), runTestMode, runTests);
    testBuildStep.setCompareOptions("");
    testBuildStep.setFilterFile("");
    testBuildStep.setDgenFile("");
    jenkins.assertEqualDataBoundBeans(testBuildStep, project.getBuildersList().get(0));
  }

  @Test
  public void testConfigRoundtripAdvanced() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    TestStepBuilder builder = new TestStepBuilder(packageId, serverType, server, database,
      authenticationType, userName, Secret.fromString(password), runTestMode, runTests);
    builder.setGenerateTestData(generateTestData);
    builder.setDgenFile(dgenFile);
    builder.setCompareOptions(compareOptions);
    builder.setFilterFile(filterFile);
    project.getBuildersList().add(builder);
    project = jenkins.configRoundtrip(project);

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
      authenticationType, userName, Secret.fromString(password), runTestMode, runTests);
    testBuildStep.setGenerateTestData(generateTestData);
    testBuildStep.setDgenFile(dgenFile);
    testBuildStep.setCompareOptions(compareOptions);
    testBuildStep.setFilterFile(filterFile);
    jenkins.assertEqualDataBoundBeans(testBuildStep, project.getBuildersList().get(0));
  }

  @Test
  public void testProperties() {

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
      authenticationType, userName, Secret.fromString(password), runTestMode, runTests);
    testBuildStep.setGenerateTestData(generateTestData);
    testBuildStep.setDgenFile(dgenFile);
    testBuildStep.setCompareOptions(compareOptions);
    testBuildStep.setFilterFile(filterFile);

    assertEquals(testBuildStep.getPackageId(), packageId);
    assertEquals(testBuildStep.getServerType(), serverType);
    assertEquals(testBuildStep.getServer(), server);
    assertEquals(testBuildStep.getAuthenticationType(), authenticationType);
    assertEquals(testBuildStep.getUserName(), userName);
    assertEquals(testBuildStep.getPassword(), Secret.fromString(password));
    assertEquals(testBuildStep.getDatabase(), database);
    assertEquals(testBuildStep.getRunTestMode(), runTestMode);
    assertEquals(testBuildStep.getRunTests(), runTests);
    assertEquals(testBuildStep.getGenerateTestData(), generateTestData);
    assertEquals(testBuildStep.getDgenFile(), dgenFile);
    assertEquals(testBuildStep.getCompareOptions(), compareOptions);
    assertEquals(testBuildStep.getFilterFile(), filterFile);
  }

  @Test
  public void testPrebuildSuccess1() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
            authenticationType, userName, Secret.fromString(password), runTestMode, runTests);
    testBuildStep.setGenerateTestData(true);
    testBuildStep.setDgenFile(dgenFile);
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    jenkins.assertLogContains(String.format("Started '%s'", testBuildStep.getDescriptor().getDisplayName()), build);
  }

  @Test
  public void testPrebuildSuccess2() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
            authenticationType, userName, Secret.fromString(password), runTestMode, runTests);
    testBuildStep.setGenerateTestData(false);
    testBuildStep.setDgenFile("D:/D:/");
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    jenkins.assertLogContains(String.format("Started '%s'", testBuildStep.getDescriptor().getDisplayName()), build);
  }

  @Test
  public void testPrebuildSuccess3() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
            authenticationType, userName, Secret.fromString(password), runTestMode, runTests);
    testBuildStep.setFilterFile(filterFile);
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    jenkins.assertLogNotContains("has invalid parameter", build);
    jenkins.assertLogContains(String.format("Started '%s'", testBuildStep.getDescriptor().getDisplayName()), build);
  }

  @Test
  public void testPrebuildHasInvalidParameter1() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
            authenticationType, userName, Secret.fromString(password), runTestMode, runTests);
    testBuildStep.setGenerateTestData(true);
    testBuildStep.setDgenFile("D:/D:/");
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    assertTrue(build.getResult() == Result.FAILURE);
    jenkins.assertLogNotContains(String.format("Started '%s'", testBuildStep.getDescriptor().getDisplayName()), build);
    jenkins.assertLogContains("has invalid parameter", build);
  }

  @Test
  public void testPrebuildHasInvalidParameter2() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
            authenticationType, userName, Secret.fromString(password), runTestMode, runTests);
    testBuildStep.setFilterFile("qwerty");
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    assertTrue(build.getResult() == Result.FAILURE);
    jenkins.assertLogNotContains(String.format("Started '%s'", testBuildStep.getDescriptor().getDisplayName()), build);
    jenkins.assertLogContains("has invalid parameter", build);
  }

  @Test
  public void testServerTypeEquals() {

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
      authenticationType, userName, Secret.fromString(password), runTestMode, runTests);

    assertEquals(testBuildStep.serverTypeEquals("localDb"), "false");
    assertEquals(testBuildStep.serverTypeEquals("server"), "true");
  }

  @Test
  public void testAuthenticationTypeEquals() {

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
      authenticationType, userName, Secret.fromString(password), runTestMode, runTests);

    assertEquals(testBuildStep.authenticationTypeEquals("windowsAuthentication"), "false");
    assertEquals(testBuildStep.authenticationTypeEquals("serverAuthentication"), "true");
  }

  @Test
  public void testRunTestModeEquals() {

    TestStepBuilder testBuildStep = new TestStepBuilder(packageId, serverType, server, database,
      authenticationType, userName, Secret.fromString(password), runTestMode, runTests);

    assertEquals(testBuildStep.runTestModeEquals("runAll"), "false");
    assertEquals(testBuildStep.runTestModeEquals("runOnly"), "true");
  }
}