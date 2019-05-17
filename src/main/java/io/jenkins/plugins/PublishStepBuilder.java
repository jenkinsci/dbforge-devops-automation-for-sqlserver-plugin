package io.jenkins.plugins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.Models.*;
import io.jenkins.plugins.Presenters.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.springframework.util.StringUtils;

public class PublishStepBuilder extends Builder {

  private final String packageId, nugetFeedUrl, nugetFeedUrlApi, packageVersion;

  @DataBoundConstructor
  public PublishStepBuilder(String packageId, String nugetFeedUrl, String nugetFeedUrlApi, String packageVersion) {

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

  public String getNugetFeedUrlApi() {

    return nugetFeedUrlApi;
  }

  public String getPackageVersion() {

    return packageVersion;
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

    listener.getLogger().println(String.format("Started '%s'", getDescriptor().getDisplayName()));
    listener.getLogger().println(String.format("Package ID: '%s'", packageId));

    boolean buildResult;
    PackageProject project = ProjectRepository.getInstance().getPackageProject(packageId);
    if (project == null) {
      listener.getLogger().println(String.format(io.jenkins.plugins.Messages.PackageMustBeBuilt(), packageId));
      buildResult = false;
    } else {
      PowerShellCommand command = new PowerShellCommand();
      command.AddNewDatabaseProject(project);
      command.AddPackageInfo(project.getDatabaseProjectName(), project.getId(), packageVersion);
      command.AddPublishDatabaseProject(project.getDatabaseProjectName(), packageVersion, StringUtils.quote(nugetFeedUrl), nugetFeedUrlApi);
      buildResult = PowerShellExecuter.getInstance().Execute(launcher, listener, build.getWorkspace(), command);
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
        return FormValidation.warning(io.jenkins.plugins.Messages.InvalidPackageId());
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
