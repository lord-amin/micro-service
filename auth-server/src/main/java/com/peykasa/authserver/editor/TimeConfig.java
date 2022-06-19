package com.peykasa.authserver.editor;

/**
 * @author Yaser(amin) Sadeghi
 */
public class TimeConfig {

    private long millis = 0;
    private String text = "";

    public TimeConfig(String text) {
        this.text = text;
        millis = toMillis(text);
    }

    private long toMillis(String time)  {
        String t = validate(time);
        int d = fetch(t, "d");
        int h = fetch(t, "h");
        int m = fetch(t, "m([^s]|$)");
        int s = fetch(t, "s");
        int ms = fetch(t, "ms");
        return (d * 24 * 60 * 60 * 1000L) + (h * 60 * 60 * 1000L) + (m * 60 * 1000) + (s * 1000L) + ms;
    }

    private int fetch(String t, String unit) {
        String s = t.replaceAll("(^|.*\\s*)(\\d+)" + unit + ".*", "$2");
        if (!s.matches("\\d+"))
            return 0;
        return Integer.parseInt(s);
    }

    private String validate(String t) throws RuntimeException {
        String time = t + "";
        if (t == null || time.isEmpty() || time.trim().isEmpty() || !time.matches(".*\\d+.*")) {
            throw new IllegalArgumentException("invalid time string : " + time + ", valid-example=1h 1d 1m 1s 1ms");
        }
        time = time.trim().toLowerCase();
        if (count("\\d+d", time) > 1 || count("\\d+h", time) > 1 || count("\\d+m([^s]|$)", time) > 1 || count("\\d+s", time) > 1 || count("\\d+ms", time) > 1) {
            throw new IllegalArgumentException("invalid time string : " + time + ", valid-example=1h 1d 1m 1s 1ms");
        }
        if (time.contains("mss") || time.contains("sms") || time.contains("mms") || time.contains("msm")) {
            throw new IllegalArgumentException("invalid time string : " + time + ", valid-example=1h 1d 1m 1s 1ms");
        }
        for (String s : time.split("[a-z]+")) {
            try {
                int i = Integer.parseInt(s.trim());
                if (i < 1)
                    throw new IllegalArgumentException("invalid time string : " + time + ", valid-example=1h 1d 1m 1s 1ms");
            } catch (Exception e) {
                throw new IllegalArgumentException("invalid time string : " + time + ", valid-example=1h 1d 1m 1s 1ms");
            }
        }
        if (time.matches("\\d+")) {
            throw new IllegalArgumentException("invalid time string : " + time + ", valid-example=1h 1d 1m 1s 1ms");
        }
        if (!time.matches("(\\s*\\d*[dhms])+")) {
            throw new IllegalArgumentException("invalid time string : " + time + ", valid-example=1h 1d 1m 1s 1ms");
        }
        return time;
    }


    private int count(String regex, String input) {
        return input.split(regex, -1).length - 1;
    }

    public long getMillis() {
        return millis;
    }

    @Override
    public String toString() {
        return "TimeConfig{" +
                "text='" + text + '\'' +
                ", millis=" + millis +
                '}';
    }
}
