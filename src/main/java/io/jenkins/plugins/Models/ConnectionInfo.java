package io.jenkins.plugins.Models;

import hudson.util.Secret;

public class ConnectionInfo {

  public static String LocalDbInstance = "DbForgeDevopsLocalDb";
  private final String connectionName = "devartConnection", server, database, userName;
  private final Secret password;
  private final boolean isLocalDb, isWindowsAuthentication;

  public ConnectionInfo(boolean isLocalDb, String server, String database, boolean isWindowsAuthentication, String userName, Secret password){

    this.isLocalDb = isLocalDb;
    if(isLocalDb){
      this.server = String.format("(LocalDb)\\%s", LocalDbInstance);
      this.database = String.format("dbForgeDevopsTempDb_%s", java.util.UUID.randomUUID());
      this.isWindowsAuthentication = true;
      this.userName = null;
      this.password = null;
    }
    else {
      this.server = server;
      this.database = database;
      this.isWindowsAuthentication = isWindowsAuthentication;
      this.userName = userName;
      this.password = password;
    }
  }

  public String getConnectionName() {

    return connectionName;
  }

  public boolean getIsLocalDb() {

    return isLocalDb;
  }

  public String getServer() {

    return server;
  }

  public String getDatabase() {

    return database;
  }

  public boolean getIsWindowsAuthentication() {

    return isWindowsAuthentication;
  }

  public String getUserName() {

    return userName;
  }

  public Secret getPassword() {

    return password;
  }
}
