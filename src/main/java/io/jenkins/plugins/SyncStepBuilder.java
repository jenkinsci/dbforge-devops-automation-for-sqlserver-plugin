package io.jenkins.plugins;

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
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class SyncStepBuilder extends BaseStepBuilder {

  private String transactionIsoLvl = "Serializable";
  private SyncDatabaseInfo syncDatabaseInfo;

  @DataBoundConstructor
  public SyncStepBuilder(String packageId, String server, String database, String authenticationType, String userName, Secret password) {

    super(packageId, "server", server, authenticationType, userName, password, database);
    this.stepId = StepIds.Sync;
  }

  public String getTransactionIsoLvl() {

    return transactionIsoLvl;
  }

  @DataBoundSetter
  public void setTransactionIsoLvl(String transactionIsoLvl) {

    this.transactionIsoLvl = transactionIsoLvl;
  }

  @Override
  protected boolean preExecute(Launcher launcher, TaskListener listener, FilePath workspace) {

    if (ProjectRepository.getInstance().getPackageProject(packageId) == null) {
      listener.getLogger().println(String.format(io.jenkins.plugins.Messages.PackageMustBeBuilt(), packageId));
      return false;
    }

    syncDatabaseInfo = new SyncDatabaseInfo(compareOptions, transactionIsoLvl);
    return super.preExecute(launcher, listener, workspace);
  }

  @Override
  protected PowerShellCommand getPowerShellCommand(FilePath workspace) {

    PackageProject project = ProjectRepository.getInstance().getPackageProject(packageId);
    PowerShellCommand command = new PowerShellCommand();

    command.AddConnectionScript(connection);
    command.AddSyncDatabaseScript(project.getSourceFolder(), connection.getConnectionName(), syncDatabaseInfo);

    return command;
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

