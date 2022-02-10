package com.haibin.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        if ("创建请假单".equals(delegateTask.getName())
                && "create".equals(delegateTask.getEventName())){
            //指定任务的负责人
            delegateTask.setAssignee("张三-Listener");
        }
    }
}
