package io.jenkins.plugins.Presenters;

import hudson.FilePath;
import hudson.util.Secret;
import io.jenkins.plugins.Models.ConnectionInfo;
import io.jenkins.plugins.Models.PackageProject;
import io.jenkins.plugins.Models.RunTestInfo;
import io.jenkins.plugins.Models.SyncDatabaseInfo;

public class PowerShellCommand {

  public String Separator = ";";
  private StringBuilder sb = new StringBuilder();
  private final String scriptTemplate = "try{%s\n}\ncatch  { Write-Host $_.Exception.Message -ForegroundColor Red; [System.Environment]::Exit(1); }";

  public void AddConnectionScript(ConnectionInfo connection) {

    sb.append(String.format("\n$%s = New-DevartSqlDatabaseConnection -Server \"%s\" -Database \"%s\"", connection.getConnectionName(), connection.getServer(), connection.getDatabase()));
    if (connection.getIsWindowsAuthentication())
      sb.append(" -WindowsAuthentication");
    else {
      sb.append(String.format(" -UserName %s", connection.getUserName()));
      if (!Secret.toString(connection.getPassword()).isEmpty())
        sb.append(String.format(" -Password %s", connection.getPassword()));
    }
    addScriptSeparator();
  }

  public void AddDatabaseBuildScript(PackageProject project, String connectionName, String compareOptions) {

    sb.append(String.format("\n$%s = Invoke-DevartDatabaseBuild -SourceScriptsFolder \"%s\" -Connection $%s",
      project.getDatabaseProjectName(),
      project.getSourceFolder(),
      connectionName));

    if (!compareOptions.isEmpty())
      sb.append(String.format(" -SynchronizationOptions \"%s\"", compareOptions));
    addScriptSeparator();

    sb.append(String.format("\nif(-Not $%s.Valid) { [System.Environment]::Exit(1); }", project.getDatabaseProjectName()));
  }

  public void AddTestBuildScript(String scriptFolder, String connectionName, RunTestInfo testInfo) {

    sb.append(String.format("\n$result = Invoke-DevartDatabaseTests -InputObject \"%s\" -TemporaryDatabaseServer $%s -OutReportFileName:\"%s\" -ReportFormat %s -RewriteReport",
      scriptFolder,
      connectionName,
      testInfo.getOutputReport(),
      testInfo.getOutputReportFormat()));

    if (testInfo.getInstallFramework())
      sb.append(String.format(" -InstalltSQLtFramework -UnInstalltSQLtFramework"));

    if (testInfo.getGenerateTestData())
      sb.append(String.format(" -IncludeTestData -DataGeneratorProject \"%s\"", testInfo.getDgenFile()));

    if (!testInfo.getRunEveryTests())
      sb.append(String.format(" -UnitTests %s", "\"" + testInfo.getRunTests()+"\""));

    if (!testInfo.getCompareOptions().isEmpty())
      sb.append(String.format(" -SynchronizationOptions \"%s\"", testInfo.getCompareOptions()));
    addScriptSeparator();

    sb.append("\nif(-Not $result) { [System.Environment]::Exit(1); }");
  }

  public void AddSyncDatabaseScript(String scriptFolder, String targetConnectionName, SyncDatabaseInfo syncDatabaseInfo) {

    sb.append(String.format("\n$result = Invoke-DevartSyncDatabaseSchema -Source \"%s\" -Target $%s",
      scriptFolder,
      targetConnectionName));

    if (!syncDatabaseInfo.getTransactionIsoLvl().isEmpty())
      sb.append(String.format(" -TransactionIsolationLevel %s", syncDatabaseInfo.getTransactionIsoLvl()));

    if (!syncDatabaseInfo.getCompareOptions().isEmpty())
      sb.append(String.format(" -SynchronizationOptions \"%s\"", syncDatabaseInfo.getCompareOptions()));
    addScriptSeparator();

    sb.append("\nif(-Not $result) { [System.Environment]::Exit(1); }");
  }

  public void AddNewDatabaseProject(PackageProject project){

    sb.append(String.format("\n$%s = New-DevartDatabaseProject -SourceScriptsFolder \"%s\"", project.getDatabaseProjectName(), project.getSourceFolder()));
    addScriptSeparator();
  }

  public void AddPackageInfo(String databaseProjectName, String id, String packageVersion) {

    sb.append(String.format("\nSet-DevartPackageInfo -Project $%s -Id %s", databaseProjectName, id));
    if (!packageVersion.isEmpty())
      sb.append(String.format(" -Version %s", packageVersion));
    addScriptSeparator();
  }

  public void AddPublishDatabaseProject(String databaseProjectName, String packageVersion, String repository, String api) {

    sb.append(String.format("\nPublish-DevartDatabaseProject -Project $%s -Repository %s", databaseProjectName, repository));
    if (!api.isEmpty())
      sb.append(String.format(" -ApiKey %s", api));
    if (packageVersion.isEmpty())
      sb.append(" -AutoIncrementVersion");
    addScriptSeparator();
  }

  public void AddExecuteScript(boolean isLocalDbServer, FilePath scriptPath) {

    if (isLocalDbServer) {
      String resultVariable = String.format("execute_%s", scriptPath.getBaseName());
      sb.append(String.format("\n$%s = Invoke-DevartExecuteScript -Connection \"Data Source=(LocalDb)\\%s;Integrated Security=True\" -Input \"%s\";",
              resultVariable, ConnectionInfo.LocalDbInstance, scriptPath.getRemote()));
      sb.append(String.format("\nif(-Not $%s) { [System.Environment]::Exit(1); };", resultVariable));
    }
  }

  @Override
  public String toString() {

    return String.format(scriptTemplate, sb.toString());
  }

  private void addScriptSeparator(){

    sb.append((Separator));
  }
}
