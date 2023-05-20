package com.haibin.activiti.command;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.behavior.MultiInstanceActivityBehavior;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

import java.util.*;

public class JumpTestCmd implements Command<Void> {


    private String taskId;  //当前任务ID
    private String targetNodeName;    //跳转的目标节点ID
    private String operationId;     //指定处理人

    public JumpTestCmd(String taskId, String targetNodeName,String operationId) {

        this.taskId = taskId;
        this.targetNodeName = targetNodeName;
        this.operationId = operationId;
    }

    @Override
    public Void execute(CommandContext commandContext) {

//        ActivitiEngineAgenda contextAgenda = commandContext.getAgenda();
//        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
//        TaskEntity taskEntity = taskEntityManager.findById(taskId);
//        //执行实例ID
//        String executionId = taskEntity.getExecutionId();
//        //流程定义ID
//        String processDefinitionId = taskEntity.getProcessDefinitionId();
//        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
//
//        //获取到执行实例对象，当前节点 【流程实例在执行过程中，一定是执行实例在运转，多实例情况下，指代的是三级执行实例】
//        ExecutionEntity executionEntity = executionEntityManager.findById(executionId);
//        //通过流程部署的ID获取整个流程对象
//        Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
//        Collection<FlowElement> flowElements = process.getFlowElements();
//        HistoryManager historyManager = commandContext.getHistoryManager();
//
//        //有了流程对象，可以获取到各个节点
//        FlowElement flowElement =null;
//       if(StringUtil.isNotEmpty(targetNodeName)){

//           for (FlowElement flow: flowElements) {

//               if(targetNodeName.equals(flow.getName())){

//                   flowElement = flow;
//                   break;
//               }
//           }
//       }
//
//        if (flowElement == null) {

//            throw new RuntimeException("target flow not exist !");
//        }
//        //todo 更新历史活动表 调整了顺序，在节点实例进行跳转之前记录一下历史活动表的数据
//        historyManager.recordActivityEnd(executionEntity,"移动节点");
//        executionEntity.setCurrentFlowElement(flowElement);
//        //使用contextAgenda让执行实例进行跳转
//        contextAgenda.planContinueProcessInCompensation(executionEntity);
//        //流程节点实例跳转之后，应该删除掉当前的任务
//        taskEntityManager.deleteTasksByProcessInstanceId(taskEntity.getProcessInstanceId(), "移动节点", true);
//        //更新历史任务表
//        historyManager.recordTaskEnd(taskId,"移动节点");
//        return null;

        //获取任务
        TaskEntityManager taskEntityManager = commandContext.getTaskEntityManager();
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        TaskEntity currentTask = taskEntityManager.findById(taskId);
        String processDefinitionId = currentTask.getProcessDefinitionId();
        String executionId = currentTask.getExecutionId();
        ExecutionEntity executionEntity = commandContext.getExecutionEntityManager().findById(executionId);

        //根据节点名称查询对应的节点
        org.activiti.bpmn.model.Process process = ProcessDefinitionUtil.getProcess(processDefinitionId);
        Collection<FlowElement> flowElements = process.getFlowElements();
        FlowElement flowElement =null;
        if(Objects.nonNull(targetNodeName)){

            for (FlowElement flow: flowElements) {

                if(targetNodeName.equals(flow.getName())){

                    flowElement = flow;
                    break;
                }
            }
        }

        if (flowElement == null) {

            throw new RuntimeException("target flow not exist !");
        }

        // 获取目标节点的来源连线
        FlowNode targetNode = (FlowNode) flowElement;
        List<SequenceFlow> flows = targetNode.getIncomingFlows();
        if (flows == null || flows.isEmpty()) {

            throw new ActivitiException("回退错误，目标节点没有来源连线");
        }


        Object behavior = ((Activity)executionEntity.getCurrentFlowElement()).getBehavior();
        if (behavior instanceof MultiInstanceActivityBehavior) {

            //对当前节点处理
            executionEntity = executionEntity.getParent();
            executionEntityManager.deleteChildExecutions(executionEntity, "报告更改跳转", false);
            executionEntity.setActive(true);
            executionEntity.setMultiInstanceRoot(false);
            executionEntityManager.update(executionEntity);
        }else{

            //不是多实例
            //对当前节点处理
            taskEntityManager.deleteTask(currentTask, "报告更改跳转", false, false);
            HistoryManager historyManager = commandContext.getHistoryManager();
            historyManager.recordTaskEnd(taskId, "报告更改跳转");
            historyManager.recordActivityEnd(executionEntity, "报告更改跳转");
        }

        //判断当前节点的行为类，即是否多实例任务类型，主要做两件事，删除2级实例下的三级实例，然后重新设置2级实例的信息执行更新操作
        behavior = ((Activity)flowElement).getBehavior();
        if (behavior instanceof MultiInstanceActivityBehavior) {

            //设置目标节点流转
            executionEntity.setCurrentFlowElement(flows.get(0));
            //设置下一步处理人
            Map<String,Object> map = new HashMap();
            map.put("multiAssignees", Arrays.asList(new String[]{
                    operationId}));
            executionEntity.setVariables(map);
            commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionEntity, true);
        }else {

            //不是多实例
            //设置目标节点流转
            executionEntity.setCurrentFlowElement(flows.get(0));
            //设置下一步处理人
            Map<String,Object> map = new HashMap();
            map.put("assignee", operationId);
            executionEntity.setVariables(map);
            commandContext.getAgenda().planTakeOutgoingSequenceFlowsOperation(executionEntity, true);
        }

        return null;


    }
}
