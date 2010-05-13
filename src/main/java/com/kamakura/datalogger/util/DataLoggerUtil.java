/**
 * DataLoggerUtil.java
 *
 * Copyright 2010-2010 Daniel de Aguiar Kamakura
 */
package com.kamakura.datalogger.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
	
	public static void main(String[] args) {
		char t = DataLoggerUtil.calculateLRC("12345678901234500100123.12456.34789.562009120614020220091225120000");
		System.out.println((int)t);
	}
}
