package cn.itcast.ssh.service;


import cn.itcast.ssh.domain.Employee;

public interface IEmployeeService {


	Employee findUserByName(String name);
}
