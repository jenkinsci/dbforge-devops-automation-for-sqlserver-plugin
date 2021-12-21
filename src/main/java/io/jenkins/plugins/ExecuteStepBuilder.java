package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.Models.Constants;
import io.jenkins.plugins.Models.StepIds;
import io.jenkins.plugins.Presenters.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.nio.charset.Charset;

public class ExecuteStepBuilder extends BaseExecuteStepBuilder {

    private final String filesToExecute;
    private String fileEncoding;
    private Secret zipPassword;
    private Boolean ignoreError;

    @DataBoundConstructor
    public ExecuteStepBuilder(String server, String authenticationType, String userName, Secret password, String database, String filesToExecute) {

        super(Constants.server, server, authenticationType, userName, password, database);
        this.filesToExecute = filesToExecute;
        this.stepId = StepIds.Execute;
    }

    public String getFilesToExecute() {

        return filesToExecute;
    }

    public String getFileEncoding() {

        return fileEncoding;
    }

    public Secret getZipPassword() {

        return zipPassword;
    }

    public Boolean getIgnoreError() {

        return ignoreError;
    }

    @DataBoundSetter
    public void setFileEncoding(String value) {

        fileEncoding = value;
    }

    @DataBoundSetter
    public void setZipPassword(Secret value) {

        zipPassword = value;
    }

    @DataBoundSetter
    public void setIgnoreError(Boolean value) {

        ignoreError = value;
    }

    @Override
    @SuppressFBWarnings
    public boolean prebuild(Build build, BuildListener listener) {

        FreeStyleProject freeStyleProject = (FreeStyleProject) build.getProject();
        if (freeStyleProject != null)
            return EnvironmentValidator.validate(freeStyleProject.getBuilders(), build.getWorkspace(), listener);

        return true;
    }

    @Override
    protected PowerShellCommand getPowerShellCommand(FilePath workspace) {

        PowerShellCommand command = new PowerShellCommand();

        command.addConnectionScript(connection);
        command.addDatabaseExecuteScript(connection.getConnectionName(), filesToExecute, fileEncoding, zipPassword, ignoreError);

        return command;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
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

        public FormValidation doCheckFilesToExecute(@QueryParameter String value) {
            if (value.length() == 0 || !Utils.isValidPath(value))
                return FormValidation.error(io.jenkins.plugins.Messages.ExecuteStepBuilder_DescriptorImpl_errors_wrongPath());
            return FormValidation.ok();
        }

        public ListBoxModel doFillFileEncodingItems() {
            ListBoxModel items = new ListBoxModel();
            items.add(Constants.defaultListItemValue, "");

            for (String key : Charset.availableCharsets().keySet())
                items.add(key);

            return items;
        }

        @Override
        public String getDisplayName() {
            return Messages.ExecuteStepBuilder_DescriptorImpl_DisplayName();
        }
    }
}
