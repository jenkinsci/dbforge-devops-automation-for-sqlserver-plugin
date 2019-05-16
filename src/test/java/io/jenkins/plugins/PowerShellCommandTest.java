package io.jenkins.plugins;

import hudson.FilePath;
import io.jenkins.plugins.Models.*;
import io.jenkins.plugins.Presenters.PowerShellCommand;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertTrue;

public class PowerShellCommandTest {

  private final String packageId = "id", packageVersion = "1.0.1", nugetRepository = "repository", nugetApi = "api",
    connectionName = "connectionName", server = "srv", database = "db", userName = "su", password = "su",
    test = "test1",testResults = "testResults",  dgen = "dgen",
    sourceControlFolder = "sourceControlFolder", compareOptions = "compareOptions", transactionIsoLvl = "Serializable";

  @Test
  public void testAddConnectionScript() {

    // $%s = New-DevartSqlDatabaseConnection -Server %s
    PowerShellCommand command = new PowerShellCommand();
    ConnectionInfo connectionInfo = new ConnectionInfo(true, server, database, false, userName, password);
    command.AddConnectionScript(connectionInfo);
    assertThat(command.toString(), containsString(String.format("$%s = New-DevartSqlDatabaseConnection", connectionInfo.getConnectionName())));
    assertThat(command.toString(), containsString(String.format("-Server \"(LocalDb)\\%s\"", ConnectionInfo.LocalDbInstance)));
    assertThat(command.toString(), containsString("-Database"));
    assertThat(command.toString(), containsString("-WindowsAuthentication"));
    assertThat(command.toString(), not(containsString("-UserName")));
    assertThat(command.toString(), not(containsString("-Password")));
    assertThat(command.toString(), containsString("-WindowsAuthentication"));

    // $%s = New-DevartSqlDatabaseConnection -Server %s -Database %s -WindowsAuthentication true
    command = new PowerShellCommand();
    command.AddConnectionScript(new ConnectionInfo(false, server, database, true, userName, password));
    assertThat(command.toString(), containsString(String.format("$%s = New-DevartSqlDatabaseConnection", connectionInfo.getConnectionName())));
    assertThat(command.toString(), containsString(String.format("-Server \"%s\"", server)));
    assertThat(command.toString(), containsString(String.format("-Database \"%s\"", database)));
    assertThat(command.toString(), containsString("-WindowsAuthentication"));
    assertThat(command.toString(), not(containsString("-UserName")));
    assertThat(command.toString(), not(containsString("-Password")));

    // $%s = New-DevartSqlDatabaseConnection -Server %s -Database %s -UserName %s
    command = new PowerShellCommand();
    command.AddConnectionScript(new ConnectionInfo(false, server, database, false, userName, ""));
    assertThat(command.toString(), containsString(String.format("$%s = New-DevartSqlDatabaseConnection", connectionInfo.getConnectionName())));
    assertThat(command.toString(), containsString(String.format("-Server \"%s\"", server)));
    assertThat(command.toString(), containsString(String.format("-Database \"%s\"", database)));
    assertThat(command.toString(), not(containsString("-WindowsAuthentication")));
    assertThat(command.toString(), containsString(String.format("-UserName", userName)));
    assertThat(command.toString(), not(containsString("-Password")));

    // $%s = New-DevartSqlDatabaseConnection -Server %s -Database %s -UserName %s -Password %s
    command = new PowerShellCommand();
    command.AddConnectionScript(new ConnectionInfo(false, server, database, false, userName, password));
    assertThat(command.toString(), containsString(String.format("$%s = New-DevartSqlDatabaseConnection", connectionInfo.getConnectionName())));
    assertThat(command.toString(), containsString(String.format("-Server \"%s\"", server)));
    assertThat(command.toString(), containsString(String.format("-Database \"%s\"", database)));
    assertThat(command.toString(), not(containsString("-WindowsAuthentication")));
    assertThat(command.toString(), containsString(String.format("-UserName", userName)));
    assertThat(command.toString(), containsString(String.format("-Password", password)));
  }

  @Test
  public void testAddDatabaseBuildScript() {

    PowerShellCommand command = new PowerShellCommand();
    PackageProject project = new PackageProject(packageId);
    project.setSourceFolder(sourceControlFolder);
    ConnectionInfo connectionInfo = new ConnectionInfo(true, server, database, false, userName, password);

    // $%s = Invoke-DevartDatabaseBuild -SourceScriptsFolder %s -Connection $%s
    command.AddDatabaseBuildScript(project, connectionInfo.getConnectionName(), "");
    assertThat(command.toString(), containsString(String.format("$%s = Invoke-DevartDatabaseBuild", project.getDatabaseProjectName())));
    assertThat(command.toString(), containsString(String.format("-SourceScriptsFolder \"%s\"", project.getSourceFolder())));
    assertThat(command.toString(), containsString(String.format("-Connection $%s", connectionInfo.getConnectionName())));
    assertThat(command.toString(), not(containsString("-CompareOptions")));
    assertThat(command.toString(), containsString(String.format("if(-Not $%s.Valid) { [System.Environment]::Exit(1); }", project.getDatabaseProjectName())));

    // $%s = Invoke-DevartDatabaseBuild -SourceScriptsFolder %s -Connection $%s -CompareOptions %s
    command = new PowerShellCommand();
    command.AddDatabaseBuildScript(project, connectionInfo.getConnectionName(), compareOptions);
    assertThat(command.toString(), containsString(String.format("$%s = Invoke-DevartDatabaseBuild", project.getDatabaseProjectName())));
    assertThat(command.toString(), containsString(String.format("-SourceScriptsFolder \"%s\"", project.getSourceFolder())));
    assertThat(command.toString(), containsString(String.format("-Connection $%s", connectionInfo.getConnectionName())));
    assertThat(command.toString(), containsString(String.format("-SynchronizationOptions \"%s\"", compareOptions)));
    assertThat(command.toString(), containsString(String.format("if(-Not $%s.Valid) { [System.Environment]::Exit(1); }", project.getDatabaseProjectName())));
  }

  @Test
  public void testAddTestBuildScript() {

    PowerShellCommand command = new PowerShellCommand();
    RunTestInfo runTestInfo = new RunTestInfo(true, "", testResults,false, "", "");

    // Invoke-DevartDatabaseTests -InputObject %s -TemporaryDatabaseServer $%s -OutReportFileName:\"%s\" -ReportFormat %s
    command.AddTestBuildScript(sourceControlFolder, connectionName, runTestInfo);
    assertThat(command.toString(), containsString("$result = Invoke-DevartDatabaseTests"));
    assertThat(command.toString(), containsString(String.format("-InputObject \"%s\"", sourceControlFolder)));
    assertThat(command.toString(), containsString(String.format("-TemporaryDatabaseServer $%s", connectionName)));
    assertThat(command.toString(), containsString(String.format("-OutReportFileName:\"%s\"", testResults)));
    assertThat(command.toString(), containsString("-ReportFormat"));
    assertThat(command.toString(), not(containsString("-UnitTests")));
    assertThat(command.toString(), not(containsString("-IncludeTestData")));
    assertThat(command.toString(), not(containsString("-DataGeneratorProject")));
    assertThat(command.toString(), not(containsString("-SynchronizationOptions")));
    assertThat(command.toString(), containsString("if(-Not $result) { [System.Environment]::Exit(1); }"));

    // Invoke-DevartDatabaseTests -InputObject %s -TemporaryDatabaseServer $%s -OutReportFileName:\"%s\" -ReportFormat %s -UnitTests %s
    command = new PowerShellCommand();
    runTestInfo = new RunTestInfo(false, test, testResults,false, "", "");
    command.AddTestBuildScript(sourceControlFolder, connectionName, runTestInfo);
    assertThat(command.toString(), containsString("$result = Invoke-DevartDatabaseTests"));
    assertThat(command.toString(), containsString(String.format("-InputObject \"%s\"", sourceControlFolder)));
    assertThat(command.toString(), containsString(String.format("-TemporaryDatabaseServer $%s", connectionName)));
    assertThat(command.toString(), containsString(String.format("-OutReportFileName:\"%s\"", testResults)));
    assertThat(command.toString(), containsString("-ReportFormat"));
    assertThat(command.toString(), containsString(String.format("-UnitTests \"%s\"", test)));
    assertThat(command.toString(), not(containsString("-IncludeTestData")));
    assertThat(command.toString(), not(containsString("-DataGeneratorProject")));
    assertThat(command.toString(), not(containsString("-SynchronizationOptions")));
    assertThat(command.toString(), containsString("if(-Not $result) { [System.Environment]::Exit(1); }"));

    // Invoke-DevartDatabaseTests -InputObject %s -TemporaryDatabaseServer $%s -OutReportFileName:\"%s\" -IncludeTestData $true -DataGeneratorProject \"%s\"
    command = new PowerShellCommand();
    runTestInfo = new RunTestInfo(true, "", testResults,true, dgen, "");
    command.AddTestBuildScript(sourceControlFolder, connectionName, runTestInfo);
    assertThat(command.toString(), containsString("$result = Invoke-DevartDatabaseTests"));
    assertThat(command.toString(), containsString(String.format("-InputObject \"%s\"", sourceControlFolder)));
    assertThat(command.toString(), containsString(String.format("-TemporaryDatabaseServer $%s", connectionName)));
    assertThat(command.toString(), containsString(String.format("-OutReportFileName:\"%s\"", testResults)));
    assertThat(command.toString(), containsString("-ReportFormat"));
    assertThat(command.toString(), not(containsString("-UnitTests")));
    assertThat(command.toString(), containsString("-IncludeTestData"));
    assertThat(command.toString(), containsString(String.format("-DataGeneratorProject \"%s\"", dgen)));
    assertThat(command.toString(), not(containsString("-SynchronizationOptions")));
    assertThat(command.toString(), containsString("if(-Not $result) { [System.Environment]::Exit(1); }"));

    // Invoke-DevartDatabaseTests -InputObject %s -TemporaryDatabaseServer $%s -OutReportFileName:\"%s\" -ReportFormat %s -SynchronizationOptions %s
    command = new PowerShellCommand();
    runTestInfo = new RunTestInfo(true, "", testResults,false, "", compareOptions);
    command.AddTestBuildScript(sourceControlFolder, connectionName, runTestInfo);
    assertThat(command.toString(), containsString("$result = Invoke-DevartDatabaseTests"));
    assertThat(command.toString(), containsString(String.format("-InputObject \"%s\"", sourceControlFolder)));
    assertThat(command.toString(), containsString(String.format("-TemporaryDatabaseServer $%s", connectionName)));
    assertThat(command.toString(), containsString(String.format("-OutReportFileName:\"%s\"", testResults)));
    assertThat(command.toString(), containsString("-ReportFormat"));
    assertThat(command.toString(), not(containsString("-UnitTests")));
    assertThat(command.toString(), not(containsString("-IncludeTestData")));
    assertThat(command.toString(), not(containsString("-DataGeneratorProject")));
    assertThat(command.toString(), containsString(String.format("-SynchronizationOptions \"%s\"", compareOptions)));
    assertThat(command.toString(), containsString("if(-Not $result) { [System.Environment]::Exit(1); }"));
  }

  @Test
  public void testAddSyncBuildScript() {

    PowerShellCommand command = new PowerShellCommand();
    SyncDatabaseInfo syncDatabaseInfo = new SyncDatabaseInfo("", "");

    // Invoke-DevartSyncDatabaseSchema -Source %s -Target $%s
    command.AddSyncDatabaseScript(sourceControlFolder, connectionName, syncDatabaseInfo);
    assertThat(command.toString(), containsString("$result = Invoke-DevartSyncDatabaseSchema"));
    assertThat(command.toString(), containsString(String.format("-Source \"%s\"", sourceControlFolder)));
    assertThat(command.toString(), containsString(String.format("-Target $%s", connectionName)));
    assertThat(command.toString(), not(containsString("-SynchronizationOptions")));
    assertThat(command.toString(), not(containsString("-TransactionIsolationLevel")));
    assertThat(command.toString(), containsString("if(-Not $result) { [System.Environment]::Exit(1); }"));

    // Invoke-DevartSyncDatabaseSchema -Source %s -Target $%s -SynchronizationOptions %s
    command = new PowerShellCommand();
    syncDatabaseInfo = new SyncDatabaseInfo(compareOptions, "");
    command.AddSyncDatabaseScript(sourceControlFolder, connectionName, syncDatabaseInfo);
    assertThat(command.toString(), containsString("$result = Invoke-DevartSyncDatabaseSchema"));
    assertThat(command.toString(), containsString(String.format("-Source \"%s\"", sourceControlFolder)));
    assertThat(command.toString(), containsString(String.format("-Target $%s", connectionName)));
    assertThat(command.toString(), containsString(String.format("-SynchronizationOptions \"%s\"", compareOptions)));
    assertThat(command.toString(), not(containsString("-TransactionIsolationLevel")));
    assertThat(command.toString(), containsString("if(-Not $result) { [System.Environment]::Exit(1); }"));

    // Invoke-DevartSyncDatabaseSchema -Source %s -Target $%s -TransactionIsolationLevel %s
    command = new PowerShellCommand();
    syncDatabaseInfo = new SyncDatabaseInfo("", transactionIsoLvl);
    command.AddSyncDatabaseScript(sourceControlFolder, connectionName, syncDatabaseInfo);
    assertThat(command.toString(), containsString("$result = Invoke-DevartSyncDatabaseSchema"));
    assertThat(command.toString(), containsString(String.format("-Source \"%s\"", sourceControlFolder)));
    assertThat(command.toString(), containsString(String.format("-Target $%s", connectionName)));
    assertThat(command.toString(), not(containsString("-SynchronizationOptions")));
    assertThat(command.toString(), containsString(String.format("-TransactionIsolationLevel %s", transactionIsoLvl)));
    assertThat(command.toString(), containsString("if(-Not $result) { [System.Environment]::Exit(1); }"));
  }

  @Test
  public void testAddNewDatabaseProjectScript() {

    PowerShellCommand command = new PowerShellCommand();
    PackageProject project = new PackageProject(packageId);
    project.setSourceFolder(sourceControlFolder);

    // $%s = New-DevartDatabaseProject -Path %s
    command.AddNewDatabaseProject(project);
    assertThat(command.toString(), containsString(String.format("$%s", project.getDatabaseProjectName())));
    assertThat(command.toString(), containsString("New-DevartDatabaseProject"));
    assertThat(command.toString(), containsString(String.format("-SourceScriptsFolder \"%s\"", project.getSourceFolder())));
  }

  @Test
  public void testAddPackageInfoScript() {

    PowerShellCommand command = new PowerShellCommand();
    PackageProject project = new PackageProject(packageId);

    // Set-DevartPackageInfo -Project %s -Id %s
    command.AddPackageInfo(project.getDatabaseProjectName(), project.getId(), "");
    assertThat(command.toString(), containsString("Set-DevartPackageInfo"));
    assertThat(command.toString(), containsString(String.format("-Project $%s", project.getDatabaseProjectName())));
    assertThat(command.toString(), containsString(String.format("-Id %s", project.getId())));
    assertThat(command.toString(), not(containsString("-Version")));

    // Set-DevartPackageInfo -Project %s -Id %s -Version %s
    command = new PowerShellCommand();
    command.AddPackageInfo(project.getDatabaseProjectName(), project.getId(), packageVersion);
    assertThat(command.toString(), containsString("Set-DevartPackageInfo"));
    assertThat(command.toString(), containsString(String.format("-Project $%s", project.getDatabaseProjectName())));
    assertThat(command.toString(), containsString(String.format("-Id %s", project.getId())));
    assertThat(command.toString(), containsString(String.format("-Version %s", packageVersion)));
  }

  @Test
  public void testAddPublishDatabaseProjectScript() {

    PowerShellCommand command = new PowerShellCommand();
    PackageProject project = new PackageProject(packageId);

    // Publish-DevartDatabaseProject -Project %s -Repository %s
    command.AddPublishDatabaseProject(project.getDatabaseProjectName(), packageVersion, nugetRepository, "");
    assertThat(command.toString(), containsString("Publish-DevartDatabaseProject"));
    assertThat(command.toString(), containsString(String.format("-Project $%s", project.getDatabaseProjectName())));
    assertThat(command.toString(), containsString(String.format("-Repository %s", nugetRepository)));
    assertThat(command.toString(), not(containsString("-ApiKey")));
    assertThat(command.toString(), not(containsString("-AutoIncrementVersion")));

    // Publish-DevartDatabaseProject -Project %s -Repository %s -AutoIncrementVersion true
    command.AddPublishDatabaseProject(project.getDatabaseProjectName(), "", nugetRepository, "");
    assertThat(command.toString(), containsString("Publish-DevartDatabaseProject"));
    assertThat(command.toString(), containsString(String.format("-Project $%s", project.getDatabaseProjectName())));
    assertThat(command.toString(), containsString(String.format("-Repository %s", nugetRepository)));
    assertThat(command.toString(), not(containsString("-ApiKey")));
    assertThat(command.toString(), containsString("-AutoIncrementVersion"));

    // Publish-DevartDatabaseProject -Project %s -Repository %s -ApiKey %s -AutoIncrementVersion true
    command.AddPublishDatabaseProject(project.getDatabaseProjectName(), "", nugetRepository, nugetApi);
    assertThat(command.toString(), containsString("Publish-DevartDatabaseProject"));
    assertThat(command.toString(), containsString(String.format("-Project $%s", project.getDatabaseProjectName())));
    assertThat(command.toString(), containsString(String.format("-Repository %s", nugetRepository)));
    assertThat(command.toString(), containsString(String.format("-ApiKey %s", nugetApi)));
    assertThat(command.toString(), containsString("-AutoIncrementVersion"));
  }

  @Test
  public void testAddExecuteScript() {

    PowerShellCommand command = new PowerShellCommand();

    // $execute_filename = Invoke-DevartExecuteScript -Connection "Data Source=(LocalDb)\MSSQLLocalDB;Integrated Security=True" -InputFile filename.sql
    command.AddExecuteScript(true, new FilePath(new File("filename.sql")));
    assertThat(command.toString(), containsString("$execute_filename = Invoke-DevartExecuteScript"));
    assertThat(command.toString(), containsString(String.format("-Connection \"Data Source=(LocalDb)\\%s;Integrated Security=True\"", ConnectionInfo.LocalDbInstance)));
    assertThat(command.toString(), containsString("-Input \"filename.sql\""));
    assertThat(command.toString(), containsString("if(-Not $execute_filename) { [System.Environment]::Exit(1); };"));
  }

  @Test
  public void testMultiScripts() {

    PowerShellCommand command = new PowerShellCommand();
    PackageProject project = new PackageProject(packageId);
    project.setSourceFolder(sourceControlFolder);
    ConnectionInfo connection = new ConnectionInfo(false, server, database, false, userName, password);
    RunTestInfo runTestInfo = new RunTestInfo(true, "", testResults,false, "", "");
    SyncDatabaseInfo syncDatabaseInfo = new SyncDatabaseInfo("", "");

    command.AddConnectionScript(connection);
    command.AddDatabaseBuildScript(project, connection.getConnectionName(), "");
    command.AddTestBuildScript(sourceControlFolder, connectionName, runTestInfo);
    command.AddSyncDatabaseScript(sourceControlFolder, connectionName, syncDatabaseInfo);
    command.AddPackageInfo(project.getDatabaseProjectName(), project.getId(), "");
    command.AddPublishDatabaseProject(project.getDatabaseProjectName(), packageVersion, nugetRepository, "");

    String[] scripts = command.toString().split(command.Separator);
    assertTrue(scripts.length > 7);
  }

  @Test
  public void testOutputScript() {

    PowerShellCommand command = new PowerShellCommand();
    assertEquals(command.toString().isEmpty(), false);
    assertThat(command.toString(), startsWith("try"));
    assertThat(command.toString(), containsString("catch"));
  }
}
