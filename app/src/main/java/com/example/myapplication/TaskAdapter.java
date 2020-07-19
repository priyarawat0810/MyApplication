package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.utils.TaskPriority;
import com.example.myapplication.utils.TaskStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    Context context;
    List<TaskModel> taskModelList;
    ITaskListener iTaskListener;

    Map<TaskPriority,Integer> priorityColorMap = new HashMap<>();
    Map<TaskStatus, Integer> statusColourMap = new HashMap<>();

    public static boolean openDialog = false;

    public TaskAdapter(Context context, List<TaskModel> taskModelList) {
        this.context = context;
        this.taskModelList = taskModelList;
        this.iTaskListener = (ITaskListener) context;

        priorityColorMap.put(TaskPriority.HIGH, R.color.priorityHigh);
        priorityColorMap.put(TaskPriority.MEDIUM,R.color.priorityMedium);
        priorityColorMap.put(TaskPriority.LOW,R.color.priorityLow);
        priorityColorMap.put(TaskPriority.NONE,R.color.priorityNone);

        statusColourMap.put(TaskStatus.FAILED, R.color.fail);
        statusColourMap.put(TaskStatus.PENDING, R.color.priorityNone);
        statusColourMap.put(TaskStatus.COMPLETE, R.color.complete);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.setOnClickListener(new ViewHolder.ClickListener() {

            @Override
            public void onItemLongClick(final View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                String[] options = {"Update", "Delete"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            openDialog = true;
                            String updateId = taskModelList.get(position).getId();
                            String updateTask = taskModelList.get(position).getTask();
                            String updateDescription = taskModelList.get(position).getDsc();
                            String updateEndDate = taskModelList.get(position).getEndDate().toString();
                            String updateTaskStatus = taskModelList.get(position).getTaskStatus().toString();
                            String updateTaskPriority = taskModelList.get(position).getTaskPriority().toString();

                            Intent intent = new Intent(view.getContext(), MainActivity.class);

                            intent.putExtra("uId", updateId);
                            intent.putExtra("uTask", updateTask);
                            intent.putExtra("uDescription", updateDescription);
                            intent.putExtra("uEndDate", updateEndDate);
                            intent.putExtra("uTaskStatus", updateTaskStatus);
                            intent.putExtra("uTaskPriority", updateTaskPriority);

                            context.startActivity(intent);

                        }
                        else if(which == 1){
                            deleteTask(taskModelList.get(position).getId(), position);
                        }
                    }
                }).create().show();
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final TaskModel taskModel = taskModelList.get(position);
        holder.t1.setText(taskModelList.get(position).getTask());
        holder.t2.setText(taskModelList.get(position).getDsc());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(Date.parse(taskModelList.get(position).getEndDate().toString()));
        holder.t3.setText(date);

        holder.priority.setBackgroundColor(context.getResources().getColor(priorityColorMap.get(taskModelList.get(position).getTaskPriority())));

        holder.checkBox.setChecked(taskModelList.get(position).getTaskStatus() == TaskStatus.COMPLETE);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkBox.isChecked()) {
                    taskModel.setTaskStatus(TaskStatus.COMPLETE);
                    iTaskListener.onTaskStatusUpdate(taskModel);
                }else if((!holder.checkBox.isChecked()) && (Calendar.getInstance().getTime().after(taskModel.getEndDate()))){
                    taskModel.setTaskStatus(TaskStatus.FAILED);
                    iTaskListener.onTaskStatusUpdate(taskModel);
                }else{
                    taskModel.setTaskStatus(TaskStatus.PENDING);
                    iTaskListener.onTaskStatusUpdate(taskModel);
                }
            }
        });



        holder.checkBox.setVisibility(taskModelList.get(position).getTaskStatus() == TaskStatus.FAILED ? View.GONE : View.VISIBLE);

        holder.taskStatus.setText(taskModelList.get(position).getTaskStatus().toString());

        holder.taskStatus.setTextColor(context.getResources().getColor(statusColourMap.get(taskModelList.get(position).getTaskStatus())));


    }

    @Override
    public int getItemCount() {
        return taskModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView t1, t2, t3, taskStatus;
        View priority;
        CheckBox checkBox;
        View mView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            t1 = itemView.findViewById(R.id.t1);
            t2 = itemView.findViewById(R.id.t2);
            t3 = itemView.findViewById(R.id.t3);
            taskStatus = itemView.findViewById(R.id.text_status);
            priority = itemView.findViewById(R.id.priority);
            checkBox = itemView.findViewById(R.id.checkbox);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mClickListener.onItemLongClick(v, getAdapterPosition());
                    return true;
                }
            });
        }

        private ViewHolder.ClickListener mClickListener;

        public interface ClickListener{
            void onItemLongClick(View v, int adapterPosition);
        }

        public void setOnClickListener(ViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }
    }

    private void deleteTask(String id, final int position){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+"task/").document(id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        taskModelList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, taskModelList.size());
                        Toast.makeText(context, "Task has been deleted", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
