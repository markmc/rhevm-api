#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import datetime
from rhev import *
from rhev.test import util
from rhev.test.base import BaseTest
from rhev.test.loader import depends

from nose import SkipTest
from nose.tools import assert_raises


class TestTemplate(BaseTest):

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
        storagedomain = self.api.get(schema.StorageDomain, base=datacenter,
                                     filter={'type': 'DATA'})
        if storagedomain is None:
            raise SkipTest, 'No data domain found in datacenter'
        exportdomain = self.api.get(schema.StorageDomain, base=datacenter,
                                    filter={'type': 'EXPORT'})
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
        self.wait_for_status(vm2, 'DOWN')  # LOCKED -> DOWN
        self.store.datacenter = datacenter
        self.store.cluster = cluster
        self.store.storagedomain = storagedomain
        self.store.exportdomain = exportdomain
        self.store.vm = vm2

    @depends(test_prepare)
    def test_create(self):
        template = schema.new(schema.Template)
        template.name = util.random_name('tmpl')
        template.vm = schema.ref(self.store.vm)
        template2 = self.api.create(template)
        assert isinstance(template2, schema.Template)
        assert template2.id is not None
        template = self.api.get(schema.Template, name=template2.name)
        assert isinstance(template, schema.Template)
        assert template.id == template2.id
        self.wait_for_status(template, 'OK')
        self.store.template = template

    @depends(test_create)
    def test_create_duplicate_name(self):
        template = self.store.template
        template2 = schema.new(schema.Template)
        template2.name = template.name
        template2.vm = schema.ref(self.store.vm)
        assert_raises(Fault, self.api.create, template2)

    @depends(test_create)
    def test_get(self):
        template = self.store.template
        template2 = self.api.get(schema.Template, id=template.id)
        assert isinstance(template2, schema.Template)
        assert template2.id == template.id

    @depends(test_create)
    def test_reload(self):
        template = self.store.template
        template2 = self.api.reload(template)
        assert isinstance(template2, schema.Template)
        assert template2.id == template.id

    @depends(test_create)
    def test_getall(self):
        template = self.store.template
        templates = self.api.getall(schema.Template)
        assert isinstance(templates, schema.Templates)
        for template2 in templates:
            assert isinstance(template2, schema.Template)
        assert util.contains_id(templates, template.id)

    @depends(test_create)
    def test_search(self):
        template = self.store.template
        templates = self.api.getall(schema.Template, name=template.name)
        assert isinstance(templates, schema.Templates)
        assert len(templates) == 1
        assert isinstance(templates[0], schema.Template)
        assert templates[0].id == template.id
        templates = self.api.getall(schema.Template, search='name=%s' % template.name)
        assert isinstance(templates, schema.Templates)
        assert len(templates) == 1
        assert isinstance(templates[0], schema.Template)
        assert templates[0].id == template.id

    @depends(test_create)
    def test_update(self):
        template = self.store.template
        template.name = util.random_name('tmpl')
        # Bug: ticket #190
        template2 = self.api.update(template)
        assert isinstance(template2, schema.Template)
        assert template2.id == template.id
        assert template2.name == template.name
        template = self.api.get(schema.Template, id=template2.id)
        assert isinstance(template, schema.Template)
        assert template.id == template2.id
        assert template.name == template2.name
        self.store.template = template

    @depends(test_create)
    def test_attributes(self):
        template = self.store.template
        template = self.api.reload(template)
        assert util.is_str_uuid(template.id)
        assert util.is_str_href(template.href)
        assert util.is_str(template.name) and template.name
        if template.description:
            assert util.is_str(template.description)
        assert template.type in ('SERVER', 'DESKTOP')
        assert template.status in ('ILLEGAL', 'LOCKED', 'OK')
        assert template.origin in ('RHEV', 'VMWARE', 'XEN')
        assert isinstance(template.creation_time, datetime.datetime)
        assert util.is_int(template.memory)
        assert template.memory > 0
        assert template.cpu is not None
        assert template.cpu.topology is not None
        assert util.is_int(template.cpu.topology.cores)
        assert template.cpu.topology.cores > 0
        assert util.is_int(template.cpu.topology.sockets)
        assert template.cpu.topology.sockets > 0
        assert template.display is not None
        assert template.display.type in ('VNC', 'SPICE')
        assert util.is_int(template.display.monitors)
        assert template.display.monitors > 0
        assert template.os is not None
        assert util.is_str(template.os.type)
        assert template.os.boot is not None
        # Upper case?
        for dev in template.os.boot:
            assert dev.dev in ('cdrom', 'hd', 'network')
        assert util.is_bool(template.stateless)
        assert template.cluster is not None
        assert util.is_str_int(template.cluster.id) or util.is_str_uuid(template.cluster.id)

    @depends(test_create)
    def test_has_disks(self):
        template = self.store.template
        disks = self.api.getall(schema.Disk, base=template)
        assert isinstance(disks, schema.Disks)

    @depends(test_create)
    def test_has_nics(self):
        template = self.store.template
        nics = self.api.getall(schema.NIC, base=template)
        assert isinstance(nics, schema.Nics)

    @depends(test_create)
    def test_has_cdroms(self):
        template = self.store.template
        cdroms = self.api.getall(schema.CdRom, base=template)
        assert isinstance(cdroms, schema.CdRoms)

    @depends(test_create)
    def test_export(self):
        template = self.store.template
        exportdomain = self.store.exportdomain
        if exportdomain is None:
            raise SkipTest, 'Need an export domain for export test.'
        action = schema.new(schema.Action)
        action.storage_domain = schema.ref(exportdomain)
        action.exclusive = True
        action.discard_snapshots = True
        action = self.api.action(template, 'export', action)
        assert action.status == 'COMPLETE'

    @depends(test_export)
    def test_import(self):
        template = self.store.template
        self.api.delete(template)
        storagedomain = self.store.storagedomain
        exportdomain = self.store.exportdomain
        templates = self.api.getall(schema.Template, base=exportdomain)
        assert len(templates) > 0
        assert isinstance(templates, schema.Templates)
        assert util.contains_id(templates, template.id)
        for template2 in templates:
            if template2.id == template.id:
                break
        action = schema.new(schema.Action)
        action.storage_domain = schema.ref(storagedomain)
        action.cluster = schema.ref(template.cluster)
        action = self.api.action(template2, 'import', action)
        assert action.status == 'COMPLETE'

    @depends(test_create)
    def test_delete(self):
        template = self.store.template
        self.api.delete(template)
        templates = self.api.getall(schema.Template)
        assert not util.contains_id(templates, template.id)
        self.api.delete(self.store.vm)

    def test_prepare_nonexistent(self):
        template = schema.new(schema.Template)
        template.id = 'foo'
        template.href = '%s/templates/foo' % self.api.entrypoint
        self.store.template = template

    def test_get_nonexistent(self):
        template = self.store.template
        template = self.api.get(schema.Template, id=template.id)
        assert template is None

    def test_reload_nonexistent(self):
        template = self.store.template
        template = self.api.reload(template)
        assert template is None

    def test_update_nonexistent(self):
        template = self.store.template
        assert_raises(NotFound, self.api.update, template)

    def test_delete_nonexistent(self):
        template = self.store.template
        assert_raises(NotFound, self.api.delete, template)
