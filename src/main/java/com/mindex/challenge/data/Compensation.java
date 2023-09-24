package com.mindex.challenge.data;

import com.mindex.challenge.data.Employee;

import java.util.Date;
import java.util.List;

/**
 * So this meets the requested layout of having the employee stored as part of
 * the compensation, but this feels incorrect to me. If I was designing the
 * class I would have had this store the employee id and not the employee
 * itself. The way it is now you will end up with a disconnect between the
 * employees stored in the employee repository vs what is stored in
 * compensation.
 */
public class Compensation {
    private Employee employee;
    private int salary;
    private Date effectiveDate;

    public Compensation() {
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
