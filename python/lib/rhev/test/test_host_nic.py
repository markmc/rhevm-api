#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import struct
import socket

from rhev import *
from rhev.test import util
from rhev.test.base import BaseTest
from rhev.test.loader import depends

from nose import SkipTest
from nose.tools import assert_raises


class TestHostNic(BaseTest):

    def _prepare_host(self, name):
        address = self.get_config('address')
        if not address:
            raise SkipTest, 'Missing parameter "address" in [test].'
        netmask = self.get_config('netmask')
        if not netmask:
            raise SkipTest, 'Missing parameter "netmask" in [test].'
        host = self.api.get(schema.Host, name=name)
        if host is None:
            raise SkipTest, 'Host not found: %s' % name
        if host.status == 'UP':
            # Deactivate the host so we can manipulate its network interfaces
            assert self.retry_action(host, 'deactivate')
            assert self.wait_for_status(host, 'MAINTENANCE')
        elif host.status != 'MAINTENANCE':
            raise SkipTest, 'Host not in status UP/MAINTENANCE: %s' % name
        # BUG: if network doesn't exist but is still attached -> HTTP 500
        nics = self.api.getall(schema.HostNIC, base=host)
        assert isinstance(nics, schema.HostNics)
        for nic in nics:
            if not nic.network:
                continue
            network = self.api.reload(nic.network)
            if network.name == 'rhevm':
                continue
            action = schema.new(schema.Action)
            action.network = schema.ref(network)
            self.api.action(nic, 'detach', action)
        # All NICs should be detached now, make sure they are
        nics = self.api.getall(schema.HostNIC, base=host)
        for nic in nics:
            if nic.network:
                network = self.api.reload(nic.network)
                assert network.name == 'rhevm', 'Unable to detach all NICs'
                self.store.nic = nic
        cluster = self.api.get(schema.Cluster, id=host.cluster.id)
        assert cluster is not None
        datacenter = self.api.get(schema.DataCenter, id=cluster.data_center.id)
        self.store.host = host
        self.store.cluster = cluster
        self.store.datacenter = datacenter
        self.store.address = address
        self.store.netmask = netmask

    @depends(_prepare_host)
    def _prepare_nic(self, name):
        host = self.store.host
        nics = self.api.getall(schema.HostNIC, base=host)
        for nic in nics:
            if nic.name == name:
                break
        else:
            raise SkipTest, 'NIC not found: %s:%s' % (host.name, name)
        self.store.nic = nic
        self.store.slaves = None

    @depends(_prepare_host)
    def _prepare_bond(self, names):
        host = self.store.host
        slaves = []
        for name in names:
            slave = self.api.get(schema.HostNIC, base=host,
                                 filter={'name': name})
            if slave is None:
                raise SkipTest, 'No such NIC: %s:%s' % (host.name, name)
            slaves.append(slave)
        self.store.nic = None
        self.store.slaves = slaves

    @depends(_prepare_nic, _prepare_bond)
    def _prepare_network(self, vlan):
        host = self.store.host
        nic = self.store.nic
        if nic and nic.network:
            return
        host = self.store.host
        cluster = self.store.cluster
        datacenter = self.store.datacenter
        network = schema.new(schema.Network)
        network.name = util.random_name('net')
        if vlan:
            network.vlan = schema.new(schema.VLAN)
            network.vlan.id = vlan
        network.stp = True
        network.data_center = schema.ref(datacenter)
        network = self.api.create(network)
        assert network is not None
        clnet = self.api.create(schema.ref(network), base=cluster)
        assert network.id == clnet.id
        # Is the following good or bad?
        assert network.href != clnet.href
        self.store.network = network
        self.store.vlan = vlan

    @depends(_prepare_network)
    def _test_get(self):
        nic = self.store.nic
        host = self.store.host
        nic2 = self.api.get(schema.HostNIC, base=host, id=nic.id)
        assert nic2 is not None
        assert isinstance(nic2, schema.HostNIC)
        assert nic2.id == nic.id

    @depends(_prepare_network)
    def _test_reload(self):
        nic = self.store.nic
        nic2 = self.api.reload(nic)
        assert nic2 is not None
        assert isinstance(nic2, schema.HostNIC)
        assert nic2.id == nic.id

    @depends(_prepare_network)
    def _test_getall(self):
        nic = self.store.nic
        host = self.store.host
        nics = self.api.getall(schema.HostNIC, base=host)
        assert isinstance(nics, schema.HostNics)
        assert len(nics) > 0
        for nic2 in nics:
            assert isinstance(nic2, schema.HostNIC)
        assert util.contains_id(nics, nic.id)

    @depends(_prepare_network)
    def _test_attributes(self):
        nic = self.store.nic
        assert nic is not None
        assert util.is_str_uuid(nic.id)
        assert util.is_str(nic.href) and nic.href.endswith(nic.id)
        assert util.is_str(nic.name) and len(nic.name) > 0
        assert nic.status in ('UP', 'DOWN')  # BUG: missing
        assert util.is_str_int(nic.host.id) or util.is_str_uuid(nic.host.id)
        assert util.is_str_uuid(nic.network.id)
        assert util.is_str_mac(nic.mac.address)
        # BUG: nic.ip not set due to lack of PUT
        #assert util.is_str_ip(nic.ip.address)
        #assert util.is_str_ip(nic.ip.netmask)
        #assert util.is_str_ip(nic.ip.gateway)
        if nic.vlan is not None:
            assert util.is_int(nic.vlan.id) and nic.vlan.id > 0
        assert util.is_int(nic.speed)  # BUG: missing
        assert nic.boot_protocol in ('DHCP', 'STATIC', 'NONE')  # BUG: missing
        assert isinstance(nic.check_connectivity, bool)  # BUG: missing

    @depends(_prepare_network)
    def _test_update(self):
        nic = self.store.nic
        address = self.store.address
        netmask = self.store.netmask
        nic.ip = schema.new(schema.IP)
        nic.ip.address = util.ip_from_net(address, netmask, 2)
        nic.ip.netmask = netmask
        nic.ip.gateway = util.ip_from_net(address, netmask, 1)
        # BUG: PUT method not implemented
        # BUG: ID changes here
        nic2 = self.api.update(nic)
        assert isinstance(nic2, schema.HostNIC)
        #assert nic2.id == nic.id
        assert nic2.name == nic.name
        assert nic2.ip.address == nic.ip.address
        assert nic2.ip.netmask == nic.ip.netmask
        assert nic2.ip.gateway == nic.ip.gateway
        nic = self.api.reload(nic2)
        assert isinstance(nic2, schema.HostNIC)
        #assert nic.id == nic2.id
        assert nic.name == nic2.name
        assert nic.ip.address == nic2.ip.address
        assert nic.ip.netmask == nic2.ip.netmask
        assert nic.ip.gateway == nic2.ip.gateway
        self.store.nic = nic2  # store back changed ID

    @depends(_prepare_network)
    def _test_commitnetconfig(self):
        host = self.store.host
        result = self.api.action(host, 'commitnetconfig')
        assert result.status == 'COMPLETE'

    @depends(_prepare_network)
    def _test_attach_nic(self):
        nic = self.store.nic
        host = self.store.host
        network = self.store.network
        vlan = self.store.vlan
        action = schema.new(schema.Action)
        action.network = schema.ref(network)
        # BUG: ID changes here
        action = self.api.action(nic, 'attach', action)
        assert action.status == 'COMPLETE'
        nic = self.api.get(schema.HostNIC, base=host,
                           filter={'name': nic.name})
        assert isinstance(nic, schema.HostNIC)
        if vlan:
            assert nic.network is None
            name = '%s.%s' % (nic.name, vlan)
            nic = self.api.get(schema.HostNIC, base=host,
                               filter={'name': name})
            assert isinstance(nic, schema.HostNIC)
            assert nic.network.id == network.id
        else:
            assert nic.network.id == network.id
        self.store.nic = nic  # store back changed ID

    @depends(_test_attach_nic)
    def _test_detach_nic(self):
        nic = self.store.nic
        host = self.store.host
        network = self.store.network
        vlan = self.store.vlan
        # BUG: should not require parameter
        action = schema.new(schema.Action)
        action.network = schema.ref(network)
        self.api.action(nic, 'detach', action)
        nic2 = self.api.get(schema.HostNIC, base=host,
                            filter={'name': nic.name})
        if vlan:
            assert nic2 is None
            p1 = nic.name.find('.')
            name = nic.name[:p1]
            nic2 = self.api.get(schema.HostNIC, base=host,
                                filter={'name': name})
        assert isinstance(nic2, schema.HostNIC)
        assert nic2.network is None

    @depends(_prepare_network)
    def _delete_network(self):
        network = self.store.network
        cluster = self.store.cluster
        self.api.delete(network, base=cluster)
        self.api.delete(network)

    @depends(_prepare_network)
    def _test_attach_bond(self):
        host = self.store.host
        slaves = self.store.slaves
        network = self.store.network
        vlan = self.store.vlan
        nic = schema.new(schema.HostNIC)
        nic.name = 'bond0'
        nic.network = schema.ref(network)
        nic.slaves = schema.new(schema.Slaves)
        nic.slaves.host_nic = slaves
        #BUG: network does not exist in cluster: 500
        nic = self.api.create(nic, base=host)
        assert isinstance(nic, schema.HostNIC)
        nic2 = self.api.get(schema.HostNIC, base=host,
                            filter={'name': nic.name})
        assert isinstance(nic2, schema.HostNIC)
        assert nic2.id == nic.id
        if vlan:
            assert nic.network is None
            name = '%s.%s' % (nic.name, vlan)
            nic = self.api.get(schema.HostNIC, base=host,
                               filter={'name': name})
            assert isinstance(nic, schema.HostNIC)
            assert nic.network.id == network.id
        else:
            assert nic.network.id == network.id
        for slave in slaves:
            slave = self.api.get(schema.HostNIC, base=host,
                                 filter={'name': slave.name})
            assert isinstance(slave, schema.HostNIC)
            assert slave.network is None
        self.store.nic = nic

    @depends(_test_attach_bond)
    def _test_detach_bond(self):
        host = self.store.host
        nic = self.store.nic
        vlan = self.store.vlan
        slaves = self.store.slaves
        self.api.delete(nic)
        nic2 = self.api.get(schema.HostNIC, base=host,
                           filter={'name': nic.name})
        assert nic2 is None
        if vlan is not None:
            p1 = nic.name.find('.')
            name = nic.name[:p1]
            nic2 = self.api.get(schema.HostNIC, base=host,
                                filter={'name': name})
            assert nic2 is None
        for slave in slaves:
            slave = self.api.get(schema.HostNIC, base=host,
                                 filter={'name': slave.name})
            assert isinstance(slave, schema.HostNIC)
            assert slave.network is None

    @depends(_prepare_host)
    def _restore_host(self):
        host = self.store.host
        action = self.api.action(host, 'activate')
        assert action.status == 'COMPLETE'

    def _get_rhevm_nic(self):
        """Return the interface name connected to the RHEVM network."""
        if not hasattr(self.store, 'host'):
            return  # _prepare_host failed -> tests will be skipped
        host = self.store.host
        nics = self.api.getall(schema.HostNIC, base=host,
                               filter={'network': '*'})
        assert len(nics) == 1, '_prepare_host did not detach all nics'
        print '_get_rhevm_nic): %s(' % repr(nics[0].name)
        return nics[0].name

    def _get_test_nics(self):
        """Return a list of all test NICs. These are the physical NICs that do
        not participate in the RHEV-M network either directly or via a VLAN or
        bond."""
        if not hasattr(self.store, 'host'):
            return  [] # _prepare_host failed -> tests will be skipped
        host = self.store.host
        rhevm = self.api.get(schema.HostNIC, base=host,
                             filter={'name': self._get_rhevm_nic()})
        assert rhevm is not None
        exclude = [rhevm.name]
        # BUG: no clean way to identify if an interface belongs to a VLAN
        if '.' in rhevm.name:
            p1 = rhevm.name.find('.')
            name = rhevm.name[:p1]
            exclude.append(name)
            rhevm = self.api.get(schema.HostNIC, base=host,
                                 filter={'name': name})
            assert rhevm is not None
        if rhevm.slaves:
            remove += [ slave.name for slave in rhevm.slaves ]
        nics = self.api.getall(schema.HostNIC, base=host)
        nics = [ nic.name for nic in nics if nic.name not in exclude ]
        print '_get_test_nics(): %s' % repr(nics)
        return nics

    def _get_vlan_id(self):
        """Return a VLAN ID that is not in use in the current datacenter."""
        datacenter = self.store.datacenter
        networks = self.api.getall(schema.Network,
                    filter={'data_center.id': datacenter.id})
        vlans = [ n.vlan.id for n in networks if n.vlan and n.vlan.id ]
        for i in range(100, 4096):
            if i not in vlans:
                return i
        assert False, 'No free VLANs found in datacenter: %s' % datacenter.name

    def test_host_nic(self):
        names = self.get_config('hosts', default='')
        names = names.split()
        for name in names:
            # non-disruptive tests on the management network
            yield self._prepare_host, name
            rhevm = self._get_rhevm_nic()
            yield self._prepare_nic, rhevm
            yield self._prepare_network, None
            yield self._test_get
            yield self._test_reload
            yield self._test_getall
            yield self._test_attributes
            # more disruptive tests on other nics
            nics = self._get_test_nics()
            for nic in nics:
                vlan = self._get_vlan_id()
                for vlan in (None, vlan):
                    yield self._prepare_nic, nic
                    yield self._prepare_network, vlan
                    yield self._test_attach_nic
                    yield self._test_get
                    yield self._test_reload
                    yield self._test_getall
                    yield self._test_update
                    yield self._test_commitnetconfig
                    yield self._test_attributes
                    yield self._test_detach_nic
                    yield self._delete_network
            # now bond them 2,3,4 at a time
            for level in (2,3,4):
                if len(nics) < level:
                    continue
                for i in range(len(nics)-level+1):
                    slaves = nics[i:i+level]
                    vlan = self._get_vlan_id()
                    for vlan in (None, vlan):
                        yield self._prepare_bond, slaves
                        yield self._prepare_network, vlan
                        yield self._test_attach_bond
                        yield self._test_get
                        yield self._test_reload
                        yield self._test_getall
                        yield self._test_update
                        yield self._test_commitnetconfig
                        yield self._test_attributes
                        yield self._test_detach_bond
                        yield self._delete_network
            yield self._restore_host
            yield self._test_commitnetconfig

    def test_prepare_failures(self):
        names = self.get_config('hosts')
        if not names:
            raise SkipTest, 'No hosts configured.'
        name = names.split()[0]
        host = self.api.get(schema.Host, name=name)
        if host is None:
            raise SkipTest, 'Host does not exist: %s' % name
        nic = self.api.get(schema.HostNIC, base=host,
                           filter={'network.id': '*'})
        if nic is None:
            raise SkipTest, 'Host does not have attached NICs: %s' % name
        nonic = schema.copy(nic)
        nonic.id += 'x'
        nonic.href += 'x'
        network = self.api.reload(nic.network)
        nonetwork = schema.copy(network)
        nonetwork.id += 'x'
        nonetwork.href += 'x'
        self.store.host = host
        self.store.nic = nic
        self.store.nonic = nonic
        self.store.network = network
        self.store.nonetwork = nonetwork

    @depends(test_prepare_failures)
    def test_get_non_existent(self):
        host = self.store.host
        nonic = self.store.nonic
        nic = self.api.get(schema.HostNIC, base=host, id=nonic.id)
        assert nic is None

    @depends(test_prepare_failures)
    def test_reload_non_existent(self):
        host = self.store.host
        nonic = self.store.nonic
        nic = self.api.reload(nonic)
        assert nic is None

    @depends(test_prepare_failures)
    def test_update_non_existent(self):
        nonic = self.store.nonic
        assert_raises(NotFound, self.api.update, nonic)

    @depends(test_prepare_failures)
    def test_attach_non_existent_nic(self):
        host = self.store.host
        nonic = self.store.nonic
        network = self.store.network
        action = schema.new(schema.Action)
        action.network = schema.ref(network)
        assert_raises(NotFound, self.api.action, nonic, 'attach', action)

    @depends(test_prepare_failures)
    def test_attach_non_existent_network(self):
        host = self.store.host
        nic = self.store.nic
        nonetwork = self.store.nonetwork
        action = schema.new(schema.Action)
        action.network = schema.ref(nonetwork)
        assert_raises(Error, self.api.action, nic, 'attach', action)

    @depends(test_prepare_failures)
    def test_detach_non_existent_nic(self):
        host = self.store.host
        nonic = self.store.nonic
        network = self.store.network
        action = schema.new(schema.Action)
        action.network = schema.ref(network)
        assert_raises(NotFound, self.api.action, nonic, 'detach', action)

    @depends(test_prepare_failures)
    def test_detach_non_existent_network(self):
        host = self.store.host
        nic = self.store.nic
        nonetwork = self.store.nonetwork
        action = schema.new(schema.Action)
        action.network = schema.ref(nonetwork)
        assert_raises(Error, self.api.action, nic, 'detach', action)
