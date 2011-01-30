#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import time

from rhev import _schema, inflect
from rhev.error import ParseError
from rhev._schema import BaseResource, BaseResources

from pyxb.binding.basis import complexTypeDefinition as ComplexType
from pyxb.binding.basis import simpleTypeDefinition as SimpleType
from pyxb.exceptions_ import PyXBException

# Maintaining this table is a (small) price we pay for slight naming
# inconsistencies in the API

_mapping_data = \
[
    # (resource, collection, relationship)
    (_schema.API, None, '/', True),
    (_schema.DataCenter, _schema.DataCenters, 'datacenters', True),
    (_schema.Cluster, _schema.Clusters, 'clusters', True),
    (_schema.StorageDomain, _schema.StorageDomains, 'storagedomains', True),
    (_schema.Network, _schema.Networks, 'networks', True),
    (_schema.Host, _schema.Hosts, 'hosts', True),
    (_schema.HostNIC, _schema.HostNics, 'nics', False),
    (_schema.HostStorage, _schema.Storage, 'storage', False),
    (_schema.VM, _schema.VMs, 'vms', True),
    (_schema.NIC, _schema.Nics, 'nics', False),
    (_schema.Disk, _schema.Disks, 'disks', False),
    (_schema.CdRom, _schema.CdRoms, 'cdroms', False),
    (_schema.Floppy, _schema.Floppies, 'floppies', False),
    (_schema.Statistic, _schema.Statistics, 'statistics', False),
    (_schema.Template, _schema.Templates, 'templates', True),
    (_schema.VmPool, _schema.VmPools, 'vmpools', True),
    (_schema.User, _schema.Users, 'users', True),
    (_schema.Event, _schema.Events, 'events', True),
    (_schema.Tag, _schema.Tags, 'tags', True),
    (_schema.Action, _schema.Actions, None, False),
    (_schema.Capabilities, None, 'capabilities', True)
]

# This is a mapping of binding class -> element constructor. Element
# constructors are generated for all top-level elements.

_element_constructors = {}

for key in _schema.Namespace.elementBindings():
    cls = _schema.Namespace.elementBindings()[key].typeDefinition()
    _element_constructors[cls] = getattr(_schema, key)


def _type_info(typ_or_rel):
    """Return information for a mapping type or relationship."""
    for info in _mapping_data:
        if isinstance(typ_or_rel, basestring):
            if info[2] == typ_or_rel:
                break
        elif isinstance(typ_or_rel, type):
            if issubclass(typ_or_rel, info[0]) or \
                    info[1] is not None and issubclass(typ_or_rel, info[1]):
                break
    else:
        return
    info += (_element_constructors[info[0]].name().localName(),)
    if info[1] and info[1] in _element_constructors:
        info += (_element_constructors[info[1]].name().localName(),)
    else:
        info += (None,)
    info += (_element_constructors[info[0]],)
    if info[1] and info[1] in _element_constructors:
        info += (_element_constructors[info[1]],)
    else:
        info += (None,)
    return info

def subtype(prop):
    """Return the binding type of a property."""
    return prop.fget.im_self.elementBinding().typeDefinition()

def singular(rel):
    """Return the singular noun for a relationship name (which are plural by
    convention)."""
    info = _type_info(rel)
    if info is None:
        return None
    if rel == 'storage':
        s = 'host_storage'
    else:
        s = rel[:-1]
    return s

def plural(s):
    """Return the relationship name for a noun that was made singular by
    plural()."""
    if s == 'host_storage':
        rel = 'storage'
    else:
        rel = s + 's'
    info = _type_info(rel)
    if info is None:
        return None
    return rel

def new(cls, *args, **kwargs):
    """Create a new object."""
    info = _type_info(cls)
    if info is not None:
        if issubclass(cls, info[0]):
            factory = info[6]
        elif issubclass(cls, info[1]):
            factory = info[7]
    elif issubclass(cls, ComplexType):
        factory = cls
    else:
        raise TypeError, 'Do not know how to construct %s' % cls
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

def create_from_xml(s):
    """Parse an XML string and return a binding instance."""
    try:
        return _schema.CreateFromDocument(s)
    except PyXBException, e:
        raise ParseError, str(e)

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


class SubResourceMixin(object):
    """This class is used to extend "sub" resource types. "sub" resources are
    not real resources, but they do follow <link>s and <actions>s.
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
                    if link.rel == name:
                        info = _type_info(link.rel)
                        if info is None:
                            raise AttributeError
                        func = self._connection.getall
                        cls = info[1]
                        break
                else:
                    plural = inflect.plural_noun(name)
                    if plural is None:
                        raise AttributeError
                    for link in links:
                        if link.rel == plural:
                            info = _type_info(plural)
                            if info is None:
                                raise AttributeError
                            func = self._connection.get
                            cls = info[0]
                            break
                    else:
                        raise AttributeError
                return _bind(func, cls, base=self)
        raise AttributeError


class ResourceMixin(SubResourceMixin):
    """This class is used to extend resource types. Resources have the
    following behavior:

    * <link> dereferencing
    * <action> dereferencing
    * various shorthand methods: reload(), add(), update(), ...
    """

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
        info = _type_info(cls)
        if info is None:
            continue
        members['_resource'] = info[4]
        mixin = CollectionMixin
    elif issubclass(cls, BaseResource):
        bases += (ResourceMixin,)
    elif cls is _schema.API:
        bases += (SubResourceMixin,)
    derived = type(name, bases, members)
    cls._SetSupersedingClass(derived)
    module[name] = derived
