package cn.itcast.ssh.dao;


import cn.itcast.ssh.domain.LeaveBill;

import java.util.List;

public interface ILeaveBillDao {


	List<LeaveBill> findByCondition(String condition);

	void save(LeaveBill leaveBill);

	LeaveBill findById(Long id);
}
