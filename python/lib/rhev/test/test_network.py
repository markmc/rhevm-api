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
from nose.tools import assert_raises


class TestNetwork(BaseTest):

    def _test_create(self):
        datacenter = schema.new(schema.DataCenter)
        datacenter.name = util.random_name('dc')
        datacenter.storage_type = 'NFS'
        datacenter = self.api.create(datacenter)
        network = schema.new(schema.Network)
        network.name = util.random_name('net')
        network.data_center = schema.ref(datacenter)
        # BUG Trac ticket #12
        #net2 = self.api.create(net, base=dc)
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

    @depends(_test_create)
    def _test_prepare(self, network):
        if network is None:
            return
        network = self.api.get(schema.Network,
                               filter={'name': network})
        assert network is not None
        datacenter = self.api.reload(network.data_center)
        self.store.network = network
        self.store.datacenter = datacenter

    @depends(_test_prepare)
    def _test_get(self):
        network = self.store.network
        network2 = self.api.get(schema.Network, id=network.id)
        assert isinstance(network2, schema.Network)
        assert network2.id == network.id

    @depends(_test_prepare)
    def _test_reload(self):
        network = self.store.network
        network2 = self.api.reload(network)
        assert isinstance(network2, schema.Network)
        assert network2.id == network.id

    @depends(_test_prepare)
    def _test_getall(self):
        network = self.store.network
        networks = self.api.getall(schema.Network)
        assert isinstance(networks, schema.Networks)
        assert len(networks) > 0
        for network in networks:
            assert isinstance(network, schema.Network)
        assert util.contains_id(networks, network.id)

    @depends(_test_prepare)
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
        # BUG: ticket #193: stp and display needs to be set always
        assert isinstance(network.stp, bool)
        assert isinstance(network.display, bool)
        # XXX: statuses are missing here
        assert network.status in ('OPERATIONAL', 'NON_OPERATIONAL')

    @depends(_test_prepare)
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

    def _get_networks(self):
        networks = self.api.getall(schema.Network)
        networks = [ network.name for network in networks ]
        return networks

    def test_network(self):
        networks = self._get_networks()
        for network in networks[:2]:
            yield self._test_prepare, network
            yield self._test_get
            yield self._test_reload
            yield self._test_getall
            yield self._test_attributes
        yield self._test_create
        yield self._test_prepare, None
        yield self._test_get
        yield self._test_reload
        yield self._test_getall
        yield self._test_attributes
        yield self._test_update
        yield self._test_delete

    @depends(_test_delete)
    def test_get_nonexisting(self):
        network = self.store.network
        network = self.api.get(schema.Network, id=network.id)
        assert network is None

    @depends(_test_delete)
    def test_reload_nonexisting(self):
        network = self.store.network
        network = self.api.reload(network)
        assert network is None

    @depends(_test_delete)
    def test_update_nonexisting(self):
        network = self.store.network
        assert_raises(KeyError, self.api.update, network)

    @depends(_test_delete)
    def test_delete_nonexisting(self):
        network = self.store.network
        assert_raises(KeyError, self.api.delete, network)
