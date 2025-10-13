package com.prosilion.reqclient.controller;

public interface EventApiUiIF extends ApiUiIF {
  String getEventUiHtmlFile();

  default String getHtmlFile() {
    return getEventUiHtmlFile();
  }
}
