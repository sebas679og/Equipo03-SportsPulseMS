package com.sportspulse.fixtures.config.properties;


public interface WebClientTimeoutProperties {
    String getBaseUrl();
    long getConnectTimeoutMs();
    long getReadTimeoutMs();
    long getWriteTimeoutMs();
    int getMemorySize();
}
