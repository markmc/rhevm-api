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

import java.util.List;

import com.redhat.rhevm.api.model.LogSeverity;
import com.redhat.rhevm.api.model.Event;

public class PowerShellEventTest extends PowerShellModelTest {

    private void testEvent(Event e, String id, String description, Integer code, LogSeverity sev, String time) {
        assertEquals(id, e.getId());
        assertEquals(description, e.getDescription());
        assertEquals(code, e.getCode());
        assertEquals(sev, e.getSeverity());
        assertEquals(time, e.getTime().toString());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("events.xml");
        assertNotNull(data);

        List<Event> events = PowerShellEvent.parse(getParser(), data);

        assertEquals(events.size(), 65);

        testEvent(events.get(0),  "64", "Network foo was added to data center: Default", 942, LogSeverity.NORMAL, "2011-01-24T15:20:35.000Z");
        testEvent(events.get(1),  "63", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T15:20:14.000Z");
        testEvent(events.get(2),  "62", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T15:19:58.000Z");
        testEvent(events.get(3),  "61", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T15:19:58.000Z");
        testEvent(events.get(4),  "60", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T15:19:58.000Z");
        testEvent(events.get(5),  "59", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T15:19:58.000Z");
        testEvent(events.get(6),  "58", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T15:19:58.000Z");
        testEvent(events.get(7),  "57", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T15:19:58.000Z");
        testEvent(events.get(8),  "56", "Network foo was removed from data center: Default", 944, LogSeverity.NORMAL, "2011-01-24T15:17:01.000Z");
        testEvent(events.get(9),  "55", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T15:16:42.000Z");
        testEvent(events.get(10), "54", "Network foo was added to data center: Default", 942, LogSeverity.NORMAL, "2011-01-24T15:16:38.000Z");
        testEvent(events.get(11), "53", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T15:15:56.000Z");
        testEvent(events.get(12), "52", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T15:14:56.000Z");
        testEvent(events.get(13), "51", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T15:14:48.000Z");
        testEvent(events.get(14), "50", "Storage Domain iso0 (Data Center Default) was activated by rhevmadmin", 966, LogSeverity.NORMAL, "2011-01-24T11:37:53.000Z");
        testEvent(events.get(15), "49", "Storage Domain iso0 was attached to Data Center Default by rhevmadmin", 962, LogSeverity.NORMAL, "2011-01-24T11:37:33.000Z");
        testEvent(events.get(16), "48", "Storage Domain iso0 was added by rhevmadmin", 956, LogSeverity.NORMAL, "2011-01-24T11:35:09.000Z");
        testEvent(events.get(17), "47", "Storage Domain images0 (Data Center Default) was activated by rhevmadmin", 966, LogSeverity.NORMAL, "2011-01-24T11:03:09.000Z");
        testEvent(events.get(18), "46", "Storage Pool Manager runs on Host zig (IP Address: 192.168.1.107).", 204, LogSeverity.NORMAL, "2011-01-24T11:03:09.000Z");
        testEvent(events.get(19), "45", "Error getting Data Center Default status - setting status to Non-Responsive (Host Unavailable) - Trying to search for another host.", 986, LogSeverity.WARNING, "2011-01-24T11:02:47.000Z");
        testEvent(events.get(20), "44", "Storage Domain images0 was added by rhevmadmin", 956, LogSeverity.NORMAL, "2011-01-24T11:02:10.000Z");
        testEvent(events.get(21), "43", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T11:01:37.000Z");
        testEvent(events.get(22), "42", "Host cluster Default was updated by system", 835, LogSeverity.NORMAL, "2011-01-24T11:01:26.000Z");
        testEvent(events.get(23), "41", "Failed to connect Host zig to Storage Servers", 994, LogSeverity.WARNING, "2011-01-24T11:01:26.000Z");
        testEvent(events.get(24), "40", "Detected new Host zig. Host state was set to Up .", 13, LogSeverity.NORMAL, "2011-01-24T11:01:25.000Z");
        testEvent(events.get(25), "39", "Starting RHEV Manager.", 1, LogSeverity.NORMAL, "2011-01-24T11:01:12.000Z");
        testEvent(events.get(26), "38", "Stopping RHEV Manager.", 2, LogSeverity.NORMAL, "2011-01-24T11:01:11.000Z");
        testEvent(events.get(27), "37", "Host zig was activated by rhevmadmin.", 16, LogSeverity.NORMAL, "2011-01-24T10:59:04.000Z");
        testEvent(events.get(28), "36", "Host zig is non-responsive.", 12, LogSeverity.ERROR, "2011-01-24T10:59:04.000Z");
        testEvent(events.get(29), "35", "Host zig was switched to Maintenance mode by rhevmadmin.", 600, LogSeverity.NORMAL, "2011-01-24T10:59:02.000Z");
        testEvent(events.get(30), "34", "Power Management operation VdsNotRespondingTreatment skipped on Host zig because it is not configured.", 9003, LogSeverity.ALERT, "2011-01-24T10:58:56.000Z");
        testEvent(events.get(31), "33", "Host zig is non-responsive.", 12, LogSeverity.ERROR, "2011-01-24T10:58:56.000Z");
        testEvent(events.get(32), "32", "Host zig was activated by rhevmadmin.", 16, LogSeverity.NORMAL, "2011-01-24T10:58:54.000Z");
        testEvent(events.get(33), "31", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:58:45.000Z");
        testEvent(events.get(34), "30", "Host zig installation failed. .", 505, LogSeverity.ERROR, "2011-01-24T10:58:02.000Z");
        testEvent(events.get(35), "29", "Failed to install Host zig. Step: INSTALLER LIB; Details: deployUtil.py download failed. Pathname could not be resolved (verify computer/domain name)..", 511, LogSeverity.ERROR, "2011-01-24T10:58:02.000Z");
        testEvent(events.get(36), "28", "Installing Host zig. Step: INSTALLER; Details: Test platform succeeded.", 509, LogSeverity.NORMAL, "2011-01-24T10:58:01.000Z");
        testEvent(events.get(37), "27", "Host zig was added by rhevmadmin.", 42, LogSeverity.NORMAL, "2011-01-24T10:57:56.000Z");
        testEvent(events.get(38), "26", "Power Management is not configured for Host zig.", 9000, LogSeverity.ALERT, "2011-01-24T10:57:56.000Z");
        testEvent(events.get(39), "25", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:55:44.000Z");
        testEvent(events.get(40), "24", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T10:55:44.000Z");
        testEvent(events.get(41), "23", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:55:43.000Z");
        testEvent(events.get(42), "22", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:55:43.000Z");
        testEvent(events.get(43), "21", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T10:55:43.000Z");
        testEvent(events.get(44), "20", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T10:55:43.000Z");
        testEvent(events.get(45), "19", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:55:43.000Z");
        testEvent(events.get(46), "18", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:55:43.000Z");
        testEvent(events.get(47), "17", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:55:43.000Z");
        testEvent(events.get(48), "16", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T10:51:19.000Z");
        testEvent(events.get(49), "15", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T10:51:18.000Z");
        testEvent(events.get(50), "14", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T10:51:18.000Z");
        testEvent(events.get(51), "13", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:51:18.000Z");
        testEvent(events.get(52), "12", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:51:18.000Z");
        testEvent(events.get(53), "11", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:51:18.000Z");
        testEvent(events.get(54), "10", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:49:52.000Z");
        testEvent(events.get(55), "9", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T10:49:52.000Z");
        testEvent(events.get(56), "8", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:49:50.000Z");
        testEvent(events.get(57), "7", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:49:50.000Z");
        testEvent(events.get(58), "6", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T10:49:50.000Z");
        testEvent(events.get(59), "5", "User rhevmadmin logged out.", 31, LogSeverity.NORMAL, "2011-01-24T10:49:50.000Z");
        testEvent(events.get(60), "4", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:49:50.000Z");
        testEvent(events.get(61), "3", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:49:50.000Z");
        testEvent(events.get(62), "2", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:49:49.000Z");
        testEvent(events.get(63), "1", "User rhevmadmin logged in.", 30, LogSeverity.NORMAL, "2011-01-24T10:49:30.000Z");
        testEvent(events.get(64), "0", "Starting RHEV Manager.", 1, LogSeverity.NORMAL, "2011-01-24T10:48:14.000Z");
    }
}
