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

import org.junit.Test;

import com.redhat.rhevm.api.model.Creation;
import com.redhat.rhevm.api.model.Status;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellAsyncTaskTest extends PowerShellModelTest {

    @Test
    public void testParseTasks() throws Exception {
        String data = readFileContents("tasks.xml");
        assertNotNull(data);

        String taskIds = null;
        Status status = null;

        for (PowerShellParser.Entity entity : getParser().parse(data)) {
            if (PowerShellAsyncTask.isTask(entity)) {
                if (taskIds != null) {
                    assertEquals("9d29775d-b685-411f-b304-07acc2fb7528", taskIds);
                }
                taskIds = PowerShellAsyncTask.parseTask(entity, taskIds);
                continue;
            } else if (PowerShellAsyncTask.isStatus(entity)) {
                if (status != null) {
                    assertEquals(Status.PENDING, status);
                }
                status = PowerShellAsyncTask.parseStatus(entity, status);
                continue;
            }

        }

        assertNotNull(taskIds);
        assertEquals("9d29775d-b685-411f-b304-07acc2fb7528%2C0b9318b4-e426-4380-9e6a-bb7f3a38a2ce", taskIds);
        assertNotNull(status);
        assertEquals(Status.FAILED, status);
    }

    @Test
    public void testParseTasks22() throws Exception {
        String data = readFileContents("tasks22.xml");
        assertNotNull(data);

        String taskIds = null;
        Status status = null;

        for (PowerShellParser.Entity entity : getParser().parse(data)) {
            if (PowerShellAsyncTask.isTask(entity)) {
                taskIds = PowerShellAsyncTask.parseTask(entity, taskIds);
                continue;
            } else if (PowerShellAsyncTask.isStatus(entity)) {
                status = PowerShellAsyncTask.parseStatus(entity, status);
                continue;
            }

        }

        assertNotNull(taskIds);
        assertEquals("3571cb0a-5b9c-4387-bd82-24b60485dacc", taskIds);
        assertNotNull(status);
        assertEquals(Status.IN_PROGRESS, status);
    }

    @Test
    public void testParseCreation() throws Exception {
        String data = readFileContents("tasks.xml");
        assertNotNull(data);

        Creation creation = PowerShellAsyncTask.parse(getParser(), data);

        assertNotNull(creation);
        assertEquals(Status.FAILED, creation.getStatus());
        assertTrue(creation.isSetFault());
        assertTrue(creation.getFault().isSetReason());
        assertEquals("Barfed...", creation.getFault().getDetail());
    }

}
