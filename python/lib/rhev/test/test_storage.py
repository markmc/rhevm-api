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


class TestStorage(BaseTest):

    def _prepare(self, name):
        type = self.get_config('type', name)
        if type is None:
            raise SkipTest, 'Missing parameter "type" in [%s]' % name
        if type == 'NFS':
            params = ('address', 'path')
        elif type == 'ISCSI':
            params = ('address', 'port', 'target', 'username', 'password')
        elif type == 'FCP':
            params = ()
        else:
            raise SkipTest, 'Uknown storage type %s in [%s]' % (type, name)
        values = { 'type': type }
        for param in params:
            value = self.get_config(param, name)
            if value is None:
                raise SkipTest, 'Missing parameter "%s" in [%s]' % (param, name)
            values[param] = value
        if values['type'] in ('ISCSI', 'FCP'):
            luns = self.get_config('luns', name)
            if luns is None:
                volume_group = self.get_config('volume_group', name)
                if volume_group is None:
                    raise SkipTest, 'Either "luns" or "volume_group" has to' \
                            'be specified in [%s].' % name
                luns = None
            else:
                luns = luns.split()
                volume_group = None
            values['luns'] = luns
            values['volume_group'] = volume_group
        hosts = self.get_config('hosts').split()
        if len(hosts) == 0:
            raise SkipTest, 'No hosts configured.'
        host = self.api.get(schema.Host, name=hosts[0])
        if host is None:
            raise SkipTest, 'Host not found: %s' % hosts[0]
        values['host'] = host
        cpu = self.get_config('cpu', host.name)
        if cpu is None:
            raise SkipTest, 'Missing parameter "cpu" in [%s]' % host.name
        values['cpu'] = cpu
        for key in values:
            setattr(self.store, key, values[key])
        datacenter = schema.new(schema.DataCenter)
        datacenter.name = util.random_name('dc')
        datacenter.storage_type = self.store.type
        datacenter = self.api.create(datacenter)
        assert isinstance(datacenter, schema.DataCenter)
        assert datacenter.id is not None
        cluster = schema.new(schema.Cluster)
        cluster.name = util.random_name('cluster')
        cluster.cpu = schema.new(schema.CPU)
        cluster.cpu.id = self.store.cpu
        cluster.data_center = schema.ref(datacenter)
        cluster.version = datacenter.version
        cluster = self.api.create(cluster)
        assert isinstance(cluster, schema.Cluster)
        assert cluster.id is not None
        self.store.datacenter = datacenter
        self.store.cluster = cluster
        # BUG: cannot change cluster ID of a host. Ticket #171. This needs
        # the patch attached to that ticket.
        if host.status != 'MAINTENANCE':
            assert self.retry_action(host, 'deactivate')
            assert self.wait_for_status(host, 'MAINTENANCE')
        self.store.clusterid = host.cluster.id
        host.cluster = schema.ref(cluster)
        self.api.update(host)
        assert host.cluster.id == cluster.id
        self.api.action(host, 'activate')
        assert self.wait_for_status(host, 'UP')

    @depends(_prepare)
    def _test_iscsi_discover(self):
        host = self.store.host
        target = self.store.target
        action = schema.new(schema.Action)
        action.iscsi = schema.new(schema.IscsiParameters)
        action.iscsi.address = self.store.address
        action.iscsi.port = self.store.port
        result = self.api.action(host, 'iscsidiscover', action)
        assert result.status == 'COMPLETE'
        if target not in result.iscsi_target:
            raise SkipTest, 'ISCSI target not found: %s' % target

    @depends(_test_iscsi_discover)
    def _test_iscsi_login(self):
        host = self.store.host
        action = schema.new(schema.Action)
        action.iscsi = schema.new(schema.IscsiParameters)
        action.iscsi.address = self.store.address
        action.iscsi.port = self.store.port
        action.iscsi.target = self.store.target
        action.iscsi.username = self.store.username
        action.iscsi.password = self.store.password
        result = self.api.action(host, 'iscsilogin', action)
        assert result.status == 'COMPLETE'

    def _find_storage(self, storage, type, block_type, id):
        for stor in storage:
            if stor.type != type:
                continue
            if block_type == 'lun':
                if stor.logical_unit is None:
                    continue
                if stor.logical_unit[0].id == id:
                    return stor.logical_unit[0]
            elif block_type == 'vg':
                if store.volume_group is None:
                    continue
                if stor.volume_group[0].id == id:
                    return stor.volume_group
        return None

    @depends(_test_iscsi_login, _prepare)
    def _test_host_storage(self):
        host = self.store.host
        type = self.store.type
        storage = self.api.getall(schema.Storage, base=host)
        if self.store.luns:
            luns = []
            for lun in self.store.luns:
                storage = self._find_storage(storage, type, 'lun', lun)
                assert storage is not None
                luns.append(storage)
            self.store.luns = luns
        if self.store.volume_group:
            vg = self._find_storage(storage, type, 'vg',
                                    self.store.volume_group)
            assert vg is not None
            self.store.volume_group = vg

    @depends(_test_host_storage)
    def _test_host_storage_attributes(self):
        host = self.store.host
        storage = self.api.get(schema.Storage, base=host)
        assert isinstance(storage, schema.HostStorage)
        for stor in storage:
            assert isinstance(stor, schema.Storage)
            assert util.is_str(stor.id)
            assert util.is_str_href(stor.href)
            assert stor.type in ('ISCSI', 'FCP')
            assert stor.host is not None
            assert util.is_str_int(stor.host.id) or \
                        util.is_str_uuid(stor.host.id)
            assert util.is_str_href(stor.host.href)
            assert bool(stor.logical_unit) != bool(stor.volume_group)
            if stor.logical_unit:
                assert stor.logical_unit.id == stor.id
            else:
                assert stor.volume_group.id == stor.id
            # BUG: missing attributes
            #if stor.type == 'ISCSI':
            #    assert util.is_str(stor.target)
            #assert util.is_int(stor.size)
            #if stor.logical_unit:
            #    assert util.is_int(stor.multipathing)
            #elif stor.volume_group:
            #    assert util.is_str(volume_group.name)
            #    assert volume_group.logical_unit is not None

    @depends(_prepare)
    def _test_create_nfs_data(self):
        host = self.store.host
        datacenter = self.store.datacenter
        domain = schema.new(schema.StorageDomain)
        domain.name = util.random_name('sd')
        domain.type = 'DATA'
        domain.storage = schema.new(schema.Storage)
        domain.storage.type = 'NFS'
        domain.storage.address = self.store.address
        domain.storage.path = self.store.path
        domain.host = schema.ref(host)
        domain2 = self.api.create(domain)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id is not None
        domain = self.api.get(schema.StorageDomain, id=domain2.id)
        assert isinstance(domain, schema.StorageDomain)
        assert domain.id == domain2.id
        assert domain.storage.type == 'NFS'
        assert domain.storage.address == self.store.address
        assert domain.storage.path == self.store.path
        self.store.storagedomain = domain
        self.store.attachdomain = None

    @depends(_test_host_storage)
    def _test_create_block_data(self):
        host = self.store.host
        datacenter = self.store.datacenter
        domain = schema.new(schema.StorageDomain)
        domain.name = util.random_name('sd')
        domain.type = 'DATA'
        domain.storage = schema.new(schema.Storage)
        domain.storage.type = self.store.type
        if self.store.luns:
            luns = [schema.ref(lun) for lun in self.store.luns ]
            domain.storage.logical_unit = luns
        elif self.store.volume_group:
            domain.storage.volume_group = schema.ref(self.store.volume_group)
        domain.host = schema.ref(host)
        domain2 = self.api.create(domain)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id is not None
        domain = self.api.get(schema.StorageDomain, id=domain2.id)
        assert isinstance(domain, schema.StorageDomain)
        assert domain.id == domain2.id
        assert domain.href == domain2.href
        assert domain.storage.type == self.store.type
        self.store.storagedomain = domain
        self.store.attachdomain = None

    @depends(_test_create_nfs_data, _test_create_block_data)
    def _test_create(self):
        """Dummy test for dependency tracking."""

    @depends(_test_create)
    def _test_get(self):
        domain = self.store.storagedomain
        domain2 = self.api.get(schema.StorageDomain, id=domain.id)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id == domain.id

    @depends(_test_create)
    def _test_reload(self):
        domain = self.store.storagedomain
        domain2 = self.api.reload(domain)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id == domain.id

    @depends(_test_create)
    def _test_getall(self):
        domain = self.store.storagedomain
        domains = self.api.getall(schema.StorageDomain)
        assert isinstance(domains, schema.StorageDomains)
        assert len(domains) > 0
        assert util.contains_id(domains, domain.id)

    @depends(_test_create)
    def _test_search(self):
        domain = self.store.storagedomain
        domains = self.api.getall(schema.StorageDomain, name=domain.name)
        assert isinstance(domains, schema.StorageDomains)
        assert len(domains) == 1
        assert domains[0].id == domain.id
        domains = self.api.getall(schema.StorageDomain, search='name=%s' % domain.name)
        assert isinstance(domains, schema.StorageDomains)
        assert len(domains) == 1
        assert domains[0].id == domain.id
    
    @depends(_test_create)
    def _test_attributes(self):
        domain = self.store.storagedomain
        assert util.is_str(domain.name)
        assert domain.type in ('DATA', 'ISO', 'EXPORT')
        assert domain.storage is not None
        assert domain.storage.type in ('NFS', 'ISCSI', 'FCP')
        if domain.storage.type == 'NFS':
            assert util.is_str_host(domain.storage.address)
            assert util.is_str_path(domain.storage.path)
        assert util.is_int(domain.available)
        assert util.is_int(domain.used)
        assert util.is_int(domain.committed)
        #assert domain.shared_status in ('UNATTACHED', 'MIXED', 'ACTIVE')  # BUG: missing
        #assert util.is_bool(domain.master)  # BUG: missing when false

    @depends(_test_create)
    def _test_attach(self):
        domain = self.store.storagedomain
        datacenter = self.store.datacenter
        domref = schema.ref(domain)
        domain2 = self.api.create(domref, base=datacenter)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id == domain.id
        assert domain2.href != domain.href
        assert self.wait_for_status(datacenter, 'UP')
        self.store.attachdomain = domain2

    @depends(_test_attach)
    def _test_get_attached(self):
        datacenter = self.store.datacenter
        domain = self.store.attachdomain
        domain2 = self.api.get(schema.StorageDomain, base=datacenter, id=domain.id)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id == domain.id

    @depends(_test_attach)
    def _test_reload_attached(self):
        domain = self.store.attachdomain
        domain2 = self.api.reload(domain)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id == domain.id

    @depends(_test_attach)
    def _test_getall_attached(self):
        datacenter = self.store.datacenter
        domain = self.store.attachdomain
        domains = self.api.getall(schema.StorageDomain, base=datacenter)
        assert isinstance(domains, schema.StorageDomains)
        assert len(domains) > 0
        assert util.contains_id(domains, domain.id)

    @depends(_test_attach)
    def _test_attributes_attached(self):
        domain = self.store.attachdomain
        assert util.is_str(domain.name)
        assert domain.type in ('DATA', 'ISO', 'EXPORT')
        assert domain.status in ('ACTIVE', 'LOCKED', 'INACTIVE')
        assert domain.storage is not None
        assert domain.storage.type in ('NFS', 'ISCSI', 'FCP')
        if domain.storage.type == 'NFS':
            assert util.is_str_host(domain.storage.address)
            assert util.is_str_path(domain.storage.path)
        assert util.is_int(domain.available)
        assert util.is_int(domain.used)
        assert util.is_int(domain.committed)
        #assert util.is_bool(domain.master)  # BUG: missing when false

    @depends(_test_attach)
    def _test_update(self):
        domain = self.store.storagedomain
        domain.name = util.random_name('sd')
        # BUG: this seems to be an issue with the backend not the API
        domain2 = self.api.update(domain)
        assert domain2.id == domain.id
        assert domain2.name == domain.name

    @depends(_test_attach)
    def _test_create_iso(self, name):
        host = self.store.host
        address = self.get_config('address', name)
        if address is None:
            raise SkipTest, 'Parameter "address" not set in [%s]' % name
        path = self.get_config('path', name)
        if path is None:
            raise SkipTest, 'Parameter "path" not set in [%s]' % name
        domain = schema.new(schema.StorageDomain)
        domain.name = util.random_name('iso')
        domain.type = 'ISO'
        domain.storage = schema.new(schema.Storage)
        domain.storage.type = 'NFS'
        domain.storage.address = address
        domain.storage.path = path
        domain.host = schema.ref(host)
        domain2 = self.api.create(domain)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id is not None
        self.store.isodomain = domain2

    @depends(_test_create_iso)
    def _test_attach_iso(self):
        datacenter = self.store.datacenter
        domain = self.store.isodomain
        domain2 = self.api.create(schema.ref(domain), base=datacenter)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id == domain.id
        assert domain2.href != domain.href
        self.wait_for_status(domain2, 'INACTIVE')
        self.store.isoattach = domain2

    @depends(_test_attach_iso)
    def _test_activate_iso(self):
        domain = self.store.isoattach
        action = self.api.action(domain, 'activate')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(domain, 'ACTIVE')

    @depends(_test_activate_iso)
    def _test_deactivate_iso(self):
        domain = self.store.isoattach
        action = self.api.action(domain, 'deactivate')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(domain, 'INACTIVE')

    @depends(_test_deactivate_iso)
    def _test_detach_iso(self):
        datacenter = self.store.datacenter
        domain = self.store.isoattach
        self.api.delete(domain)
        domains = self.api.getall(schema.StorageDomain, base=datacenter)
        assert not util.contains_id(domains,  domain.id)
        domains = self.api.getall(schema.StorageDomain)
        assert util.contains_id(domains, domain.id)

    @depends(_test_detach_iso)
    def _test_destroy_iso(self):
        host = self.store.host
        domain = self.store.isodomain
        params = schema.new(schema.StorageDomain)
        params.host = schema.ref(host)
        params.format = False
        self.api.delete(domain, data=params)
        domains = self.api.getall(schema.StorageDomain)
        assert not util.contains_id(domains, domain.id)

    @depends(_test_destroy_iso)
    def _test_import_iso(self):
        host = self.store.host
        domain = self.store.isodomain
        domain2 = schema.new(schema.StorageDomain)
        domain2.name = None  # This identifies the creation as an import
        domain2.type = 'ISO'
        domain2.storage = schema.new(schema.Storage)
        domain2.storage.type = 'NFS'
        domain2.storage.address = domain.storage.address
        domain2.storage.path = domain.storage.path
        domain2.host = schema.ref(host)
        domain2 = self.api.create(domain2)
        assert isinstance(domain2, schema.StorageDomain)
        assert domain2.id is not None
        assert domain2.id == domain.id
        assert domain2.name == domain.name

    @depends(_test_create_iso)
    def _test_delete_iso(self):
        host = self.store.host
        domain = self.store.isodomain
        params = schema.new(schema.StorageDomain)
        params.host = schema.ref(host)
        params.format = True
        self.api.delete(domain, data=params)
        domains = self.api.getall(schema.StorageDomain)
        assert not util.contains_id(domains, domain.id)

    @depends(_test_attach)
    def _test_deactivate(self):
        domain = self.store.attachdomain
        datacenter = self.store.datacenter
        action = self.api.action(domain, 'deactivate')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(domain, 'INACTIVE')
        assert self.wait_for_status(datacenter, 'MAINTENANCE')

    @depends(_test_deactivate)
    def _test_activate(self):
        domain = self.store.attachdomain
        datacenter = self.store.datacenter
        action = self.api.action(domain, 'activate')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(domain, 'ACTIVE')
        assert self.wait_for_status(datacenter, 'UP')

    @depends(_test_create)
    def _test_delete(self):
        # We'd like to detach the storage domain but that's not possible as
        # this is the only data domain left. Therefore, delete the entire DC.
        # BUG: "format" option is missing
        domain = self.store.storagedomain
        atdomain = self.store.attachdomain
        cluster = self.store.cluster
        datacenter = self.store.datacenter
        host = self.store.host
        self.api.action(atdomain, 'deactivate')
        atdomain = self.api.reload(atdomain)
        assert atdomain.status == 'INACTIVE'
        self.api.delete(datacenter)
        datacenter = self.api.get(schema.DataCenter, name=datacenter.name)
        assert datacenter is None
        params = schema.new(schema.StorageDomain)
        params.host = schema.ref(self.store.host)
        params.format = True
        self.api.delete(domain, data=params)
        # Loading by name because by ID -> 500 Internal Server Error
        domain = self.api.get(schema.StorageDomain, name=domain.name)
        assert domain is None
        assert self.retry_action(host, 'deactivate')
        assert self.wait_for_status(host, 'MAINTENANCE')
        host = schema.href(host)
        host.cluster = schema.new(schema.Cluster)
        host.cluster.id = self.store.clusterid
        host = self.api.update(host)
        assert host.cluster.id == self.store.clusterid
        action = self.api.action(host, 'activate')
        assert action.status == 'COMPLETE'
        assert self.wait_for_status(host, 'UP')
        self.api.delete(cluster)
        cluster = self.api.get(schema.Cluster, name=cluster.name)
        assert cluster is None

    def test_storage_domain(self):
        storage = self.get_config('storage')
        storage = storage.split()
        for stor in storage:
            # First create a data domain
            yield self._prepare, stor
            type = self.store.type
            if type in ('ISCSI', 'FCP'):
                if type == 'ISCSI':
                    yield self._test_iscsi_discover
                    yield self._test_iscsi_login
                yield self._test_host_storage
                yield self._test_create_block_data
            elif type == 'NFS':
                yield self._test_create_nfs_data
            yield self._test_create
            yield self._test_get
            yield self._test_reload
            yield self._test_getall
            yield self._test_search
            yield self._test_attributes
            yield self._test_attach
            yield self._test_get_attached
            yield self._test_reload_attached
            yield self._test_getall_attached
            yield self._test_attributes_attached
            # We can only update an attached and activated SD
            yield self._test_update 
            yield self._test_deactivate
            yield self._test_activate
            iso = self.get_config('iso', stor)
            if iso is not None:
                yield self._test_create_iso, iso
                yield self._test_attach_iso
                yield self._test_activate_iso
                yield self._test_deactivate_iso
                yield self._test_detach_iso
                yield self._test_destroy_iso
                yield self._test_import_iso
                yield self._test_delete_iso
            # TBD: export domains
            yield self._test_delete
