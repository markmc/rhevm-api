package com.redhat.rhevm.api.powershell.resource;

import java.text.MessageFormat;

import com.redhat.rhevm.api.common.resource.AbstractActionableResource.AbstractActionTask;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;


class CommandRunner extends AbstractActionTask {

    private static final String COMMAND = "{0} -{1}id {2}";

    private String command;

    CommandRunner(Action action, String command, String type, String id) {
        super(action);
        this.command = MessageFormat.format(COMMAND, command, type, id);
    }

    public void run() {
        PowerShellCmd.runCommand(command);
    }
}

