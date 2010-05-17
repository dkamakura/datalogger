package com.kamakura.datalogger.business;

import com.kamakura.datalogger.model.DataLog;
import com.kamakura.datalogger.model.DataLoggerConfiguration;

public interface DataLoggerBusiness {
	
	public DataLog readDataLog();
	
	public void writeConfiguration(DataLoggerConfiguration dataLoggerConfiguration);
	
}
