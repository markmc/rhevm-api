/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.powershell.model;

import java.net.URLEncoder;

import com.redhat.rhevm.api.model.Creation;
import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Status;
import com.redhat.rhevm.api.powershell.enums.PowerShellAsyncTaskResult;
import com.redhat.rhevm.api.powershell.enums.PowerShellAsyncTaskStatus;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellAsyncTask {

    private static final String TASK_TYPE = "RhevmCmd.CLITask";
    private static final String STATUS_TYPE = "VdcCommon.BusinessEntities.AsyncTaskStatus";
    private static final String STATUS_TYPE_22 = "VdcCommon.AsyncTasks.AsyncTaskStatus";
    private static final String ID_SEPARATOR = ",";
    private static final String FAILURE_REASON = "RHEVM asynchronous task failed";

    public static boolean isTask(PowerShellParser.Entity entity) {
        return TASK_TYPE.equals(entity.getType());
    }

    public static boolean isStatus(PowerShellParser.Entity entity) {
        return STATUS_TYPE.equals(entity.getType()) || STATUS_TYPE_22.equals(entity.getType());
    }

    public static String parseTask(PowerShellParser.Entity entity, String taskIds) {
        String id = "";
        if (isTask(entity)) {
            id = entity.get("taskid");
        }
        return taskIds != null ? taskIds + URLEncoder.encode(ID_SEPARATOR) + id : id;
    }

    public static String[] parseTaskIds(String taskIds) {
        return taskIds.split(ID_SEPARATOR);
    }

    public static Status parseStatus(PowerShellParser.Entity entity, Status cumlative) {
        Status ret = cumlative;
        if (isStatus(entity)) {
            Boolean statusPresent =
                entity.isSet("statusspecified")
                ? entity.get("statusspecified", Boolean.class)
                : Boolean.TRUE;
            PowerShellAsyncTaskStatus status =
                statusPresent
                ? entity.get("status", PowerShellAsyncTaskStatus.class)
                : PowerShellAsyncTaskStatus.unknown;
            Boolean resultPresent =
                entity.isSet("resultspecified")
                ? entity.get("resultspecified", Boolean.class)
                : Boolean.TRUE;
            PowerShellAsyncTaskResult result =
                resultPresent
                ? entity.get("result", PowerShellAsyncTaskResult.class)
                : null;

            switch(status) {
                case unknown:
                case init:
                    ret = cumlative != Status.FAILED ? Status.PENDING : cumlative;
                    break;
                case running:
                    ret = cumlative != Status.FAILED ? Status.IN_PROGRESS : cumlative;
                    break;
                case finished:
                    if (result == PowerShellAsyncTaskResult.success) {
                        ret = cumlative != null ? cumlative : Status.COMPLETE;
                    } else {
                        ret = Status.FAILED;
                    }
                    break;
                case cleaning:
                    if (result == PowerShellAsyncTaskResult.cleanSuccess) {
                        ret = cumlative != null ? cumlative : Status.COMPLETE;
                    } else {
                        ret = Status.FAILED;
                    }
                    break;
                case aborting:
                    ret = Status.FAILED;
                    break;
            }
        }
        return ret;
    }

    public static Creation parse(PowerShellParser parser, String output) {
        Creation creation = new Creation();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (isStatus(entity)) {
                creation.setStatus(parseStatus(entity, creation.getStatus()));
                if (Status.FAILED == creation.getStatus()) {
                    creation.setFault(new Fault());
                    creation.getFault().setReason(FAILURE_REASON);
                    creation.getFault().setDetail(entity.isSet("exception") ? entity.get("exception") : entity.get("message"));
                    break;
                }
            }
        }

        return creation;
    }

}
