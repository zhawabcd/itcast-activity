package cn.itcast.ssh.dao.impl;

import cn.itcast.ssh.dao.ILeaveBillDao;
import cn.itcast.ssh.domain.LeaveBill;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public class LeaveBillDaoImpl extends HibernateDaoSupport implements ILeaveBillDao {


	@Override
	public List<LeaveBill> findByCondition(String condition) {
		String hql = "from LeaveBill l  where 1=1 " + condition;
		List list = this.getHibernateTemplate().find(hql);
		return list;
	}

	@Override
	public void save(LeaveBill leaveBill) {
		this.getHibernateTemplate().save(leaveBill);
	}

	@Override
	public LeaveBill findById(Long id) {
		return this.getHibernateTemplate().get(LeaveBill.class,id);
	}
}
