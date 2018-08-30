package com.hikvision.lohao.serviceEvent;

import org.apache.hadoop.yarn.event.AbstractEvent;

/**
 * @author: lohao
 * @date: 2018/7/31
 * @description:
 */
public class TaskEvent extends AbstractEvent<TaskEventType> {
    private  String taskId;

    public TaskEvent(String taskId, TaskEventType type) {
        super(type);
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }
}