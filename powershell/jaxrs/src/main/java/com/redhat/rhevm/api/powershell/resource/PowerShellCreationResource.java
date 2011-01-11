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

import java.net.URLEncoder;
import java.util.concurrent.Executor;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Creation;
import com.redhat.rhevm.api.resource.CreationResource;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.powershell.model.PowerShellAsyncTask;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import static com.redhat.rhevm.api.common.util.LinkHelper.combine;

public class PowerShellCreationResource extends AbstractPowerShellActionableResource<Creation> implements CreationResource {

    private String ids;

    public PowerShellCreationResource(String ids,
                                      Executor executor,
                                      UriInfoProvider uriProvider,
                                      PowerShellPoolMap shellPools,
                                      PowerShellParser parser) {
        super(PowerShellAsyncTask.parseTaskIds(ids)[0], executor, uriProvider, shellPools, parser);
        this.ids = ids;
    }

    public Creation runAndParseSingle(String command) {
        return PowerShellAsyncTask.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    public Creation addLinks(Creation creation) {
        // we avoid using the LinkHelper as the CreationResource does not
        // follow the usual pattern of collection and sub-resource
        creation.setHref(UriBuilder.fromPath(getPath(getUriInfo())).build().toString());
        return creation;
    }

    @Override
    public Creation get() {
        return addLinks(runAndParseSingle("get-tasksstatus -commandtasksids " + ids));
    }

    private String getPath(UriInfo uriInfo) {
        StringBuilder path = new StringBuilder();
        // avoid encoding forward slashes to keep URI looking consistent
        for (String p : uriInfo.getPath().split("/")) {
            (path.length() == 0 ? path : path.append("/")).append(URLEncoder.encode(p));
        }
        return combine(uriInfo.getBaseUri().getPath(), path.toString());
    }
}
