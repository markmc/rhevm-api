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
package com.redhat.rhevm.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.model.Actionable;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.CdRom;
import com.redhat.rhevm.api.model.CdRoms;
import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.Disks;
import com.redhat.rhevm.api.model.Floppy;
import com.redhat.rhevm.api.model.Floppies;
import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.Nics;
import com.redhat.rhevm.api.model.VM;


@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public interface VmResource extends UpdatableResource<VM> {

    @Path("{action: (start|stop|shutdown|suspend|detach|migrate|export|ticket)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action")String action, @PathParam("oid")String oid);

    @POST
    @Formatted
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("start")
    public Response start(Action action);

    @POST
    @Formatted
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("stop")
    public Response stop(Action action);

    @POST
    @Formatted
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("shutdown")
    public Response shutdown(Action action);

    @POST
    @Formatted
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("suspend")
    public Response suspend(Action action);

    @POST
    @Formatted
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("detach")
    public Response detach(Action action);

    @POST
    @Formatted
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("migrate")
    public Response migrate(Action action);

    @POST
    @Formatted
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("export")
    public Response export(Action action);

    @POST
    @Formatted
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Actionable
    @Path("ticket")
    public Response ticket(Action action);

    @Path("cdroms")
    public DevicesResource<CdRom, CdRoms> getCdRomsResource();

    @Path("disks")
    public DevicesResource<Disk, Disks> getDisksResource();

    @Path("floppies")
    public DevicesResource<Floppy, Floppies> getFloppiesResource();

    @Path("nics")
    public DevicesResource<NIC, Nics> getNicsResource();

    @Path("snapshots")
    public SnapshotsResource getSnapshotsResource();

    @Path("users")
    public AttachedUsersResource getUsersResource();

    @Path("tags")
    public AssignedTagsResource getTagsResource();

    @Path("permissions")
    public AssignedPermissionsResource getPermissionsResource();
}
