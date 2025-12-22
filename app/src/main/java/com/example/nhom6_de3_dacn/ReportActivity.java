package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;

public class ReportActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TextView tvReportTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initView();
        setupToolbar();
        setupTabs();

        // Mặc định load tab Ngày
        loadReportByDay();
    }

    private void initView() {
        tabLayout = findViewById(R.id.tabLayout);
        tvReportTitle = findViewById(R.id.tvReportTitle);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTabs() {
        if (tabLayout.getTabCount() == 0) {
            tabLayout.addTab(tabLayout.newTab().setText("Ngày"));
            tabLayout.addTab(tabLayout.newTab().setText("Tháng"));
            tabLayout.addTab(tabLayout.newTab().setText("Năm"));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadReportByDay();
                        break;
                    case 1:
                        loadReportByMonth();
                        break;
                    case 2:
                        loadReportByYear();
                        break;
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // ================== LOAD DATA ==================

    private void loadReportByDay() {
        tvReportTitle.setText("📅 Báo cáo theo NGÀY");
        // Sau này: query Firebase theo ngày
    }

    private void loadReportByMonth() {
        tvReportTitle.setText("📆 Báo cáo theo THÁNG");
        // Sau này: query Firebase theo tháng
    }

    private void loadReportByYear() {
        tvReportTitle.setText("🗓️ Báo cáo theo NĂM");
        // Sau này: query Firebase theo năm
    }
}
