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


class TestMapping(BaseTest):

    def test_prepare(self):
        name = self.get_config('datacenter')
        if name is None:
            raise SkipTest, 'Parameter "datacenter" not set.'
        datacenter = self.api.get(schema.DataCenter, name=name)
        if datacenter is None:
            raise SkipTest, 'Datacenter not found: %s' % name
        name = self.get_config('cluster')
        if name is None:
            raise SkipTest, 'Parameter "cluster" not set.'
        cluster = self.api.get(schema.Cluster, name=name)
        if cluster is None:
            raise SkipTest, 'Cluster not found: %s' % name
        template = self.api.get(schema.Template, name='Blank')
        if template is None:
            raise SkipTest, 'Template not found: Blank'
        vm = schema.new(schema.VM)
        vm.name = util.random_name('vm')
        vm.memory = 512 * 1024**2
        vm.type = 'DESKTOP'
        vm.cluster = schema.ref(cluster)
        vm.template = schema.ref(template)
        vm2 = self.api.create(vm)
        assert vm2 is not None
        disk = schema.new(schema.Disk)
        disk.size = 1024**3
        disk.format = 'COW'
        disk.sparse = True
        disk.wipe_after_delete = False
        disk2 = self.api.create(disk, base=vm2)
        assert disk2 is not None
        self.store.vm = vm2

    @depends(test_prepare)
    def test_list(self):
        api = self.api.api()
        vms = api.vms()
        assert isinstance(vms, schema.VMs)
        for vm in vms:
            assert isinstance(vm, schema.VM)
        for i in range(len(vms)):
            assert isinstance(vms[i], schema.VM)
        for vm in vms:
            assert isinstance(vm, schema.VM)

    @depends(test_prepare)
    def test_list_search(self):
        api = self.api.api()
        vm = self.store.vm
        vms = api.vms(name=vm.name)
        assert isinstance(vms, schema.VMs)
        assert len(vms) == 1
        assert vms[0].id == vm.id
        vms = api.vms(search='name=%s' % vm.name)
        assert isinstance(vms, schema.VMs)
        assert len(vms) == 1
        assert vms[0].id == vm.id

    @depends(test_prepare)
    def test_list_filter(self):
        api = self.api.api()
        vm = self.store.vm
        vms = api.vms(filter= {'name': vm.name})
        assert isinstance(vms, schema.VMs)
        assert len(vms) == 1
        assert vms[0].id == vm.id
       
    @depends(test_prepare)
    def test_get(self):
        api = self.api.api()
        vm = self.store.vm
        vm2 = api.vm(id=vm.id)
        assert isinstance(vm2, schema.VM)
        assert vm2.id == vm.id

    @depends(test_prepare)
    def test_get_search(self):
        api = self.api.api()
        vm = self.store.vm
        vm2 = api.vm(name=vm.name)
        assert isinstance(vm2, schema.VM)
        assert vm2.id == vm.id
        vm2 = api.vm(search='name=%s' % vm.name)
        assert isinstance(vm2, schema.VM)
        assert vm2.id == vm.id

    @depends(test_prepare)
    def test_get_filter(self):
        api = self.api.api()
        vm = self.store.vm
        vm2 = api.vm(filter={'name': vm.name})
        assert isinstance(vm2, schema.VM)
        assert vm2.id == vm.id

    @depends(test_prepare)
    def test_reload(self):
        vm = self.store.vm
        old = vm.description
        vm.description = 'foo'
        vm.reload()
        assert vm.description == old

    @depends(test_prepare)
    def test_update(self):
        api = self.api.api()
        vm = self.store.vm
        vm.description = 'foo2'
        vm.type = None  # XXX: Ticket: #179
        vm.update()
        vm2 = api.vm(id=vm.id)
        assert vm2.id == vm.id
        assert vm2.description == vm.description

    @depends(test_prepare)
    def test_action(self):
        vm = self.store.vm
        vm.start()
        vm.wait_for_status(('UP', 'POWERING_UP'), timeout=30)
        vm.stop()
        vm.wait_for_status('DOWN', timeout=30)

    @depends(test_prepare)
    def test_delete(self):
        api = self.api.api()
        vm = self.store.vm
        vm.delete()
        vm2 = api.vm(name=vm.name)
        assert vm2 is None
