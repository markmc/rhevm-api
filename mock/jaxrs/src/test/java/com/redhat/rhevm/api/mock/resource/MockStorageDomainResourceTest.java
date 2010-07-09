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
package com.redhat.rhevm.api.mock.resource;

import org.junit.Test;

import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.StorageDomainStatus;

public class MockStorageDomainResourceTest extends MockTestBase {
    private MockTestBase.StorageDomainsResource getService() {
        return createStorageDomainsResource(getEntryPoint("storagedomains").getHref());
    }

    private void checkStorageDomain(StorageDomain domain) {
        assertNotNull(domain.getName());
        assertNotNull(domain.getId());
        assertNotNull(domain.getHref());
        assertTrue(domain.getHref().endsWith("storagedomains/" + domain.getId()));
        assertNotNull(domain.getActions());
        assertTrue(domain.getActions().getLinks().size() > 0);
        boolean includesLink = false;
        for (Link actionLink : domain.getActions().getLinks()) {
            includesLink = actionLink.getHref().endsWith("storagedomains/" + domain.getId() + "/teardown");
            if (includesLink) {
                break;
            }
        }
        assertTrue("expected teardown link", includesLink);
    }

    @Test
    public void testGetStorageDomainsList() throws Exception {
        MockTestBase.StorageDomainsResource service = getService();
        assertNotNull(service);

        StorageDomains storageDomains = service.list(null);
        assertNotNull(storageDomains);
        assertTrue(storageDomains.getStorageDomains().size() > 0);

        for (StorageDomain domain : storageDomains.getStorageDomains()) {
            checkStorageDomain(domain);

            StorageDomain d = service.get(domain.getId());
            checkStorageDomain(d);
            assertEquals(domain.getId(), d.getId());
        }
    }

    @Test
    public void testGetStorageDomainsQuery() throws Exception {
        MockTestBase.StorageDomainsResource service = getService();
        assertNotNull(service);

        StorageDomains storageDomains = service.list("name=images*");
        assertNotNull(storageDomains);
        assertEquals("unepected number of query matches", 1, storageDomains.getStorageDomains().size());

        StorageDomain domain = storageDomains.getStorageDomains().get(0);
        checkStorageDomain(domain);

        StorageDomain d = service.get(domain.getId());
        checkStorageDomain(d);
        assertEquals(domain.getId(), d.getId());
    }

    private String getActionUri(StorageDomain domain, String action) {
        String uri = null;

        for (Link actionLink : domain.getActions().getLinks()) {
            if (actionLink.getRel().equals(action)) {
                uri = API_URI + actionLink.getHref();
                System.out.println("URI for " + action + " is " + uri);
                break;
            }
        }

        assertNotNull(uri);

        return uri;
    }

    @Test
    public void testStorageDomainStatus() throws Exception {
        MockTestBase.StorageDomainsResource service = getService();
        assertNotNull(service);

        StorageDomain domain = service.list(null).getStorageDomains().get(0);
        assertNotNull(domain);

        assertEquals(domain.getStatus(), StorageDomainStatus.UNATTACHED);

/* FIXME
        createActionResource(getActionUri(domain, "attach")).post(action);

        domain = service.get(domain.getId());
        assertEquals(domain.getStatus(), StorageDomainStatus.INACTIVE);
        createActionResource(getActionUri(domain, "activate")).post(action);

        domain = service.get(domain.getId());
        assertEquals(domain.getStatus(), StorageDomainStatus.ACTIVE);
        createActionResource(getActionUri(domain, "deactivate")).post(action);

        domain = service.get(domain.getId());
        assertEquals(domain.getStatus(), StorageDomainStatus.INACTIVE);
        createActionResource(getActionUri(domain, "detach")).post(action);

        domain = service.get(domain.getId());
        assertEquals(domain.getStatus(), StorageDomainStatus.UNATTACHED);
*/
    }
}
