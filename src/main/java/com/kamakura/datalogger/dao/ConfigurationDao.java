package com.kamakura.datalogger.dao;

import com.kamakura.datalogger.model.DataLoggerConfiguration;

public interface ConfigurationDao {

	public static final String START_CONFIGURE_SIGNAL = "SCS";

	public static final String END_CONFIGURE_SIGNAL = "ECS";

	public static final String ACK_CONFIGURE_SIGNAL = "ACS";

	public void writeConfiguration(DataLoggerConfiguration dataLoggerConfiguration);
}
