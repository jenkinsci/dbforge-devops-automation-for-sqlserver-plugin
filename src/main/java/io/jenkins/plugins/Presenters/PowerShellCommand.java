package io.jenkins.plugins.Presenters;

import hudson.FilePath;
import hudson.util.Secret;
import io.jenkins.plugins.Models.ConnectionInfo;
import io.jenkins.plugins.Models.PackageProject;
import io.jenkins.plugins.Models.RunTestInfo;
import io.jenkins.plugins.Models.AdditionalOptionsModel;

public class PowerShellCommand {

  public static final String separator = ";";
  private StringBuilder sb = new StringBuilder();
  private static final String scriptTemplate = "try{%s%n}%ncatch  { Write-Host $_.Exception.Message -ForegroundColor Red; [System.Environment]::Exit(1); }";

  public void addConnectionScript(ConnectionInfo connection) {

    sb.append(String.format("%n$%s = New-DevartSqlDatabaseConnection -Server \"%s\" -Database \"%s\"", connection.getConnectionName(), connection.getServer(), connection.getDatabase()));
    if (connection.getIsWindowsAuthentication())
      sb.append(" -WindowsAuthentication");
    else {
      sb.append(String.format(" -UserName %s", connection.getUserName()));
      if (!Secret.toString(connection.getPassword()).isEmpty())
        sb.append(String.format(" -Password %s", connection.getPassword()));
    }
    addScriptSeparator();
  }

  public void addDatabaseBuildScript(PackageProject project, String connectionName, AdditionalOptionsModel additionalOptions) {

    sb.append(String.format("%n$%s = Invoke-DevartDatabaseBuild -SourceScriptsFolder \"%s\" -Connection $%s",
      project.getDatabaseProjectName(),
      project.getSourceFolder(),
      connectionName));

    if (!additionalOptions.getFilterFile().isEmpty())
      sb.append(String.format(" -FilterPath \"%s\"", additionalOptions.getFilterFile()));

    if (!additionalOptions.getCompareOptions().isEmpty())
      sb.append(String.format(" -SynchronizationOptions \"%s\"", additionalOptions.getCompareOptions()));
    addScriptSeparator();

    sb.append(String.format("%nif(-Not $%s.Valid) { [System.Environment]::Exit(1); }", project.getDatabaseProjectName()));
  }

  public void addDatabaseExecuteScript(String connectionName, String filesToExecute, String fileEncoding, Secret zipPassword, Boolean ignoreError) {

    sb.append(String.format("%n$result = Invoke-DevartExecuteScript  -Connection $%s -Input \"%s\"",
            connectionName,
            filesToExecute
            ));

    if (!fileEncoding.isEmpty())
      sb.append(String.format(" -Encoding \"%s\"", fileEncoding));

    if (!Secret.toString(zipPassword).isEmpty())
      sb.append(String.format(" -ZipPassword \"%s\"", zipPassword));

    if (ignoreError)
      sb.append(" -IgnoreError");

    addScriptSeparator();

    sb.append(String.format("%nif(-Not $result) { [System.Environment]::Exit(1); }"));
  }

  public void addTestBuildScript(String scriptFolder, String connectionName, RunTestInfo testInfo, AdditionalOptionsModel additionalOptions) {

    sb.append(String.format("%n$result = Invoke-DevartDatabaseTests -InputObject \"%s\" -TemporaryDatabaseServer $%s -OutReportFileName:\"%s\" -ReportFormat %s -RewriteReport",
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

    if (!additionalOptions.getFilterFile().isEmpty())
      sb.append(String.format(" -FilterPath \"%s\"", additionalOptions.getFilterFile()));

    if (!additionalOptions.getCompareOptions().isEmpty())
      sb.append(String.format(" -SynchronizationOptions \"%s\"", additionalOptions.getCompareOptions()));
    addScriptSeparator();

    sb.append(String.format("%nif(-Not $result) { [System.Environment]::Exit(1); }"));
  }

  public void addSyncDatabaseScript(String scriptFolder, String targetConnectionName, AdditionalOptionsModel additionalOptionsModel) {

    sb.append(String.format("%n$result = Invoke-DevartSyncDatabaseSchema -Source \"%s\" -Target $%s",
      scriptFolder,
      targetConnectionName));

    if (!additionalOptionsModel.getTransactionIsoLvl().isEmpty())
      sb.append(String.format(" -TransactionIsolationLevel %s", additionalOptionsModel.getTransactionIsoLvl()));

    if (!additionalOptionsModel.getCompareOptions().isEmpty())
      sb.append(String.format(" -SynchronizationOptions \"%s\"", additionalOptionsModel.getCompareOptions()));

    if (!additionalOptionsModel.getFilterFile().isEmpty())
      sb.append(String.format(" -FilterPath \"%s\"", additionalOptionsModel.getFilterFile()));
    addScriptSeparator();

    sb.append(String.format("%nif(-Not $result) { [System.Environment]::Exit(1); }"));
  }

  public void addNewDatabaseProject(PackageProject project){

    sb.append(String.format("%n$%s = New-DevartDatabaseProject -SourceScriptsFolder \"%s\"", project.getDatabaseProjectName(), project.getSourceFolder()));
    addScriptSeparator();
  }

  public void addPackageInfo(String databaseProjectName, String id, String packageVersion) {

    sb.append(String.format("%nSet-DevartPackageInfo -Project $%s -Id %s", databaseProjectName, id));
    if (!packageVersion.isEmpty())
      sb.append(String.format(" -Version %s", packageVersion));
    addScriptSeparator();
  }

  public void addPublishDatabaseProject(String databaseProjectName, String packageVersion, String repository, Secret api) {

    sb.append(String.format("%nPublish-DevartDatabaseProject -Project $%s -Repository %s", databaseProjectName, repository));
    if (!Secret.toString(api).isEmpty())
      sb.append(String.format(" -ApiKey %s", api));
    if (packageVersion.isEmpty())
      sb.append(" -AutoIncrementVersion");
    addScriptSeparator();
  }

  public void addExecuteScript(boolean isLocalDbServer, FilePath scriptPath) {

    if (isLocalDbServer) {
      String resultVariable = String.format("execute_%s", scriptPath.getBaseName());
      sb.append(String.format("%n$%s = Invoke-DevartExecuteScript -Connection \"Data Source=(LocalDb)\\%s;Integrated Security=True\" -Input \"%s\";",
              resultVariable, ConnectionInfo.localDbInstance, scriptPath.getRemote()));
      sb.append(String.format("%nif(-Not $%s) { [System.Environment]::Exit(1); };", resultVariable));
    }
  }

  @Override
  public String toString() {

    return String.format(scriptTemplate, sb.toString());
  }

  private void addScriptSeparator(){

    sb.append((separator));
  }
}
