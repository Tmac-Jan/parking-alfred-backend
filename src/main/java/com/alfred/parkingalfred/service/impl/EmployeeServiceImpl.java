package com.alfred.parkingalfred.service.impl;

import com.alfred.parkingalfred.converter.EmployeeToEmployeeVOConverter;
import com.alfred.parkingalfred.entity.Employee;
import com.alfred.parkingalfred.entity.ParkingLot;
import com.alfred.parkingalfred.enums.ResultEnum;
import com.alfred.parkingalfred.exception.EmployeeNotExistedException;
import com.alfred.parkingalfred.form.EmployeeForm;
import com.alfred.parkingalfred.repository.EmployeeRepository;
import com.alfred.parkingalfred.repository.ParkingLotRepository;
import com.alfred.parkingalfred.service.EmployeeService;
import com.alfred.parkingalfred.utils.EncodingUtil;
import com.alfred.parkingalfred.vo.EmployeeVO;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeRepository employeeRepository;

  private final ParkingLotRepository parkingLotRepository;

  public EmployeeServiceImpl(EmployeeRepository employeeRepository,
      ParkingLotRepository parkingLotRepository) {
    this.employeeRepository = employeeRepository;
    this.parkingLotRepository = parkingLotRepository;
  }

  @Override
  public Employee getEmployeeByMailAndPassword(String mail, String password) {
    String encodedPassword = EncodingUtil.encodingByMd5(password);
    Employee employee = employeeRepository.findByMailAndPassword(mail, encodedPassword);
    if (employee == null) {
      throw new EmployeeNotExistedException(ResultEnum.RESOURCES_NOT_EXISTED);
    }
    return employee;
  }

  @Override
  public boolean doesEmployeeHasNotFullParkingLots(Long employeeId) {
    employeeRepository.findById(employeeId).orElseThrow(() ->
        new EmployeeNotExistedException(ResultEnum.RESOURCES_NOT_EXISTED));
    int result = parkingLotRepository.findALLNotFullParkingLotRowsByEmployeeId(employeeId);
    return result > 0;
  }

  @Override
  public List<EmployeeVO> getEmployeesByRoleWithFilterByPageAndSize(Integer page, Integer size,Integer role) {
    PageRequest pageRequest = PageRequest.of(page - 1, size);
    Page<Employee> employeePage = employeeRepository.findAllByRole(role,pageRequest);
    return EmployeeToEmployeeVOConverter
        .convert(employeePage.getContent());
  }

  @Override
  public Employee getEmployeeById(Long id) {
    return employeeRepository.findById(id)
        .orElseThrow(() -> new EmployeeNotExistedException(ResultEnum.RESOURCES_NOT_EXISTED));
  }

  @Override
  public EmployeeVO createEmployee(EmployeeForm employeeForm) {
    Employee employee = new Employee();
    BeanUtils.copyProperties(employeeForm, employee);
//    String password = UUIDUtil.generateUUID()
//        .replace("-", "").substring(0, 8);
    //TODO delete this line after test
   //    System.out.println("password:"+password);
    String password = "123456";
    employee.setPassword(EncodingUtil.encodingByMd5(password));
    Employee employeeResult = employeeRepository.save(employee);
    EmployeeVO employeeVOResult = new EmployeeVO();
    BeanUtils.copyProperties(employeeResult, employeeVOResult);
    return employeeVOResult;
  }

  public int getEmployeeCount() {
    return employeeRepository.getEmployeeCount();
  }

  @Override
  public Employee updateEmployeeParkingLots(Long employeeId, List<Long> parkingLotIdList) {
    Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotExistedException(ResultEnum.RESOURCES_NOT_EXISTED));
    List<ParkingLot> parkingLots = parkingLotRepository.findAllByIdIn(parkingLotIdList);
    List<ParkingLot> employeeParkingLots = employee.getParkingLots();
    employeeParkingLots.addAll(parkingLots);
    employeeRepository.save(employee);
    return employee;
  }

}
