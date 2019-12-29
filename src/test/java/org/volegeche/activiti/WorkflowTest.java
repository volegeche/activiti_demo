package org.volegeche.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author: lgyu6
 * @date: 2019-12-26 21:38
 * project:activiti
 */

@SpringBootTest
public class WorkflowTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowTest.class);

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;
    /**
     * 部署流程模版 创建流程定义
     */
    @Test
    public void deployProcess() throws IOException {
        String modelId = "20";
        Model model = repositoryService.getModel(modelId);
        byte[] source = repositoryService.getModelEditorSource(modelId);
        JsonNode modelNode = new ObjectMapper().readTree(source);
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        String processName = model.getName();
        String sourceName = processName + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(processName)
                .addBytes(sourceName,bpmnBytes)
                .deploy();
        LOGGER.info("部署ID:{}",deployment.getId());
    }


    /**
     * 下载流程文件
     */
    @Test
    public void downloadBpmn() throws IOException {
        String processId = "leaveflowProcess:6:97504";
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processId)
                .singleResult();
        String resourceName = processDefinition.getResourceName();
        String deploymentId = processDefinition.getDeploymentId();
        InputStream in = repositoryService.getResourceAsStream(deploymentId,resourceName);
        File bpmn = new File("./file/"+resourceName);
        FileOutputStream out = new FileOutputStream(bpmn);
        byte[] bytes = new byte[1024];
        int count = 0;
        while ((count = in.read(bytes)) != -1){
            out.write(bytes,0,count);
        }
        out.close();
        in.close();

    }

    /**
     * 下载流程文件
     */
    @Test
    public void downloadPng() throws IOException {
        String processId = "leaveflowProcess:1:2504";
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processId)
                .singleResult();
        String resourceName = processDefinition.getDiagramResourceName();
        String deploymentId = processDefinition.getDeploymentId();
        InputStream in = repositoryService.getResourceAsStream(deploymentId,resourceName);
        File png = new File("./file/"+resourceName);
        FileOutputStream out = new FileOutputStream(png);
        byte[] bytes = new byte[1024];
        int count = 0;
        while ((count = in.read(bytes)) != -1){
            out.write(bytes,0,count);
        }
        out.close();
        in.close();

    }

    @Test
    public void deploy(){
        InputStream bpmn = this.getClass().getResourceAsStream("/leaveflowModel.bpmn20.xml");
        InputStream png = this.getClass().getResourceAsStream("/leaveflowModel.leaveflowProcess.png");
        Deployment deployment = repositoryService.createDeployment()
                .name("leaveflowBpmn")
                .addInputStream("leaveflowModel.bpmn20.xml",bpmn)
                .addInputStream("leaveflowModel.leaveflowProcess.png",png)
                .deploy();
        LOGGER.info("部署ID:{}",deployment.getId());
    }

    /**
     * 删除部署
     */
    @Test
    public void deletePeloy(){
        String deploy = "30004";
        repositoryService.deleteDeployment(deploy,true);
    }


    /**
     * 删除流程实例
     */
    @Test
    public void deleteProcessInst(){
        String processInstId = "67501";
        runtimeService.deleteProcessInstance(processInstId,"创建多了");
    }

    /**
     * 启动流程 创建流程实例
     */
    @Test
    public void startProcess(){
        String processKey = "leaveflowProcess";
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionKey(processKey)
                .start();
        LOGGER.info("流程实例ID：{}，流程定义ID：{}",processInstance.getId(),processInstance.getProcessDefinitionId());
    }


    /**
     * 查询某人任务
     */
    @Test
    public void queryTask(){
        String userId = "张三";
        List<Task> taskList = taskService.createTaskQuery()
                .taskCandidateOrAssigned(userId)
                .orderByTaskCreateTime().asc()
                .list();
        taskList.forEach((item)->{
            System.out.println(item);
        });
    }

    /**
     * 提交任务
     */
    @Test
    public void commitTask(){
        String taskId = "125004";
        //taskService.removeVariable(taskId,"comment");
        taskService.setVariable(taskId,"comment","不同意");
        //taskService.setVariableLocal(taskId,"comment","不同意");
        taskService.complete(taskId);
    }

    /**
     * 提交任务
     */
    @Test
    public void commitTaskVariable(){
        String taskId = "15002";
        taskService.complete(taskId);
    }

    /**
     * 增加一个变量
     */
    @Test
    public void addVariable(){
        String taskId = "15002";
        taskService.setVariable(taskId,"message","这就是个变量");
    }

    /**
     * 增加一个local变量
     */
    @Test
    public void addLocalVariable(){
        String taskId = "15002";
        taskService.setVariableLocal(taskId,"localMessage","这就是个local变量");
    }

    /**
     * 任务审批意见
     */
    @Test
    public void addComment(){
       String taskId = "15002";
       taskService.addComment(taskId,null,"同意");
       Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
       taskService.addComment(taskId,task.getProcessInstanceId(),"流程实例同意");
    }

    /**
     * 查询历史审批意见
     */
    @Test
    public void queryComment(){
        String taskId = "15002";
        List<Comment> commentList = taskService.getTaskComments(taskId);
        commentList.forEach((o)->{
            System.out.println("taskId: "+o.getTaskId()+" commentId: "+o.getId()+" message: "+o.getFullMessage());
        });
        System.out.println("---------------------------");
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        commentList = taskService.getProcessInstanceComments(task.getProcessInstanceId());
        commentList.forEach((o)->{
            System.out.println("taskId: "+o.getTaskId()+" commentId: "+o.getId()+" message: "+o.getFullMessage());
        });
    }






}
