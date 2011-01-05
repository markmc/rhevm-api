#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

from rhev import schema
from rhev.test import util
from rhev.test.base import BaseTest
from rhev.test.loader import depends
from nose import SkipTest


class TestVM(BaseTest):

    def test_prepare(self):
        dcname = self.get_config('datacenter')
        if dcname is None:
            raise SkipTest, 'Parameter "datacenter" not set.'
        clname = self.get_config('cluster')
        if clname is None:
            raise SkipTest, 'Parameter "cluster" not set.'
        datacenter = self.api.get(schema.DataCenter, name=dcname)
        if datacenter is None:
            raise SkipTest, 'Datacenter not found: %s' % dcname
        cluster = self.api.get(schema.Cluster, name=clname)
        if cluster is None:
            raise SkipTest, 'Cluster not found: %s' % clname
        # BUG: ticket #104
        #template = self.api.get(schema.Template, special='blank')
        template = self.api.get(schema.Template, name='Blank')
        if template is None:
            raise SkipTest, 'Blank template not found.'
        #XXX: network issue: #157
        network = schema.new(schema.Network)
        network.id = '00000000-0000-0000-0000-000000000009'
        # End hack
        self.store.datacenter = datacenter
        self.store.cluster = cluster
        self.store.template = template
        self.store.network = network

    @depends(test_prepare)
    def test_create_vm(self):
        vm = schema.new(schema.VM)
        vm.name = util.random_name('vm')
        vm.type = 'SERVER'
        vm.memory = 1024**3
        vm.cpu = schema.new(schema.CPU)
        vm.cpu.topology = schema.new(schema.CpuTopology)
        vm.cpu.topology.cores = 2
        vm.cpu.topology.sockets = 1
        vm.display = schema.new(schema.Display)
        vm.display.type = 'VNC'
        vm.os = schema.new(schema.OperatingSystem)
        vm.os.type = 'RHEL5'
        # XXX: this is a bit of a kludge. Ticket #125
        vm.os.boot = [schema.new(schema.Boot)]
        vm.os.boot[0].dev = 'hd'
        # XXX: this is a bit of a kludge
        vm.highly_available = schema.new(schema.HighlyAvailable, True)
        vm.highly_available.priority = 10
        vm.cluster = schema.ref(self.store.cluster)
        vm.template = schema.ref(self.store.template)
        vm2 = self.api.create(vm)
        assert isinstance(vm2, schema.VM)
        assert vm2.id is not None
        vm = self.api.get(schema.VM, name=vm2.name)
        assert isinstance(vm, schema.VM)
        assert vm.id == vm2.id
        self.store.vm = vm

    @depends(test_create_vm)
    def test_get(self):
        vm = self.store.vm
        vm2 = self.api.get(schema.VM, id=vm.id)
        assert isinstance(vm2, schema.VM)
        assert vm2.id == vm.id

    @depends(test_create_vm)
    def test_reload(self):
        vm = self.store.vm
        vm2 = self.api.reload(vm)
        assert isinstance(vm2, schema.VM)
        assert vm2.id == vm.id

    @depends(test_create_vm)
    def test_getall(self):
        vms = self.api.getall(schema.VM)
        assert isinstance(vms, schema.VMs)
        for vm in vms:
            assert isinstance(vm, schema.VM)
        vm = self.store.vm
        assert util.contains_id(vms, vm.id)
            
    @depends(test_create_vm)
    def test_search(self):
        vm = self.store.vm
        vms = self.api.getall(schema.VM, name=vm.name)
        assert isinstance(vms, schema.VMs)
        assert len(vms) == 1
        assert vms[0].id == vm.id
        vms = self.api.getall(schema.VM, search='name=%s' % vm.name)
        assert isinstance(vms, schema.VMs)
        assert len(vms) == 1
        assert vms[0].id == vm.id

    @depends(test_create_vm)
    def test_update(self):
        vm = self.store.vm
        # BUG: ticket #179
        vm.type = None
        vm.description = 'foo'
        vm2 = self.api.update(vm)
        assert vm2.description == vm.description
        vm = self.api.reload(vm2)
        assert vm.description == vm2.description
        self.store.vm = vm

    @depends(test_create_vm)
    def test_attributes(self):
        vm = self.store.vm
        vm = self.api.reload(vm)
        assert util.is_str_uuid(vm.id)
        assert util.is_str_href(vm.href)
        assert util.is_str(vm.name)
        assert vm.description is None or util.is_str(vm.description)
        assert vm.type in ('SERVER', 'DESKTOP')
        assert vm.status in ('UNASSIGNED', 'DOWN', 'UP', 'POWERING_UP',
                'POWERED_DOWN', 'PAUSED', 'MIGRATING_FROM', 'MIGRATING_TO',
                'UNKNOWN', 'NOT_RESPONDING', 'WAIT_FOR_LAUNCH',
                'REBOOT_IN_PROGRESS', 'SAVING_STATE', 'RESTORING_STATE',
                'SUSPENDED', 'IMAGE_ILLEGAL', 'IMAGE_LOCKED', 'POWERING_DOWN')
        assert vm.origin in ('RHEV', 'VMWARE', 'XEN')
        assert util.is_str_date(vm.creation_time)
        assert util.is_int(vm.memory) and vm.memory > 0
        assert vm.cpu is not None
        assert vm.cpu.topology is not None
        assert util.is_int(vm.cpu.topology.cores)
        assert vm.cpu.topology.cores > 0
        assert util.is_int(vm.cpu.topology.sockets)
        assert vm.cpu.topology.sockets > 0
        assert vm.display is not None
        assert vm.display.type in ('VNC', 'SPICE')
        if vm.display.port is not None:
            assert util.is_int(vm.display.port)
            assert vm.display.port > 0
        assert util.is_int(vm.display.monitors)
        assert vm.display.monitors > 0
        # BUG: ticket #180
        assert util.is_str_host(vm.display.address)
        assert vm.os is not None
        assert util.is_str(vm.os.type)
        assert vm.os.boot is not None
        # Upper case?
        for dev in vm.os.boot:
            assert dev.dev in ('cdrom', 'hd', 'network')
        # BUG: missing when false: #183
        #assert util.is_bool(vm.stateless)
        if vm.host is not None:
            assert util.is_str_int(vm.host.id) or util.is_str_uuid(vm.host.id)
        assert vm.cluster is not None
        assert util.is_str_int(vm.cluster.id) or util.is_str_uuid(vm.cluster.id)
        assert vm.template is not None
        assert util.is_str_uuid(vm.template.id)
        if vm.vmpool is not None:
            assert util.is_str_uuid(vm.vmpool.id)

    @depends(test_create_vm)
    def test_add_disk(self):
        vm = self.store.vm
        disk = schema.new(schema.Disk)
        disk.size = 8 * 1024**3
        disk.type = 'SYSTEM'
        disk.interface = 'VIRTIO'
        disk.format = 'COW'
        disk.sparse = True
        disk.wipe_after_delete = True
        disk.propagate_errors = True
        disk2 = self.api.create(disk, base=vm)
        assert isinstance(disk2, schema.Disk)
        assert disk2.id is not None
        self.store.disk = disk2

    @depends(test_add_disk)
    def test_disk_attributes(self):
        disk = self.store.disk
        assert util.is_int(disk.size)
        assert disk.type in ('DATA', 'SHARED', 'SYSTEM', 'SWAP', 'TEMP')
        assert disk.status in ('ILLEGAL', 'INVALID', 'LOCKED', 'OK')
        assert disk.interface in ('IDE', 'SCSI', 'VIRTIO')
        assert disk.format in ('COW', 'RAW')
        assert util.is_bool(disk.sparse)
        assert util.is_bool(disk.bootable)
        assert util.is_bool(disk.wipe_after_delete)

    @depends(test_create_vm)
    def test_add_nic(self):
        vm = self.store.vm
        network = self.store.network
        nic = schema.new(schema.NIC)
        nic.name = 'eth0'
        nic.type = 'PV'
        nic.network = schema.ref(network)
        nic2 = self.api.create(nic, base=vm)
        assert isinstance(nic2, schema.NIC)
        assert nic2.id is not None
        self.store.nic = nic2

    @depends(test_add_nic)
    def test_nic_attributes(self):
        nic = self.store.nic
        assert util.is_str(nic.name)
        assert nic.network is not None
        assert util.is_str_uuid(nic.network.id)
        assert nic.type in ('E1000', 'PV', 'RTL8139', 'RTL8139_PV')
        assert nic.mac is not None
        # BUG: ticket #177
        assert util.is_str_mac(nic.mac.address)

    @depends(test_create_vm)
    def test_start_vm(self):
        vm = self.store.vm
        action = self.api.action(vm, 'start')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(vm, 'POWERING_UP')

    @depends(test_start_vm)
    def test_stop_vm(self):
        vm = self.store.vm
        action = self.api.action(vm, 'stop')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(vm, 'DOWN')

    @depends(test_stop_vm)
    def test_delete_vm(self):
        vm = self.store.vm
        self.api.delete(vm)
        vm2 = self.api.get(schema.VM, name=vm.name)
        assert vm2 is None
