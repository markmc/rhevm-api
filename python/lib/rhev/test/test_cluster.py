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


class TestCluster(BaseTest):

    def _test_use_existing(self, name):
        cluster = self.api.get(schema.Cluster, name=name)
        if cluster is None:
            raise SkipTest, 'Cluster not found: %s' % name
        assert cluster is not None
        if cluster.data_center is None or cluster.data_center.id is None:
            raise SkipTest, 'Skipping tests on orphaned cluster: %s' % name
        self.store.cluster = cluster

    def _test_create(self):
        datacenter = schema.new(schema.DataCenter)
        datacenter.name = util.random_name('dc')
        datacenter.version = self.get_version()
        datacenter.storage_type = 'nfs'
        datacenter = self.api.create(datacenter)
        assert datacenter is not None
        datacenter2 = schema.new(schema.DataCenter)
        datacenter2.name = util.random_name('dc')
        datacenter2.version = self.get_version()
        datacenter2.storage_type = 'nfs'
        datacenter2 = self.api.create(datacenter2)
        assert datacenter2 is not None
        cluster = schema.new(schema.Cluster)
        cluster.name = util.random_name('cluster')
        cluster.data_center = schema.ref(datacenter)
        cluster.cpu = self.get_cpu()
        cluster.version = self.get_version()
        cluster2 = self.api.create(cluster)
        assert cluster2.id is not None
        assert cluster2.href is not None
        assert cluster2.data_center.id == cluster.data_center.id
        assert cluster2.cpu.id == cluster.cpu.id
        cluster = self.api.get(schema.Cluster, id=cluster2.id)
        assert cluster is not None
        assert isinstance(cluster, schema.Cluster)
        assert cluster.id == cluster2.id
        self.store.datacenter = datacenter
        self.store.datacenter2 = datacenter2
        self.store.cluster = cluster

    @depends(_test_create)
    def _test_create_duplicate_name(self):
        cluster = self.store.cluster
        cluster2 = schema.new(schema.Cluster)
        cluster2.name = cluster.name
        cluster2.data_center = schema.ref(self.store.datacenter)
        cluster2.cpu = self.get_cpu()
        cluster2.version = self.get_version()
        assert_raises(Fault, self.api.create, cluster2)

    @depends(_test_use_existing, _test_create)
    def _test_get(self):
        cluster = self.store.cluster
        cluster2 = self.api.get(schema.Cluster, id=cluster.id)
        assert isinstance(cluster2, schema.Cluster)
        assert cluster2.id == cluster.id

    @depends(_test_use_existing, _test_create)
    def _test_reload(self):
        cluster = self.store.cluster
        cluster2 = self.api.reload(cluster)
        assert isinstance(cluster2, schema.Cluster)
        assert cluster2.id == cluster.id

    @depends(_test_use_existing, _test_create)
    def _test_getall(self):
        cluster = self.store.cluster
        clusters = self.api.getall(schema.Cluster)
        assert isinstance(clusters, schema.Clusters)
        assert len(clusters) > 0
        assert util.contains_id(clusters, cluster.id)

    @depends(_test_use_existing, _test_create)
    def _test_search(self):
        cluster = self.store.cluster
        clusters = self.api.getall(schema.Cluster, name=cluster.name)
        assert isinstance(clusters, schema.Clusters)
        assert len(clusters) == 1
        assert clusters[0].id == cluster.id
        clusters = self.api.getall(schema.Cluster, search='name=%s' % cluster.name)
        assert isinstance(clusters, schema.Clusters)
        assert len(clusters) == 1
        assert clusters[0].id == cluster.id

    @depends(_test_use_existing, _test_create)
    def _test_attributes(self):
        cluster = self.store.cluster
        assert util.is_str_uuid(cluster.id) or util.is_str_int(cluster.id)
        assert util.is_str(cluster.href) and cluster.href.endswith(cluster.id)
        assert util.is_str(cluster.name) and len(cluster.name) > 0
        assert cluster.description is None or util.is_str(cluster.description)
        assert cluster.data_center is not None
        assert util.is_str_uuid(cluster.data_center.id)
        assert cluster.version is not None
        assert util.is_int(cluster.version.major) and cluster.version.major > 0
        assert util.is_int(cluster.version.minor)
        assert cluster.supported_versions is not None
        for version in cluster.supported_versions.version:
            assert util.is_int(version.major) and version.major > 0
            assert util.is_int(version.minor)
        assert cluster.cpu is not None
        assert util.is_str(cluster.cpu.id)
        assert cluster.memory_policy is not None
        assert util.is_int(cluster.memory_policy.overcommit.percent)
        if cluster.scheduling_policy:
            assert cluster.scheduling_policy.policy in \
                        ('even_distribution', 'power_saving')
            assert util.is_int(cluster.scheduling_policy.thresholds.low)
            assert util.is_int(cluster.scheduling_policy.thresholds.high)
            assert util.is_int(cluster.scheduling_policy.thresholds.duration)

    @depends(_test_use_existing, _test_create)
    def _test_has_networks(self):
        cluster = self.store.cluster
        networks = self.api.getall(schema.Network, base=cluster)
        assert isinstance(networks, schema.Networks)
        for network in networks:
            assert isinstance(network, schema.Network)
            assert network.id is not None

    @depends(_test_create)
    def _test_update(self):
        cluster = self.store.cluster
        cluster.description = 'foobar'
        cluster2 = self.api.update(cluster)
        assert cluster2.description == 'foobar'
        cluster = self.api.get(schema.Cluster, id=cluster2.id)
        assert cluster.description == 'foobar'

    @depends(_test_create)
    def _test_attach_network(self):
        cluster = self.store.cluster
        network = schema.new(schema.Network)
        network.name = util.random_name('net')
        network.description = 'foo'
        network.data_center = schema.ref(self.store.datacenter)
        network.stp = False
        network.vlan = self.get_vlan()
        network.ip = self.get_unused_ip()
        network.display = False
        network = self.api.create(network)
        assert network is not None
        atnetwork = self.api.create(network, base=cluster)
        networks = self.api.getall(schema.Network, base=cluster)
        assert util.contains_id(networks, atnetwork.id)
        self.store.network = network
        self.store.atnetwork = atnetwork

    @depends(_test_attach_network)
    def _test_network_attributes(self):
        cluster = self.store.cluster
        network = self.store.network
        atnetwork = self.store.atnetwork
        assert isinstance(atnetwork, schema.Network)
        assert util.is_str_uuid(atnetwork.id)
        assert atnetwork.id == network.id
        assert util.is_str_href(atnetwork.href)
        assert atnetwork.href != network.href
        assert atnetwork.href.startswith(cluster.href)
        assert atnetwork.href.endswith(network.id)
        assert util.is_str(atnetwork.name) and len(atnetwork.name) > 0
        assert atnetwork.name == network.name
        assert util.is_str(atnetwork.description) and len(atnetwork.description) > 0
        assert atnetwork.description == network.description
        assert atnetwork.status in ('OPERATIONAL', 'NON_OPERATIONAL')
        assert isinstance(atnetwork.data_center, schema.DataCenter)
        assert atnetwork.data_center.id == network.data_center.id
        assert isinstance(atnetwork.cluster, schema.Cluster)
        assert atnetwork.cluster.id == cluster.id
        assert util.is_bool(atnetwork.stp)
        assert atnetwork.stp == network.stp
        assert util.is_bool(atnetwork.display)
        assert isinstance(atnetwork.vlan, schema.VLAN)
        assert util.is_int(atnetwork.vlan.id)
        assert atnetwork.vlan.id == network.vlan.id
        assert isinstance(atnetwork.ip, schema.IP)
        assert util.is_str_ip(atnetwork.ip.address)
        assert util.is_str_ip(atnetwork.ip.netmask)
        assert util.is_str_ip(atnetwork.ip.gateway)
        assert atnetwork.ip.address == network.ip.address
        assert atnetwork.ip.netmask == network.ip.netmask
        assert atnetwork.ip.gateway == network.ip.gateway

    @depends(_test_attach_network)
    def _test_set_display_network(self):
        atnetwork = self.store.atnetwork
        atnetwork.display = True
        atnetwork2 = self.api.update(atnetwork)
        assert isinstance(atnetwork2, schema.Network)
        assert atnetwork2.id == atnetwork.id
        assert atnetwork2.display is True
        atnetwork = self.api.get(schema.Network, base=self.store.cluster,
                                 id=atnetwork2.id)
        assert isinstance(atnetwork, schema.Network)
        assert atnetwork.id == atnetwork2.id
        assert atnetwork.display is True

    @depends(_test_attach_network)
    def _test_detach_network(self):
        cluster = self.store.cluster
        network = self.store.network
        atnetwork = self.store.atnetwork
        self.api.delete(atnetwork)
        networks = self.api.getall(schema.Network, base=cluster)
        assert not util.contains_id(networks, atnetwork.id)
        networks = self.api.getall(schema.Network)
        assert util.contains_id(networks, network.id)
        self.api.delete(network)
        networks = self.api.getall(schema.Network)
        assert not util.contains_id(networks, network.id)

    @depends(_test_detach_network)
    def _test_get_nonexistent_network(self):
        cluster = self.store.cluster
        atnetwork = self.store.atnetwork
        assert_raises(NotFound, self.api.get, schema.Network,
                      base=cluster, id=atnetwork.id)
 
    @depends(_test_detach_network)
    def _test_detach_nonexistent_network(self):
        atnetwork = self.store.atnetwork
        assert_raises(NotFound, self.api.delete, atnetwork)
 
    @depends(_test_detach_network)
    def _test_update_nonexistent_network(self):
        atnetwork = self.store.atnetwork
        assert_raises(NotFound, self.api.update, atnetwork)

    @depends(_test_attach_network)
    def _test_detach_network(self):
        cluster = self.store.cluster
        network = self.store.network
        atnetwork = self.store.atnetwork
        self.api.delete(atnetwork)
        networks = self.api.getall(schema.Network, base=cluster)
        assert not util.contains_id(networks, atnetwork.id)
        networks = self.api.getall(schema.Network)
        assert util.contains_id(networks, network.id)
        self.api.delete(network)
        networks = self.api.getall(schema.Network)
        assert not util.contains_id(networks, network.id)

    @depends(_test_create)
    def _test_change_datacenter(self):
        cluster = self.store.cluster
        cluster.data_center = schema.ref(self.store.datacenter2)
        cluster2 = self.api.update(cluster)
        assert isinstance(cluster2, schema.Cluster)
        assert cluster2.id == cluster.id
        assert cluster2.data_center.id == self.store.datacenter2.id
        cluster = self.api.get(schema.Cluster, name=cluster2.name)
        assert isinstance(cluster, schema.Cluster)
        assert cluster.id == cluster2.id
        assert cluster.data_center.id == self.store.datacenter2.id

    @depends(_test_create)
    def _test_delete(self):
        cluster = self.store.cluster
        self.api.delete(cluster)
        cluster = self.api.get(schema.Cluster, name=cluster.name)
        assert cluster is None
        self.api.delete(self.store.datacenter)
        self.api.delete(self.store.datacenter2)

    def test_cluster(self):
        clusters = self.api.getall(schema.Cluster)
        clusters = [ cluster.name for cluster in clusters ]
        # Non-intrusive tests on existing clusters
        for cluster in clusters:
            yield self._test_use_existing, cluster
            yield self._test_get
            yield self._test_reload
            yield self._test_getall
            yield self._test_search
            yield self._test_attributes
            yield self._test_has_networks
        # Intrusive tests on a newly created cluster
        yield self._test_create
        yield self._test_create_duplicate_name
        yield self._test_get
        yield self._test_reload
        yield self._test_getall
        yield self._test_search
        yield self._test_attributes
        yield self._test_attach_network
        yield self._test_network_attributes
        yield self._test_set_display_network
        yield self._test_detach_network
        yield self._test_get_nonexistent_network
        yield self._test_update_nonexistent_network
        yield self._test_detach_nonexistent_network
        yield self._test_change_datacenter
        yield self._test_update
        yield self._test_delete

    def test_prepare_nonexistent(self):
        cluster = schema.new(schema.Cluster)
        cluster.id = 'foo'
        cluster.href = '%s/clusters/foo' % self.api.entrypoint
        self.store.cluster = cluster

    @depends(test_prepare_nonexistent)
    def test_get_nonexistent(self):
        cluster = self.store.cluster
        cluster = self.api.get(schema.Cluster, id=cluster.id)
        assert cluster is None

    @depends(test_prepare_nonexistent)
    def test_reload_nonexistent(self):
        cluster = self.store.cluster
        cluster = self.api.reload(cluster)
        assert cluster is None

    @depends(test_prepare_nonexistent)
    def test_update_nonexistent(self):
        cluster = self.store.cluster
        assert_raises(NotFound, self.api.update, cluster)

    @depends(test_prepare_nonexistent)
    def test_delete_nonexistent(self):
        cluster = self.store.cluster
        assert_raises(NotFound, self.api.delete, cluster)
