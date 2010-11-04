#!/usr/bin/env python

# Copyright (C) 2010 Red Hat, Inc.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

import xml.dom
import xml.dom.minidom

MEDIA_TYPE = 'application/xml'

class Element:
    ATTRIBUTES = []
    ELEMENTS = []
    NAME = None
    COLLECTIONS = [ None, ]
    KEY = None

    def __str__(self):
        dict = {}
        for key in self.ATTRIBUTES + self.ELEMENTS:
            if hasattr(self, key):
                dict[key] = getattr(self, key)
        return str(dict)

    def dump(self):
        s = '<' + self.NAME
        for a in self.ATTRIBUTES:
            if hasattr(self, a):
                s += ' ' + a + '=\'' + str(getattr(self, a)) + '\''
        close_tag = True
        for e in self.ELEMENTS:
            if hasattr(self, e):
                if close_tag:
                    s += '>'
                    close_tag = False
                l = getattr(self, e)
                if findCollectionType(e) != None:
                    s += '<' + e + '>'
                if isinstance(l, dict):
                    l = l.values()
                if not isinstance(l, list):
                    l = [l]
                for obj in l:
                    if isinstance(obj, Element):
                        s += obj.dump()
                    else:
                        s += '<' + e + '>' + str(obj) + '</' + e + '>'
                if findCollectionType(e) != None:
                    s += '</' + e + '>'
        if close_tag:
            s += '/>'
        else:
            s += '</' + self.NAME + '>'
        return s

class Link(Element):
    NAME = 'link'
    ATTRIBUTES = Element.ATTRIBUTES + ['rel', 'href']
    KEY = 'rel'

class Actions(Element):
    NAME = 'actions'
    ELEMENTS = Element.ELEMENTS + ['link']

class Action(Element):
    NAME = 'action'
    ATTRIBUTES = Element.ATTRIBUTES + ['id', 'href']
    ELEMENTS = Element.ELEMENTS + ['async', 'status', 'grace_period', 'root_password', 'host']

class Boot(Element):
    NAME = 'boot'
    ATTRIBUTES = Element.ATTRIBUTES + ['dev']

class CPU(Element):
    NAME = 'cpu'
    COLLECTIONS = [ 'cpus', ]
    ATTRIBUTES = Element.ATTRIBUTES + ["id"]
    ELEMENTS = Element.ELEMENTS + ["level", "topology"]

class GracePeriod(Element):
    NAME = 'grace_period'
    ELEMENTS = Element.ELEMENTS + ['expiry', 'absolute']

class OS(Element):
    NAME = 'os'
    ELEMENTS = Element.ELEMENTS + ['boot']

class Topology(Element):
    NAME = 'topology'
    ATTRIBUTES = Element.ATTRIBUTES + ['sockets', 'cores']

class MAC(Element):
    NAME = 'mac'
    ATTRIBUTES = Element.ATTRIBUTES + ['address']

class IP(Element):
    NAME = 'ip'
    ATTRIBUTES = Element.ATTRIBUTES + ['address', 'netmask', 'gateway']

class VLAN(Element):
    NAME = 'vlan'
    ATTRIBUTES = Element.ATTRIBUTES + ['id']

class Version(Element):
    NAME = 'version'
    ATTRIBUTES = Element.ATTRIBUTES + ['major', 'minor']
    COLLECTIONS = [ 'supported_versions', ]

class Base(Element):
    ATTRIBUTES = Element.ATTRIBUTES + ["id", "href"]
    ELEMENTS = Element.ELEMENTS + ["name", "description", "actions", "link"]

class CdRom(Base):
    NAME = 'cdrom'
    COLLECTIONS = [ 'cdroms', ]
    ELEMENTS = Base.ELEMENTS + ['iso', 'vm']

class Cluster(Base):
    NAME = "cluster"
    COLLECTIONS = [ "clusters", ]
    ELEMENTS = Base.ELEMENTS + ["data_center", "cpu", "version", "supported_versions"]

class DataCenter(Base):
    NAME = "data_center"
    COLLECTIONS = [ "data_centers", ]
    ELEMENTS = Base.ELEMENTS + ["storage_type", "version", "supported_versions"]

class Disk(Base):
    NAME = 'disk'
    COLLECTIONS = [ 'disks', ]
    ELEMENTS = Base.ELEMENTS + ['size', 'type', 'status', 'interface', 'format', 'sparse', 'bootable', 'wipe_after_delete', 'propagate_errors', 'vm']

class Host(Base):
    NAME = "host"
    COLLECTIONS = [ "hosts", ]
    ELEMENTS = Base.ELEMENTS + ["address", "status"]

class Iso(Base):
    NAME = 'iso'
    COLLECTIONS = [ 'isos', ]

class Network(Base):
    NAME = "network"
    COLLECTIONS = [ "networks", ]
    ELEMENTS = Base.ELEMENTS + ['data_center', 'cluster', 'ip', 'vlan', 'stp', 'status']

class NIC(Base):
    NAME = 'nic'
    COLLECTIONS = [ 'nics', ]
    ELEMENTS = Base.ELEMENTS + ['network', 'type', 'mac', 'ip', 'vm']

class HostNIC(Base):
    NAME = 'host_nic'
    COLLECTIONS = [ 'host_nics', 'slaves' ]
    ELEMENTS = Base.ELEMENTS + ['network', 'mac', 'ip', 'vlan', 'host', 'slaves']

class LogicalUnit(Element):
    NAME = 'logical_unit'
    ATTRIBUTES = Element.ATTRIBUTES + ['id']
    ELEMENTS = Element.ELEMENTS + ['address', 'target']

class Storage(Element):
    NAME = 'storage'
    ELEMENTS = Element.ELEMENTS + ['type', 'address', 'path', 'logical_unit']

class StorageDomain(Base):
    NAME = "storage_domain"
    COLLECTIONS = [ "storage_domains", ]
    ELEMENTS = Base.ELEMENTS + ['type', 'status', 'master', 'storage', 'host']

class VM(Base):
    NAME = "vm"
    COLLECTIONS = [ "vms", ]
    ELEMENTS = Base.ELEMENTS + ['type', 'status', 'memory', 'os', 'cpu', 'cluster', 'template', 'vmpool']

class VmPool(Base):
    NAME = "vmpool"
    COLLECTIONS = [ "vmpools", ]
    ELEMENTS = Base.ELEMENTS + ['size', 'cluster', 'template']

class Parent(Element):
    NAME = 'parent'
    ELEMENTS = Element.ELEMENTS + ['tag']

class Tag(Base):
    NAME = "tag"
    COLLECTIONS = [ "tags", ]
    ELEMENTS = Base.ELEMENTS + ['vm', 'host', 'user', 'parent']

class Template(Base):
    NAME = "template"
    COLLECTIONS = [ "templates", ]
    ELEMENTS = Base.ELEMENTS + ['type', 'status', 'memory', 'os', 'cpu', 'cluster', 'vm']

class Snapshot(Base):
    NAME = "snapshot"
    COLLECTIONS = [ "snapshots", ]
    ELEMENTS = Base.ELEMENTS + ['vm', 'date']

class Role(Base):
    NAME = "role"
    COLLECTION = "roles"
    ELEMENTS = Base.ELEMENTS + ['user']

class User(Base):
    NAME = "user"
    COLLECTION = "users"
    ELEMENTS = Base.ELEMENTS + ['domain', 'department', 'logged_in', 'last_name', 'user_name', 'groups', 'roles']


TYPES = [ Action, Actions, Boot, CdRom, Cluster, CPU, DataCenter, Disk, GracePeriod, Host, HostNIC, IP, Iso, Link, LogicalUnit, MAC, Network, NIC, OS, Parent, Role, Storage, StorageDomain, Snapshot, Tag, Template, Topology, User, Version, VLAN, VM, VmPool]

def findEntityType(name):
    for t in TYPES:
        if t.NAME == name:
            return t
    return None

def findCollectionType(name):
    for t in TYPES:
        for ct in t.COLLECTIONS:
            if ct == name:
                return t
    return None

def getText(nodelist):
    rc = ""
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc = rc + node.data
    return rc

def parseNode(node):
    t = findCollectionType(node.nodeName)
    if not t is None:
        l = []
        for n in node.childNodes:
            if n.nodeType != n.ELEMENT_NODE:
                continue
            obj = parseNode(n)
            if not obj is None:
                l.append(obj)
        return l

    t = findEntityType(node.nodeName)
    if not t is None:
        obj = t()
        for n in node.attributes.keys():
            if n in obj.ATTRIBUTES:
                setattr(obj, n, node.attributes[n].nodeValue)
        for n in node.childNodes:
            if n.nodeType != n.ELEMENT_NODE:
                continue
            if n.nodeName in obj.ELEMENTS:
                e = parseNode(n)
                if e is None:
                    e = getText(n.childNodes)
                if isinstance(e, Element) and not e.KEY is None:
                    if not hasattr(obj, n.nodeName):
                        setattr(obj, n.nodeName, {})
                    getattr(obj, n.nodeName)[getattr(e, e.KEY)] = e
                else:
                    setattr(obj, n.nodeName, e)
        return obj

    return None

def parse(doc):
    return parseNode(xml.dom.minidom.parseString(doc).documentElement)
