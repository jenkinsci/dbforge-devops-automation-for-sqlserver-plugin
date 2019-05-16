package io.jenkins.plugins;

import io.jenkins.plugins.Models.*;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class ModelsTest {

  private final String packageId = "id", sourceControlFolder = "sourceControlFolder", compareOptions = "compareOptions",
    server = "srv", database = "db", userName = "su", password = "su",
    test = "test1", testResults = "testResults", dgen = "dgen", transactionIsoLvl = "transactionIsoLvl";

  @Test
  public void testPackageProject() {

    PackageProject packageProject = new PackageProject(packageId);
    packageProject.setSourceFolder(sourceControlFolder);
    ConnectionInfo connectionInfo = new ConnectionInfo(true, server, database, false, userName, password);

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

    ConnectionInfo connectionInfo = new ConnectionInfo(false, server, database, true, userName, password);
    assertEquals(connectionInfo.getIsLocalDb(), false);
    assertEquals(connectionInfo.getServer(), server);
    assertEquals(connectionInfo.getDatabase(), database);
    assertEquals(connectionInfo.getIsWindowsAuthentication(), true);
    assertEquals(connectionInfo.getUserName(), userName);
    assertEquals(connectionInfo.getPassword(), password);
  }

  @Test
  public void testConnectionInfoLocalDb() {

    ConnectionInfo connectionInfo = new ConnectionInfo(true, server, database, true, userName, password);
    assertEquals(connectionInfo.getIsLocalDb(), true);
    assertEquals(connectionInfo.getServer(), String.format("(LocalDb)\\%s", ConnectionInfo.LocalDbInstance));
    assertThat(connectionInfo.getDatabase(), containsString("dbForgeDevopsTempDb"));
    assertEquals(connectionInfo.getIsWindowsAuthentication(), true);
    assertNull(connectionInfo.getUserName());
    assertNull(connectionInfo.getPassword());
  }

  @Test
  public void testRunTestInfo(){

    RunTestInfo runTestInfo = new RunTestInfo(true, test, testResults,true, dgen, compareOptions);
    assertEquals(runTestInfo.getRunEveryTests(), true);
    assertEquals(runTestInfo.getRunTests(), test);
    assertEquals(runTestInfo.getGenerateTestData(), true);
    assertEquals(runTestInfo.getDgenFile(), dgen);
    assertEquals(runTestInfo.getCompareOptions(), compareOptions);
  }

  @Test
  public void testSyncDatabaseInfo(){

    SyncDatabaseInfo syncDatabaseInfo = new SyncDatabaseInfo(compareOptions, transactionIsoLvl);
    assertEquals(syncDatabaseInfo.getCompareOptions(), compareOptions);
    assertEquals(syncDatabaseInfo.getTransactionIsoLvl(), transactionIsoLvl);
  }
}
