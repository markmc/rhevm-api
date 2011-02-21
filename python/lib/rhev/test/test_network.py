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
from nose.tools import assert_raises


class TestNetwork(BaseTest):

    def _test_use_existing(self, network):
        network = self.api.get(schema.Network,
                               filter={'name': network})
        assert network is not None
        datacenter = self.api.reload(network.data_center)
        self.store.network = network
        self.store.datacenter = datacenter

    def _test_create(self):
        datacenter = schema.new(schema.DataCenter)
        datacenter.name = util.random_name('dc')
        datacenter.version = self.get_version()
        datacenter.storage_type = 'NFS'
        datacenter = self.api.create(datacenter)
        network = schema.new(schema.Network)
        network.name = util.random_name('net')
        network.description = 'foo'
        network.data_center = schema.ref(datacenter)
        network.stp = False
        network.vlan = self.get_vlan()
        network.ip = self.get_unused_ip()
        network.display = False
        network2 = self.api.create(network)
        assert isinstance(network2, schema.Network)
        assert network2.id is not None
        assert network2.href is not None
        assert network2.data_center.id == datacenter.id
        network = self.api.get(schema.Network, id=network2.id)
        assert isinstance(network, schema.Network)
        assert network.id == network2.id
        self.store.datacenter = datacenter
        self.store.network = network

    @depends(_test_use_existing, _test_create)
    def _test_get(self):
        network = self.store.network
        network2 = self.api.get(schema.Network, id=network.id)
        assert isinstance(network2, schema.Network)
        assert network2.id == network.id

    @depends(_test_use_existing, _test_create)
    def _test_reload(self):
        network = self.store.network
        network2 = self.api.reload(network)
        assert isinstance(network2, schema.Network)
        assert network2.id == network.id

    @depends(_test_use_existing, _test_create)
    def _test_getall(self):
        network = self.store.network
        networks = self.api.getall(schema.Network)
        assert isinstance(networks, schema.Networks)
        assert len(networks) > 0
        for network in networks:
            assert isinstance(network, schema.Network)
        assert util.contains_id(networks, network.id)

    @depends(_test_use_existing, _test_create)
    def _test_attributes(self):
        network = self.store.network
        assert util.is_str_uuid(network.id)
        assert util.is_str(network.href) and network.href.endswith(network.id)
        assert util.is_str(network.name) and len(network.name) > 0
        assert network.description is None or util.is_str(network.description)
        assert util.is_str_uuid(network.data_center.id)
        if network.ip is not None:
            assert network.ip.address is None or \
                    util.is_str_ip(network.ip.address)
            assert network.ip.netmask is None or \
                    util.is_str_ip(network.ip.netmask)
            assert network.ip.gateway is None or \
                    util.is_str_ip(network.ip.gateway)
        if network.vlan is not None:
            assert util.is_int(network.vlan.id)
        assert util.is_bool(network.stp)
        assert util.is_bool(network.display)
        assert network.status in ('OPERATIONAL', 'NON_OPERATIONAL')

    @depends(_test_create)
    def _test_update(self):
        network = self.store.network
        network.description = 'foobar'
        network2 = self.api.update(network)
        assert network2.description == 'foobar'
        network = self.api.get(schema.Network, id=network2.id)
        assert network.id == network2.id
        assert network.description == 'foobar'

    @depends(_test_create)
    def _test_delete(self):
        datacenter = self.store.datacenter
        network = self.store.network
        network2 = self.api.delete(network)
        assert network2 is None
        network2 = self.api.get(schema.Network,
                                filter={'name': network.name})
        assert network2 is None
        self.api.delete(datacenter)

    def test_network(self):
        networks = self.api.getall(schema.Network)
        networks = [ network.name for network in networks ]
        # Non-intrusive tests on existing networks
        for network in networks:
            yield self._test_use_existing, network
            yield self._test_get
            yield self._test_reload
            yield self._test_getall
            yield self._test_attributes
        # Intrusive tests on a newly created network
        yield self._test_create
        yield self._test_get
        yield self._test_reload
        yield self._test_getall
        yield self._test_attributes
        yield self._test_update
        yield self._test_delete

    def test_prepare_nonexisting(self):
        network = schema.new(schema.Network)
        network.id = 'foo'
        network.href = '%s/networks/foo' % self.api.entrypoint
        self.store.network = network

    @depends(test_prepare_nonexisting)
    def test_get_nonexisting(self):
        network = self.store.network
        network = self.api.get(schema.Network, id=network.id)
        assert network is None

    @depends(test_prepare_nonexisting)
    def test_reload_nonexisting(self):
        network = self.store.network
        network = self.api.reload(network)
        assert network is None

    @depends(test_prepare_nonexisting)
    def test_update_nonexisting(self):
        network = self.store.network
        assert_raises(NotFound, self.api.update, network)

    @depends(test_prepare_nonexisting)
    def test_delete_nonexisting(self):
        network = self.store.network
        assert_raises(NotFound, self.api.delete, network)
