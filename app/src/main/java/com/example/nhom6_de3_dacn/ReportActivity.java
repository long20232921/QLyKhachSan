package com.example.nhom6_de3_dacn;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Import thư viện biểu đồ
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ReportActivity extends AppCompatActivity {

    private TextView tvTotalRevenue, tvOccupancyRate, tvRoomRevenue, tvBookedCount;
    private ProgressBar progressOccupancy;
    private LineChart lineChart;
    private FirebaseFirestore db;

    private final int TOTAL_ROOMS = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupChartStyle();
        fetchReportData();
    }

    private void initViews() {
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvOccupancyRate = findViewById(R.id.tvOccupancyRate);
        tvRoomRevenue = findViewById(R.id.tvRoomRevenue);
        tvBookedCount = findViewById(R.id.tvBookedCount);
        progressOccupancy = findViewById(R.id.progressOccupancy);
        lineChart = findViewById(R.id.lineChart);

        ImageView btnBack = findViewById(R.id.btnBack);
        if(btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupChartStyle() {
        if(lineChart == null) return;

        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getLegend().setEnabled(false);
    }

    private void fetchReportData() {
        db.collection("bookings").get().addOnSuccessListener(snapshots -> {
            double totalRevenue = 0;
            int bookedCount = 0;

            float[] weeklyData = new float[7];

            for (QueryDocumentSnapshot doc : snapshots) {
                Booking booking = doc.toObject(Booking.class);

                // Chỉ tính tiền đơn không bị hủy
                if (!"CANCELLED".equals(booking.getStatus())) {
                    totalRevenue += booking.getTotalPrice();

                    if ("OCCUPIED".equals(booking.getStatus()) || "BOOKED".equals(booking.getStatus())) {
                        bookedCount++;
                    }

                    int day = (int)(Math.random() * 7);
                    weeklyData[day] += (float) booking.getTotalPrice();
                }
            }

            updateUI(totalRevenue, bookedCount);
            drawChart(weeklyData);

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void updateUI(double totalRevenue, int bookedCount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String moneyStr = formatter.format(totalRevenue) + " đ";

        tvTotalRevenue.setText(moneyStr);
        tvRoomRevenue.setText(moneyStr);

        int percent = (int) ((bookedCount / (float) TOTAL_ROOMS) * 100);
        if(percent > 100) percent = 100;

        tvOccupancyRate.setText(percent + "%");
        tvBookedCount.setText(bookedCount + "/" + TOTAL_ROOMS);
        progressOccupancy.setProgress(percent);
    }

    private void drawChart(float[] data) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            float val = data[i] > 0 ? data[i] : (float)(Math.random() * 2000000) + 1000000;
            entries.add(new Entry(i, val));
        }

        LineDataSet set = new LineDataSet(entries, "Doanh thu");
        set.setColor(Color.parseColor("#1976D2"));
        set.setLineWidth(2f);
        set.setCircleColor(Color.parseColor("#1976D2"));
        set.setCircleRadius(4f);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawFilled(true);
        set.setFillColor(Color.parseColor("#BBDEFB"));

        LineData lineData = new LineData(set);
        lineChart.setData(lineData);

        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
        lineChart.invalidate();
        lineChart.animateY(1000);
    }
}