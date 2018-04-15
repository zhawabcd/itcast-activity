package cn.itcast.ssh.utils;

import cn.itcast.ssh.domain.Employee;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;

/**
 * 登录验证拦截器
 */
@SuppressWarnings("serial")
public class LoginInteceptor extends MethodFilterInterceptor {


	@Override
	protected String doIntercept(ActionInvocation actionInvocation) throws Exception {
		System.out.println("经过了拦截器");

		Employee user = SessionContext.get();
		if (user == null) {
			return "login";
		}

		return actionInvocation.invoke();
	}
}
