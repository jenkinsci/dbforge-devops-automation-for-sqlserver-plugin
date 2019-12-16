package io.jenkins.plugins;

import io.jenkins.plugins.Models.*;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import hudson.util.Secret;
import org.jvnet.hudson.test.JenkinsRule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class ModelsTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  private final String packageId = "id", sourceControlFolder = "sourceControlFolder",
    server = "srv", database = "db", userName = "su", password = "su",
    test = "test1", testResults = "testResults", dgen = "dgen",
    compareOptions = "compareOptions", filterFile  = "filter.scflt", transactionIsoLvl = "transactionIsoLvl";

  @Test
  public void testPackageProject() {

    PackageProject packageProject = new PackageProject(packageId);
    packageProject.setSourceFolder(sourceControlFolder);

    assertEquals(packageProject.getId(), packageId);
    assertEquals(packageProject.getSourceFolder(), sourceControlFolder);
  }

  @Test
  public void testProjectRepository() {

    PackageProject packageProject = new PackageProject(packageId);
    packageProject.setSourceFolder(sourceControlFolder);

    Assert.assertEquals(ProjectRepository.getInstance().addPackageProject(packageProject), true);
    assertEquals(ProjectRepository.getInstance().getPackageProject(packageId), packageProject);
    assertNull(ProjectRepository.getInstance().getPackageProject("id2"));
    assertEquals(ProjectRepository.getInstance().addPackageProject(packageProject), false);
  }

  @Test
  public void testConnectionInfo() {

    ConnectionInfo connectionInfo = new ConnectionInfo(false, server, database, true, userName, Secret.fromString(password));
    assertEquals(connectionInfo.getIsLocalDb(), false);
    assertEquals(connectionInfo.getServer(), server);
    assertEquals(connectionInfo.getDatabase(), database);
    assertEquals(connectionInfo.getIsWindowsAuthentication(), true);
    assertEquals(connectionInfo.getUserName(), userName);
    assertEquals(connectionInfo.getPassword(), Secret.fromString(password));
  }

  @Test
  public void testConnectionInfoLocalDb() {

    ConnectionInfo connectionInfo = new ConnectionInfo(true, server, database, true, userName, Secret.fromString(password));
    assertEquals(connectionInfo.getIsLocalDb(), true);
    assertEquals(connectionInfo.getServer(), String.format("(LocalDb)\\%s", ConnectionInfo.localDbInstance));
    assertThat(connectionInfo.getDatabase(), containsString("dbForgeDevopsTempDb"));
    assertEquals(connectionInfo.getIsWindowsAuthentication(), true);
    assertNull(connectionInfo.getUserName());
    assertNull(connectionInfo.getPassword());
  }

  @Test
  public void testRunTestInfo(){

    RunTestInfo runTestInfo = new RunTestInfo(true, test, testResults,true, dgen);
    assertEquals(runTestInfo.getRunEveryTests(), true);
    assertEquals(runTestInfo.getRunTests(), test);
    assertEquals(runTestInfo.getGenerateTestData(), true);
    assertEquals(runTestInfo.getDgenFile(), dgen);
  }

  @Test
  public void testAdditionalOptionsModel(){

    AdditionalOptionsModel additionalOptionsModel = new AdditionalOptionsModel(compareOptions, filterFile, transactionIsoLvl);
    assertEquals(additionalOptionsModel.getCompareOptions(), compareOptions);
    assertEquals(additionalOptionsModel.getFilterFile(), filterFile);
    assertEquals(additionalOptionsModel.getTransactionIsoLvl(), transactionIsoLvl);
  }
}
