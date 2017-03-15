package com.feedss.base.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具<br>
 * 
 * @author wangjingqing
 * @date 2016-07-31
 */
public class DateUtil {

	public static String FORMAT_STANDERD = "yyyy-MM-dd HH:mm:ss";

	public static String FORMAT_STANDERD_MINUTE = "yyyy-MM-dd HH:mm";
	// 年月日时分秒
	public static String FORMAT_STANDERD_CN = "yyyy年MM月dd HH时mm分ss秒";

	// 年月日
	public static String FORMAT_DAY_CN = "yyyy年MM月dd日";

	// 年月日
	public static String FORMAT_MINUTE_CN = "yyyy年MM月dd日 HH:mm";

	public static String FORMAT_MONTH_DAY_SIMPLE = "M月d日";

	/**
	 * 时间转化String<br>
	 * 
	 * @param date
	 * @param 格式
	 * @return
	 */
	public static String dateToString(Date date, String format) {
		if (date == null)
			return "";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(date);
	}

	/**
	 * String 转换Date<br>
	 * 
	 * @param String
	 * @param 格式
	 * @return Date
	 */
	public static Date stringToDate(String date, String format) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		Date time = new Date();
		try {
			time = simpleDateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	/**
	 * 获取当月天数<br>
	 * 
	 * @return
	 */
	public static int getDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取当前星期的开始时间<br>
	 * 
	 * @return
	 */
	public static Date getWeekStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		Date startTime = getDateBefore(date, day - 1);
		return getStartTime(startTime);
	}

	/**
	 * 获取当前月的开始时间<br>
	 * 
	 * @return
	 */
	public static Date getMonthStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		Date startTime = getDateBefore(date, day - 1);
		return getStartTime(startTime);
	}

	/**
	 * 获取当前年的开始时间<br>
	 * 
	 * @return
	 */
	public static Date getYearStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DAY_OF_YEAR);
		Date startTime = getDateBefore(date, day - 1);
		return getStartTime(startTime);
	}

	/**
	 * 获取之前多少天的当前时分秒时间<br>
	 * 
	 * @param date
	 * @param day
	 */
	public static Date getDateBefore(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - day);
		return calendar.getTime();
	}

	/**
	 * Date时间分钟移动<br>
	 * 
	 * @param date
	 * @param day
	 */
	public static Date getMinuteMove(Date date, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - minute);
		return calendar.getTime();
	}

	/**
	 * 得到当天的开始时间<br>
	 * 
	 * @param date
	 * @return Date
	 */
	public static Date getStartTime(Date date) {
		Calendar todayStart = Calendar.getInstance();
		todayStart.setTime(date);
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);
		return todayStart.getTime();
	}
	/**
	 * 得到当天的结束时间<br>
	 * 
	 * @param date
	 * @return Date
	 */
	public static Date getEndTime(Date date) {
		Calendar todayEnd = Calendar.getInstance();
		todayEnd.setTime(date);
		todayEnd.set(Calendar.HOUR_OF_DAY, 23);
		todayEnd.set(Calendar.MINUTE, 59);
		todayEnd.set(Calendar.SECOND, 59);
		return todayEnd.getTime();
	}

	/**
	 * 秒转化小时<br>
	 * 
	 * @param date
	 * @return Date
	 */
	public static String toHour(long second) {
		float hour = (float) second / (60 * 60);
		DecimalFormat simpleFormat = new DecimalFormat("######.##");
		return simpleFormat.format(hour);
	}

	/**
	 * 获取时长,返回格式 10:20:30<br>
	 * 
	 * @return
	 */
	public static String duration(Date startTiem, Date endTiem) {
		long time = (endTiem.getTime() - startTiem.getTime()) / 1000;// 秒
		int hour = (int) time / (60 * 60);// 小时
		int minute = (int) (time - hour * (60 * 60)) / 60;// 分钟
		int second = (int) time % 60;// 秒
		StringBuilder builder = new StringBuilder();
		if (hour > 0) {
			// if(hour < 10){
			// builder.append("0");
			// }
			builder.append(hour + "时");
		}
		if (minute > 0) {
			// if(minute < 10){
			// builder.append("0");
			// }
			builder.append(minute + "分");
		}
		if (second > 0) {
			// if(second < 10){
			// builder.append("0");
			// }
			builder.append(second + "秒");
		}

		return builder.toString();
	}
	
	/**
	 * 获取时长,返回格式 10:20:30<br>
	 * 
	 * @return
	 */
	public static String formatDuration(long time) {
		int hour = (int) time / (60 * 60);// 小时
		int minute = (int) (time - hour * (60 * 60)) / 60;// 分钟
		int second = (int) time % 60;// 秒
		StringBuilder builder = new StringBuilder();
		if (hour > 0) {
			// if(hour < 10){
			// builder.append("0");
			// }
			builder.append(hour + "时");
		}
		if (minute > 0) {
			// if(minute < 10){
			// builder.append("0");
			// }
			builder.append(minute + "分");
		}
		if (second > 0) {
			// if(second < 10){
			// builder.append("0");
			// }
			builder.append(second + "秒");
		}

		return builder.toString();
	}
}
