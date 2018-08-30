package com.hikvision.lohao.serviceEvent;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.service.Service;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.event.AsyncDispatcher;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.event.EventHandler;

/**
 * @author: lohao
 * @date: 2018/7/31
 * @description:
 */
public class SimpleMRAM extends CompositeService {
    private Dispatcher dispatcher; // 中央异步调度器
    private String jobID;
    private int taskNumber; // 该作业包含的任务数目
    private String[] taskIDs; // 该作业内部包含的所有任务
    public SimpleMRAM(String name, String jobID, int taskNumber) {
        super(name);
        this.jobID = jobID;
        this.taskNumber = taskNumber;
        taskIDs = new String[taskNumber];
        for(int i = 0; i < taskNumber; i++) {
            taskIDs[i] = new String(jobID + "_task_" + i);
        }
    }

    @Override
    public void serviceInit(final Configuration conf) throws Exception {
        dispatcher = new AsyncDispatcher();// 定义一个中央异步调度器
        // 分别注册 Job 和 Task 事件调度器
        dispatcher.register(JobEventType.class, new JobEventDispatcher());
        dispatcher.register(TaskEventType.class, new TaskEventDispatcher());
        addService((Service) dispatcher);
        super.serviceInit(conf);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    private class JobEventDispatcher implements EventHandler<JobEvent> {
        @Override
        public void handle(JobEvent event) {
            if(event.getType() == JobEventType.J_KILL) {
                System.out.println(Thread.currentThread() + "Receive JOB_KILL event, killing all the tasks");
                for(int i = 0; i < taskNumber; i++) {
                    dispatcher.getEventHandler().handle(new TaskEvent(taskIDs[i],
                            TaskEventType.T_KILL));
                }
            } else if(event.getType() == JobEventType.J_INIT) {
                System.out.println(Thread.currentThread() + "Receive JOB_INIT event, scheduling tasks");
                for(int i = 0; i < taskNumber; i++) {
                    dispatcher.getEventHandler().handle(new TaskEvent(taskIDs[i],
                            TaskEventType.T_SCHEDULE));
                }
            }
        }
    }

    private class TaskEventDispatcher implements EventHandler<TaskEvent> {
        @Override
        public void handle(TaskEvent event) {
            if(event.getType() == TaskEventType.T_KILL) {
                System.out.println(Thread.currentThread() + "Receive T_KILL event of task " + event.getTaskId());
            } else if(event.getType() == TaskEventType.T_SCHEDULE) {
                System.out.println(Thread.currentThread() + "Receive T_SCHEDULE event of task " + event.getTaskId());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String jobID = "job_20131215_12";
        SimpleMRAM appMaster = new SimpleMRAM("Simple MRAppMaster", jobID, 5);
        YarnConfiguration conf = new YarnConfiguration(new Configuration());
        appMaster.serviceInit(conf);
        appMaster.serviceStart();
        appMaster.getDispatcher().getEventHandler().handle(new JobEvent(jobID,
                JobEventType.J_KILL));
        appMaster.getDispatcher().getEventHandler().handle(new JobEvent(jobID,
                JobEventType.J_INIT));
        System.out.println("end");
    }
}