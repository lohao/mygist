package com.hikvision.lohao.serviceEvent;

import org.apache.hadoop.yarn.event.AbstractEvent;

/**
 * @author: lohao
 * @date: 2018/7/31
 * @description:
 */
public class JobEvent extends AbstractEvent<JobEventType>{
    private String jobId;

    public JobEvent(String jobId, JobEventType jobEventType) {
        super(jobEventType);
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }
}