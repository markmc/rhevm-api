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

def getText(nodelist):
    rc = ""
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc = rc + node.data
    return rc

def parseNode(node):
    ret = {}
    for n in node.attributes.keys():
        n = node.attributes[n]
        ret[n.nodeName] = n.nodeValue
    for n in node.childNodes:
        if n.nodeType != n.ELEMENT_NODE:
            continue
        if n.nodeName == 'link':
            ret[n.attributes['rel'].nodeValue] = n.attributes['href'].nodeValue
        else:
            ret[n.nodeName] = getText(n.childNodes)
    return ret

class Link:
    def __init__(self, rel, href):
        self.rel = rel
        self.href = href

class Base:
    def __init__(self, node = None):
        if node is None:
            return

        dict = parseNode(node)
        for key in self.KEYS:
            if key in dict:
                setattr(self, key, dict[key])

    def __str__(self):
        dict = {}
        for key in self.KEYS:
            if hasattr(self, key):
                dict[key] = getattr(self, key)
        return str(dict)

class VM(Base):
    COLLECTION_ELEMENT = 'vms'
    ROOT_ELEMENT = 'vm'
    KEYS = ['id', 'name', 'href']

    def dump(self):
        s = '<vm'
        if hasattr(self, 'href'):
            s += ' href=\'' + getattr(self, 'href') + '\''
        if hasattr(self, 'id'):
            s += ' id=\'' + getattr(self, 'id') + '\''
        s += '>'
        if hasattr(self, 'name'):
            s += '<name>' + getattr(self, 'name') + '</name>'
        s += '</vm>'
        return s

class Host(Base):
    COLLECTION_ELEMENT = 'hosts'
    ROOT_ELEMENT = 'host'
    KEYS = ['id', 'name', 'href']

    def dump(self):
        s = '<host'
        if hasattr(self, 'href'):
            s += ' href=\'' + getattr(self, 'href') + '\''
        if hasattr(self, 'id'):
            s += ' id=\'' + getattr(self, 'id') + '\''
        s += '>'
        if hasattr(self, 'name'):
            s += '<name>' + getattr(self, 'name') + '</name>'
        s += '</host>'
        return s

class Cluster(Base):
    COLLECTION_ELEMENT = 'clusters'
    ROOT_ELEMENT = 'cluster'
    KEYS = ['id', 'name', 'href']

    def dump(self):
        s = '<cluster'
        if hasattr(self, 'href'):
            s += ' href=\'' + getattr(self, 'href') + '\''
        if hasattr(self, 'id'):
            s += ' id=\'' + getattr(self, 'id') + '\''
        s += '>'
        if hasattr(self, 'name'):
            s += '<name>' + getattr(self, 'name') + '</name>'
        s += '</cluster>'
        return s

class StorageDomain(Base):
    COLLECTION_ELEMENT = 'storage_domains'
    ROOT_ELEMENT = 'storage_domain'
    KEYS = ['id', 'name', 'href', 'type', 'status', 'attachments']

    def dump(self):
        s = '<storage_domain'
        if hasattr(self, 'href'):
            s += ' href=\'' + getattr(self, 'href') + '\''
        if hasattr(self, 'id'):
            s += ' id=\'' + getattr(self, 'id') + '\''
        s += '>'
        if hasattr(self, 'name'):
            s += '<name>' + getattr(self, 'name') + '</name>'
        s += '</storage_domain>'
        return s

class Action(Base):
    ROOT_ELEMENT = 'action'
    KEYS = ['id', 'async', 'status', 'href', 'grace']

    def dump(self):
        s = '<action'
        if hasattr(self, 'href'):
            s += ' href=\'' + getattr(self, 'href') + '\''
        if hasattr(self, 'id'):
            s += ' id=\'' + getattr(self, 'id') + '\''
        s += '>'
        if hasattr(self, 'async'):
            s += '<async>' + getattr(self, 'async') + '</async>'
        if hasattr(self, 'grace'):
            s += getattr(self, 'grace')
        s += '</action>'
        return s

class GracePeriod(Base):
    ROOT_ELEMENT = 'grace_period'
    KEYS = ['expiry', 'absolute']

    def dump(self):
        s = '<grace_period>'
        if hasattr(self, 'expiry'):
            s += '<expiry>' + getattr(self, 'expiry') + '</expiry>'
        if hasattr(self, 'absolute'):
            s += '<absolute>' + getattr(self, 'absolute') + '</absolute>'
        s += '</grace_period>'
        return s

def parseAction(doc):
    return Action(xml.dom.minidom.parseString(doc).getElementsByTagName(Action.ROOT_ELEMENT)[0])

def parseVM(doc):
    return VM(xml.dom.minidom.parseString(doc).getElementsByTagName(VM.ROOT_ELEMENT)[0])

def parseHost(doc):
    return Host(xml.dom.minidom.parseString(doc).getElementsByTagName(Host.ROOT_ELEMENT)[0])

def parseCluster(doc):
    return Cluster(xml.dom.minidom.parseString(doc).getElementsByTagName(Cluster.ROOT_ELEMENT)[0])

def parseStorageDomain(doc):
    return StorageDomain(xml.dom.minidom.parseString(doc).getElementsByTagName(StorageDomain.ROOT_ELEMENT)[0])

def parseCollection(doc, entityType):
    collection = xml.dom.minidom.parseString(doc).getElementsByTagName(entityType.COLLECTION_ELEMENT)[0]

    ret = []
    for n in collection.childNodes:
        if n.nodeType != n.ELEMENT_NODE or n.nodeName != entityType.ROOT_ELEMENT:
            continue
        ret.append(entityType(n))

    return ret

def parseVmCollection(doc):
    return parseCollection(doc, VM)

def parseHostCollection(doc):
    return parseCollection(doc, Host)

def parseClusterCollection(doc):
    return parseCollection(doc, Cluster)

def parseStorageDomainCollection(doc):
    return parseCollection(doc, StorageDomain)
