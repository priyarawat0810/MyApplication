package com.example.myapplication;

import com.example.myapplication.utils.TaskPriority;
import com.example.myapplication.utils.TaskStatus;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class TaskModel {

    private String id;
    private String task;
    private String dsc;
    private @ServerTimestamp
    Date createDate;
    public Date endDate;
    private TaskPriority taskPriority;
    private TaskStatus taskStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;}

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDsc() {
        return dsc;
    }

    public void setDsc(String dsc) {
        this.dsc = dsc;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public TaskPriority getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(TaskPriority taskPriority) {
        this.taskPriority = taskPriority;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return "MyTask{" +
                "id='" + id + '\'' +
                ", task='" + task + '\'' +
                ", dsc='" + dsc + '\'' +
                ", createDate=" + createDate +
                ", endDate=" + endDate +
                ", taskPriority=" + taskPriority +
                ", taskStatus=" + taskStatus +
                '}';
    }

}
