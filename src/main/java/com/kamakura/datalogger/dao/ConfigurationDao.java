package com.kamakura.datalogger.dao;

import com.kamakura.datalogger.model.DataLoggerConfiguration;

public interface ConfigurationDao {
	public void writeConfiguration(DataLoggerConfiguration dataLoggerConfiguration);
}
