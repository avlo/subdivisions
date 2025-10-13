package com.prosilion.reqclient.controller;

public interface ReqApiUiIF extends ApiUiIF {
  String getReqUiHtmlFile();

  default String getHtmlFile() {
    return getReqUiHtmlFile();
  }
}
