package lch.lpiechart.utils;

import android.graphics.Color;

/**
 * 取色工具
 *
 * @author liyong
 * @date 2017/2/14 10:06
 */
@SuppressWarnings("ALL")
public class ColorUtils {

    public static final int[] LINE_CHART_MARKER_COLORS = {
            rgb("#20a0ff"), rgb("#f7b52c"), rgb("#ff4949")
    };
    public static final int[] MAIN_COLORS = {
            rgb("#59b7ff"), rgb("#a4b8ff"), rgb("#f2b2fc"), rgb("#ffa88c"), rgb("#e5ed9a"), rgb("#aae6aa")
    };

    public static final String[] SECORDARY_COLORS = {
            "#59b7ff", "#7fb8ff", "#a4b8ff", "#cbb5fe", "#f1b2fb", "#f9adc4", "#ffa88c", "#f2cb93", "#e5ed9a", "#c8eaa2", "#aae6aa", "#82cfd4"
    };

    /**
     * Converts the given hex-color-string to rgb.
     *
     * @param hex
     * @return
     */
    public static int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
    }


    /**
     * Converts the given hex-color-string to rgb.
     *
     * @param hex
     * @return
     */
    public static int createRgb(String hex, int value) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        r = (r + value > 255) ? 255 : (r + value);
        int g = (color >> 8) & 0xFF;
        g = (g + value > 255) ? 255 : (g + value);
        int b = (color >> 0) & 0xFF;
        b = (b + value > 255) ? 255 : (b + value);
        return Color.rgb(r, g, b);
    }


    /**
     * 获取颜色
     *
     * @param size
     * @param index
     */
    public static int getColor(int size, int index) {
        if (size <= 6) {
            return MAIN_COLORS[index];
        } else if (size <= 12) {
            return rgb(SECORDARY_COLORS[index]);
        } else {
            if (index < 12) {
                return rgb(SECORDARY_COLORS[index]);
            } else {
                return createRgb(SECORDARY_COLORS[index % 12], index);
            }
        }
    }
}
