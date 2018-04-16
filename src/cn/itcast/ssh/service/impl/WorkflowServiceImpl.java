package cn.itcast.ssh.service.impl;

import cn.itcast.ssh.dao.ILeaveBillDao;
import cn.itcast.ssh.domain.LeaveBill;
import cn.itcast.ssh.service.IWorkflowService;
import cn.itcast.ssh.utils.SessionContext;
import cn.itcast.ssh.web.form.WorkflowBean;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipInputStream;

public class WorkflowServiceImpl implements IWorkflowService {
	/**
	 * 请假申请Dao
	 */
	private ILeaveBillDao leaveBillDao;

	private RepositoryService repositoryService;

	private RuntimeService runtimeService;

	private TaskService taskService;

	private FormService formService;

	private HistoryService historyService;

	public void setLeaveBillDao(ILeaveBillDao leaveBillDao) {
		this.leaveBillDao = leaveBillDao;
	}

	public void setHistoryService(HistoryService historyService) {
		this.historyService = historyService;
	}

	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	@Override
	public void saveDeploy(WorkflowBean workflowBean) {
		try {
			ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(workflowBean.getFile()));
			repositoryService.createDeployment()
					.name(workflowBean.getFilename())
					.addZipInputStream(zipInputStream)
					.deploy();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Deployment> findDeploymentList() {
		List<Deployment> deploymentList = repositoryService.createDeploymentQuery()
				.orderByDeploymenTime().asc()
				.list();
		return deploymentList;
	}

	@Override
	public List<ProcessDefinition> findProcessDefinitionList() {
		List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
				.orderByProcessDefinitionVersion().asc()
				.list();
		return processDefinitionList;
	}

	@Override
	public InputStream getImageInputStream(String deploymentId, String imageName) {
		return repositoryService.getResourceAsStream(deploymentId, imageName);
	}

	@Override
	public void deleteDeploymentById(String deploymentId) {
		repositoryService.deleteDeployment(deploymentId, true);
	}

	@Override
	public void saveStartProcess(WorkflowBean workflowBean) {
		LeaveBill leaveBill = leaveBillDao.findById(workflowBean.getId());
		leaveBill.setState(1);

		String key = leaveBill.getClass().getSimpleName();
		Map<String, Object> variables = new HashMap<>();
		variables.put("userID", SessionContext.get().getName());
		String businessKey = key + "#" + leaveBill.getId();
		variables.put("objID", businessKey);
		runtimeService.startProcessInstanceByKey(key, businessKey, variables);
	}

	@Override
	public List<Task> findTaskListByAssigneeName(String assigneeName) {
		return taskService.createTaskQuery()
				.taskAssignee(assigneeName)
				.orderByTaskCreateTime().asc()
				.list();
	}

	@Override
	public String findFormKeyByTaskId(String taskId) {
		return formService.getTaskFormData(taskId).getFormKey();
	}

	@Override
	public LeaveBill findLeaveBillByTaskId(String taskId) {
		Task task = taskService.createTaskQuery()
				.taskId(taskId)
				.singleResult();

		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId())
				.singleResult();

		String businessKey = processInstance.getBusinessKey();
		String[] temp = businessKey.split("#");
		LeaveBill leaveBill = null;
		if ("LeaveBill".equals(temp[0])) {
			leaveBill = leaveBillDao.findById(Long.parseLong(temp[1]));
		}

		return leaveBill;
	}

	@Override
	public List<String> findOutcomeListByTaskId(String taskId) {
		Task task = taskService.createTaskQuery()
				.taskId(taskId)
				.singleResult();

		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId())
				.singleResult();

		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
		ActivityImpl activity = processDefinitionEntity.findActivity(processInstance.getActivityId());
		List<PvmTransition> outgoingTransitions = activity.getOutgoingTransitions();

		List<String> outcomeList = new ArrayList<>();
		if (outgoingTransitions != null && outgoingTransitions.size() > 0) {
			for (PvmTransition transition : outgoingTransitions) {
				String name = (String) transition.getProperty("name");
				if (StringUtils.isNotBlank(name)) {
					outcomeList.add(name);
				} else {
					outcomeList.add("默认提交");
				}
			}
		}

		return outcomeList;
	}

	@Override
	public void saveSubmitTask(WorkflowBean workflowBean) {
		// 添加批注
		Task task = taskService.createTaskQuery()
				.taskId(workflowBean.getTaskId())
				.singleResult();
		Authentication.setAuthenticatedUserId(SessionContext.get().getName());
		taskService.addComment(workflowBean.getTaskId(), task.getProcessInstanceId(), workflowBean.getComment());

		// 完成任务
		HashMap<String, Object> variables = new HashMap<>();
		if (!"默认提交".equals(workflowBean.getOutcome())) {
			variables.put("message", workflowBean.getOutcome());
		}
		taskService.complete(workflowBean.getTaskId(), variables);

		// 流程完成后更新LeaveBill状态
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId())
				.singleResult();
		if (processInstance == null) {
			LeaveBill leaveBill = leaveBillDao.findById(workflowBean.getId());
			leaveBill.setState(2);
		}
	}

	@Override
	public List<Comment> findCommentListByTaskId(String taskId) {
		Task task = taskService.createTaskQuery()
				.taskId(taskId)
				.singleResult();

		List<Comment> commentList = taskService.getProcessInstanceComments(task.getProcessInstanceId());
		commentList.sort(new Comparator<Comment>() {
			@Override
			public int compare(Comment o1, Comment o2) {
				long result = o1.getTime().getTime() - o2.getTime().getTime();
				return result <= 0 ? -1 : 1;
			}
		});

		return commentList;
	}

	@Override
	public LeaveBill findLeaveBillById(Long id) {
		return leaveBillDao.findById(id);
	}

	@Override
	public List<Comment> findCommentListByLeaveBillId(Long leaveBillId) {
		LeaveBill leaveBill = leaveBillDao.findById(leaveBillId);
		String temp = leaveBill.getClass().getSimpleName();
		String businessKey = temp + "#" + leaveBillId;

		HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
				.processInstanceBusinessKey(businessKey)
				.singleResult();

		List<Comment> commentList = taskService.getProcessInstanceComments(hpi.getId());
		commentList.sort(new Comparator<Comment>() {
			@Override
			public int compare(Comment o1, Comment o2) {
				long result = o1.getTime().getTime() - o2.getTime().getTime();
				return result <= 0 ? -1 : 1;
			}
		});

		return commentList;
	}

	@Override
	public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
		Task task = taskService.createTaskQuery()
				.taskId(taskId)
				.singleResult();
		return repositoryService.createProcessDefinitionQuery()
				.processDefinitionId(task.getProcessDefinitionId())
				.singleResult();
	}

	@Override
	public Map<String, Object> findDiagramPositionByTaskId(String taskId) {
		Task task = taskService.createTaskQuery()
				.taskId(taskId)
				.singleResult();

		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(task.getProcessInstanceId())
				.singleResult();

		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
		ActivityImpl activity = processDefinitionEntity.findActivity(processInstance.getActivityId());

		Map<String, Object> map = new HashMap<>();
		map.put("x", activity.getX()-3);
		map.put("y", activity.getY()-3);
		map.put("width", activity.getWidth());
		map.put("height", activity.getHeight());
		return map;
	}


}
























