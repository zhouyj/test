package com.feedss.base.util;

/**
 * 坐标换算工具类
 */
public class LonLatUtil {

    private final static double EARTH_RADIUS = 6378.137; // 地球半径
    static double DEF_PI = 3.14159265359; // PI
    static double DEF_2PI = 6.28318530712; // 2*PI
    static double DEF_PI180 = 0.01745329252; // PI/180.0
    static double DEF_R = 6370693.5; // radius of earth

    public static double getShortDistance(double lon1, double lat1, double lon2, double lat2) {
        double ew1, ns1, ew2, ns2;
        double dx, dy, dew;
        double distance;
        // 角度转换为弧度
        ew1 = lon1 * DEF_PI180;
        ns1 = lat1 * DEF_PI180;
        ew2 = lon2 * DEF_PI180;
        ns2 = lat2 * DEF_PI180;
        // 经度差
        dew = ew1 - ew2;
        // 若跨东经和西经180 度，进行调整
        if (dew > DEF_PI)
            dew = DEF_2PI - dew;
        else if (dew < -DEF_PI)
            dew = DEF_2PI + dew;
        dx = DEF_R * Math.cos(ns1) * dew; // 东西方向长度(在纬度圈上的投影长度)
        dy = DEF_R * (ns1 - ns2); // 南北方向长度(在经度圈上的投影长度)
        // 勾股定理求斜边长
        distance = Math.sqrt(dx * dx + dy * dy);
        return distance;
    }

    /**
     * 根据圆心、半径算出经纬度范围
     *
     * @param x 圆心经度
     * @param y 圆心纬度
     * @param r 半径（米）
     * @return double[4] 南侧经度，北侧经度，西侧纬度，东侧纬度
     */
    public static double[] getRange(double lon, double lat, int r) {
        double[] range = new double[4];
        // 角度转换为弧度
        double ns = lat * DEF_PI180;
        double sinNs = Math.sin(ns);
        double cosNs = Math.cos(ns);
        double cosTmp = Math.cos(r / DEF_R);
        // 经度的差值
        double lonDif = Math.acos((cosTmp - sinNs * sinNs) / (cosNs * cosNs)) / DEF_PI180;
        // 保存经度
        range[0] = lon - lonDif;
        range[1] = lon + lonDif;
        double m = 0 - 2 * cosTmp * sinNs;
        double n = cosTmp * cosTmp - cosNs * cosNs;
        double o1 = (0 - m - Math.sqrt(m * m - 4 * (n))) / 2;
        double o2 = (0 - m + Math.sqrt(m * m - 4 * (n))) / 2;
        // 纬度
        double lat1 = 180 / DEF_PI * Math.asin(o1);
        double lat2 = 180 / DEF_PI * Math.asin(o2);
        // 保存
        range[2] = lat1;
        range[3] = lat2;
        return range;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0; // 计算弧长
    }

    // lng1 第一个点经度，lat1第一点纬度；lng2第二点经度，lat2第二点纬度
    public static double getShortestDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        // s = s * 1000; //换算成米
        return s; // 得到千米数

    }
    


    /**
     * 获取距离 返回<code>String<code>类型。
     * <pre>
     *    如距离小于1km返回 m ： 231m,大于就返回1.2km
     * </pre>
     * @param lon1
     * @param lat1
     * @param lon2
     * @param lat2
     * @return
     */
    public static String getDistance(double lon1, double lat1, double lon2, double lat2){
    	double distance = getShortDistance(lon1, lat1, lon2, lat2);
    	if(distance < 1000){
    		return distance+"m";
    	}
    	distance = distance/1000;
    	return distance+"km";
    }
}
