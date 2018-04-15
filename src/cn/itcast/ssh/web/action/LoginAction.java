package cn.itcast.ssh.web.action;

import cn.itcast.ssh.domain.Employee;
import cn.itcast.ssh.service.IEmployeeService;
import cn.itcast.ssh.utils.SessionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

@SuppressWarnings("serial")
public class LoginAction extends ActionSupport implements ModelDriven<Employee> {

	private Employee employee = new Employee();

	@Override
	public Employee getModel() {
		return employee;
	}

	private IEmployeeService employeeService;

	public void setEmployeeService(IEmployeeService employeeService) {
		this.employeeService = employeeService;
	}


	/**
	 * 登录
	 *
	 * @return
	 */
	public String login() {
		Employee user = employeeService.findUserByName(this.employee.getName());
		SessionContext.setUser(user);
		return "success";
	}

	public String test() {
		System.out.println("test方法");
		return NONE;
	}

	/**
	 * 标题
	 *
	 * @return
	 */
	public String top() {
		return "top";
	}

	/**
	 * 左侧菜单
	 *
	 * @return
	 */
	public String left() {
		return "left";
	}

	/**
	 * 主页显示
	 *
	 * @return
	 */
	public String welcome() {
		return "welcome";
	}

	public String logout() {
		return "login";
	}
}
