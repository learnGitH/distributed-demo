package com.haibin.activiti;

import com.haibin.activiti.utils.SecurityUtil;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.RepositoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ActSpringbootApplicationTests {

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void contextLoads(){
        System.out.println(taskRuntime);
    }

    /**
     * 查询流程定义
     */
    @Test
    public void test01(){
        securityUtil.logInAs("system");
        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,10));
        System.out.println("可用的流程定义数量：" + processDefinitionPage.getTotalItems());
        for (ProcessDefinition processDefinition : processDefinitionPage.getContent()){
            System.out.println("流程定义：" +   processDefinition);
        }
    }

    /**
     * 部署流程
     */
    @Test
    public void test03(){
        repositoryService.createDeployment()
                .addClasspathResource("processes/evection.bpmn")
                .addClasspathResource("processes/evection.png")
                .name("出差申请单")
                .deploy();
    }



    /**
     * 启动流程实例
     */
    @Test
    public void test04(){
        securityUtil.logInAs("system");
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey("my-evection")
                .build()
        );
        System.out.println("流程实例id:" + processInstance.getId());
    }

    /**
     * 任务查询、拾取及完成操作
     */
    @Test
    public void test05(){
        securityUtil.logInAs("jack");
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,10));
        if (tasks != null && tasks.getTotalItems() > 0){
            for (Task task : tasks.getContent()){
                //拾取任务
                taskRuntime.claim(TaskPayloadBuilder
                                .claim()
                                .withTaskId(task.getId())
                                .build()
                );
                System.out.println("任务：" + task);
                taskRuntime.complete(TaskPayloadBuilder
                                .complete()
                                .withTaskId(task.getId())
                                .build()
                );
            }
        }
        Page<Task> taskPage2 = taskRuntime.tasks(Pageable.of(0,10));
        if(taskPage2 .getTotalItems() > 0){
            System.out.println("任务：" + taskPage2.getContent());
        }

    }


}
