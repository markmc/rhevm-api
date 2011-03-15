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
package com.redhat.rhevm.api.common.util;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;

import org.junit.Assert;
import org.junit.Test;


import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;


public class QueryHelperTest extends Assert {

    private static final String QUERY = "name=zibert AND id=0*";

    @Test
    public void testGetVMConstraint() throws Exception {
        doTestGetConstraint(VM.class, "VMs : ");
    }

    @Test
    public void testGetHostConstraint() throws Exception {
        doTestGetConstraint(Host.class, "Hosts : ");
    }

    @Test
    public void testGetClusterConstraint() throws Exception {
        doTestGetConstraint(Cluster.class, "Clusters : ");
    }

    @Test
    public void testGetDataCenterConstraint() throws Exception {
        doTestGetConstraint(DataCenter.class, "Datacenter : ");
    }

    @Test
    public void testGetStorageConstraint() throws Exception {
        doTestGetConstraint(StorageDomain.class, "Storage : ");
    }

    @Test
    public void testGetTemplateConstraint() throws Exception {
        doTestGetConstraint(Template.class, "Template : ");
    }

    private void doTestGetConstraint(Class<?> clz, String expectedPrefix) throws Exception {

        UriInfo uriInfo = createMock(UriInfo.class);
        MultivaluedMap<String, String> queries = createMock(MultivaluedMap.class);
        List<String> queryParam = new ArrayList<String>();
        queryParam.add(QUERY);
        expect(queries.get("search")).andReturn(queryParam).anyTimes();
        expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();

        replay(uriInfo, queries);

        if ("".equals(expectedPrefix)) {
            assertEquals(QUERY, QueryHelper.getConstraint(uriInfo, clz, false));
        } else {
            assertEquals(expectedPrefix + QUERY, QueryHelper.getConstraint(uriInfo, clz));
        }

        verify(uriInfo, queries);
    }

    @Test
    public void testGetVMConstraintNoPrefix() throws Exception {
        doTestGetConstraint(VM.class, "VMs : ");
    }

    @Test
    public void testGetHostConstraintNoPrefix() throws Exception {
        doTestGetConstraint(Host.class, "Hosts : ");
    }

    @Test
    public void testGetClusterConstraintNoPrefix() throws Exception {
        doTestGetConstraint(Cluster.class, "Clusters : ");
    }

    @Test
    public void testGetDataCenterConstraintNoPrefix() throws Exception {
        doTestGetConstraint(DataCenter.class, "Datacenter : ");
    }

    @Test
    public void testGetStorageConstraintNoPrefix() throws Exception {
        doTestGetConstraint(StorageDomain.class, "Storage : ");
    }

    @Test
    public void testGetTemplateConstraintNoPrefix() throws Exception {
        doTestGetConstraint(Template.class, "Template : ");
    }

    @Test
    public void testGetVMDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(VM.class, "VMs : ");
    }

    @Test
    public void testGetHostDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(Host.class, "Hosts : ");
    }

    @Test
    public void testGetClusterDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(Cluster.class, "Clusters : ");
    }

    @Test
    public void testGetDataCenterDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(DataCenter.class, "Datacenter : ");
    }

    @Test
    public void testGetStorageDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(StorageDomain.class, "Storage : ");
    }

    @Test
    public void testGetTemplateDefaultConstraint() throws Exception {
        doTestGetDefaultConstraint(Template.class, "Template : ");
    }

    private void doTestGetDefaultConstraint(Class<?> clz, String expectedConstraint) throws Exception {
        UriInfo uriInfo = createMock(UriInfo.class);
        MultivaluedMap<String, String> queries = createMock(MultivaluedMap.class);
        List<String> queryParam = new ArrayList<String>();
        expect(queries.get("search")).andReturn(queryParam).anyTimes();
        expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();

        replay(uriInfo, queries);

        assertEquals(expectedConstraint, QueryHelper.getConstraint(uriInfo, "", clz));

        verify(uriInfo, queries);
    }
}
