package io.jenkins.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.plugins.Models.*;
import io.jenkins.plugins.Presenters.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestStepBuilder extends BaseStepBuilder {

  private final String runTestMode, runTests;
  private boolean generateTestData;
  private String dgenFile;
  private RunTestInfo testInfo;

  @DataBoundConstructor
  public TestStepBuilder(String packageId, String serverType, String server, String database, String authenticationType,
                         String userName, Secret password, String runTestMode, String runTests) {

    super(packageId, serverType, server, authenticationType, userName, password, database);
    this.runTestMode = runTestMode;
    this.runTests = runTests;
    this.stepId = StepIds.Test;
  }

  public String getRunTestMode() {

    return runTestMode;
  }

  public String getRunTests() {

    return runTests;
  }

  public String getDgenFile() {

    return dgenFile;
  }

  public boolean getGenerateTestData() {

    return generateTestData;
  }

  @DataBoundSetter
  public void setDgenFile(String dgenFile) {

    this.dgenFile = dgenFile;
  }

  @DataBoundSetter
  public void setGenerateTestData(boolean generateTestData) {

    this.generateTestData = generateTestData;
  }

  public String runTestModeEquals(String runTestMode) {

    return this.runTestMode.equalsIgnoreCase(runTestMode) ? "true" : "false";
  }

  @Override
  public boolean prebuild(Build build, BuildListener listener){

    if (generateTestData  && (!Utils.isValidPath(dgenFile) || Paths.get(dgenFile).isAbsolute())) {
      processStepParameterInvalid(io.jenkins.plugins.Messages.TestStepBuilder_PropertiesNames_GenerateTestDataFile(), dgenFile, io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_wrongRelativePath(), listener);
      return false;
    }
    return true;
  }

  @Override
  protected boolean preExecute(Launcher launcher, TaskListener listener, FilePath workspace) {

    if (ProjectRepository.getInstance().getPackageProject(packageId) == null) {
      listener.getLogger().println(String.format(io.jenkins.plugins.Messages.packageMustBeBuilt(), packageId));
      return false;
    }
    String outputReportFilename = new SimpleDateFormat("'dbforgeDevopsTestResults_'yyyyMMdd_HHmmss'.xml'").format(new Date());
    testInfo = new RunTestInfo(runTestMode.equalsIgnoreCase("runAll"),
            runTests,
            Paths.get(workspace.getRemote(), outputReportFilename).toString(),
            generateTestData,
            generateTestData ? Paths.get(workspace.getRemote(), new String[]{dgenFile}).toString() : "",
            compareOptions
    );
    return super.preExecute(launcher, listener, workspace);
  }

  @Override
  protected PowerShellCommand getPowerShellCommand(FilePath workspace) {

    PowerShellCommand command = new PowerShellCommand();

    PackageProject project = ProjectRepository.getInstance().getPackageProject(packageId);
    command.addConnectionScript(connection);
    command.addTestBuildScript(project.getSourceFolder(), connection.getConnectionName(), testInfo);

    return command;
  }

  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    public FormValidation doCheckPackageId(@QueryParameter String value) {
      if (value.length() == 0)
        return FormValidation.error(io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_missingPackageId());
      if (!Utils.isValidPackageId(value))
        return FormValidation.warning(io.jenkins.plugins.Messages.invalidPackageId());
      return FormValidation.ok();
    }

    public FormValidation doCheckServer(@QueryParameter String value) {
      if (value.length() == 0)
        return FormValidation.error(io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_missingServer());
      return FormValidation.ok();
    }

    public FormValidation doCheckDatabase(@QueryParameter String value) {
      if (value.length() == 0)
        return FormValidation.error(io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_missingDatabase());
      return FormValidation.ok();
    }

    public FormValidation doCheckUserName(@QueryParameter String value) {
      if (value.length() == 0)
        return FormValidation.error(io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_missingUserName());
      return FormValidation.ok();
    }

    public FormValidation doCheckDgenFile(@QueryParameter String value) {
      if (value.length() == 0)
        return FormValidation.error(io.jenkins.plugins.Messages.TestStepBuilder_DescriptorImpl_errors_missingDgenPath());
      if (!Utils.isValidPath(value) || Paths.get(value).isAbsolute())
        return FormValidation.error(io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_wrongRelativePath());
      return FormValidation.ok();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return Messages.TestStepBuilder_DescriptorImpl_DisplayName();
    }
  }
}

