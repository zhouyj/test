package com.feedss.manage.util.ueditor.define;

import java.util.HashMap;
import java.util.Map;

public class FileType {

	public static final String JPG = "JPG";
	public static final String JPEG = "JPEG";
	public static final String BMP = "BMP";
	public static final String PNG = "PNG";
	public static final String GIF = "GIF";

	public static final String MP4 = "MP4";
	public static final String MKV = "MKV";
	public static final String RMVB = "RMVB";
	public static final String SWF = "SWF";
	public static final String AVI = "AVI";
	public static final String FLV = "FLV";
	public static final String RM = "RM";
	public static final String MP3 = "MP3";

	private static final Map<String, String> types = new HashMap<String, String>(){{
		
		put( FileType.JPG, ".jpg" );
		put( FileType.JPEG, ".jpeg" );
		put( FileType.BMP, ".bmp" );
		put( FileType.PNG, ".png" );
		put( FileType.GIF, ".gif" );

		put( FileType.MP4, ".mp4" );
		put( FileType.MKV, ".mkv" );
		put( FileType.RMVB, ".rmvb" );
		put( FileType.SWF, ".swf" );
		put( FileType.AVI, ".avi" );
		put( FileType.FLV, ".flv" );
		put( FileType.RM, ".rm" );
		put( FileType.MP3, ".mp3" );
	}};

	private static final Map<String, String> videoTypes = new HashMap<String, String>(){{

	}};

	public static String getSuffix ( String key ) {
		return FileType.types.get( key );
	}
	
	/**
	 * 根据给定的文件名,获取其后缀信息
	 * @param filename
	 * @return
	 */
	public static String getSuffixByFilename ( String filename ) {
		filename = filename.substring( filename.lastIndexOf( "." ) ).toLowerCase();
		if(!types.containsValue(filename.toLowerCase()))
			return types.get(FileType.JPG);
		return filename;
		
	}
	
}
