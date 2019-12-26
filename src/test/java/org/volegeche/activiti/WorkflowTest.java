package org.volegeche.activiti;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
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
    /**
     * 部署流程模版 创建流程定义
     */
    @Test
    public void deployProcess() throws IOException {
        String modelId = "7503";
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
     * 启动流程 创建流程实例
     */
    @Test
    public void startProcess(){
        String processKey = "process_test";
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
        String taskId = "22505";
        taskService.complete(taskId);
    }

    /**
     * 任务回退
     */
    @Test
    public void fallbackTask(){
        String taskId = "";
        //taskService.
    }

}
