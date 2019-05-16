package io.jenkins.plugins.Models;

import java.util.HashMap;

public class ProjectRepository {

  private static ProjectRepository ourInstance = new ProjectRepository();
  private HashMap<String, PackageProject> projects;

  public static ProjectRepository getInstance() {
    return ourInstance;
  }

  private ProjectRepository() {

    projects = new HashMap<String, PackageProject>(1);
  }

  public PackageProject getPackageProject(String packageId) {

    if (projects.containsKey(packageId))
      return projects.get(packageId);
    return null;
  }

  public boolean addPackageProject(PackageProject project) {

    if (!projects.containsKey(project.getId())) {
      projects.put(project.getId(), project);
      return true;
    }
    return false;
  }

  public boolean removePackageProject(String packageId) {

    return projects.remove(packageId) != null;
  }
}
