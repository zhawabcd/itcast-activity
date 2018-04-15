package cn.itcast.ssh.service.impl;

import cn.itcast.ssh.dao.ILeaveBillDao;
import cn.itcast.ssh.domain.Employee;
import cn.itcast.ssh.domain.LeaveBill;
import cn.itcast.ssh.service.ILeaveBillService;
import cn.itcast.ssh.utils.SessionContext;

import java.util.List;

public class LeaveBillServiceImpl implements ILeaveBillService {

	private ILeaveBillDao leaveBillDao;

	public void setLeaveBillDao(ILeaveBillDao leaveBillDao) {
		this.leaveBillDao = leaveBillDao;
	}

	@Override
	public List<LeaveBill> findLeaveBillOfCurUser() {
		Employee user = SessionContext.get();
		String condition = " and l.user.name = '" + user.getName() + "'";
		List<LeaveBill> leaveBillList = leaveBillDao.findByCondition(condition);
		return leaveBillList;
	}

	@Override
	public void save(LeaveBill leaveBill) {
		if (leaveBill.getId() == null) {
			leaveBill.setUser(SessionContext.get());
			leaveBillDao.save(leaveBill);
		} else {
			LeaveBill temp = findById(leaveBill.getId());
			temp.setDays(leaveBill.getDays());
			temp.setContent(leaveBill.getContent());
			temp.setRemark(leaveBill.getRemark());
			temp.setLeaveDate(leaveBill.getLeaveDate());
		}
	}

	@Override
	public LeaveBill findById(Long id) {
		return leaveBillDao.findById(id);
	}
}
