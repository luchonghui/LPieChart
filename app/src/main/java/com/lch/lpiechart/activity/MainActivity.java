package com.lch.lpiechart.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.lch.lpiechart.R;
import com.lch.lpiechart.bean.ChartData;
import com.lch.lpiechart.databinding.ActivityMainBinding;
import com.lch.lpiechart.view.LPiechartView;
import com.lch.lpiechart.view.PieSlideView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Random random;
    private int max, min;
    int length = 6;
    ChartData[] mChartData = new ChartData[6];
    private ActivityMainBinding mDataBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.inflate(LayoutInflater.from(MainActivity.this), R.layout.activity_main, null, false);
        setContentView(mDataBinding.getRoot());
        //是否展示饼状图间隔线
        mDataBinding.lpieChart.setShowDivid(true);
        //是否在饼状图上显示百分比
        mDataBinding.lpieChart.setShowProportion(false);
        max = 360;
        min = 0;
        random = new Random();
        for (int i = 0; i < length; i++) {
            ChartData pie = new ChartData();
            pie.data = Math.random() * 100;
            pie.name = "邻里超市" + (i + 1);
            mChartData[i] = pie;
        }
        mDataBinding.lpieChart.setDatas(mChartData);
        mDataBinding.slide.setSlideTabs(mChartData);
        mDataBinding.refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < length; i++) {
                    ChartData pie = new ChartData();
                    pie.data = Math.random() * 100;
                    pie.name = "邻里超市" + (i + 1);
                    mChartData[i] = pie;
                }
                mDataBinding.lpieChart.setDatas(mChartData);
                mDataBinding.slide.setSlideTabs(mChartData);
            }
        });
        mDataBinding.lpieChart.setOnItemChangedListener(new LPiechartView.OnItemChangedListener() {
            @Override
            public void onItemChanged(int index, ChartData value) {
                mDataBinding.slide.setSelectedIndex(index);
            }
        });

        mDataBinding.slide.setOnItemChangedListener(new PieSlideView.OnItemChangedListener() {
            @Override
            public void onItemChanged(int index) {
                mDataBinding.lpieChart.setOnItemSelectedIndex(index);
            }
        });
    }

}
