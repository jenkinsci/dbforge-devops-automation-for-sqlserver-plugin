package io.jenkins.plugins.Models;

public class PackageProject {

  private static final String databaseProjectName = "devartDatabaseProjectName";
  private String id, sourceFolder;

  public PackageProject(String id) {

    this.id = id;
  }

  public String getDatabaseProjectName() {

    return databaseProjectName;
  }

  public String getId() {

    return this.id;
  }

  public String getSourceFolder() {

    return this.sourceFolder;
  }

  public void setSourceFolder(String sourceFolder) {

    this.sourceFolder = sourceFolder;
  }
}
