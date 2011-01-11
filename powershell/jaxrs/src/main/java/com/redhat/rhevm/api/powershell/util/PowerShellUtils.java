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
package com.redhat.rhevm.api.powershell.util;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PowerShellUtils {

    private static final Log log = LogFactory.getLog(PowerShellCmd.class);

    private static final String QUOTE = "\"";
    private static final String ESCAPE = "`";
    private static final String DOLLAR = "$";

    public static String escape(String arg) {
        String[] toEscape = { ESCAPE, QUOTE, DOLLAR};
        for (String c : toEscape) {
            arg = arg.replace(c, ESCAPE + c);
        }
        return new StringBuffer(QUOTE).append(arg).append(QUOTE).toString();
    }

    /* Nasty, nasty hack to print a given System.DateTime field followed
     * by a simple string representation of it relative to UTC. This allows
     * us to parse the date, which we can't do with the standard string
     * representation of System.DateTime.
     */
    public static String getDateHack(String field) {
        StringBuilder buf = new StringBuilder();
        buf.append("$d = $_." + field + "; ");
        buf.append("$d; ");
        buf.append("$d = [system.datetime]::specifykind($d, [system.datetimeKind]::utc); ");
        buf.append("[string]::format(\"{0}:{1}:{2}:{3}:{4}:{5}\", ");
        buf.append("$d.year, $d.month, $d.day, $d.hour,$d.minute, $d.second); ");
        return buf.toString();
    }

    private static DatatypeFactory datatypeFactory;

    private static DatatypeFactory getDatatypeFactory() {
        if (datatypeFactory == null) {
            try {
                datatypeFactory = DatatypeFactory.newInstance();
            } catch (javax.xml.datatype.DatatypeConfigurationException dtce) {
                log.warn("Failed to initialize DatatypeFactory: " + dtce);
            }
        }
        return datatypeFactory;
    }

    public static XMLGregorianCalendar parseDate(String date) {
        String[] parts = date.split(":");
        return getDatatypeFactory().newXMLGregorianCalendar(Integer.parseInt(parts[0]),
                                                            Integer.parseInt(parts[1]),
                                                            Integer.parseInt(parts[2]),
                                                            Integer.parseInt(parts[3]),
                                                            Integer.parseInt(parts[4]),
                                                            Integer.parseInt(parts[5]),
                                                            0, 0);
    }

    public static XMLGregorianCalendar getDate(int secondsAgo) {
        if (secondsAgo == 0) {
            return null;
        }
        XMLGregorianCalendar ret =
            getDatatypeFactory().newXMLGregorianCalendar(new GregorianCalendar(TimeZone.getTimeZone("UTC")));
        ret.add(getDatatypeFactory().newDuration(false, 0, 0, 0, 0, 0, secondsAgo));
        return ret;
    }

    public static String encode(Boolean b) {
        return Boolean.TRUE.equals(b) ? "$true" : "$false";
    }

    public static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }
}
