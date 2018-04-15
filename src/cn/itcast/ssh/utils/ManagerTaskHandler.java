package cn.itcast.ssh.utils;

import cn.itcast.ssh.domain.Employee;
import cn.itcast.ssh.service.IEmployeeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.struts2.ServletActionContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 员工经理任务分配
 */
@SuppressWarnings("serial")
public class ManagerTaskHandler implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		String name = SessionContext.get().getName();
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(ServletActionContext.getServletContext());
		IEmployeeService employeeService = (IEmployeeService) applicationContext.getBean("employeeService");
		Employee manager = employeeService.findUserByName(name).getManager();
		delegateTask.setAssignee(manager.getName());
	}

}
