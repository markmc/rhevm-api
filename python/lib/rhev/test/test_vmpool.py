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


class TestVmPool(BaseTest):

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
        self.wait_for_status(vm2, 'DOWN')
        template = schema.new(schema.Template)
        template.name = util.random_name('tmpl')
        template.vm = schema.ref(vm2)
        template2 = self.api.create(template)
        self.wait_for_status(template2, 'OK')
        assert template2 is not None
        self.store.datacenter = datacenter
        self.store.cluster = cluster
        self.store.vm = vm2
        self.store.template = template2

    @depends(test_prepare)
    def test_create(self):
        template = self.store.template
        cluster = self.store.cluster
        pool = schema.new(schema.VmPool)
        pool.name = util.random_name('pool')
        pool.size = 1
        pool.template = schema.ref(template)
        pool.cluster = schema.ref(cluster)
        pool2 = self.api.create(pool)
        assert isinstance(pool2, schema.VmPool)
        assert pool2.id is not None
        pool = self.api.get(schema.VmPool, name=pool2.name)
        assert isinstance(pool, schema.VmPool)
        assert pool.id == pool2.id
        self.store.pool = pool

    @depends(test_create)
    def test_get(self):
        pool = self.store.pool
        pool2 = self.api.get(schema.VmPool, id=pool.id)
        assert isinstance(pool2, schema.VmPool)
        assert pool2.id == pool.id

    @depends(test_create)
    def test_reload(self):
        pool = self.store.pool
        pool2 = self.api.reload(pool)
        assert isinstance(pool2, schema.VmPool)
        assert pool2.id == pool.id

    @depends(test_create)
    def test_getall(self):
        pool = self.store.pool
        pools = self.api.getall(schema.VmPool)
        assert isinstance(pools, schema.VmPools)
        for pool2 in pools:
            assert isinstance(pool2, schema.VmPool)
        assert util.contains_id(pools, pool.id)

    @depends(test_create)
    def test_search(self):
        pool = self.store.pool
        pools = self.api.getall(schema.VmPool, name=pool.name)
        assert isinstance(pools, schema.VmPools)
        assert len(pools) == 1
        assert isinstance(pools[0], schema.VmPool)
        assert pools[0].id == pool.id
        pools = self.api.getall(schema.VmPool, search='name=%s' % pool.name)
        assert isinstance(pools, schema.VmPools)
        assert len(pools) == 1
        assert isinstance(pools[0], schema.VmPool)
        assert pools[0].id == pool.id

    @depends(test_create)
    def test_update(self):
        pool = self.store.pool
        pool.description = 'foo'
        pool.size += 1
        pool2 = self.api.update(pool)
        assert isinstance(pool2, schema.VmPool)
        assert pool2.id == pool.id
        assert pool2.description == pool.description
        assert pool2.size == pool.size
        pool = self.api.get(schema.VmPool, id=pool2.id)
        assert isinstance(pool, schema.VmPool)
        assert pool.id == pool2.id
        assert pool.description == pool2.description
        assert pool.size == pool2.size
        self.store.pool = pool

    @depends(test_create)
    def test_attributes(self):
        pool = self.store.pool
        pool = self.api.reload(pool)
        assert util.is_str_int(pool.id) or util.is_str_uuid(pool.id)
        assert util.is_str_href(pool.href)
        assert util.is_str(pool.name) and pool.name
        if pool.description:
            assert util.is_str(pool.description)
        # BUG: #186: attributes missing here

    @depends(test_create)
    def test_delete(self):
        pool = self.store.pool
        vms = self.api.getall(schema.VM, filter={'vmpool.id': pool.id})
        for vm in vms:
            action = self.api.action(vm, 'detach')
            assert action.status == 'COMPLETE'
            self.api.delete(vm)
        self.api.delete(pool)
        pools = self.api.getall(schema.VmPool)
        assert not util.contains_id(pools, pool.id)
        self.api.delete(self.store.template)
        self.api.delete(self.store.vm)
