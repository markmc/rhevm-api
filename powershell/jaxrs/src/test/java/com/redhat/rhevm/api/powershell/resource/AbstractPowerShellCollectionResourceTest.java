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

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.common.resource.AbstractActionableResource;
import com.redhat.rhevm.api.common.resource.AbstractUpdatableResource;
import com.redhat.rhevm.api.common.util.ReflectionHelper;

import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

import static org.easymock.classextension.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;


@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellUtils.class, PowerShellCmd.class })
public abstract class AbstractPowerShellCollectionResourceTest<R extends BaseResource,
                                                               U extends AbstractUpdatableResource<R>,
                                                               T extends AbstractPowerShellCollectionResource<R, U>>
    extends Assert {

    private static final String URI_ROOT = "http://localhost:8099";

    protected static final String[] NAMES = {"sedna", "eris", "orcus"};
    protected static final String NEW_NAME = "ceres";
    private static final String SELECT_COMMAND = "select-{0}";
    private static final String SELECT_RETURN =
	"{0}id: " + "sedna".hashCode() + " \n name: sedna\n\n" +
	"{0}id: " + "eris".hashCode() + " \n name: eris\n\n" +
	"{0}id: " + "orcus".hashCode() + " \n name: orcus";

    private static final String ADD_COMMAND = "add-{0} -name ceres ";
    private static final String ADD_RETURN =
	"{0}id: " + "ceres".hashCode() + " \n name: ceres\n\n";

    private static final String REMOVE_COMMAND = "remove-{0} -{0}id " + "eris".hashCode();

    protected T resource;
    protected U updatable;
    protected String collectionName;
    protected String individualName;
    protected Executor executor;

    protected AbstractPowerShellCollectionResourceTest(U updatable, String collectionName, String individualName) {
	this.updatable = updatable;
	this.collectionName = collectionName;
	this.individualName = individualName;
    }

    @Before
    public void setUp() {
	executor = new ControllableExecutor();
	resource = getResource();
        resource.setExecutor(executor);
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    protected UriInfo setUpResourceExpectations(String command, String ret, String ... names) throws Exception {
        if (command != null) {
            mockStatic(PowerShellCmd.class);
            expect(PowerShellCmd.runCommand(command)).andReturn(ret);
        }
        if (ret != null) {
            mockStatic(PowerShellUtils.class);
            expect(PowerShellUtils.parseProps(ret)).andReturn(getProps(names));
        }
        UriInfo uriInfo = createMock(UriInfo.class);
        for (String name : names) {
            UriBuilder uriBuilder = createMock(UriBuilder.class);
            expect(uriInfo.getRequestUriBuilder()).andReturn(uriBuilder);
            expect(uriBuilder.path(Integer.toString(name.hashCode()))).andReturn(uriBuilder);
            expect(uriBuilder.build()).andReturn(new URI(URI_ROOT + collectionName + name.hashCode())).anyTimes();
        }
        replayAll();

        return uriInfo;
    }

    protected String getSelectCommand() {
	return MessageFormat.format(SELECT_COMMAND, individualName);
    }

    protected String getSelectReturn() {
	return MessageFormat.format(SELECT_RETURN, individualName);
    }

    protected String getAddCommand() {
	return MessageFormat.format(ADD_COMMAND, individualName);
    }

    protected String getAddReturn() {
	return MessageFormat.format(ADD_RETURN, individualName);
    }

    protected String getRemoveCommand() {
	return MessageFormat.format(REMOVE_COMMAND, individualName);
    }

    private ArrayList<HashMap<String,String>> getProps(String ... names) {
        ArrayList<HashMap<String,String>> parsedProps = new ArrayList<HashMap<String,String>>();
        for (String name : names) {
            HashMap<String,String> vmProps = new HashMap<String,String>();
            vmProps.put(individualName + "id", Integer.toString(name.hashCode()));
            vmProps.put("name", name);
            parsedProps.add(vmProps);
        }
        return parsedProps;
    }

    protected R getModel(String name) {
        R model = ReflectionHelper.newModel(updatable);
        model.setId(Integer.toString(name.hashCode()));
        model.setName(name);
        setExtraProperties(model);
        return model;
    }

    protected void verifyResponse(Response r, String name) {
        assertEquals("unexpected status", 201, r.getStatus());
        Object entity = r.getEntity();
        assertTrue("expect response entity", entity instanceof BaseResource);
        BaseResource model = (BaseResource)entity;
        assertEquals(model.getId(), Integer.toString(name.hashCode()));
        assertEquals(model.getName(), name);
        assertNotNull(r.getMetadata().get("Location"));
        assertTrue("expected location header",
        	   r.getMetadata().get("Location").size() > 0);
        assertEquals("unexpected location header",
        	     URI_ROOT + collectionName + name.hashCode(),
        	     r.getMetadata().get("Location").get(0).toString());
    }

    protected void verifyCollection(List<R> collection, String ... names) {
        assertNotNull(collection);
        assertEquals("unexpected number of VMs", collection.size(), names.length);
        for (String name: names) {
            R model = collection.remove(0);
            assertEquals(model.getId(), Integer.toString(name.hashCode()));
            assertEquals(model.getName(), name);
        }
    }

    protected void verifyResource(AbstractActionableResource<R> resource, String name) {
	assertNotNull(resource);
	assertEquals(resource.getId(), Integer.toString(name.hashCode()));
	assertSame(resource.getExecutor(), executor);
    }

    protected abstract T getResource();

    protected abstract void setExtraProperties(R model);
}

