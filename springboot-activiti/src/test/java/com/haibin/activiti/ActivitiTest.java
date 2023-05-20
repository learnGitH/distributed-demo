package com.haibin.activiti;

import com.haibin.activiti.command.JumpTestCmd;
import com.haibin.activiti.pojo.Evection;
import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActivitiTest {

    /**
     * 生成Activiti的相关的表结构
     */
    @Test
    public void test01(){
        //使用classpath下的activiti.cfg.xml中的配置来创建ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        System.out.println(engine);
    }

    /**
     * 实现文件的单个部署
     */
    @Test
    public void test02(){
        //1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        //2.获取RepositoryService进行部署操作
        RepositoryService service = engine.getRepositoryService();
        //3.使用RepositoryService进行部署操作
        Deployment deploy = service.createDeployment()
                .addClasspathResource("bpmn/evection.bpmn")     //添加bpmn资源
                .addClasspathResource("bpmn/evection.png")      //添加png资源
                .name("出差申请流程")
                .deploy();  //部署流程
        //4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());
    }

    /**
     * 启动一个流程实例
     */
    @Test
    public void start(){
        // 1.创建ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RuntimeService对象
        RuntimeService runtimeService = engine.getRuntimeService();
        // 3.根据流程定义的id启动流程
        String id= "evection";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(id);
        // 4.输出相关的流程实例信息
        System.out.println("流程定义的ID：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例的ID：" + processInstance.getId());
        System.out.println("当前活动的ID：" + processInstance.getActivityId());
    }
    /**
     * 流程任务的处理
     */
    @Test
    public void deal(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee("wangwu")
                .singleResult();
        // 完成任务
        taskService.complete(task.getId());
    }

    @Test
    public void jumpWorkflow(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
      //  Task task = taskService.createTaskQuery().taskId("1002").singleResult();
        processEngine.getManagementService().executeCommand(new JumpTestCmd("2505","总经理审批","haibin"));
    }


    @Test
    public void test03(){
        //1.获取ProcessEngine对象
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //2.获取RuntimeService对象
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //3.启动流程实例
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("evection","1001");
        //4.输出processInstance相关属性
        System.out.println("businessKey = " + instance.getBusinessKey());
    }

    /**
     * 先将新定义的流程部署到Activiti中数据库中
     */
    @Test
    public void test04(){
        //1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        //2.获取RepositoryService进行部署操作
        RepositoryService service = engine.getRepositoryService();
        //3.使用RepositoryService进行部署操作
        Deployment deploy = service.createDeployment()
                .addClasspathResource("bpmn/evection-variable.bpmn")     //添加bpmn资源
                .addClasspathResource("bpmn/evection-variable.png")      //添加png资源
                .name("出差申请流程-流程变量")
                .deploy();
        //4.输出流程部署的信息
        System.out.println("流程部署id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());
    }

    /**
     * 创建一个流程实例 给流程定义中的UEL表达式赋值
     */
    @Test
    public void test05(){
        //获取流程引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 获取RuntimeService对象
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //设置assignee的取值
        Map<String,Object> map = new HashMap<>();
        map.put("assignee0","张三");
        map.put("assignee1","李四");
        map.put("assignee2","王五");
        map.put("assignee3","赵财务");
        //创建流程实例
        runtimeService.startProcessInstanceByKey("evection-uel",map);
    }

    /**
     * 创建一个流程实例 给流程定义中的UEL表达式赋值
     */
    @Test
    public void test06(){
        //获取流程引擎
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        //获取RuntimeService对象
        RuntimeService runtimeService = engine.getRuntimeService();
        //创建流程实例
        runtimeService.startProcessInstanceByKey("evection-listener");
    }

    public static ProcessEngine getProcessEngine(){
        return ProcessEngines.getDefaultProcessEngine();
    }

    /**
     * 查询当前个人待执行的任务
     */
    @Test
    public void findPersonalTaskList(){
        //流程定义key
        String processDefinitionKey = "evection-uel";
        //任务负责人
        String assignee = "张三";
        //获取TaskService
        TaskService taskService = getProcessEngine().getTaskService();
        List<Task> taskList = taskService.createTaskQuery()
                .processDefinitionKey(processDefinitionKey)
                .includeProcessVariables()
                .taskAssignee(assignee)
                .list();
        for (Task task : taskList){
            System.out.println("----------------------------");
            System.out.println("流程实例id： " + task.getProcessInstanceId());
            System.out.println("任务id： " + task.getId());
            System.out.println("任务负责人： " + task.getAssignee());
            System.out.println("任务名称： " + task.getName());
        }
    }

    @Test
    public void findProcessInstance(){
        //获取processEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //获取TaskService
        TaskService taskService = processEngine.getTaskService();
        //获取RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //查询流程定义的对象
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee("张三")
                .singleResult();
        //使用task对象获取实例id
        String processInstanceId = task.getProcessInstanceId();
        //使用实例id,获取流程实例对象
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        //使用processInstance,得到businessKey
        String businessKey = processInstance.getBusinessKey();
        System.out.println("businessKey==" + businessKey);
    }

    /**
     * 完成任务，判断当前用户是否有权限
     */
    @Test
    public void completTask(){
        //任务id
        String taskId = "15005";
        //任务负责人
        String assignee = "张三";
        //获取processEngine
        ProcessEngine engine = getProcessEngine();
        //创建TaskService
        TaskService taskService = engine.getTaskService();
        // 完成任务前，需要校验该负责人可以完成当前任务
        // 校验方法：
        // 根据任务id和任务负责人查询当前任务，如果查到该用户有权限，就完成
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskAssignee(assignee)
                .singleResult();
        if (Objects.nonNull(task)){
            taskService.complete(taskId);
            System.out.println("完成任务");
        }
    }

    /**
     * 启动流程实例，设置l流程变量
     */
    @Test
    public void test07(){
        ProcessEngine engine = getProcessEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        //流程定义Key
        String key = "evection-variable";
        //创建变量集合
        Map<String,Object> variables = new HashMap<>();
        //创建出差对象POJO
        Evection evection = new Evection();
        //设置出差天数
        evection.setNum(4d);
        //定义流程变量到集合中
        variables.put("evection",evection);
        // 设置assignee的取值
        variables.put("assignee0","张三1");
        variables.put("assignee1","李四1");
        variables.put("assignee2","王五1");
        variables.put("assignee3","赵财务1");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key,variables);
        //输出信息
        System.out.println("获取流程实例名称："+processInstance.getName());
        System.out.println("流程定义ID:"+processInstance.getRootProcessInstanceId());
    }

    /**
     * 完成任务
     */
    @Test
    public void test08(){
        String key = "evection-variable";
        String assignee = "李四1";
        ProcessEngine engine = getProcessEngine();
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(key)
                .taskAssignee(assignee)
                .singleResult();
        if (task != null){
            taskService.complete(task.getId());
            System.out.println("任务执行完成。。。");
        }
    }

}
