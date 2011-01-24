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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Event;
import com.redhat.rhevm.api.model.LogSeverity;

import com.redhat.rhevm.api.powershell.util.ControllableExecutor;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.easymock.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
public class PowerShellEventsResourceTest extends BasePowerShellResourceTest {

    private static final String[] NAMES = new String[]{ "1", "2", "3", "4" };
    private static final String[] IDS = new String[] { Integer.toString(NAMES[0].hashCode()),
                                                       Integer.toString(NAMES[1].hashCode()),
                                                       Integer.toString(NAMES[2].hashCode()),
                                                       Integer.toString(NAMES[3].hashCode()) };
    private static final String[] DESCRIPTIONS = new String[] {"First event", "Second event", "Third event", "Fourth event"};

    private static final String SELECT_COMMAND = "select-event";
    private static final String QUERY = "type = 111";
    private static final String QUERY_COMMAND = SELECT_COMMAND + " -searchtext \"Events : " + QUERY + "\"";
    private static final String GET_COMMAND = SELECT_COMMAND + " | ? { $_.id -eq \"" + IDS[0] + "\" }";

    private static final Integer CODE = 111;
    private static final LogSeverity SEVERITY = LogSeverity.NORMAL;

    private static final String[] extraArgs = new String[] { CODE.toString(), Integer.toString(SEVERITY.ordinal()) };

    protected PowerShellEventsResource resource;
    protected Executor executor;
    protected PowerShellParser parser;
    protected HttpHeaders httpHeaders;

    @Before
    public void setUp() throws Exception {
        resource = new PowerShellEventsResource();
        resource.setExecutor(executor = new ControllableExecutor());
        resource.setParser(parser = PowerShellParser.newInstance());
        resource.setHttpHeaders(httpHeaders = createMock(HttpHeaders.class));
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testList() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(SELECT_COMMAND, formatReturn(), null));
        verifyCollection(resource.list().getEvent());
    }


    @Test
    public void testQuery() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(QUERY_COMMAND, formatReturn(), QUERY));
        verifyCollection(resource.list().getEvent());
    }

    @Test
    public void testGet() throws Exception {
        PowerShellEventResource subResource = new PowerShellEventResource(IDS[0], resource);
        resource.setUriInfo(setUpResourceExpectations(GET_COMMAND, formatReturn(), null));
        verifyModel(subResource.get(), 0);
    }

    @Test
    public void testGetSubResource() throws Exception {
        resource.setUriInfo(setUpBasicUriExpectations());
        replayAll();
        verifySubResource((PowerShellEventResource)resource.getEventSubResource(IDS[0]), IDS[0]);
    }

    private String formatReturn() {
        return formatXmlReturn("event", NAMES, DESCRIPTIONS, extraArgs);
    }

    protected PowerShellPool setUpPoolExpectations() {
        PowerShellPoolMap poolMap = createMock(PowerShellPoolMap.class);
        resource.setPowerShellPoolMap(poolMap);
        PowerShellPool pool = createMock(PowerShellPool.class);
        expect(poolMap.get()).andReturn(pool);
        return pool;
    }

    protected UriInfo setUpResourceExpectations(String command, String ret, String query) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);

        UriInfo uriInfo = setUpBasicUriExpectations();

        if (query != null) {
            MultivaluedMap<String, String> queries = createMock(MultivaluedMap.class);
            List<String> queryParam = new ArrayList<String>();
            queryParam.add(query);
            expect(queries.get("search")).andReturn(queryParam).anyTimes();
            expect(uriInfo.getQueryParameters()).andReturn(queries).anyTimes();
        } else {
            expect(uriInfo.getQueryParameters()).andReturn(null).anyTimes();
        }

        replayAll();

        return uriInfo;
    }

    protected void verifyCollection(List<Event> collection) {
        assertNotNull(collection);
        assertEquals("unexpected collection size", collection.size(), NAMES.length);
        for (int i = 0; i < NAMES.length; i++) {
            verifyModel(collection.get(i), i);
        }
    }

    protected void verifyModel(Event model, int index) {
        assertEquals(IDS[index], model.getId());
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        assertEquals(CODE, model.getCode());
        assertEquals(SEVERITY, model.getSeverity());
    }

    protected void verifySubResource(PowerShellEventResource subResource, String id) {
        assertNotNull(subResource);
        assertEquals(id, subResource.getId());
        assertSame(resource, subResource.getParent());
    }
}
