package io.jenkins.plugins;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.util.Secret;
import hudson.tasks.Builder;
import io.jenkins.plugins.Models.*;
import io.jenkins.plugins.Presenters.*;
import org.kohsuke.stapler.DataBoundSetter;

public abstract class BaseStepBuilder extends Builder {

  private final String serverType, authenticationType, server, database, userName;
  private final Secret password;
  protected final String packageId;
  protected StepIds stepId;
  protected String compareOptions;
  protected ConnectionInfo connection;

  public BaseStepBuilder(String packageId, String serverType, String server, String authenticationType, String userName, Secret password, String database) {

    this.packageId = packageId;
    this.serverType = serverType;
    this.server = server;
    this.database = database;
    this.authenticationType = authenticationType;
    this.userName = userName;
    this.password = password;
  }

  public String getPackageId() {

    return packageId;
  }

  public String getServerType() {

    return serverType;
  }

  public String getServer() {

    return server;
  }

  public String getDatabase() {

    return database;
  }

  public String getAuthenticationType() {

    return authenticationType;
  }

  public String getUserName() {

    return userName;
  }

  public Secret getPassword() {

    return password;
  }

  public String getCompareOptions() {

    return compareOptions;
  }

  @DataBoundSetter
  public void setCompareOptions(String compareOptions) {

    this.compareOptions = compareOptions;
  }

  public String serverTypeEquals(String serverType) {

    return this.serverType.equalsIgnoreCase(serverType) ? "true" : "false";
  }

  public String authenticationTypeEquals(String authenticationType) {

    return this.authenticationType.equalsIgnoreCase(authenticationType) ? "true" : "false";
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

    listener.getLogger().println(String.format("Started '%s'", getDescriptor().getDisplayName()));
    listener.getLogger().println(String.format("Package ID: '%s'", packageId));

    FilePath workspace = build.getWorkspace();
    boolean result = preExecute(launcher, listener, workspace) && PowerShellExecuter.getInstance().Execute(launcher, listener, workspace, getPowerShellCommand(workspace));

    listener.getLogger().println(String.format("Finished '%s'", getDescriptor().getDisplayName()));
    listener.getLogger().println();

    postExecute(launcher, listener, workspace, result);
    return result;
  }

  protected boolean preExecute(Launcher launcher, TaskListener listener, FilePath workspace) {

    connection = new ConnectionInfo(serverType.equalsIgnoreCase("localDb"),
            server,
            database,
            authenticationType.equalsIgnoreCase("windowsAuthentication"),
            userName,
            password);

    if (connection.getIsLocalDb()) {
      FilePath scriptLocation = new FilePath(workspace, Constants.PSScriptsLocation);
      if (PowerShellExecuter.getInstance().Execute(launcher, listener, workspace, scriptLocation, "CreateLocalDbInstance", new String[]{ConnectionInfo.LocalDbInstance}))
        return executeScript(launcher, listener, workspace, String.format("CREATE DATABASE \"%s\";", connection.getDatabase()));
      return false;
    }

    return true;
  }

  protected void ProcessStepParameterInvalid(String parameterName, String parameterValue, String errorMessage, BuildListener listener) {

    listener.error(getDescriptor().getDisplayName() + " has invalid parameter.");
    listener.error("\"" + parameterName +"\": \"" + parameterValue + "\"");
    listener.error(errorMessage);
  }

  protected void postExecute(Launcher launcher, TaskListener listener, FilePath workspace, boolean buildSuccessful){

    if (connection.getIsLocalDb()) {
      executeScript(launcher, listener, workspace, String.format("DROP DATABASE \"%s\";", connection.getDatabase()));
      FilePath scriptLocation = new FilePath(workspace, Constants.PSScriptsLocation);
      PowerShellExecuter.getInstance().Execute(launcher, listener, workspace, scriptLocation, "DeleteLocalDbInstance", new String[]{ConnectionInfo.LocalDbInstance});
    }
  }

  protected abstract PowerShellCommand getPowerShellCommand(FilePath workspace);

  public StepIds GetStepId()
  {
    return stepId;
  }

  private boolean executeScript(Launcher launcher, TaskListener listener, FilePath workspace, String script){

    FilePath fileScript = Utils.generateScriptFile(listener, workspace, script, ".sql");
    if (fileScript == null)
      return false;

    PowerShellCommand command = new PowerShellCommand();
    command.AddExecuteScript(true, fileScript);
    boolean result = PowerShellExecuter.getInstance().Execute(launcher, listener, workspace, command);
    try {
      fileScript.delete();
    }
    catch (Exception exc) {
    }

    return result;
  }
}

