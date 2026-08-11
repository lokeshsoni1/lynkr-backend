package com.lynkr.backend.util;

public class UserAgentParser {

    public static String getDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("ipad") || ua.contains("tablet") || (ua.contains("android") && !ua.contains("mobile"))) {
            return "Tablet";
        }
        if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("ipod") || ua.contains("android")) {
            return "Mobile";
        }
        return "Desktop";
    }

    public static String getBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg") || ua.contains("edge")) {
            return "Edge";
        }
        if (ua.contains("opera") || ua.contains("opr")) {
            return "Opera";
        }
        if (ua.contains("chrome") && !ua.contains("chromium")) {
            return "Chrome";
        }
        if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        }
        if (ua.contains("firefox")) {
            return "Firefox";
        }
        return "Other";
    }
}
