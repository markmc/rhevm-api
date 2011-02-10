#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import time
import struct
import socket

from rhev import *
from rhev.test import util
from rhev.test.base import BaseTest
from rhev.test.loader import depends

from nose import SkipTest
from nose.tools import assert_raises


class TestHost(BaseTest):

    def _test_prepare(self, name):
        host = self.api.get(schema.Host, name=name)
        if host is None:
            raise SkipTest, 'Host does not exist: %s' % name
        hosts = self.get_config('hosts')
        hosts = hosts.split()
        if name not in hosts:
            self.store.host = host
            return
        # Prepare for invasive tests
        if host.status == 'MAINTENANCE':
            self.api.action(host, 'activate')
            assert self.wait_for_status(host, 'UP')
        elif host.status != 'UP':
            raise SkipTest, 'Host not in status UP/MAINTENANCE: %s' % name
        # BUG: missing attribute "type"
        type = self.get_config('type', name)
        if type not in ('RHEL', 'RHEV_H'):
            raise SkipTest, 'Unknown host type in [%s]: %s' % (name, type)
        if type == 'RHEL':
            address = self.get_config('address', name)
            if address is None:
                raise SkipTest, 'Missing "address" parameter in [%s]' % name
            password = self.get_config('password', name)
            if password is None:
                raise SkipTest, 'Missing "password" parameter in [%s]' % name
            self.store.address = address
            self.store.password = password
        elif type == 'RHEV_H':
            isofile = self.get_config('isofile', name)
            if isofile is None:
                raise SkipTest, 'Missing "isofile" parameter in [%s]' % name
            self.store.isofile = isofile
        cluster = self.api.get(schema.Cluster, id=host.cluster.id)
        self.store.host = host
        self.store.type = type
        self.store.name = name
        self.store.cluster = cluster

    @depends(_test_prepare)
    def _test_get(self):
        host = self.store.host
        host2 = self.api.get(schema.Host, id=host.id)
        assert isinstance(host2, schema.Host)
        assert host2.id == host.id

    @depends(_test_prepare)
    def _test_reload(self):
        host = self.store.host
        host2 = self.api.reload(host)
        assert isinstance(host2, schema.Host)
        assert host2.id == host.id

    @depends(_test_prepare)
    def _test_getall(self):
        host = self.store.host
        hosts = self.api.getall(schema.Host)
        assert isinstance(hosts, schema.Hosts)
        assert len(hosts) > 0
        for h in hosts:
            assert isinstance(h, schema.Host)
        assert util.contains_id(hosts, host.id)

    @depends(_test_prepare)
    def _test_search(self):
        host = self.store.host
        host2 = self.api.get(schema.Host, name=host.name)
        assert isinstance(host2, schema.Host)
        assert host2.id == host.id
        host2 = self.api.get(schema.Host, search='name=%s' % host.name)
        assert isinstance(host2, schema.Host)
        assert host2.id == host.id

    @depends(_test_prepare)
    def _test_attributes(self):
        host = self.store.host
        assert util.is_str_int(host.id) or util.is_str_uuid(host.id)
        assert util.is_str(host.href) and host.href.endswith(host.id)
        assert util.is_str(host.name) and len(host.name) > 0
        assert host.status in ( 'DOWN', 'ERROR', 'INITIALIZING', 'INSTALLING',
                'INSTALL_FAILED', 'MAINTENANCE', 'NON_OPERATIONAL',
                'NON_RESPONSIVE', 'PENDING_APPROVAL',
                'PREPARING_FOR_MAINTENANCE', 'PROBLEMATIC', 'REBOOT',
                'UNASSIGNED', 'UP')
        #assert host.type in ('RHEV_H', 'RHEL')  # BUG: missing
        assert util.is_str_ip(host.address)  # BUG: missing
        assert util.is_str_int(host.cluster.id) or util.is_str_uuid(host.cluster.id)
        assert util.is_int(host.port) and host.port > 0
        #BUG: do we want the password to show up here?
        assert host.root_password is None
        assert host.power_management is not None
        assert util.is_bool(host.power_management.enabled)
        if host.power_management.enabled:
            # BUG: expose password here?
            assert util.is_str_ip(host.power_management.address)
            assert util.is_str(host.power_management.username)
            assert host.power_management.password is None
            if host.power_management.options:
                opts = host.power_management.options.option
                #if not isinstance(opts, list):
                #    opts = [opts]
                for opt in opts:
                    assert util.is_str(opt.name) and len(opt.name) > 0
                    assert util.is_str(opt.value_)
        assert util.is_bool(host.storage_manager)
        assert isinstance(host.summary.vms.count, int)  # BUG: missing
        assert isinstance(host.summary.vms.active, int)  # BUG: missing
        assert isinstance(host.summary.vms.migrating, int)  # BUG: missing

    @depends(_test_prepare)
    def _test_update(self):
        host = self.store.host
        host.name += 'x'
        host2 = self.api.update(host)
        assert isinstance(host2, schema.Host)
        assert host2.id == host.id
        assert host2.name == host.name
        host = self.api.get(schema.Host, id=host2.id)
        assert isinstance(host, schema.Host)
        assert host.id == host2.id
        assert host.name == host2.name
        host.name = host.name[:-1]
        host2 = self.api.update(host)
        assert isinstance(host2, schema.Host)
        assert host2.id == host.id
        assert host2.name == host.name
        host = self.api.get(schema.Host, id=host2.id)
        assert isinstance(host, schema.Host)
        assert host.id == host2.id
        assert host.name == host2.name

    @depends(_test_prepare)
    def _test_has_vms(self):
        host = self.store.host
        vms = self.api.getall(schema.VM, base=host)
        assert isinstance(vms, schema.VMs)
        for vm in vms:
            assert isinstance(vm, schema.VM)
            assert vm.id is not None

    @depends(_test_prepare)
    def _test_has_nics(self):
        host = self.store.host
        nics = self.api.getall(schema.HostNIC, base=host)
        assert isinstance(nics, schema.HostNics)
        for nic in nics:
            assert isinstance(nic, schema.HostNIC)
            assert nic.id is not None

    @depends(_test_prepare)
    def _test_has_tags(self):
        host = self.store.host
        tags = self.api.getall(schema.Tag, base=host)
        assert isinstance(tags, schema.Tags)
        for tag in tags:
            assert isinstance(tag, schema.Tag)
            assert tag.id is not None

    @depends(_test_prepare)
    def _test_has_storage(self):
        host = self.store.host
        storage = self.api.getall(schema.Storage, base=host)
        assert isinstance(storage, schema.HostStorage)
        for stor in storage:
            assert isinstance(stor, schema.Storage)
            assert stor.id is not None

    @depends(_test_prepare)
    def _test_delete(self):
        host = self.store.host
        assert host.status == 'UP'
        # A deactivate can fail if the host is currently contending to become
        # an SPM, which is possible as _test_prepare may have just activate
        # it. Retry a couple of times.
        assert self.retry_action(host, 'deactivate')
        assert self.wait_for_status(host, 'MAINTENANCE')
        self.api.delete(host)
        hosts = self.api.getall(schema.Host)
        assert not util.contains_id(hosts, host.id)

    @depends(_test_delete)
    def _test_create(self):
        host = schema.new(schema.Host)
        host.name = self.store.name
        host.address = self.store.address
        host.root_password = self.store.password
        host.cluster = schema.ref(self.store.cluster)
        host2 = self.api.create(host)
        assert isinstance(host2, schema.Host)
        assert host2.id is not None
        assert host2.status == 'INSTALLING'
        assert self.wait_for_status(host2, 'UP')
        self.store.host = host2

    @depends(_test_prepare, _test_delete)
    def _test_deactivate(self):
        host = self.store.host
        assert host.status == 'UP'
        assert self.retry_action(host, 'deactivate')
        assert self.wait_for_status(host, 'MAINTENANCE')

    @depends(_test_deactivate)
    def _test_install(self):
        host = self.store.host
        assert host.status == 'MAINTENANCE'
        action = schema.new(schema.Action)
        type = self.store.type
        if type == 'RHEL':
            action.root_password = self.store.password
        # BUG: Ticket: #170
        elif type == 'RHEV_H':
            action.isofile = self.store.isofile
        # BUG: no error is raised when the host is up
        self.api.action(host, 'install', action)
        host = self.api.reload(host)
        assert host.status == 'INSTALLING'
        assert self.wait_for_status(host, 'UP')

    @depends(_test_deactivate)
    def _test_activate(self):
        host = self.store.host
        if host.status == 'UP':
            assert self.retry_action(host, 'deactivate')
            assert self.wait_for_status(host, 'MAINTENANCE')
        self.api.action(host, 'activate')
        assert self.wait_for_status(host, 'UP')

    @depends(_test_activate)
    def _test_fence_stop(self):
        host = self.store.host
        if not host.power_management.enabled:
            raise SkipTest, 'Power management not enabled.'
        # BUG: no check on maintenance mode
        assert host.status == 'UP'
        self.retry_action(host, 'deactivate')
        self.wait_for_status(host, 'MAINTENANCE')
        action = schema.new(schema.Action)
        action.fence_type = 'STOP'
        result = self.api.action(host, 'fence', action)
        assert isinstance(result, schema.Action)
        assert result.status == 'COMPLETE'

    @depends(_test_fence_stop)
    def _test_fence_start(self):
        host = self.store.host
        assert host.status == 'MAINTENANCE'
        action = schema.new(schema.Action)
        action.fence_type = 'START'
        result = self.api.action(host, 'fence', action)
        assert isinstance(result, schema.Action)
        assert result.status == 'COMPLETE'
        assert self.wait_for_status(host, 'UP')

    @depends(_test_fence_start)
    def _test_fence_restart(self):
        host = self.store.host
        assert host.status == 'UP'
        self.retry_action(host, 'deactivate')
        self.wait_for_status(host, 'MAINTENANCE')
        action = schema.new(schema.Action)
        action.fence_type = 'RESTART'
        result = self.api.action(host, 'fence', action)
        assert isinstance(result, schema.Action)
        assert result.status == 'COMPLETE'
        assert self.wait_for_status(host, 'UP')

    @depends(_test_fence_restart)
    def _test_fence_manual(self):
        # XXX: How to test this?
        pass

    def _get_hosts(self):
        hosts = self.api.getall(schema.Host)
        hosts = [ host.name for host in hosts ]
        return hosts

    def test_host(self):
        hosts = self._get_hosts()
        test_hosts = self.get_config('hosts', default='')
        test_hosts = test_hosts.split()
        hosts = [ host for host in hosts if not host in test_hosts ]
        # Non-invasive tests on all hosts
        for host in hosts:
            yield self._test_prepare, host
            yield self._test_get
            yield self._test_reload
            yield self._test_getall
            yield self._test_search
            yield self._test_attributes
            yield self._test_has_vms
            yield self._test_has_nics
            yield self._test_has_tags
            yield self._test_has_storage
        # Invasive tests on named hosts
        for host in test_hosts[1:]:
            yield self._test_prepare, host
            yield self._test_get
            yield self._test_reload
            yield self._test_getall
            yield self._test_search
            yield self._test_attributes
            yield self._test_update
            type = self.get_config('type', host)
            if type == 'RHEL':
                yield self._test_delete
                yield self._test_create
            yield self._test_deactivate
            yield self._test_install
            yield self._test_activate
            yield self._test_fence_stop
            yield self._test_fence_start
            yield self._test_fence_restart
            yield self._test_fence_manual

    def test_prepare_nonexistent(self):
        nohost = self.api.get(schema.Host)
        assert nohost is not None
        nohost.id += 'x'
        nohost.href += 'x'
        self.store.nohost = nohost

    @depends(test_prepare_nonexistent)
    def test_get_nonexistent(self):
        nohost = self.store.nohost
        host = self.api.get(schema.Host, id=nohost.id)
        assert host is None

    @depends(test_prepare_nonexistent)
    def test_reload_nonexistent(self):
        nohost = self.store.nohost
        host = self.api.reload(nohost)
        assert host is None

    @depends(test_prepare_nonexistent)
    def test_update_nonexistent(self):
        nohost = self.store.nohost
        assert_raises(NotFound, self.api.update, nohost)

    @depends(test_prepare_nonexistent)
    def test_delete_nonexistent(self):
        nohost = self.store.nohost
        assert_raises(NotFound, self.api.delete, nohost)
