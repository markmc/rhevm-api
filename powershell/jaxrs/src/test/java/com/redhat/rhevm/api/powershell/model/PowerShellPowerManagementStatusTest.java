package com.redhat.rhevm.api.powershell.model;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.redhat.rhevm.api.model.PowerManagementStatus;


public class PowerShellPowerManagementStatusTest extends PowerShellModelTest {


    private List<PowerShellPowerManagementStatus> statuses;

    @Before
    public void setUp() throws Exception {
        String data = readFileContents("hostfencestatus.xml");
        assertNotNull(data);
        statuses = PowerShellPowerManagementStatus.parse(getParser(), data);
    }

    @Test
    public void test2StatusesFound() {
        assertNotNull(statuses);
        assertEquals(2, statuses.size());
    }

    @Test
    public void testSucceedOn() throws Exception {
        PowerShellPowerManagementStatus status1 = statuses.get(0);
        assertEquals(status1.isSuccess(), true);
        assertEquals(status1.getStatus(), PowerManagementStatus.ON);
        assertEquals(status1.getMessage(), "");
    }

    @Test
    public void testFailedOff() throws Exception {
        PowerShellPowerManagementStatus status1 = statuses.get(1);
        assertEquals(status1.isSuccess(), false);
        assertEquals(status1.getStatus(), PowerManagementStatus.OFF);
        assertEquals(status1.getMessage(), "Host was burned by a madman");
    }
}
