package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.Secret;
import hudson.util.FormValidation;
import io.jenkins.plugins.Models.*;
import io.jenkins.plugins.Presenters.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.nio.file.Paths;

public class SyncStepBuilder extends BaseExecuteStepBuilder {

  protected final String packageId;

  @DataBoundConstructor
  public SyncStepBuilder(String packageId, String server, String database, String authenticationType, String userName, Secret password) {

    super(StepIds.Sync, Constants.server, server, authenticationType, userName, password, database);
    this.packageId = packageId;
  }

  public String getPackageId() {

    return packageId;
  }

  @Override
  @SuppressFBWarnings
  public boolean prebuild(Build build, BuildListener listener){

    return validateFilterFile(listener);
  }

  @Override
  protected void onStarted(BuildListener listener) {
    listener.getLogger().println(String.format("Package ID: '%s'", packageId));
  }

  @Override
  protected boolean preExecute(Launcher launcher, TaskListener listener, FilePath workspace) {

    if (ProjectRepository.getInstance().getPackageProject(packageId) == null) {
      listener.error(String.format(io.jenkins.plugins.Messages.packageMustBeBuilt(), packageId));
      return false;
    }

    return super.preExecute(launcher, listener, workspace);
  }

  @Override
  protected PowerShellCommand getPowerShellCommand(FilePath workspace) {

    PackageProject project = ProjectRepository.getInstance().getPackageProject(packageId);
    PowerShellCommand command = new PowerShellCommand();

    command.addConnectionScript(connection);
    command.addSyncDatabaseScript(project.getSourceFolder(), connection.getConnectionName(), additionalOptions);

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
      return Messages.SyncStepBuilder_DescriptorImpl_DisplayName();
    }
  }
}

