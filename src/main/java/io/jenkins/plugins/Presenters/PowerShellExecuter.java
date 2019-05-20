package io.jenkins.plugins.Presenters;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;

import java.io.*;

public class PowerShellExecuter {

  private static PowerShellExecuter ourInstance = new PowerShellExecuter();

  public static PowerShellExecuter getInstance() {
    return ourInstance;
  }

  private PowerShellExecuter() {
  }

  public boolean execute(Launcher launcher, TaskListener listener, FilePath workspace, PowerShellCommand command) {

    FilePath psScriptFile = Utils.generateScriptFile(listener, workspace, command.toString(), ".ps1");
    if (psScriptFile != null) {
      boolean result = execute(launcher, listener, workspace, psScriptFile, null, null);
      if (!result) {
        listener.getLogger().println("Command execution failed:");
        listener.getLogger().println(command.toString());
        listener.getLogger().println();
      }
      try {
        psScriptFile.delete();
      }
      catch (Exception exc) {
      }
      return result;
    }
    return false;
  }

  public boolean execute(Launcher launcher, TaskListener listener, FilePath workspace, FilePath psScriptFile, String functionName, String[] functionParams) {

    try {
      listener.getLogger().println("Begin Execute command:");
      Launcher.ProcStarter procStarter = this.defineProcess(launcher, listener.getLogger(), workspace, psScriptFile, functionName, functionParams);
      return this.executeProcess(launcher, procStarter);
    }
    catch (IOException e) {
      listener.error("Unexpected I/O exception executing PS script: " + e.getMessage());
      return false;
    }
    catch (InterruptedException e) {
      listener.error("Unexpected thread interruption executing PS script");
      return false;
    }
    finally {
      listener.getLogger().println("Command Executed");
      listener.getLogger().println();
    }
  }

  private Launcher.ProcStarter defineProcess(Launcher launcher, OutputStream outputStream, FilePath workspace, FilePath scaRunnerLocation, String functionName, String[] functionParams) {

    String cmdString;
    Launcher.ProcStarter procStarter;

    if(functionName == null)
      cmdString = this.generateCmdString(scaRunnerLocation.getRemote());
    else
      cmdString = this.generateCmdString(scaRunnerLocation.getRemote(), functionName, functionParams);

    procStarter = launcher.launch();
    procStarter.cmdAsSingleString(cmdString).stdout(outputStream).stderr(outputStream).pwd(workspace);
    return procStarter;
  }

  private String generateCmdString(String sqlCiLocation) {

    StringBuilder longStringBuilder = new StringBuilder();
    longStringBuilder.append("\"").append(PowerShellExecuter.getPowerShellExeLocation()).append("\" -NonInteractive -ExecutionPolicy Bypass -File \"").append(sqlCiLocation).append("\"").append(" -Verbose");
    return longStringBuilder.toString();
  }

  private String generateCmdString(String sqlCiLocation, String functionName, String[] functionParams) {

    if(functionName == null)
      return generateCmdString(sqlCiLocation);

    StringBuilder longStringBuilder = new StringBuilder();
    longStringBuilder.append("\"").append(PowerShellExecuter.getPowerShellExeLocation()).append("\" -NonInteractive -ExecutionPolicy Bypass -command \"& { . ").append(sqlCiLocation).append("; " + functionName);
    for(int i = 0; i < functionParams.length; i++)
    {
      longStringBuilder.append(" " + functionParams[i]);
    }
    longStringBuilder.append("}\" -Verbose");

    return longStringBuilder.toString();
  }

  private boolean executeProcess(Launcher launcher, Launcher.ProcStarter procStarter) throws IOException, InterruptedException {

    Proc proc = launcher.launch(procStarter);
    int exitCode = proc.join();
    return exitCode == 0;
  }

  private static String getPowerShellExeLocation() {

    String psHome = System.getenv("PS_HOME");
    if (psHome != null) {
      return psHome;
    }
    return "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe";
  }
}
