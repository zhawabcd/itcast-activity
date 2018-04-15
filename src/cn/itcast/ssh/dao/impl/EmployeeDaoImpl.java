package cn.itcast.ssh.dao.impl;

import cn.itcast.ssh.dao.IEmployeeDao;
import cn.itcast.ssh.domain.Employee;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class EmployeeDaoImpl extends HibernateDaoSupport implements IEmployeeDao {


	@Override
	public Employee findUserByName(String name) {
		String hql = "from Employee e where e.name = ?";
		List list = this.getHibernateTemplate().find(hql, name);

		Employee employee = null;
		if (list != null && list.size() > 0) {
			employee = (Employee) list.get(0);
		}

		return employee;
	}
}
