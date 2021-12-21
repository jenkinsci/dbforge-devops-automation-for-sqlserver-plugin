package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

public class BuildStepBuilder extends BaseExecuteStepBuilder {

  private final String sourceFolderMode, subfolder;
  private PackageProject project;

  protected final String packageId;

  @DataBoundConstructor
  public BuildStepBuilder(String sourceFolderMode, String subfolder, String packageId,
                          String serverType, String server, String authenticationType, String userName, Secret password, String database) {

    super(serverType, server, authenticationType, userName, password, database);
    this.packageId = packageId;
    this.sourceFolderMode = sourceFolderMode;
    this.subfolder = subfolder;
    this.stepId = StepIds.Buid;
  }

  public String getPackageId() {

    return packageId;
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
  @SuppressFBWarnings
  public boolean prebuild(Build build, BuildListener listener) {

    if (sourceFolderMode.equalsIgnoreCase("subfolder") && (!Utils.isValidPath(subfolder) || Paths.get(subfolder).isAbsolute())) {
      processStepParameterInvalid(io.jenkins.plugins.Messages.BuildStepBuilder_PropertiesNames_SubFolder(), subfolder, io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_wrongRelativePath(), listener);
      return false;
    }

    if(!validateFilterFile(listener))
      return false;

    FreeStyleProject freeStyleProject = (FreeStyleProject) build.getProject();
    if (freeStyleProject != null)
      return EnvironmentValidator.validate(freeStyleProject.getBuilders(), build.getWorkspace(), listener);

    return true;
  }

  @Override
  protected void OnStarted(BuildListener listener) {
    listener.getLogger().println(String.format("Package ID: '%s'", packageId));
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

    command.addConnectionScript(connection);
    command.addDatabaseBuildScript(project, connection.getConnectionName(), additionalOptions);

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

    public FormValidation doCheckSubfolder(@QueryParameter String value) {
      if (value.length() == 0 || !Utils.isValidPath(value) || Paths.get(value).isAbsolute())
        return FormValidation.error(io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_wrongRelativePath());
      return FormValidation.ok();
    }

    public FormValidation doCheckFilterFile(@QueryParameter String value) {
      if (value.length() == 0)
        return FormValidation.ok();
      if (!value.endsWith(".scflt"))
        return FormValidation.error(io.jenkins.plugins.Messages.BuildStepBuilder_DescriptorImpl_errors_wrongScfltPath());
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
