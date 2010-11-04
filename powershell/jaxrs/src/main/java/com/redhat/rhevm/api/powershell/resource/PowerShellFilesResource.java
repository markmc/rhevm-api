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

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.File;
import com.redhat.rhevm.api.model.Files;
import com.redhat.rhevm.api.resource.FileResource;
import com.redhat.rhevm.api.resource.FilesResource;
import com.redhat.rhevm.api.powershell.model.PowerShellFile;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellFilesResource extends UriProviderWrapper implements FilesResource {

    private String dataCenterId;

    public PowerShellFilesResource(String dataCenterId,
                                  PowerShellPoolMap shellPools,
                                  PowerShellParser parser,
                                  UriInfoProvider uriProvider) {
        super(null, shellPools, parser, uriProvider);
        this.dataCenterId = dataCenterId;
    }

    @Override
    public Files list() {
        StringBuilder buf = new StringBuilder();
        buf.append("$d = get-datacenter " + PowerShellUtils.escape(dataCenterId));
        buf.append("; ");
        buf.append("if ($d.status -eq \"Up\") { ");
        buf.append("get-isoimages");
        buf.append(" -datacenterid " + PowerShellUtils.escape(dataCenterId));
        buf.append("}");
        Files ret = new Files();
        for (File file : PowerShellFile.parse(parser, PowerShellCmd.runCommand(getPool(), buf.toString()))) {
            ret.getFiles().add(addLinks(file, dataCenterId));
        }
        return ret;
    }

    @Override
    public FileResource getFileSubResource(String id) {
        return new PowerShellFileResource(id, dataCenterId, this);
    }

    public File addLinks(File file, String dataCenterId) {
        file.setDataCenter(new DataCenter());
        file.getDataCenter().setId(dataCenterId);
        return LinkHelper.addLinks(getUriInfo(), file);
    }
}
