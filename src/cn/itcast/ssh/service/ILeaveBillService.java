package cn.itcast.ssh.service;


import cn.itcast.ssh.domain.LeaveBill;

import java.util.List;

public interface ILeaveBillService {

	List<LeaveBill> findLeaveBillOfCurUser();

	void save(LeaveBill leaveBill);

	LeaveBill findById(Long id);
}
