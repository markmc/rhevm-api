package com.redhat.rhevm.api.powershell.resource;

import java.text.MessageFormat;

import com.redhat.rhevm.api.common.resource.AbstractActionableResource.AbstractActionTask;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellException;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


class CommandRunner extends AbstractActionTask {

    private static final String COMMAND = "{0} -{1}id {2}";
    // REVISIT: i18n
    private static final String FAILED = "Powershell command \"{0}\" failed with {1}";

    private String command;

    CommandRunner(Action action, String command, String type, String id) {
        super(action);
        this.command = MessageFormat.format(COMMAND, command, type, PowerShellUtils.escape(id));
    }

    public void run() {
        try {
            PowerShellCmd.runCommand(command);
        } catch (PowerShellException pse) {
            String detail = pse.getMessage() != null ? pse.getMessage() : pse.getClass().getName();
            setFault(MessageFormat.format(FAILED, command, detail), pse);
        }
    }
}

