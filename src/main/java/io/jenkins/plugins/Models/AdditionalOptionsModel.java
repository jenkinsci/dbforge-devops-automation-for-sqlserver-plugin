package io.jenkins.plugins.Models;

public class AdditionalOptionsModel {

  private final String compareOptions, filterFile, transactionIsoLvl;

  public AdditionalOptionsModel(String compareOptions, String filterFile, String transactionIsoLvl){

    this.compareOptions = compareOptions;
    this.filterFile = filterFile;
    this.transactionIsoLvl=  transactionIsoLvl;
  }

  public String getCompareOptions() {

    return compareOptions;
  }

  public String getFilterFile(){

    return filterFile;
  }

  public String getTransactionIsoLvl() {

    return transactionIsoLvl;
  }
}