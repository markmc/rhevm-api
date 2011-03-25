package com.redhat.rhevm.api.powershell.model;

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.PowerManagementStatus;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellPowerManagementStatus {

    public static String TEST_SUCCEEDED = "Test Succeeded";
    public static String TEST_FAILED = "Test Failed";
    public static String ON = "on";
    public static String OFF = "off";
    public static String UNKNOWN = "unknown";

    private PowerManagementStatus status;
    private Boolean success;
    private String message;

    public PowerManagementStatus getStatus() {
        return status;
    }

    public void setStatus(PowerManagementStatus status) {
        this.status = status;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static List<PowerShellPowerManagementStatus> parse(PowerShellParser parser, String output) {
        List<PowerShellPowerManagementStatus> ret = new ArrayList<PowerShellPowerManagementStatus>();
        for (PowerShellParser.Entity entity : parser.parse(output)) {
            String value = entity.getValue();
            PowerShellPowerManagementStatus pmStatus = new PowerShellPowerManagementStatus();
            pmStatus.setStatus(parseStatus(value));
            pmStatus.setSuccess(parseSuccess(value));
            pmStatus.setMessage(parseMessage(value));
            ret.add(pmStatus);
        }
        return ret;
    }

    private static String parseMessage(String value) {
        return value.indexOf('.') != -1 ? value.substring(value.indexOf('.') + 1).trim() : "";
    }

    private static PowerManagementStatus parseStatus(String value) {
        if (value.contains(OFF)) {
            return PowerManagementStatus.OFF;
        } else if (value.contains(ON)) {
            return PowerManagementStatus.ON;
        } else {
            assert false;
            return null;
        }
    }

    private static Boolean parseSuccess(String value) {
        if (value.contains(TEST_SUCCEEDED)) {
            return true;
        } else if (value.contains(TEST_FAILED) || value.contains(UNKNOWN)) {
            return false;
        } else {
            assert false;
            return null;
        }
    }
}
