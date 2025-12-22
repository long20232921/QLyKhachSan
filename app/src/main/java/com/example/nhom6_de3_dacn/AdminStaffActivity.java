package com.example.nhom6_de3_dacn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminStaffActivity extends AppCompatActivity {

    private RecyclerView rvEmployees;
    private FloatingActionButton fabAdd;
    private EmployeeAdapter adapter;
    private List<Employee> employeeList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_staff);

        db = FirebaseFirestore.getInstance();

        rvEmployees = findViewById(R.id.rvEmployees);
        fabAdd = findViewById(R.id.fabAddEmployee);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupRecyclerView();
        loadEmployees();

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
    }

    private void setupRecyclerView() {
        adapter = new EmployeeAdapter(this, employeeList, new EmployeeAdapter.OnEmployeeAction() {
            @Override
            public void onEdit(Employee emp) {
                showAddEditDialog(emp);
            }

            @Override
            public void onDelete(Employee emp) {
                confirmDelete(emp);
            }
        });
        rvEmployees.setLayoutManager(new LinearLayoutManager(this));
        rvEmployees.setAdapter(adapter);
    }

    private void loadEmployees() {
        db.collection("employees").get().addOnSuccessListener(snapshots -> {
            employeeList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                try {
                    Employee emp = doc.toObject(Employee.class);
                    emp.setId(doc.getId());
                    employeeList.add(emp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void showAddEditDialog(Employee emp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_employee, null);
        builder.setView(view);

        EditText etImage = view.findViewById(R.id.etEmpImage);

        EditText etName = view.findViewById(R.id.etEmpName);
        EditText etPhone = view.findViewById(R.id.etEmpPhone);
        EditText etTask = view.findViewById(R.id.etEmpTask);
        Spinner spinnerRole = view.findViewById(R.id.spinnerRole);

        String[] roles = {"Lễ tân", "Buồng phòng", "Quản lý", "Kế toán", "Bảo vệ"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerRole.setAdapter(roleAdapter);

        if (emp != null) {
            etImage.setText(emp.getImage());
            etName.setText(emp.getName());
            etPhone.setText(emp.getPhone());
            etTask.setText(emp.getCurrentTask());

            for(int i=0; i<roles.length; i++) {
                if(roles[i].equals(emp.getRole())) {
                    spinnerRole.setSelection(i);
                    break;
                }
            }
            builder.setTitle("Cập nhật nhân viên");
        } else {
            builder.setTitle("Thêm nhân viên mới");
        }

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String imageLink = etImage.getText().toString().trim();

            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            String task = etTask.getText().toString();
            String role = spinnerRole.getSelectedItem().toString();

            if(name.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            Employee newEmp = new Employee(null, name, role, phone, task, imageLink);

            if (emp == null) {
                db.collection("employees").add(newEmp).addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Đã thêm thành công!", Toast.LENGTH_SHORT).show();
                    loadEmployees();
                });
            } else {
                db.collection("employees").document(emp.getId()).set(newEmp).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
                    loadEmployees();
                });
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void confirmDelete(Employee emp) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa nhân viên?")
                .setMessage("Bạn có chắc chắn muốn xóa " + emp.getName() + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("employees").document(emp.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                                loadEmployees();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}