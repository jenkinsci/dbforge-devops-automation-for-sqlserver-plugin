package io.jenkins.plugins;

import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

public class PublishStepBuilderTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();
  private final String packageId = "id", nugetFeedUrl = "nugetFeedUrl", nugetFeedUrlApi = "nugetFeedUrlApi", packageVersion = "packageVersion";

  @Test
  public void testConfigRoundtrip() throws Exception {

    FreeStyleProject project = jenkins.createFreeStyleProject();
    project.getBuildersList().add(new PublishStepBuilder(packageId, nugetFeedUrl, nugetFeedUrlApi, packageVersion));
    project = jenkins.configRoundtrip(project);

    PublishStepBuilder publishStepBuilder = new PublishStepBuilder(packageId, nugetFeedUrl, nugetFeedUrlApi, packageVersion);
    jenkins.assertEqualDataBoundBeans(publishStepBuilder, project.getBuildersList().get(0));
  }

  @Test
  public void testProperties() {

    PublishStepBuilder publishStepBuilder = new PublishStepBuilder(packageId, nugetFeedUrl, nugetFeedUrlApi, packageVersion);

    assertEquals(publishStepBuilder.getPackageId(), packageId);
    assertEquals(publishStepBuilder.getNugetFeedUrl(), nugetFeedUrl);
    assertEquals(publishStepBuilder.getNugetFeedUrlApi(), nugetFeedUrlApi);
    assertEquals(publishStepBuilder.getPackageVersion(), packageVersion);
  }
}

