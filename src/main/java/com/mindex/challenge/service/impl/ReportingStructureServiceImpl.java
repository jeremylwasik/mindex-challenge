package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import com.mindex.challenge.service.ReportingStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
/**
 * The read me requested a "fully filled out reporting structure" which I have
 * interpreted to mean that the employee structure returned here will have all
 * of the information on the reports not just the employee id like the employee
 * endpoint provides. This is also true for each of the employees that report 
 * to the original employee so that you may end up with multiple levels of 
 * employees with all information included. 
 *
 * This implementaions also assumes that there are no cycles within the
 * reporting structure which would cause endless recursion until you were to
 * run out of memory. 
 *
 * There's also a possible point of optimization that could be considered if
 * for a reporting structure it was possible for an employee to report to more
 * than one boss. If that happens you could have that employee appear (along
 * with all their reports) multiple times in the final data structure. If your
 * usage was memory bound, and you had known cases where large structures could
 * be doubled, it would be worth considering revamping the such that the employee
 * data was kept as a set of all the employee in the report so you only had the
 * information once, and then kept the reporting structure as only the ids in a
 * map of the bosses id to the list of reports ids so that if anyone employee
 * does appear twice you are only duplicating the id, and not all of the data
 * for both them and their reports.
 */
public class ReportingStructureServiceImpl implements ReportingStructureService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

    @Autowired
    private EmployeeService employeeService;

    @Override
    public ReportingStructure read(String id) {
        LOG.debug("Creating reporting structure for employee id [{}]", id);

        Employee employee = employeeService.read(id);
        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }
	
        return fillReportingStructure(employee);
    }

    /**
     * Recursively fill in the full reporting structure, essentially we trace
     * down the reporting structure until we find a someone with no report.
     * That structure is just the full employee data with 0 reports. We then
     * save the full employee data off to the side and add them to their 
     * managers reports. Then when we have all reports for a manager we replace
     * the list of reports that are normally only the id, with their full data.
     * We then pass the manager's information up the chain to their manager,
     * accumulating the direct reports, plus the now reporting employee along
     * with the hydrated information as we go, until all links have been filled.
     */
    private ReportingStructure fillReportingStructure(Employee employee) {
        ReportingStructure reportingStructure = new ReportingStructure();
        List<Employee> dehydratedReports = employee.getDirectReports();
	// Base case, return employee as is with 0 reports
	if (dehydratedReports == null || dehydratedReports.size() == 0) {
            reportingStructure.setEmployee(employee);
            reportingStructure.setNumberOfReports(0);

            return reportingStructure;
        }

	int numberOfReports = 0;
        ArrayList<Employee> hydratedReports = new ArrayList<Employee>();
	// Create the reporting structure for each direct reports and combine
	for (Employee dehydratedReport : dehydratedReports) {
            // Fill in details of the report employee, currently just an id
            Employee partlyHydratedReport = employeeService.read(dehydratedReport.getEmployeeId());
            // Get full reporting structure under this report employee
            ReportingStructure hydratedReport = fillReportingStructure(partlyHydratedReport);
            // Number of reports are this employee (the 1) and their reports
            numberOfReports += 1 + hydratedReport.getNumberOfReports();
            // Add this employee with all their reports to our list of reports
            hydratedReports.add(hydratedReport.getEmployee());
        }
       
        employee.setDirectReports(hydratedReports);
        reportingStructure.setEmployee(employee);
        reportingStructure.setNumberOfReports(numberOfReports);

        return reportingStructure;
    }
}
