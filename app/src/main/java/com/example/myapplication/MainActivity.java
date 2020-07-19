package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.utils.TaskPriority;
import com.example.myapplication.utils.TaskStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ITaskListener{

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String id;

    FloatingActionButton fab;
    private ProgressBar list_progressbar;

    private boolean isFinishDateSet = false;
    Calendar calendar = Calendar.getInstance();
    int year,month,day;
    Date endDate;

    private static final String TAG = "MainActivity__";

    List<TaskModel> taskModelList = new ArrayList<>();
    TaskAdapter taskAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    Map<Integer, TaskPriority> priorityMap = new HashMap<>();

    private String uId, uTask, uDescription, uTaskPriority, uEndDate, uTaskStatus, date;

    protected void init(){
        mAuth = FirebaseAuth.getInstance();

        list_progressbar = findViewById(R.id.list_progressbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = UUID.randomUUID().toString();
                taskDialog(0, MainActivity.this);
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        priorityMap.put(R.id.high,TaskPriority.HIGH);
        priorityMap.put(R.id.med,TaskPriority.MEDIUM);
        priorityMap.put(R.id.low,TaskPriority.LOW);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        init();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy < 0){
                    fab.show();
                }else if(dy > 0){
                    fab.hide();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.log_out:
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, SignupActivity.class));
                finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getTasks();

        if (taskAdapter.openDialog){
            taskDialog(1, MainActivity.this);
            taskAdapter.openDialog = false;
        }
    }

    public void taskDialog(final int form, Context context) {    // form 0 = add new task , 1 = edit old task
        final Dialog dialog = TaskDialog.getCustomDialog(context, R.layout.task_dialog);
        final LinearLayout llDate = dialog.findViewById(R.id.llDate);
        TextView title = dialog.findViewById(R.id.title);
        final EditText taskEd = dialog.findViewById(R.id.taskEd);
        final EditText dscEd = dialog.findViewById(R.id.dscEd);
        final RadioGroup rgP = dialog.findViewById(R.id.rgPriority);
        final TextView finishDateText = dialog.findViewById(R.id.finish_date_text);
        final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        final Button doneBtn = dialog.findViewById(R.id.doneBtn);

        title.setText(form==0?"Add Task":"Edit Task");

        if (form == 1) {
            Intent bundle = getIntent();
            if (bundle != null) {
                uId = bundle.getStringExtra("uId");
                uTask = bundle.getStringExtra("uTask");
                uDescription = bundle.getStringExtra("uDescription");
                uTaskPriority = bundle.getStringExtra("uTaskPriority");
                uTaskStatus = bundle.getStringExtra("uTaskStatus");
                uEndDate = bundle.getStringExtra("uEndDate");

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                date = formatter.format(Date.parse(uEndDate));

                taskEd.setText(uTask);
                dscEd.setText(uDescription);
                finishDateText.setText(date);

                if (uTaskPriority.equals(TaskPriority.HIGH.toString())) {
                    rgP.check(R.id.high);
                }
                if (uTaskPriority.equals(TaskPriority.MEDIUM.toString())) {
                    rgP.check(R.id.med);
                }
                if (uTaskPriority.equals(TaskPriority.LOW.toString())) {
                    rgP.check(R.id.low);
                }

            }
        }

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskModel taskModel = new TaskModel();
                if(form ==  0){

                    String task, dsc;
                    task = taskEd.getText().toString().trim();
                    dsc = dscEd.getText().toString().trim();
                    taskEd.setError(null);
                    if (taskEd.getText().toString().isEmpty()) {
                        taskEd.setError("Required");
                        return;
                    }
                    taskModel.setId(id);
                    taskModel.setTask(task);
                    taskModel.setDsc(dsc);
                    taskModel.setTaskPriority(rgP.getCheckedRadioButtonId() == -1 ? TaskPriority.NONE : priorityMap.get(rgP.getCheckedRadioButtonId()));
                    taskModel.setTaskStatus(TaskStatus.PENDING);
                    if (isFinishDateSet) {
                        taskModel.setEndDate(endDate);
                    } else {
                        Toast.makeText(MainActivity.this, "Please select end date", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addTaskTOFire(taskModel, doneBtn, progressBar, dialog);

                }else if(form == 1){
                    if (form == 1) {
                        Bundle bundle = getIntent().getExtras();
                        if (bundle != null) {

                            String updateId = uId;
                            String task = taskEd.getText().toString().trim();
                            String description = dscEd.getText().toString().trim();

                            taskModel.setId(updateId);
                            taskModel.setTask(task);
                            taskModel.setDsc(description);
                            taskModel.setTaskPriority(rgP.getCheckedRadioButtonId() == -1 ? TaskPriority.NONE : priorityMap.get(rgP.getCheckedRadioButtonId()));


                            if (isFinishDateSet) {
                                taskModel.setEndDate(endDate);
                                taskModel.setTaskStatus(TaskStatus.PENDING);
                            } else {
                                if (uTaskStatus.equals(TaskStatus.FAILED.toString())){
                                    Toast.makeText(MainActivity.this, "Please select an extended deadline", Toast.LENGTH_SHORT).show();
                                    return;
                                }else{
                                    Date uDate;
                                    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
                                    try {
                                        uDate = formatter.parse(uEndDate);
                                        taskModel.setEndDate(uDate);
                                        if (uTaskStatus.equals(TaskStatus.PENDING.toString())){
                                            taskModel.setTaskStatus(TaskStatus.PENDING);
                                        }
                                        if (uTaskStatus.equals(TaskStatus.COMPLETE.toString())){
                                            taskModel.setTaskStatus(TaskStatus.COMPLETE);
                                        }

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                            updateTask(taskModel, dialog);

                        }
                    }
                }
            }
        });


        llDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFinishDateSet = false;
                callDatepicker(finishDateText);
            }
        });
    }

    private void callDatepicker(final TextView finishDateText){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                finishDateText.setText(String.format("%d-%02d-%02d",year,month+1,dayOfMonth));
                String currentDateString = String.format("%02d/%02d/%d 23:59:59",month+1,dayOfMonth,year);
                SimpleDateFormat sd = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                try {
                    endDate  = sd.parse(currentDateString);
                    isFinishDateSet = true;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, year, month,day);
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    public void addTaskTOFire(final TaskModel taskModel, final Button button, final ProgressBar progressBar, final Dialog dialog) {
        button.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        db.collection("user/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+"task/")
                .document(taskModel.getId())
                .set(taskModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        button.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                        dialog.dismiss();
                        taskModelList.add(0, taskModel);
                        taskAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error adding document", e);
                        button.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    public void getTasks(){
        list_progressbar.setVisibility(View.VISIBLE);
        db.collection("user/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+"task/")
                .orderBy("createDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        taskModelList.clear();
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            TaskModel task = documentSnapshot.toObject(TaskModel.class);
                            task.setId(documentSnapshot.getId());
                            if(task.getTaskStatus() == TaskStatus.PENDING){
                                task.setTaskStatus(Calendar.getInstance().getTime().after(task.getEndDate()) ? TaskStatus.FAILED : TaskStatus.PENDING);
                                updateTaskStatus(task);
                            }
                            taskModelList.add(task);
                            list_progressbar.setVisibility(View.GONE);
                        }
                        taskAdapter = new TaskAdapter(MainActivity.this, taskModelList);
                        recyclerView.setAdapter(taskAdapter);
                        taskAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"error : "+e.getMessage());
                list_progressbar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateTaskStatus(final TaskModel taskModel){
        list_progressbar.setVisibility(View.VISIBLE);
        db.collection("user/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+"task/").document(taskModel.getId())
                .update("taskStatus",taskModel.getTaskStatus().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                        list_progressbar.setVisibility(View.GONE);
//                        getTasks();
                        taskAdapter.notifyDataSetChanged();
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        list_progressbar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onTaskStatusUpdate(TaskModel taskModel) {
        updateTaskStatus(taskModel);
    }

    public void updateTask(TaskModel taskModel, final Dialog dialog){
        db.collection("user/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+"task/")
                .document(uId)
                .update("task", taskModel.getTask(), "dsc", taskModel.getDsc(),
                        "taskPriority", taskModel.getTaskPriority().toString(), "endDate", taskModel.getEndDate(), "taskStatus", taskModel.getTaskStatus())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        getTasks();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


}
