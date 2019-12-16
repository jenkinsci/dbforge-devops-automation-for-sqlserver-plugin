package io.jenkins.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.util.Secret;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class BuildStepBuilderTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  private final String sourceFolderMode = "subfolder", subfolder = "folder", packageId = "id";
  private final String serverType = "server", authenticationType = "serverAuthentication";
  private final String server = "srv", database = "db", userName = "su", password = "su";
  private final String compareOptions = "compareOptions", filterFile = "filter.scflt";

  @Test
  public void testConfigRoundtrip() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    project.getBuildersList().add(new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
      serverType, server, authenticationType, userName, Secret.fromString(password), database));
    project = jenkins.configRoundtrip(project);

    BuildStepBuilder testBuildStep = new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
      serverType, server, authenticationType, userName, Secret.fromString(password), database);
    testBuildStep.setCompareOptions("");
    testBuildStep.setFilterFile("");
    jenkins.assertEqualDataBoundBeans(testBuildStep, project.getBuildersList().get(0));
  }

  @Test
  public void testConfigRoundtripAdvanced() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    BuildStepBuilder builder = new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
      serverType, server, authenticationType, userName, Secret.fromString(password), database);
    builder.setCompareOptions(compareOptions);
    builder.setFilterFile(filterFile);
    project.getBuildersList().add(builder);
    project = jenkins.configRoundtrip(project);

    BuildStepBuilder testBuildStep = new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
      serverType, server, authenticationType, userName, Secret.fromString(password), database);
    testBuildStep.setCompareOptions(compareOptions);
    testBuildStep.setFilterFile(filterFile);
    jenkins.assertEqualDataBoundBeans(testBuildStep, project.getBuildersList().get(0));
  }

  @Test
  public void testProperties() {

    BuildStepBuilder testBuildStep = new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
      serverType, server, authenticationType, userName, Secret.fromString(password), database);
    testBuildStep.setCompareOptions(compareOptions);
    testBuildStep.setFilterFile(filterFile);

    assertEquals(testBuildStep.getSourceFolderMode(), sourceFolderMode);
    assertEquals(testBuildStep.getSubfolder(), subfolder);
    assertEquals(testBuildStep.getPackageId(), packageId);
    assertEquals(testBuildStep.getServerType(), serverType);
    assertEquals(testBuildStep.getServer(), server);
    assertEquals(testBuildStep.getAuthenticationType(), authenticationType);
    assertEquals(testBuildStep.getUserName(), userName);
    assertEquals(testBuildStep.getPassword(), Secret.fromString(password));
    assertEquals(testBuildStep.getDatabase(), database);
    assertEquals(testBuildStep.getCompareOptions(), compareOptions);
    assertEquals(testBuildStep.getFilterFile(), filterFile);
  }

  @Test
  public void testPrebuildHasValidParameter1() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    BuildStepBuilder testBuildStep = new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
            serverType, server, authenticationType, userName, Secret.fromString(password), database);
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    jenkins.assertLogNotContains("has invalid parameter", build);
  }

  @Test
  public void testPrebuildHasValidParameter2() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    BuildStepBuilder testBuildStep = new BuildStepBuilder("vcsroot", "D:/D:/", packageId,
            serverType, server, authenticationType, userName, Secret.fromString(password), database);
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    jenkins.assertLogNotContains("has invalid parameter", build);
  }

  @Test
  public void testPrebuildHasValidParameter3() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    BuildStepBuilder testBuildStep = new BuildStepBuilder("vcsroot", subfolder, packageId,
            serverType, server, authenticationType, userName, Secret.fromString(password), database);
    testBuildStep.setFilterFile(filterFile);
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    jenkins.assertLogNotContains("has invalid parameter", build);
  }

  @Test
  public void testPrebuildHasInvalidParameter1() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    BuildStepBuilder testBuildStep = new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
            serverType, server, authenticationType, userName, Secret.fromString(password), database);
    testBuildStep.setFilterFile("qwerty");
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    assertTrue(build.getResult() == Result.FAILURE);
    jenkins.assertLogNotContains(String.format("Started '%s'", testBuildStep.getDescriptor().getDisplayName()), build);
    jenkins.assertLogContains("has invalid parameter", build);
  }

  @Test
  public void testPrebuildHasInvalidParameter2() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    BuildStepBuilder testBuildStep = new BuildStepBuilder(sourceFolderMode, "D:/D:/", packageId,
            serverType, server, authenticationType, userName, Secret.fromString(password), database);
    project.getBuildersList().add(testBuildStep);

    FreeStyleBuild build = project.scheduleBuild2(0).get();
    assertTrue(build.getResult() == Result.FAILURE);
    jenkins.assertLogNotContains(String.format("Started '%s'", testBuildStep.getDescriptor().getDisplayName()), build);
    jenkins.assertLogContains("has invalid parameter", build);
  }

  @Test
  public void testServerTypeEquals() {

    BuildStepBuilder testBuildStep = new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
      serverType, server, authenticationType, userName, Secret.fromString(password), database);

    assertEquals(testBuildStep.serverTypeEquals("localDb"), "false");
    assertEquals(testBuildStep.serverTypeEquals("server"), "true");
  }

  @Test
  public void testAuthenticationTypeEquals() {

    BuildStepBuilder testBuildStep = new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
      serverType, server, authenticationType, userName, Secret.fromString(password), database);

    assertEquals(testBuildStep.authenticationTypeEquals("windowsAuthentication"), "false");
    assertEquals(testBuildStep.authenticationTypeEquals("serverAuthentication"), "true");
  }

  @Test
  public void testSourceFolderModeEquals() {

    BuildStepBuilder testBuildStep = new BuildStepBuilder(sourceFolderMode, subfolder, packageId,
      serverType, server, authenticationType, userName, Secret.fromString(password), database);

    assertEquals(testBuildStep.sourceFolderModeEquals("vcsroot"), "false");
    assertEquals(testBuildStep.sourceFolderModeEquals("subfolder"), "true");
  }
}