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
from nose.tools import assert_raises


class TestCluster(BaseTest):

    def _get_cpu(self):
        if not hasattr(self.store, 'cpu'):
            caps = self.api.get(schema.Capabilities)
            for version in caps.version:
                if not version.current:
                    continue
                self.store.cpu = version.cpus.cpu[0]
                break
        return self.store.cpu

    def _get_version(self):
        caps = self.api.get(schema.Capabilities)
        for version in caps.version:
            if not version.current:
                continue
            copy = schema.new(schema.Version)
            copy.minor = version.minor
            copy.major = version.major
            return copy

    def _test_create(self):
        datacenter = schema.new(schema.DataCenter)
        datacenter.name = util.random_name('dc')
        datacenter.storage_type = 'NFS'
        datacenter = self.api.create(datacenter)
        cluster = schema.new(schema.Cluster)
        cluster.name = util.random_name('cluster')
        cluster.data_center = schema.ref(datacenter)
        cluster.cpu = schema.ref(self._get_cpu())
        cluster.version = self._get_version()
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
        self.store.cluster = cluster

    @depends(_test_create)
    def _test_prepare(self, cluster):
        if cluster is None:
            return
        cluster = self.api.get(schema.Cluster, name=cluster)
        assert cluster is not None
        assert cluster.data_center is not None
        if cluster.data_center.id is None:
            raise SkipTest, 'Not testing orphaned cluster'
        datacenter = self.api.reload(cluster.data_center)
        self.store.cluster = cluster
        self.store.datacenter = datacenter

    @depends(_test_prepare)
    def _test_get(self):
        cluster = self.store.cluster
        cluster2 = self.api.get(schema.Cluster, id=cluster.id)
        assert isinstance(cluster2, schema.Cluster)
        assert cluster2.id == cluster.id

    @depends(_test_prepare)
    def _test_reload(self):
        cluster = self.store.cluster
        cluster2 = self.api.reload(cluster)
        assert isinstance(cluster2, schema.Cluster)
        assert cluster2.id == cluster.id

    @depends(_test_prepare)
    def _test_getall(self):
        cluster = self.store.cluster
        clusters = self.api.getall(schema.Cluster)
        assert isinstance(clusters, schema.Clusters)
        assert len(clusters) > 0
        assert util.contains_id(clusters, cluster.id)

    @depends(_test_prepare)
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

    @depends(_test_prepare)
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
        # below have been recently added
        assert cluster.memory_policy is not None
        assert util.is_int(cluster.memory_policy.overcommit.percent)
        if cluster.scheduling_policy:
            assert cluster.scheduling_policy.policy in \
                        ('EVEN_DISTRIBUTION', 'POWER_SAVING')
            assert util.is_int(cluster.scheduling_policy.thresholds.low)
            assert util.is_int(cluster.scheduling_policy.thresholds.high)
            assert util.is_int(cluster.scheduling_policy.thresholds.duration)

    @depends(_test_prepare)
    def _test_update(self):
        cluster = self.store.cluster
        cluster.description = 'foobar'
        cluster2 = self.api.update(cluster)
        assert cluster2.description == 'foobar'
        cluster = self.api.get(schema.Cluster, id=cluster2.id)
        assert cluster.description == 'foobar'

    @depends(_test_prepare)
    def _test_delete(self):
        datacenter = self.store.datacenter
        cluster = self.store.cluster
        cluster2 = self.api.delete(cluster)
        assert cluster2 is None
        cluster = self.api.get(schema.Cluster, name=cluster.name)
        assert cluster is None
        self.api.delete(datacenter)

    @depends(_test_prepare)
    def _test_has_networks(self):
        cluster = self.store.cluster
        networks = self.api.getall(schema.Network, base=cluster)
        assert isinstance(networks, schema.Networks)
        for network in networks:
            assert isinstance(network, schema.Network)
            assert util.is_str_uuid(network.id)

    @depends(_test_prepare)
    def _prepare_network(self):
        cluster = self.store.cluster
        datacenter = self.store.datacenter
        network = schema.new(schema.Network)
        network.name = util.random_name('net')
        network.data_center = schema.ref(datacenter)
        network = self.api.create(network)
        assert network is not None
        self.store.network = network

    @depends(_prepare_network)
    def _test_attach_network(self):
        cluster = self.store.cluster
        network = self.store.network
        network2 = self.api.create(network, base=cluster)
        assert isinstance(network2, schema.Network)
        assert network2.id == network.id
        assert network2.href != network.href  # points to sub-collection
        networks = self.api.getall(schema.Network, base=cluster)
        assert util.contains_id(networks, network.id)

    @depends(_test_attach_network)
    def _test_detach_network(self):
        cluster = self.store.cluster
        network = self.store.network
        self.api.delete(network, base=cluster)
        networks = self.api.getall(schema.Network, base=cluster)
        assert not util.contains_id(networks, network.id)
        networks = self.api.getall(schema.Network)
        assert util.contains_id(networks, network.id)

    @depends(_prepare_network)
    def _delete_network(self):
        network = self.store.network
        self.api.delete(network)

    def _get_clusters(self):
        clusters = self.api.getall(schema.Cluster)
        clusters = [ cluster.name for cluster in clusters ]
        return clusters

    def test_cluster(self):
        clusters = self._get_clusters()
        # Tests on existing clusters
        for cluster in clusters:
            yield self._test_prepare, cluster
            yield self._test_get
            yield self._test_reload
            yield self._test_getall
            yield self._test_search
            yield self._test_attributes
            yield self._test_has_networks
        # Tests on a newly created cluster
        yield self._test_create
        yield self._test_prepare, None
        yield self._test_get
        yield self._test_reload
        yield self._test_getall
        yield self._test_search
        yield self._test_attributes
        # BUG? asymmetry between adding networks and adding vms/hosts
        yield self._prepare_network
        yield self._test_attach_network
        yield self._test_detach_network
        yield self._delete_network
        yield self._test_delete

    @depends(_test_delete)
    def test_get_nonexistent(self):
        cluster = self.store.cluster
        cluster = self.api.get(schema.Cluster, id=cluster.id)
        assert cluster is None

    @depends(_test_delete)
    def test_reload_nonexistent(self):
        cluster = self.store.cluster
        cluster = self.api.reload(cluster)
        assert cluster is None

    @depends(_test_delete)
    def test_update_nonexistent(self):
        cluster = self.store.cluster
        assert_raises(KeyError, self.api.update, cluster)

    @depends(_test_delete)
    def test_delete_nonexistent(self):
        cluster = self.store.cluster
        assert_raises(KeyError, self.api.delete, cluster)
