package cn.itcast.ssh.web.action;

import cn.itcast.ssh.domain.LeaveBill;
import cn.itcast.ssh.service.ILeaveBillService;
import cn.itcast.ssh.service.IWorkflowService;
import cn.itcast.ssh.utils.SessionContext;
import cn.itcast.ssh.utils.ValueContext;
import cn.itcast.ssh.web.form.WorkflowBean;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.struts2.ServletActionContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@SuppressWarnings("serial")
public class WorkflowAction extends ActionSupport implements ModelDriven<WorkflowBean> {

	private WorkflowBean workflowBean = new WorkflowBean();

	@Override
	public WorkflowBean getModel() {
		return workflowBean;
	}

	private IWorkflowService workflowService;

	private ILeaveBillService leaveBillService;

	public void setLeaveBillService(ILeaveBillService leaveBillService) {
		this.leaveBillService = leaveBillService;
	}

	public void setWorkflowService(IWorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * 部署管理首页显示
	 *
	 * @return
	 */
	public String deployHome() {
		List<Deployment> deploymentList = workflowService.findDeploymentList();
		List<ProcessDefinition> processDefinitionList = workflowService.findProcessDefinitionList();
		ValueContext.putValue2ObjStack("depList", deploymentList);
		ValueContext.putValue2ObjStack("prdList", processDefinitionList);
		return "deployHome";
	}

	/**
	 * 发布流程
	 *
	 * @return
	 */
	public String newdeploy() {
		workflowService.saveDeploy(workflowBean);
		return "list";
	}

	/**
	 * 删除部署信息
	 */
	public String delDeployment() {
		workflowService.deleteDeploymentById(workflowBean.getDeploymentId());
		return "list";
	}

	/**
	 * 查看流程图
	 */
	public String viewImage() throws IOException {
		InputStream inputStream = workflowService.getImageInputStream(workflowBean.getDeploymentId(), workflowBean.getImageName());

		OutputStream outputStream = ServletActionContext.getResponse().getOutputStream();
		int length = -1;
		byte[] temp = new byte[1024];

		while ((length = inputStream.read(temp)) != -1) {
			outputStream.write(temp, 0, length);
		}

		inputStream.close();
		outputStream.close();

		return NONE;
	}

	// 启动流程
	public String startProcess() {
		workflowService.saveStartProcess(workflowBean);
		return "listTask";
	}


	/**
	 * 任务管理首页显示
	 *
	 * @return
	 */
	public String listTask() {
		List<Task> taskList = workflowService.findTaskListByAssigneeName(SessionContext.get().getName());
		ValueContext.putValue2ObjStack("taskList", taskList);
		return "task";
	}

	/**
	 * 打开任务表单
	 */
	public String viewTaskForm() {
		String value = workflowService.findFormKeyByTaskId(workflowBean.getTaskId());
		value += "?taskId=" + workflowBean.getTaskId();
		ValueContext.putValue2ObjStack("url", value);
		return "viewTaskForm";
	}

	// 准备表单数据
	public String audit() {
		LeaveBill leaveBill = workflowService.findLeaveBillByTaskId(workflowBean.getTaskId());
		ValueContext.putValueStack(leaveBill);
		List<String> outcomeList = workflowService.findOutcomeListByTaskId(workflowBean.getTaskId());
		ValueContext.putValue2ObjStack("outcomeList", outcomeList);
		return "taskForm";
	}

	/**
	 * 提交任务
	 */
	public String submitTask() {
		return "listTask";
	}

	/**
	 * 查看当前流程图（查看当前活动节点，并使用红色的框标注）
	 */
	public String viewCurrentImage() {
		return "image";
	}

	// 查看历史的批注信息
	public String viewHisComment() {
		return "viewHisComment";
	}
}
