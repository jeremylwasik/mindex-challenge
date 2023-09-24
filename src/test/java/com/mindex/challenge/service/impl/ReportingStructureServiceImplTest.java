package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceImplTest {

    private String employeeUrl;
    private String reportingStructureUrl;

    @Autowired
    private ReportingStructureService reportingStructureService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        reportingStructureUrl = "http://localhost:" + port + "/reporting-structure/{id}";
    }

    @Test
    public void testReadNoReports() {
        int expectedReports = 0;
        Employee testEmployee = createTestEmployee(expectedReports);

        // Read checks
        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, testEmployee.getEmployeeId()).getBody();

	Employee readEmployee = reportingStructure.getEmployee();
	int readReports = reportingStructure.getNumberOfReports();
	
        assertEquals(expectedReports, readReports);
        assertEquals(testEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, readEmployee);
    }

    @Test
    /**
     * The straight chain of single reports isn't the most robust of tests, but
     * this proves out the basic concept. Along with testing the endpoint by hand
     * to match the expected output in the read me, this seemed to be adequate 
     * for the current usage. If this code were going to be part of an enterprise
     * application, I'd look more into a senerio like the one from the read me to
     * better test the functionality especially with an eye toward verifying the
     * fully filled out reporting structure.
     */
    public void testReadWithReports() {
        int expectedReports = 3;
        Employee testEmployee = createTestEmployee(expectedReports);

        // Read checks
        ReportingStructure reportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, testEmployee.getEmployeeId()).getBody();

	Employee readEmployee = reportingStructure.getEmployee();
	int readReports = reportingStructure.getNumberOfReports();
	
        assertEquals(expectedReports, readReports);
        assertEquals(testEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, readEmployee);
    }

    private Employee generateBasicTestEmployee(int id) {
        // Generates the basic data for an employee by id, does not store
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John" + id);
        testEmployee.setLastName("Doe" + id);
        testEmployee.setDepartment("Engineering" + id);
        testEmployee.setPosition("Developer" + id);

       return testEmployee; 
    }

    private Employee createTestEmployee(int numberOfReports) {
       // Creates a chain of employees such that each int reports to the next larger
       // The int passed in is the id of the largest and the total number of reports in the chain
       Employee retEmployee = null;
       for(int x = 0; x <= numberOfReports; x++) {
           Employee curEmployee = generateBasicTestEmployee(x);
           if (retEmployee != null) {
               ArrayList<Employee> reports = new ArrayList<Employee>();
               reports.add(retEmployee);
               curEmployee.setDirectReports(reports);
           }
           // Create employee and ready for return if the last
           retEmployee = restTemplate.postForEntity(employeeUrl, curEmployee, Employee.class).getBody();
        }
        return retEmployee;
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
