package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.plugins.Models.*;
import io.jenkins.plugins.Presenters.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.util.StringUtils;

public class PublishStepBuilder extends Builder {

  private final String packageId, nugetFeedUrl, packageVersion;
  private final Secret nugetFeedUrlApi;

  @DataBoundConstructor
  public PublishStepBuilder(String packageId, String nugetFeedUrl, Secret nugetFeedUrlApi, String packageVersion) {

    this.packageId = packageId;
    this.nugetFeedUrl = nugetFeedUrl;
    this.nugetFeedUrlApi = nugetFeedUrlApi;
    this.packageVersion = packageVersion;
  }

  public String getPackageId() {

    return packageId;
  }

  public String getNugetFeedUrl() {

    return nugetFeedUrl;
  }

  public Secret getNugetFeedUrlApi() {

    return nugetFeedUrlApi;
  }

  public String getPackageVersion() {

    return packageVersion;
  }

  @Override
  @SuppressFBWarnings
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

    listener.getLogger().println(String.format("Started '%s'", getDescriptor().getDisplayName()));
    listener.getLogger().println(String.format("Package ID: '%s'", packageId));

    boolean buildResult;
    PackageProject project = ProjectRepository.getInstance().getPackageProject(packageId);
    if (project == null) {
      listener.error(String.format(io.jenkins.plugins.Messages.packageMustBeBuilt(), packageId));
      buildResult = false;
    }
    else {
      PowerShellCommand command = new PowerShellCommand();
      command.addNewDatabaseProject(project);
      command.addPackageInfo(project.getDatabaseProjectName(), project.getId(), packageVersion);
      command.addPublishDatabaseProject(project.getDatabaseProjectName(), packageVersion, StringUtils.quote(nugetFeedUrl), nugetFeedUrlApi);
      buildResult = PowerShellExecuter.getInstance().execute(launcher, listener, build.getWorkspace(), command);
    }

    listener.getLogger().println(String.format("Finished '%s'", getDescriptor().getDisplayName()));
    listener.getLogger().println();

    return buildResult;
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

    public FormValidation doCheckNugetFeedUrl(@QueryParameter String value) {
      if (value.length() == 0)
        return FormValidation.error(io.jenkins.plugins.Messages.PublishStepBuilder_DescriptorImpl_errors_missingNuGetFeedUrl());
      return FormValidation.ok();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return Messages.PublishStepBuilder_DescriptorImpl_DisplayName();
    }
  }
}
