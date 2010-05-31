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

# FIXME: handle links

class Element:
    ATTRIBUTES = []
    ELEMENTS = []
    NAME = None
    COLLECTION = None

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
                s += ' ' + a + '=\'' + getattr(self, a) + '\''
        s += '>'
        for e in self.ELEMENTS:
            if hasattr(self, e):
                obj = getattr(self, e)
                if isinstance(obj, Element):
                    s += obj.dump()
                else:
                    s += '<' + e + '>' + obj + '</' + e + '>'
        s += '</' + self.NAME + '>'
        return s

class Action(Element):
    NAME = 'action'
    ATTRIBUTES = Element.ATTRIBUTES + ['id', 'href']
    ELEMENTS = Element.ELEMENTS + ['async', 'status', 'grace_period']

class CPU(Element):
    NAME = 'cpu'
    COLLECTION = 'cpus'
    ATTRIBUTES = Element.ATTRIBUTES + ["id"]
    ELEMENTS = Element.ELEMENTS + ["level"] # FIXME: flags

class GracePeriod(Element):
    NAME = 'grace_period'
    ELEMENTS = Element.ELEMENTS + ['expiry', 'absolute']

class Base(Element):
    ATTRIBUTES = Element.ATTRIBUTES + ["id", "href"]
    ELEMENTS = Element.ELEMENTS + ["name"]

class Cluster(Base):
    NAME = "cluster"
    COLLECTION = "clusters"
    ELEMENTS = Base.ELEMENTS + ["data_center", "cpu"]

class DataCenter(Base):
    NAME = "data_center"
    COLLECTION = "data_centers"

class Host(Base):
    NAME = "host"
    COLLECTION = "hosts"

class StorageDomain(Base):
    NAME = "storage_domain"
    COLLECTION = "storage_domains"
    ELEMENTS = Base.ELEMENTS + ['type', 'status'] # FIXME: attachments

class VM(Base):
    NAME = "vm"
    COLLECTION = "vms"
    ELEMENTS = Base.ELEMENTS + ['cluster', 'template']

class Template(Base):
    NAME = "template"
    COLLECTION = "templates"

TYPES = [ Action, Cluster, CPU, DataCenter, GracePeriod, Host, StorageDomain, Template, VM ]

def findEntityType(name):
    for t in TYPES:
        if t.NAME == name:
            return t
    return None

def findCollectionType(name):
    for t in TYPES:
        if t.COLLECTION == name:
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
                setattr(obj, n.nodeName, e)
        return obj

    return None

def parse(doc):
    return parseNode(xml.dom.minidom.parseString(doc).documentElement)

def parseAction(doc):
    return parse(doc)
def parseVM(doc):
    return parse(doc)
def parseDataCenter(doc):
    return parse(doc)
def parseHost(doc):
    return parse(doc)
def parseCluster(doc):
    return parse(doc)
def parseCPU(doc):
    return parse(doc)
def parseStorageDomain(doc):
    return parse(doc)
def parseTemplate(doc):
    return parse(doc)
def parseVmCollection(doc):
    return parse(doc)
def parseDataCenterCollection(doc):
    return parse(doc)
def parseHostCollection(doc):
    return parse(doc)
def parseClusterCollection(doc):
    return parse(doc)
def parseCpuCollection(doc):
    return parse(doc)
def parseStorageDomainCollection(doc):
    return parse(doc)
def parseTemplateCollection(doc):
    return parse(doc)
