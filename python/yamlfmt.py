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

# yum install -y PyYAML
import yaml

MEDIA_TYPE = "application/x-yaml"

class Base(yaml.YAMLObject):
    def dump(self):
        return yaml.dump(self, explicit_start=True)

    def __str__(self):
        return self.dump()

class VM(Base):
    yaml_tag = '!vm'

class Host(Base):
    yaml_tag = '!host'

class StorageDomain(Base):
    yaml_tag = '!storagedomain'

class VMs(Base):
    yaml_tag = '!vms'

class Hosts(Base):
    yaml_tag = '!hosts'

class StorageDomains(Base):
    yaml_tag = '!storagedomains'

class Actions(Base):
    yaml_tag = '!actions'

class Link(Base):
    yaml_tag = '!link'

class Action(Base):
    yaml_tag = '!action'

class GracePeriod(Base):
    yaml_tag = '!grace_period'

def parseAction(doc):
    return yaml.load(doc)

def parseVM(doc):
    return yaml.load(doc)

def parseHost(doc):
    return yaml.load(doc)

def parseStorageDomain(doc):
    return yaml.load(doc)

def parseVmCollection(doc):
    return yaml.load(doc).VMs

def parseHostCollection(doc):
    return yaml.load(doc).hosts

def parseStorageDomainCollection(doc):
    return yaml.load(doc).hosts
