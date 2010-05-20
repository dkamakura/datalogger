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
	
	public static void main(String[] args) {
		char t = DataLoggerUtil.calculateLRC("1234567890123450010012.145.378.520091206140220091225120078.5");
		System.out.println((int)t);
	}
}
