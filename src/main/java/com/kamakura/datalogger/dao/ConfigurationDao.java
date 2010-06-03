package com.kamakura.datalogger.dao;

import com.kamakura.datalogger.model.DataLoggerConfiguration;

public interface ConfigurationDao {

	public static final String START_CONFIGURATION_SIGNAL = "SCS";

	public static final String END_CONFIGURATION_SIGNAL = "ECS";

	public static final String ACK_CONFIGURATION_SIGNAL = "ACS";

	public void writeConfiguration(DataLoggerConfiguration dataLoggerConfiguration);
}
