package com.br.azevedo.conversor.date;

import java.time.format.DateTimeFormatter;

public final class DateFormat {

	/** Pattern: dd/MM/yyyy */
	public static final String DATE_FORMAT = "dd/MM/yyyy";

	/** Pattern: dd-MM-yyyy */
	public static final String DATE_FORMAT_DDMMYYYY = "dd-MM-yyyy";
	
	/** Pattern: MM/dd/yyyy */
	public static final String DATE_FORMAT_US = "MM/dd/yyyy";
	
	/** Pattern: dd/MM/yyyy HH:mm:ss */
	public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
	
	/** Pattern: yyyy-MM-dd'T'HH:mm:ss.SSSXX */
	public static final String LOCALDATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXX";
	
	/** Pattern: dd/MM/yyyy */
	public static final DateTimeFormatter FORMATER_DDMMYYYY = DateTimeFormatter.ofPattern(DATE_FORMAT);
    
	/** Pattern: yyyy-MM-dd */
	public static final DateTimeFormatter FORMATER_YYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	/** Pattern: yyyy-MM-dd HH:mm:ss */
	public static final DateTimeFormatter FORMATER_YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	/** Pattern: dd/MM/yyyy HH:mm:ss */
	public static final DateTimeFormatter FORMATER_DDMMYYYYHHMMSS = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
	
	/** Pattern: YYYY-MM-dd'T'HH:mm:ss.SSS */
	public static final DateTimeFormatter ANO_MES_DIA_T_HORA = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss.SSS");

	/** Pattern: yyyyMMddHHmmss */
	public static final DateTimeFormatter FORMATER_YYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	/** Pattern: yyyyMMddHHmmssSSS */
	public static final DateTimeFormatter FORMATER_YYYYMMDDHHMMSSSSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	/** Pattern: yyMMddHHmmss */
	public static final DateTimeFormatter FORMATER_YYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyMMddHHmmss");
	
	private DateFormat() {
		super();
	}
}