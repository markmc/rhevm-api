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
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;

public class DummyStorageDomainResourceTest extends DummyTestBase {
    private DummyTestBase.StorageDomainsResource getService() {
        return createStorageDomainsResource(getEntryPoint("storagedomains").getHref());
    }

    private void checkStorageDomain(StorageDomain domain) {
        assertNotNull(domain.getName());
        assertNotNull(domain.getId());
        assertNotNull(domain.getLink());
        assertNotNull(domain.getLink().getRel());
        assertNotNull(domain.getLink().getHref());
        assertTrue(domain.getLink().getHref().endsWith("/storagedomains/" + domain.getId()));
        assertNotNull(domain.getActions());
        assertTrue(domain.getActions().getLinks().size() > 0);
        boolean includesInitLink = false;
        for (Link actionLink : domain.getActions().getLinks()) {
            includesInitLink = actionLink.getHref().endsWith("/storagedomains/" + domain.getId() + "/initialize");
            if (includesInitLink) {
                break;
            }
        }
        assertTrue("expected initialize link", includesInitLink);
    }

    @Test
    public void testGetStorageDomainsList() throws Exception {
        DummyTestBase.StorageDomainsResource service = getService();
        assertNotNull(service);

        StorageDomains storageDomains = service.list();
        assertNotNull(storageDomains);
        assertTrue(storageDomains.getStorageDomains().size() > 0);

        for (StorageDomain domain : storageDomains.getStorageDomains()) {
            checkStorageDomain(domain);

            StorageDomain d = service.get(domain.getId());
            checkStorageDomain(d);
            assertEquals(domain.getId(), d.getId());
        }
    }
}
