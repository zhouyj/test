package com.feedss.base.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 转换工具
 *
 */
public class ConvertUtil {

	private static final DecimalFormat simpleFormat = new DecimalFormat("######.00");

	/**
	 * 对象转换为Integer</br>
	 * <p>
	 * 判断对象是否为{@link Number} 类型对象，如果是转换Number。<br>
	 * obj如果为空，则根据isNulll true：返回null false：返回0
	 * </p>
	 * 
	 * @param obj
	 *            要转换对象
	 * @param isNull
	 *            为空时是否返回 null
	 */
	public static final Integer objectToInt(final Object obj, final boolean isNull) {
		if (obj != null) {
			if (obj instanceof Number) {
				return ((Number) obj).intValue();
			}
			return Integer.valueOf(obj.toString());
		}
		if (isNull) {
			return null;
		}
		return 0;
	}

	/**
	 * 对象转换为double
	 * 
	 * @param o
	 *            object
	 * @param isNull
	 *            为空是否返回 null
	 */
	public static final Double objectToDouble(final Object o, final boolean isNull) {
		if (o != null) {
			if (o instanceof Number) {
				return ((Number) o).doubleValue();
			}
			return Double.parseDouble(o.toString());
		}
		if (isNull) {
			return null;
		}
		return 0d;
	}

	/**
	 * 对象转换为string
	 * 
	 * @param obj
	 *            object
	 * @param isNull
	 *            是否为空 true：null false：""
	 * 
	 */
	public static final String objectToString(final Object obj, final boolean isNull) {
		if (obj != null) {
			return obj.toString();
		}
		if (isNull) {
			return null;
		}
		return "";
	}

	/**
	 * 把价格保留2位小数<br>
	 * 
	 * @param value
	 * @return
	 */
	public static String bigDecimalToString(BigDecimal value) {
		if (value == null) {
			return "";
		}
		return simpleFormat.format(value);
	}
}
