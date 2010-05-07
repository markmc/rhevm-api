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

import javax.ws.rs.Path;
import javax.ws.rs.POST;

import com.redhat.rhevm.api.model.Actionable;
import com.redhat.rhevm.api.model.Host;

public interface HostResource extends UpdatableResource<Host> {

    @POST
    @Actionable
    @Path("approve")
    public void approve();

    @POST
    @Actionable
    @Path("fence")
    public void fence();

    @POST
    @Actionable
    @Path("resume")
    public void resume();

    //@WebMethod public void connectStorage(String id, String storageDevice);
}
