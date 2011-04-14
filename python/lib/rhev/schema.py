#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import os
import os.path
import time
import inspect
from compat import etree

from rhev import _schema
from rhev.error import ParseError
from rhev._schema import BaseResource, BaseResources

from pyxb.binding.basis import complexTypeDefinition as ComplexType
from pyxb.binding.basis import simpleTypeDefinition as SimpleType
from pyxb.exceptions_ import PyXBException

# Maintaining this table is a (small) price we pay for slight naming
# inconsistencies in the API

_mapping_data = \
[
    # (resource, collection, singular, relationship[, parent])
    (_schema.API, None, 'api', '/'),
    (_schema.DataCenter, _schema.DataCenters, 'datacenter', 'datacenters'),
    (_schema.Cluster, _schema.Clusters, 'cluster', 'clusters'),
    (_schema.StorageDomain, _schema.StorageDomains, 'storagedomain', 'storagedomains'),
    (_schema.Network, _schema.Networks, 'network', 'networks'),
    (_schema.Host, _schema.Hosts, 'host', 'hosts'),
    (_schema.HostNIC, _schema.HostNics, 'nic', 'nics', _schema.Host),
    (_schema.Storage, _schema.HostStorage, 'host_storage', 'storage'),
    (_schema.VM, _schema.VMs, 'vm', 'vms'),
    (_schema.NIC, _schema.Nics, 'nic', 'nics', _schema.VM),
    (_schema.Disk, _schema.Disks, 'disk', 'disks'),
    (_schema.CdRom, _schema.CdRoms, 'cdrom', 'cdroms'),
    (_schema.Floppy, _schema.Floppies, 'floppy', 'floppies'),
    (_schema.Snapshot, _schema.Snapshots, 'snapshot', 'snapshots'),
    (_schema.File, _schema.Files, 'file', 'files'),
    (_schema.Statistic, _schema.Statistics, 'statistic', 'statistics'),
    (_schema.Template, _schema.Templates, 'template', 'templates'),
    (_schema.VmPool, _schema.VmPools, 'vmpool', 'vmpools'),
    (_schema.User, _schema.Users, 'user', 'users'),
    (_schema.Role, _schema.Roles, 'role', 'roles'),
    (_schema.Event, _schema.Events, 'event', 'events'),
    (_schema.Tag, _schema.Tags, 'tag', 'tags'),
    (_schema.Action, _schema.Actions, None, None),
    (_schema.Capabilities, None, None, 'capabilities')
]

_xml_schema = None
_ns_xml_schema = 'http://www.w3.org/2001/XMLSchema'


def type_info(key, base=None):
    """Return information for a mapping type or name."""
    # (resourcetype, collectiontype, singluar, relationship)
    for info in _mapping_data:
        if isinstance(key, str) and \
                (info[2] == key or info[3] == key) and \
                (base is None or len(info) == 4 or issubclass(base, info[4])):
            break
        elif isinstance(key, type) and \
                (issubclass(key, info[0]) or
                 info[1] is not None and issubclass(key, info[1])):
            break
    else:
        return
    result = (info[0]._SupersedingClass(),
              info[1] and info[1]._SupersedingClass(),
              info[2], info[3])
    return result

def get_xml_schema():
    """Return the XML schema defintion as a parsed elementtree."""
    global _xml_schema
    if _xml_schema is not None:
        return _xml_schema
    pkgdir = os.path.split(inspect.getfile(_schema))[0]
    fname = os.path.join(pkgdir, 'data', 'api.xsd')
    fin = file(fname)
    parsed = etree.parse(fin)
    fin.close()
    _xml_schema = parsed
    return _xml_schema

def new(cls, *args, **kwargs):
    """Create a new object."""
    if not issubclass(cls, ComplexType):
        raise TypeError, 'Do not know how to construct %s' % cls
    schema = get_xml_schema()
    element = schema.find('{%s}element[@type="%s"]' %
                          (_ns_xml_schema, cls.__name__))
    if element is not None:
        factory = getattr(_schema, element.attrib['name'])
    else:
        factory = cls
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
    """Update elements from `ojb' with those of `upd'."""
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

def create_from_xml(s):
    """Parse an XML string and return a binding instance."""
    try:
        return _schema.CreateFromDocument(s)
    except PyXBException, e:
        raise ParseError, str(e)

def subtype(prop):
    """Return the binding type of a property."""
    return prop.fget.im_self.elementBinding().typeDefinition()


def _bind(func, *bargs, **bkwargs):
    """INTERNAL"""
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
    """This class is used to extend resource types. Resources have the
    following behavior:

    * <link> dereferencing
    * <action> dereferencing
    * various shorthand methods: reload(), add(), update(), ...
    """

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
                for link in links:
                    info = type_info(link.rel)
                    if info is None:
                        continue
                    if info[2] == name:
                        func = self._connection.getall
                        cls = info[1]
                        break
                    elif info[3] == name:
                        func = self._connection.get
                        cls = info[0]
                        break
                else:
                    raise AttributeError
                return _bind(func, cls, base=self)
        raise AttributeError

    def reload(self):
        new = self._connection.reload(self)
        update(self, new)

    def add(self, resource):
        new = self._connection.add(resource, base=self)
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


get_xml_schema()
module = globals()

for sym in dir(_schema):
    cls = getattr(_schema, sym)
    if not isinstance(cls, type) or not issubclass(cls, ComplexType):
        continue
    name = cls.__name__
    bases = (cls,)
    members = {}
    if cls in (BaseResource, BaseResources):
        continue
    elif issubclass(cls, BaseResources):
        bases += (CollectionMixin,)
        element = _xml_schema.find('{%s}complexType[@name="%s"]//{%s}element'
                                   % (_ns_xml_schema, name, _ns_xml_schema))
        if element is None:
            continue
        members['_resource'] = element.attrib['ref']
        mixin = CollectionMixin
    elif issubclass(cls, BaseResource):
        bases += (ResourceMixin,)
    derived = type(name, bases, members)
    cls._SetSupersedingClass(derived)
    module[name] = derived
