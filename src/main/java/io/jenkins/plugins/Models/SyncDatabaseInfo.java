package io.jenkins.plugins.Models;

public class SyncDatabaseInfo {

  private final String compareOptions, transactionIsoLvl;

  public SyncDatabaseInfo(String compareOptions, String transactionIsoLvl){

    this.compareOptions = compareOptions;
    this.transactionIsoLvl=  transactionIsoLvl;
  }

  public String getCompareOptions() {

    return compareOptions;
  }

  public String getTransactionIsoLvl() {

    return transactionIsoLvl;
  }
}