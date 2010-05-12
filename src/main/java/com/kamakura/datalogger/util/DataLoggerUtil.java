/**
 * DataLoggerUtil.java
 *
 * Copyright 2010-2010 Daniel de Aguiar Kamakura
 */
package com.kamakura.datalogger.util;

public class DataLoggerUtil {
	public static char calculateLRC(String str) {
		char lrc = 0;
		char[] charArray = str.toCharArray();
		for(char c : charArray) {
			lrc = (char)(lrc ^ c);
		}
		return lrc;
	}

	public static String addLRC(String str) {
		return str + DataLoggerUtil.calculateLRC(str);
	}
}
