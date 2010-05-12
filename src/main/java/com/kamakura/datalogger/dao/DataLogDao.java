package com.kamakura.datalogger.dao;

import com.kamakura.datalogger.model.DataLog;

public interface DataLogDao {

	public static final String START_DATA_SIGNAL = "SDS"; 

	public static final String END_DATA_SIGNAL = "EDS"; 

	public static final String ACK_DATA_SIGNAL = "ADS"; 

	public DataLog readDataLog();

}
