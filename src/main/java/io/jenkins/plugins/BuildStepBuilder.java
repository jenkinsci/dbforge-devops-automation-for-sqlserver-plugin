package io.jenkins.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.Secret;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import io.jenkins.plugins.Models.*;
import io.jenkins.plugins.Presenters.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import hudson.model.TaskListener;

public class BuildStepBuilder extends BaseStepBuilder {

  private final String sourceFolderMode, subfolder;
  private PackageProject project;

  @DataBoundConstructor
  public BuildStepBuilder(String sourceFolderMode, String subfolder, String packageId,
                          String serverType, String server, String authenticationType, String userName, Secret password, String database) {

    super(packageId, serverType, server, authenticationType, userName, password, database);
    this.sourceFolderMode = sourceFolderMode;
    this.subfolder = subfolder;
    this.stepId = StepIds.Buid;
  }

  public String getSourceFolderMode() {

    return sourceFolderMode;
  }

  public String getSubfolder() {

    return subfolder;
  }

  public String sourceFolderModeEquals(String sourceFolderMode) {

    return this.sourceFolderMode.equalsIgnoreCase(sourceFolderMode) ? "true" : "false";
  }

  @Override
  public boolean prebuild(Build build, BuildListener listener){

    boolean result = true;

    if (sourceFolderMode.equalsIgnoreCase("subfolder")  && (!Utils.isValidPath(subfolder) || Paths.get(subfolder).isAbsolute())) {
      ProcessStepParameterInvalid(io.jenkins.plugins.Messages.BuildStepBuilder_PropertiesNames_SubFolder(), subfolder, io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_wrongRelativePath(), listener);
      result = false;
    }

    result &= EnvironmentValidator.Validate(((FreeStyleProject)build.getProject()).getBuilders(), build.getWorkspace(), listener);
    return result;
  }

  @Override
  protected boolean preExecute(Launcher launcher, TaskListener listener, FilePath workspace) {

    ProjectRepository.getInstance().removePackageProject(packageId);
    project = new PackageProject((packageId));
    project.setSourceFolder(getScriptFolder(workspace));

    return super.preExecute(launcher, listener, workspace);
  }

  @Override
  protected PowerShellCommand getPowerShellCommand(FilePath workspace) {

    PowerShellCommand command = new PowerShellCommand();

    command.AddConnectionScript(connection);
    command.AddDatabaseBuildScript(project, connection.getConnectionName(), compareOptions);

    return command;
  }

  @Override
  protected void postExecute(Launcher launcher, TaskListener listener, FilePath workspace, boolean buildSuccessful) {

    super.postExecute(launcher, listener, workspace, buildSuccessful);
    if (buildSuccessful)
      ProjectRepository.getInstance().addPackageProject(project);
  }

  private String getScriptFolder(FilePath checkOutPath) {

    switch (sourceFolderMode) {
      case "vcsroot":
        return checkOutPath.getRemote();
      case "subfolder":
        Path subfolderPath = Paths.get(checkOutPath.getRemote(), new String[] { subfolder });
        return subfolderPath.toString();
    }
    return null;
  }

  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    public FormValidation doCheckPackageId(@QueryParameter String value) {

      if (value.length() == 0)
        return FormValidation.error(io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_missingPackageId());
      if (!Utils.isValidPackageId(value))
        return FormValidation.warning(io.jenkins.plugins.Messages.InvalidPackageId());
      return FormValidation.ok();
    }

    public FormValidation doCheckSourceControlFolder(@QueryParameter String value) {
      if (value.length() == 0)
        return FormValidation.error(io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_missingLocation());
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

    public FormValidation doCheckSubfolder(@QueryParameter String value) {
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
      return Messages.BuildStepBuilder_DescriptorImpl_DisplayName();
    }
  }
}
