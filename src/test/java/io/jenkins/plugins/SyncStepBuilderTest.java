package io.jenkins.plugins;

import hudson.model.FreeStyleProject;
import hudson.util.Secret;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

public class SyncStepBuilderTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  private final String packageId = "id";
  private final String serverType = "server", authenticationType = "serverAuthentication";
  private final String server = "srv", database = "db", userName = "su", password = "su";
  private final String transactionIsoLvl = "Serializable", compareOptions = "compareOptions";

  @Test
  public void testConfigRoundtrip() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    project.getBuildersList().add(new SyncStepBuilder(packageId, server, database, authenticationType, userName, Secret.fromString(password)));
    project = jenkins.configRoundtrip(project);

    SyncStepBuilder syncBuildStep = new SyncStepBuilder(packageId, server, database, authenticationType, userName, Secret.fromString(password));
    syncBuildStep.setCompareOptions("");
    syncBuildStep.setTransactionIsoLvl(transactionIsoLvl); // default value is Serializable
    jenkins.assertEqualDataBoundBeans(syncBuildStep, project.getBuildersList().get(0));
  }

  @Test
  public void testConfigRoundtripAdvanced() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    SyncStepBuilder builder = new SyncStepBuilder(packageId, server, database, authenticationType, userName, Secret.fromString(password));
    builder.setCompareOptions(compareOptions);
    builder.setTransactionIsoLvl(transactionIsoLvl);
    project.getBuildersList().add(builder);
    project = jenkins.configRoundtrip(project);

    SyncStepBuilder syncBuildStep = new SyncStepBuilder(packageId, server, database, authenticationType, userName, Secret.fromString(password));
    syncBuildStep.setCompareOptions(compareOptions);
    syncBuildStep.setTransactionIsoLvl(transactionIsoLvl);
    jenkins.assertEqualDataBoundBeans(syncBuildStep, project.getBuildersList().get(0));
  }

  @Test
  public void testProperties() {

    SyncStepBuilder syncBuildStep = new SyncStepBuilder(packageId, server, database, authenticationType, userName, Secret.fromString(password));
    syncBuildStep.setCompareOptions(compareOptions);
    syncBuildStep.setTransactionIsoLvl(transactionIsoLvl);

    assertEquals(syncBuildStep.getPackageId(), packageId);
    assertEquals(syncBuildStep.getServerType(), serverType);
    assertEquals(syncBuildStep.getServer(), server);
    assertEquals(syncBuildStep.getAuthenticationType(), authenticationType);
    assertEquals(syncBuildStep.getUserName(), userName);
    assertEquals(syncBuildStep.getPassword(), Secret.fromString(password));
    assertEquals(syncBuildStep.getDatabase(), database);
    assertEquals(syncBuildStep.getTransactionIsoLvl(), transactionIsoLvl);
    assertEquals(syncBuildStep.getCompareOptions(), compareOptions);
  }

  @Test
  public void testAuthenticationTypeEquals() {

    SyncStepBuilder syncBuildStep = new SyncStepBuilder(packageId, server, database, authenticationType, userName, Secret.fromString(password));

    assertEquals(syncBuildStep.authenticationTypeEquals("windowsAuthentication"), "false");
    assertEquals(syncBuildStep.authenticationTypeEquals("serverAuthentication"), "true");
  }
}
