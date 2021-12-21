package io.jenkins.plugins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.util.Secret;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

public class ExecuteStepBuilderTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();
  private final String filesToExecute = "filesToExecute", fileEncoding = "UTF-8", zipPassword = "zipPassword",
    server = "server", authenticationType = "serverAuthentication", userName = "userName", password = "password", database = "database";
  private final Boolean ignoreError = false;

  @Test
  public void testConfigRoundtrip() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    ExecuteStepBuilder builder = new ExecuteStepBuilder(server, authenticationType, userName, Secret.fromString(password), database, filesToExecute);
    project.getBuildersList().add(builder);
    project = jenkins.configRoundtrip(project);

    ExecuteStepBuilder executeStepBuilder = new ExecuteStepBuilder(server, authenticationType, userName, Secret.fromString(password), database, filesToExecute);
    executeStepBuilder.setFileEncoding("");
    executeStepBuilder.setZipPassword(Secret.fromString(""));
    executeStepBuilder.setIgnoreError(false);
    jenkins.assertEqualDataBoundBeans(executeStepBuilder, project.getBuildersList().get(0));
  }

  @Test
  public void testConfigRoundtripAdvanced() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    ExecuteStepBuilder builder = new ExecuteStepBuilder(server, authenticationType, userName, Secret.fromString(password), database, filesToExecute);
    builder.setFileEncoding(fileEncoding);
    builder.setZipPassword(Secret.fromString(zipPassword));
    builder.setIgnoreError(ignoreError);
    project.getBuildersList().add(builder);
    project = jenkins.configRoundtrip(project);

    ExecuteStepBuilder executeStepBuilder = new ExecuteStepBuilder(server, authenticationType, userName, Secret.fromString(password), database, filesToExecute);
    executeStepBuilder.setFileEncoding(fileEncoding);
    executeStepBuilder.setZipPassword(Secret.fromString(zipPassword));
    executeStepBuilder.setIgnoreError(ignoreError);
    jenkins.assertEqualDataBoundBeans(executeStepBuilder, project.getBuildersList().get(0));
  }

  @Test
  public void testProperties() {

    ExecuteStepBuilder executeStepBuilder = new ExecuteStepBuilder(server, authenticationType, userName, Secret.fromString(password), database, filesToExecute);
    executeStepBuilder.setFileEncoding(fileEncoding);
    executeStepBuilder.setZipPassword(Secret.fromString(zipPassword));
    executeStepBuilder.setIgnoreError(ignoreError);

    assertEquals(executeStepBuilder.getFilesToExecute(), filesToExecute);
    assertEquals(executeStepBuilder.getFileEncoding(), fileEncoding);
    assertEquals(executeStepBuilder.getZipPassword(), Secret.fromString(zipPassword));
    assertEquals(executeStepBuilder.getIgnoreError(), ignoreError);
    assertEquals(executeStepBuilder.getServer(), server);
    assertEquals(executeStepBuilder.getAuthenticationType(), authenticationType);
    assertEquals(executeStepBuilder.getUserName(), userName);
    assertEquals(executeStepBuilder.getPassword(), Secret.fromString(password));
    assertEquals(executeStepBuilder.getDatabase(), database);
  }

  @Test
  public void testPrebuildHasValidParameters() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    ExecuteStepBuilder testExecuteStep = new ExecuteStepBuilder(server, authenticationType, userName, Secret.fromString(password), database, filesToExecute);
    testExecuteStep.setFileEncoding("");
    testExecuteStep.setZipPassword(Secret.fromString(""));
    testExecuteStep.setIgnoreError(false);
    project.getBuildersList().add(testExecuteStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    jenkins.assertLogNotContains("has invalid parameter", build);
  }

  @Test
  public void testAuthenticationTypeEquals() {

    ExecuteStepBuilder testExecuteStep = new ExecuteStepBuilder(server, authenticationType, userName, Secret.fromString(password), database, filesToExecute);

    assertEquals(testExecuteStep.authenticationTypeEquals("windowsAuthentication"), "false");
    assertEquals(testExecuteStep.authenticationTypeEquals("serverAuthentication"), "true");
  }
}

