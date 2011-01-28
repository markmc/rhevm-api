#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import time

from rhev import _schema
from rhev._schema import BaseResource, BaseResources

# Maintaining this table is a (small) price we pay for slight naming
# inconsistencies in the API

_mapping_data = \
{
    # For resources: Type: (constructor, tag, collection, relationship)
    # For collections: Type: (constructor, tag, resource, relationship)
    _schema.API: (_schema.api, 'api', None, '/'),
    _schema.DataCenter: (_schema.data_center, 'data_center', _schema.DataCenters, 'datacenters'),
    _schema.DataCenters: (_schema.data_centers, 'data_centers', _schema.DataCenter, 'datacenters'),
    _schema.Cluster: (_schema.cluster, 'cluster', _schema.Clusters, 'clusters'),
    _schema.Clusters: (_schema.clusters, 'clusters', _schema.Cluster, 'clusters'),
    _schema.StorageDomain: (_schema.storage_domain, 'storage_domain', _schema.StorageDomains, 'storagedomains'),
    _schema.StorageDomains: (_schema.storage_domains, 'storage_domains', _schema.StorageDomain,'storagedomains'),
    _schema.Network: (_schema.network, 'network', _schema.Networks, 'networks'),
    _schema.Networks: (_schema.networks, 'networks', _schema.Network, 'networks'),
    _schema.Host: (_schema.host, 'host', _schema.Hosts, 'hosts'),
    _schema.Hosts: (_schema.hosts, 'hosts', _schema.Host, 'hosts'),
    _schema.HostNIC: (_schema.host_nic, 'host_nic', _schema.HostNics, 'nics'),
    _schema.HostNics: (_schema.host_nics, 'host_nics', _schema.HostNIC, 'nics'),
    _schema.Storage: (_schema.storage, 'storage', _schema.HostStorage, 'storage'),
    _schema.HostStorage: (_schema.host_storage, 'host_storage', _schema.Storage, 'storage'),
    _schema.VM: (_schema.vm, 'vm', _schema.VMs, 'vms'),
    _schema.VMs: (_schema.vms, 'vms', _schema.VM, 'vms'),
    _schema.NIC: (_schema.nic, 'nic', _schema.Nics, 'nics'),
    _schema.Nics: (_schema.nics, 'nics', _schema.NIC, 'nics'),
    _schema.Disk: (_schema.disk, 'disk', _schema.Disks, 'disks'),
    _schema.Disks: (_schema.disks, 'disks', _schema.Disk, 'disks'),
    _schema.CdRom: (_schema.cdrom, 'cdrom', _schema.CdRoms, 'cdroms'),
    _schema.CdRoms: (_schema.cdroms, 'cdroms', _schema.CdRom, 'cdroms'),
    _schema.Template: (_schema.template, 'template', _schema.Templates, 'templates'),
    _schema.Templates: (_schema.templates, 'templates', _schema.Template, 'templates'),
    _schema.VmPool: (_schema.vmpool, 'vmpool', _schema.VmPools, 'vmpools'),
    _schema.VmPools: (_schema.vmpools, 'vmpools', _schema.VmPool, 'vmpools'),
    _schema.User: (_schema.user, 'user', _schema.Users, 'users'),
    _schema.Users: (_schema.users, 'users', _schema.User, 'users'),
    _schema.Tag: (_schema.tag, 'tag', _schema.Tags, 'tags'),
    _schema.Tags: (_schema.tags, 'tags', _schema.Tag, 'tags'),
    _schema.TagParent: (_schema.parent, 'parent', None, None),
    _schema.Capabilities: (_schema.capabilities, 'capabilities', None, 'capabilities'),
    _schema.Version: (_schema.version, 'version', None, None),
    _schema.Action: (_schema.action, 'action', None, None),
    _schema.IP: (_schema.ip, 'ip', None, None),
    _schema.VLAN: (_schema.vlan, 'vlan', None, None),
    _schema.MAC: (_schema.mac, 'mac', None, None),
    _schema.Slaves: (_schema.slaves, 'slaves', None, None),
    _schema.IscsiParameters: (_schema.iscsi, 'iscsi', None, None),
    _schema.LogicalUnit: (_schema.logical_unit, 'logical_unit', None, None),
    _schema.VolumeGroup: (_schema.volume_group, 'volume_group', None, None),
    _schema.CPU: (_schema.cpu, 'cpu', None, None),
    _schema.CpuTopology: (_schema.topology, 'topology', None, None),
    _schema.Display: (_schema.display, 'display', None, None),
    _schema.HighlyAvailable: (_schema.highly_available, 'highly_available', None, None),
    _schema.OperatingSystem: (_schema.os, 'os', None, None),
    _schema.Boot: (_schema.boot, 'boot', None, None),
    _schema.Fault: (_schema.fault, 'fault', None, None)
}

_type_mapping = {}


def _type_info(obj):
    """Return information for a mapping intance or type."""
    if obj not in _type_mapping:
        return None
    base = _type_mapping[obj]
    info = (base,) + _mapping_data[base]
    return info

def new(cls, *args, **kwargs):
    """Create a new object."""
    if cls not in _type_mapping:
        raise TypeError, 'Do not know how to construct %s' % cls
    base = _type_mapping[cls]
    factory = _mapping_data[base][0]
    obj = factory(*args, **kwargs)
    return obj

def ref(obj):
    """Return a reference to an object. This is a copy of the object, with
    only the "id"  parameter set."""
    newobj = new(type(obj))
    newobj.id = obj.id
    return newobj

def href(obj):
    """Return a reference to an object. This is a copy of the object, with
    only the "id" and "href" parameters set."""
    newobj = new(type(obj))
    newobj.id = obj.id
    newobj.href = obj.href
    return newobj

def update(obj, upd):
    for el in obj._ElementMap:
        name = el.localName()
        value = getattr(upd, name)
        if value is not None:
            setattr(obj, name, value)
    for el in obj._AttributeMap:
        name = el.localName()
        value = getattr(upd, name)
        if value is not None:
            setattr(obj, name, value)
    return obj

def copy(obj):
    """Return a shallow copy of an object."""
    obj2 = new(type(obj))
    update(obj2, obj)
    return obj2

_create_from_xml = _schema.CreateFromDocument


def _bind(func, *bargs, **bkwargs):
    def _bound(*args, **kwargs):
        fargs = bargs + args
        fkwargs = bkwargs.copy()
        fkwargs.update(kwargs)
        return func(*fargs, **fkwargs)
    return _bound


class CollectionMixin(object):
    """This class is used to extend collection types with the following
    behavior:

     * list operations
     * an add() method to add a resource
    """

    def __getitem__(self, ix):
        items = getattr(self, self._resource)
        if not 0 <= ix < len(items):
            raise IndexError
        items[ix]._connection = self._connection
        return items[ix]

    def __setslice__(self, ix, iy, seq):
        items = getattr(self, self._resource)
        items[ix:iy] = seq

    def __len__(self):
        items = getattr(self, self._resource)
        return len(items)

    def __iter__(self):
        for i in range(len(self)):
            yield self[i]


class ResourceMixin(object):

    def _get_mapping_type(self, name):
        for cls in _type_mapping:
            if issubclass(cls, BaseResources):
                continue
            base = _type_mapping[cls]
            if _mapping_data[base][3] == name:
                return cls
        return None

    def __getattr__(self, name):
        if '_connection' not in self.__dict__:
            raise AttributeError
        # "actions" is a property and therefore not in self.__dict__. So we
        # need to look for it in our __class__, but then subsequently access
        # it via self to invoke the property behavior.
        if hasattr(self.__class__, 'actions'):
            actions = self.actions
            if actions is not None:
                for link in actions.link:
                    if link.rel == name:
                        return _bind(self._connection.action, self, name)
        if hasattr(self.__class__, 'link'):
            links = self.link
            if links is not None:
                cls = self._get_mapping_type(name)
                if cls is None:
                    # Very crude singular -> plural inflection. It works for
                    # the types we support....
                    if name.endswith('s'):
                        raise AttributeError
                    name += 's'
                    cls = self._get_mapping_type(name)
                    if cls is None:
                        raise AttributeError
                    func = self._connection.get
                else:
                    func = self._connection.getall
                if name not in [ link.rel for link in links ]:
                    raise AttributeError
                return _bind(func, cls, base=self)
        raise AttributeError

    def reload(self):
        new = self._connection.reload(self)
        update(self, new)

    def add(self, resource):
        new = self._connection.add(resource, self)
        update(resource, new)
        return resource

    def update(self):
        return self._connection.update(self)

    def delete(self, data=None):
        return self._connection.delete(self, data=data)

    def wait_for_status(self, status, timeout=None):
        start = time.time()
        if isinstance(status, str):
            status = (status,)
        delay = 1
        while True:
            self.reload()
            if self.status in status:
                break
            time.sleep(delay)
            delay = min(60, delay*2)
            time.sleep(delay)
            now = time.time()
            if timeout is not None and start + timeout >= now:
                return False
        return True


module = globals()

for cls in _mapping_data:
    name = cls.__name__
    members = {}
    if issubclass(cls, BaseResources):
        resource = _mapping_data[cls][2]
        members['_resource'] = _mapping_data[resource][1]
        mixin = CollectionMixin
    else:
        mixin = ResourceMixin
    derived = type(name, (cls, mixin), members)
    cls._SetSupersedingClass(derived)
    module[name] = derived
    _type_mapping[derived] = cls
