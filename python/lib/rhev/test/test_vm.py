#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

from rhev import *
from rhev.test import util
from rhev.test.base import BaseTest
from rhev.test.loader import depends

from nose import SkipTest
from nose.tools import assert_raises


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
        network = self.api.get(schema.Network, base=cluster,
                               filter={'name': 'rhevm'})
        if network is None:
            raise SkipTest, 'Network not found: rhevm'
        storagedomain = self.api.get(schema.StorageDomain, base=datacenter,
                                     filter={'type': 'DATA'})
        if storagedomain is None:
            raise SkipTest, 'No data domain found in datacenter'
        exportdomain = self.api.get(schema.StorageDomain, base=datacenter,
                                    filter={'type': 'EXPORT'})
        self.store.datacenter = datacenter
        self.store.cluster = cluster
        self.store.template = template
        self.store.network = network
        self.store.storagedomain = storagedomain
        self.store.exportdomain = exportdomain

    @depends(test_prepare)
    def test_create(self):
        vm = schema.new(schema.VM)
        vm.name = util.random_name('vm')
        vm.type = 'SERVER'
        vm.memory = 512*1024**2
        vm.cpu = schema.new(schema.CPU)
        vm.cpu.topology = schema.new(schema.CpuTopology)
        vm.cpu.topology.cores = 1
        vm.cpu.topology.sockets = 1
        vm.display = schema.new(schema.Display)
        vm.display.type = 'VNC'
        vm.os = schema.new(schema.OperatingSystem)
        vm.os.type = 'RHEL5'
        vm.os.boot = [schema.new(schema.Boot, dev='hd')]
        # XXX: this is a bit of a kludge: #229
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

    @depends(test_create)
    def test_create_duplicate_name(self):
        vm = self.store.vm
        vm2 = schema.new(schema.VM)
        vm2.name = vm.name
        vm2.type = 'SERVER'
        vm2.memory = 512*1024**2
        vm2.cluster = schema.ref(self.store.cluster)
        vm2.template = schema.ref(self.store.template)
        assert_raises(Fault, self.api.create, vm2)

    @depends(test_create)
    def test_get(self):
        vm = self.store.vm
        vm2 = self.api.get(schema.VM, id=vm.id)
        assert isinstance(vm2, schema.VM)
        assert vm2.id == vm.id

    @depends(test_create)
    def test_reload(self):
        vm = self.store.vm
        vm2 = self.api.reload(vm)
        assert isinstance(vm2, schema.VM)
        assert vm2.id == vm.id

    @depends(test_create)
    def test_getall(self):
        vms = self.api.getall(schema.VM)
        assert isinstance(vms, schema.VMs)
        for vm in vms:
            assert isinstance(vm, schema.VM)
        vm = self.store.vm
        assert util.contains_id(vms, vm.id)
            
    @depends(test_create)
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

    @depends(test_create)
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

    @depends(test_create)
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
        assert util.is_date(vm.creation_time)
        assert util.is_int(vm.memory) and vm.memory > 0
        assert vm.cpu is not None
        assert vm.cpu.topology is not None
        assert util.is_int(vm.cpu.topology.cores)
        assert vm.cpu.topology.cores > 0
        assert util.is_int(vm.cpu.topology.sockets)
        assert vm.cpu.topology.sockets > 0
        assert vm.display is not None
        assert vm.display.type in ('VNC', 'SPICE')
        if vm.status == 'UP':
            assert util.is_int(vm.display.port)
            assert vm.display.port > 0
            assert util.is_str_host(vm.display.address)
        assert util.is_int(vm.display.monitors)
        assert vm.display.monitors > 0
        assert vm.os is not None
        assert util.is_str(vm.os.type)
        assert vm.os.boot is not None
        for dev in vm.os.boot:
            assert dev.dev in ('cdrom', 'hd', 'network')
        assert util.is_bool(vm.stateless)
        if vm.host is not None:
            assert util.is_str_int(vm.host.id) or util.is_str_uuid(vm.host.id)
        assert vm.cluster is not None
        assert util.is_str_int(vm.cluster.id) or util.is_str_uuid(vm.cluster.id)
        assert vm.template is not None
        assert util.is_str_uuid(vm.template.id)
        if vm.vmpool is not None:
            assert util.is_str_uuid(vm.vmpool.id)

    @depends(test_create)
    def test_has_statistics(self):
        vm = self.store.vm
        vm2 = self.api.get(schema.VM, id=vm.id)
        assert vm2 is not None
        assert vm2.statistics is None
        vm2 = self.api.get(schema.VM, id=vm.id, detail='statistics')
        assert vm2 is not None
        assert vm2.statistics is not None
        vms = self.api.getall(schema.VM, name=vm.name)
        assert isinstance(vms, schema.VMs)
        assert len(vms) == 1
        assert vms[0] is not None
        assert vms[0].statistics is None
        vms = self.api.getall(schema.VM, name=vm.name, detail='statistics')
        assert isinstance(vms, schema.VMs)
        assert len(vms) == 1
        assert vms[0] is not None
        assert vms[0].statistics is not None
        vm2 = self.api.reload(vm)
        assert vm2 is not None
        assert vm2.statistics is None
        vm2 = self.api.reload(vm, detail='statistics')
        assert vm2 is not None
        assert vm2.statistics is not None
        assert isinstance(vm2.statistics, schema.Statistics)
        self.store.vm = vm2

    @depends(test_has_statistics)
    def test_statistics_attributes(self):
        vm = self.store.vm
        for stat in vm.statistics.statistic:
            assert isinstance(stat, schema.Statistic)
            assert util.is_str_uuid(stat.id)
            assert util.is_str(stat.name) and len(stat.name) > 0
            assert util.is_str(stat.description) and len(stat.description) > 0
            assert isinstance(stat.values, schema.Values)
            assert stat.values.type in ('DECIMAL', 'INTEGER')
            for val in stat.values.value_:
                assert isinstance(val, schema.Value)
                assert util.is_int(val.datum) or \
                        util.is_float(val.datum)
            assert stat.type in ('GAUGE', 'COUNTER')
            assert stat.unit in ('NONE', 'PERCENT', 'BYTES', 'SECONDS',
                                 'BYTES_PER_SECOND', 'BITS_PER_SECOND',
                                 'COUNT_PER_SECOND')

    @depends(test_create)
    def test_has_disks(self):
        vm = self.store.vm
        disks = self.api.getall(schema.Disk, base=vm)
        assert isinstance(disks, schema.Disks)

    @depends(test_create)
    def test_add_disk(self):
        vm = self.store.vm
        disk = schema.new(schema.Disk)
        disk.size = 1 * 1024**3
        disk.type = 'SYSTEM'
        disk.interface = 'VIRTIO'
        disk.format = 'COW'
        disk.sparse = True
        disk.wipe_after_delete = True
        disk.propagate_errors = True
        disk.bootable = True
        disk.storage_domain = schema.ref(self.store.storagedomain)
        disk2 = self.api.create(disk, base=vm)
        assert isinstance(disk2, schema.Disk)
        assert disk2.id is not None
        self.wait_for_status(vm, 'DOWN')
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

    @depends(test_create)
    def test_has_nics(self):
        vm = self.store.vm
        nics = self.api.getall(schema.NIC, base=vm)
        assert isinstance(nics, schema.Nics)

    @depends(test_create)
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

    @depends(test_create)
    def test_has_cdroms(self):
        vm = self.store.vm
        cdroms = self.api.getall(schema.CdRom, base=vm)
        assert isinstance(cdroms, schema.CdRoms)
        self.store.cdroms = cdroms

    @depends(test_has_cdroms)
    def test_cdrom_attributes(self):
        cdroms = self.store.cdrom
        for cdrom in cdroms:
            assert isinstance(cdrom, schema.CdRom)
            assert cdrom.id is not None
            assert util.is_str_href(cdrom.href) and \
                    cdrom.href.endswith(cdrom.id)
            if cdrom.file:
                assert util.is_str(cdrom.file) and len(cdrom.file) > 0

    @depends(test_create)
    def test_has_snapshots(self):
        vm = self.store.vm
        snapshots = self.api.getall(schema.Snapshot, base=vm)
        assert isinstance(snapshots, schema.Snapshots)

    @depends(test_create)
    def test_has_tags(self):
        vm = self.store.vm
        tags = self.api.getall(schema.Tag, base=vm)
        assert isinstance(tags, schema.Tags)

    @depends(test_create)
    def test_start(self):
        vm = self.store.vm
        # Wait until disk image is unlocked
        self.wait_for_status(vm, 'DOWN')
        action = self.api.action(vm, 'start')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(vm, 'UP')

    @depends(test_start)
    def test_ticket(self):
        vm = self.store.vm
        action = schema.new(schema.Action)
        action.expiry = 120
        action = self.api.action(vm, 'ticket')
        assert action.status == 'COMPLETE'
        assert isinstance(action.ticket, schema.Ticket)
        assert util.is_str(action.ticket.value_) and \
                len(action.ticket.value_) > 0
        assert util.is_int(action.ticket.expiry) and \
                action.ticket.expiry > 0

    @depends(test_start)
    def test_suspend(self):
        vm = self.store.vm
        action = self.api.action(vm, 'suspend')
        assert action.status == 'COMPLETE'
        self.wait_for_status(vm, 'SUSPENDED')
        action = self.api.action(vm, 'start')
        assert action.status == 'COMPLETE'
        self.wait_for_status(vm, ('POWERING_UP', 'UP'))
        self.store.vm = self.api.reload(vm)

    @depends(test_start)
    def test_migrate(self):
        vm = self.store.vm
        hosts = self.api.getall(schema.Host,
                filter={'cluster.id': vm.cluster.id, 'status': 'UP'})
        if len(hosts) < 2:
            raise SkipTest, 'Need 2 hosts for migrate test.'
        # First with host
        action = schema.new(schema.Action)
        other = [ host for host in hosts if host.id != vm.host.id ]
        target = other[0]
        action.host = schema.ref(target)
        action = self.api.action(vm, 'migrate', action)
        assert action.status == 'COMPLETE'
        self.wait_for_status(vm, 'UP')
        vm = self.api.reload(vm)
        assert vm.host.id == target.id
        # Now with host autoselection: BUG: #184
        action = schema.new(schema.Action)
        action = self.api.action(vm, 'migrate', action)
        assert action.status == 'COMPLETE'
        self.wait_for_status(vm, 'UP')
        vm = self.api.reload(vm)
        assert vm.host.id != target.id

    @depends(test_start)
    def test_shutdown(self):
        vm = self.store.vm
        # Without an ACPI handler running in the OS, it seems that the VM gets
        # stuck on POWERING_DOWN. That's fine for our test, as we know the
        # action was delivered. Force it off in that case.
        action = self.api.action(vm, 'shutdown')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(vm, ('POWERING_DOWN', 'DOWN'))
        if vm.status == 'POWERING_DOWN':
            self.api.action(vm, 'stop')
            self.wait_for_status(vm, 'DOWN')

    @depends(test_start)
    def test_stop(self):
        vm = self.store.vm
        action = self.api.action(vm, 'start')
        assert action.status == 'COMPLETE'
        self.wait_for_status(vm, ('UP', 'POWERING_UP'))
        action = self.api.action(vm, 'stop')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(vm, 'DOWN')

    @depends(test_create)
    def test_export(self):
        vm = self.store.vm
        exportdomain = self.store.exportdomain
        if exportdomain is None:
            raise SkipTest, 'Need an export domain for export test.'
        action = schema.new(schema.Action)
        action.storage_domain = schema.ref(exportdomain)
        action.exclusive = True
        action.discard_snapshots = True
        action = self.api.action(vm, 'export', action)
        assert action.status == 'COMPLETE'

    @depends(test_export)
    def test_import(self):
        vm = self.store.vm
        self.api.delete(vm)
        storagedomain = self.store.storagedomain
        exportdomain = self.store.exportdomain
        vms = self.api.getall(schema.VM, base=exportdomain)
        assert len(vms) > 0
        assert isinstance(vms, schema.VMs)
        assert util.contains_id(vms, vm.id)
        for vm2 in vms:
            if vm2.id == vm.id:
                break
        action = schema.new(schema.Action)
        action.storage_domain = schema.ref(storagedomain)
        action.cluster = schema.ref(vm.cluster)
        action = self.api.action(vm2, 'import', action)
        assert action.status == 'COMPLETE'

    @depends(test_create)
    def test_delete(self):
        vm = self.store.vm
        self.api.delete(vm)
        vm2 = self.api.get(schema.VM, name=vm.name)
        assert vm2 is None

    def test_prepare_nonexistent(self):
        vm = schema.new(schema.VM)
        vm.id = 'foo'
        vm.href = '%s/vms/foo' % self.api.entrypoint
        self.store.vm = vm

    @depends(test_prepare_nonexistent)
    def test_get_nonexistent(self):
        vm = self.store.vm
        vm = self.api.get(schema.VM, id=vm.id)
        assert vm is None

    @depends(test_prepare_nonexistent)
    def test_reload_nonexistent(self):
        vm = self.store.vm
        vm = self.api.reload(vm)
        assert vm is None

    @depends(test_prepare_nonexistent)
    def test_update_nonexistent(self):
        vm = self.store.vm
        assert_raises(NotFound, self.api.update, vm)

    @depends(test_prepare_nonexistent)
    def test_delete_nonexistent(self):
        vm = self.store.vm
        assert_raises(NotFound, self.api.delete, vm)
