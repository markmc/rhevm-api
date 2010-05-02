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
package com.redhat.rhevm.api.dummy.resource;

import org.junit.Test;

import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.DataCenters;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomainStatus;

public class DummyDataCenterResourceTest extends DummyTestBase {
    private DummyTestBase.DataCentersResource getService() {
        return createDataCentersResource(getEntryPoint("datacenters").getHref());
    }

    private DummyTestBase.StorageDomainsResource getStorageDomainsService() {
        return createStorageDomainsResource(getEntryPoint("storagedomains").getHref());
    }

    private void checkDataCenter(DataCenter dataCenter) {
        assertNotNull(dataCenter.getName());
        assertNotNull(dataCenter.getId());
        assertNotNull(dataCenter.getLink());
        assertNotNull(dataCenter.getLink().getRel());
        assertNotNull(dataCenter.getLink().getHref());
        assertTrue(dataCenter.getLink().getHref().endsWith("/datacenters/" + dataCenter.getId()));
    }

    @Test
    public void testGetStorageDomainsList() throws Exception {
        DummyTestBase.DataCentersResource service = getService();
        assertNotNull(service);

        DataCenters dataCenters = service.list();
        assertNotNull(dataCenters);
        assertTrue(dataCenters.getDataCenters().size() > 0);

        for (DataCenter dataCenter : dataCenters.getDataCenters()) {
            checkDataCenter(dataCenter);

            DataCenter d = service.get(dataCenter.getId());
            checkDataCenter(d);
            assertEquals(dataCenter.getId(), d.getId());
        }
    }

    @Test
    public void testStorageDomainAttach() throws Exception {
        DummyTestBase.DataCentersResource service = getService();
        assertNotNull(service);

        DummyTestBase.StorageDomainsResource storageDomainsService = getStorageDomainsService();
        assertNotNull(service);

        StorageDomain domain = storageDomainsService.list().getStorageDomains().get(0);
        assertNotNull(domain);

        DataCenter dataCenter = service.list().getDataCenters().get(0);

        domain = service.attachStorageDomain(dataCenter.getId(), domain);
        assertNotNull(domain);

        domain = storageDomainsService.get(domain.getId());
        assertEquals(domain.getStatus(), StorageDomainStatus.INACTIVE);

    }
}
