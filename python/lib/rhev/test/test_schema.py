#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

from rhev import schema
from nose.tools import assert_raises


class TestSchema(object):

    def test_new(self):
        vm = schema.new(schema.VM)
        assert isinstance(vm, schema.VM)
        vms = schema.new(schema.VMs)
        assert isinstance(vms, schema.VMs)
        assert_raises(TypeError, schema.new, object)

    def test_ref(self):
        vm = schema.new(schema.VM)
        vm.id = 10
        vm.href = '/vms/10'
        vm.description = 'foo'
        vm2 = schema.ref(vm)
        assert vm2.id == vm.id
        assert vm2.href is None
        assert vm2.description is None

    def test_href(self):
        vm = schema.new(schema.VM)
        vm.id = 10
        vm.href = '/vms/10'
        vm.description = 'foo'
        vm2 = schema.href(vm)
        assert vm2.id == vm.id
        assert vm2.href == vm.href
        assert vm2.description is None

    def test_update(self):
        vm = schema.new(schema.VM)
        vm2 = schema.new(schema.VM)
        vm2.description = 'foo'
        schema.update(vm, vm2)
        assert vm.description == vm2.description

    def test_copy(self):
        vm = schema.new(schema.VM)
        vm.description = 'foo'
        vm2 = schema.copy(vm)
        assert vm2.description == vm.description

    def test_interface(self):
        assert hasattr(schema, 'BaseResource')
        assert hasattr(schema, 'BaseResources')
        assert hasattr(schema, 'API')
        assert hasattr(schema, 'DataCenter')
        assert hasattr(schema, 'DataCenters')
        assert hasattr(schema, 'Cluster')
        assert hasattr(schema, 'Clusters')
        assert hasattr(schema, 'StorageDomain')
        assert hasattr(schema, 'StorageDomains')
        assert hasattr(schema, 'Network')
        assert hasattr(schema, 'Networks')
        assert hasattr(schema, 'Host')
        assert hasattr(schema, 'Hosts')
        assert hasattr(schema, 'HostNIC')
        assert hasattr(schema, 'HostNics')
        assert hasattr(schema, 'Storage')
        assert hasattr(schema, 'HostStorage')
        assert hasattr(schema, 'VM')
        assert hasattr(schema, 'VMs')
        assert hasattr(schema, 'NIC')
        assert hasattr(schema, 'Nics')
        assert hasattr(schema, 'Disk')
        assert hasattr(schema, 'Disks')
        assert hasattr(schema, 'CdRom')
        assert hasattr(schema, 'CdRoms')
        assert hasattr(schema, 'Template')
        assert hasattr(schema, 'Templates')
        assert hasattr(schema, 'VmPool')
        assert hasattr(schema, 'VmPools')
        assert hasattr(schema, 'User')
        assert hasattr(schema, 'Users')
        assert hasattr(schema, 'Tag')
        assert hasattr(schema, 'Tags')
        assert hasattr(schema, 'TagParent')
        assert hasattr(schema, 'Capabilities')
        assert hasattr(schema, 'Version')
        assert hasattr(schema, 'Action')
        assert hasattr(schema, 'IP')
        assert hasattr(schema, 'VLAN')
        assert hasattr(schema, 'MAC')
        assert hasattr(schema, 'Slaves')
        assert hasattr(schema, 'IscsiDetails')
        assert hasattr(schema, 'LogicalUnit')
        assert hasattr(schema, 'VolumeGroup')
        assert hasattr(schema, 'CPU')
        assert hasattr(schema, 'CpuTopology')
        assert hasattr(schema, 'Display')
        assert hasattr(schema, 'HighAvailability')
        assert hasattr(schema, 'OperatingSystem')
        assert hasattr(schema, 'Boot')
        assert hasattr(schema, 'Fault')
