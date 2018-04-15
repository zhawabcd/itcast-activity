package cn.itcast.ssh.service;


import cn.itcast.ssh.domain.LeaveBill;
import cn.itcast.ssh.web.form.WorkflowBean;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;

import java.io.InputStream;
import java.util.List;

public interface IWorkflowService {


	void saveDeploy(WorkflowBean workflowBean);

	List<Deployment> findDeploymentList();

	List<ProcessDefinition> findProcessDefinitionList();

	InputStream getImageInputStream(String deploymentId, String imageName);

	void deleteDeploymentById(String deploymentId);

	void saveStartProcess(WorkflowBean workflowBean);

	List<Task> findTaskListByAssigneeName(String assigneeName);

	String findFormKeyByTaskId(String taskId);

	LeaveBill findLeaveBillByTaskId(String taskId);

	List<String> findOutcomeListByTaskId(String taskId);
}
