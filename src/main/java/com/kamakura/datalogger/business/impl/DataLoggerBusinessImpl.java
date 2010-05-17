package com.kamakura.datalogger.business.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kamakura.datalogger.business.DataLoggerBusiness;
import com.kamakura.datalogger.dao.ConfigurationDao;
import com.kamakura.datalogger.dao.DataLogDao;
import com.kamakura.datalogger.model.DataLog;
import com.kamakura.datalogger.model.DataLoggerConfiguration;

@Service
public class DataLoggerBusinessImpl implements DataLoggerBusiness {

	@Autowired
	private ConfigurationDao configurationDao;
	
	@Autowired
	private DataLogDao dataLogDao;
	
	@Override
	public DataLog readDataLog() {
		return dataLogDao.readDataLog();
	}

	@Override
	public void writeConfiguration(DataLoggerConfiguration dataLoggerConfiguration) {
		configurationDao.writeConfiguration(dataLoggerConfiguration);
	}

}
