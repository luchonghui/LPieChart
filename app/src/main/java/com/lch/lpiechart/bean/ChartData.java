package com.lch.lpiechart.bean;

import java.io.Serializable;

/**
 * 图表数据
 *
 * @author luchonghui
 * @date 2016/11/25 8:55
 */
public class ChartData implements Serializable {

    /**
     * 初始角度
     */
    public float offsetArg = 0.0f;
    /**
     * 结束角度
     */
    public float endArg = 0.0f;
    /**
     * 中间角度
     */
    public float midArg = 0.0f;
    /**
     * 角度
     */
    public float arg = 0.0f;
    /**
     * 位置
     */
    public int index = 0;
    /**
     * 名称描述
     */
    public String name;
    /**
     * 数据
     */
    public double data;
    /**
     * 占比
     */
    public String percentage;

}
