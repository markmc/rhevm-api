package com.redhat.rhevm.api.dummy;

import java.util.List;
import org.junit.Test;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.client.ClientResponse;
import com.redhat.rhevm.api.Link;
import com.redhat.rhevm.api.VM;

public class DummyVMsTest extends DummyTestBase
{
	private VMs getService() {
		return createVMs(getEntryPoint("vms").getHref());
	}

	private void checkVM(VM vm) {
		// FIXME: assertNotNull(vm.getName());
		assertNotNull(vm.getId());
		assertNotNull(vm.getLink());
		assertNotNull(vm.getLink().getRel());
		assertNotNull(vm.getLink().getHref());
		assertTrue(vm.getLink().getHref().endsWith(vm.getId()));
		assertNotNull(vm.getActions());
		assertTrue(vm.getActions().getLinks().size() > 0);
	}

	@Test
	public void testGetVmsList() throws Exception {
		VMs service = getService();
		assertNotNull(service);

		List<VM> vms = service.list();
		assertNotNull(vms);
		assertTrue(vms.size() > 0);

		for (VM vm : vms) {
			checkVM(vm);

			VM t = service.get(vm.getId());
			checkVM(t);
			assertEquals(vm.getId(), t.getId());
		}
	}
}
