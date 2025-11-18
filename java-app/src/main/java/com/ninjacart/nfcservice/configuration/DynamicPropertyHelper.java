package com.ninjacart.nfcservice.configuration;

import com.netflix.config.DynamicPropertyFactory;
import lombok.extern.slf4j.Slf4j;

/** config helper for config files */
@Slf4j
public class DynamicPropertyHelper {
  public static final String getValue(String key, String defaultValue) {
    return DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue).getValue();
  }
}
