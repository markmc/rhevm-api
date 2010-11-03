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
package com.redhat.rhevm.api.powershell.resource;

import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.util.QueryHelper;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class AbstractPowerShellResource {

    protected final static String SEARCH_TEXT = " -searchtext ";

    protected PowerShellParser parser;
    protected PowerShellPoolMap shellPools;
    protected Executor executor;

    protected AbstractPowerShellResource() {
    }

    protected AbstractPowerShellResource(PowerShellPoolMap shellPools,
                                         PowerShellParser parser) {
        this(null, shellPools, parser);
    }

    protected AbstractPowerShellResource(Executor executor,
                                         PowerShellPoolMap shellPools,
                                         PowerShellParser parser) {
        this.executor = executor;
        this.shellPools = shellPools;
        this.parser = parser;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public PowerShellParser getParser() {
        return parser;
    }

    public void setParser(PowerShellParser parser) {
        this.parser = parser;
    }

    public void setPowerShellPoolMap(PowerShellPoolMap shellPools) {
        this.shellPools = shellPools;
    }

    protected PowerShellPool getPool() {
        return shellPools.get();
    }

    protected String getSelectCommand(String root, UriInfo uriInfo, Class<?> collectionType) {
        return getSelectCommand(root, uriInfo, collectionType, true);
    }

    protected String getSelectCommand(String root, UriInfo uriInfo, Class<?> collectionType, boolean typePrefix) {
        String ret = root;
        String constraint = QueryHelper.getConstraint(uriInfo, collectionType, typePrefix);
        if (constraint != null) {
            ret = new StringBuffer(root).append(SEARCH_TEXT)
                                        .append(PowerShellUtils.escape(constraint))
                                        .toString();
        }
        return ret;
    }

}
