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

import json

MEDIA_TYPE = "application/json"

class Link:
    def __init__(self, rel, href):
        self.rel = rel
        self.href = href

class Base:
    def __init__(self, dict = None):
        if dict is None:
            return

        dict = dict[self.ROOT_ELEMENT]

        for key in self.KEYS:
            if key in dict:
                setattr(self, key, dict[key])

        if hasattr(self, 'link'):
            self.link = Link(self.link['@rel'], self.link['@href'])

    def dump(self):
        return json.dumps(self, cls=Encoder);

    def __str__(self):
        return str(self.as_dict())

    def as_dict(self):
        dict = {}

        for key in self.KEYS:
            if hasattr(self, key):
                dict[key] = getattr(self, key)

        if 'link' in dict:
            dict['link'] = {'@rel' : dict['link'].rel, '@href' : dict['link'].href}

        return {self.ROOT_ELEMENT : dict}

class VM(Base):
    ROOT_ELEMENT = 'vm'
    KEYS = ['id', 'name', 'link']

class Host(Base):
    ROOT_ELEMENT = 'host'
    KEYS = ['id', 'name', 'link']

class Action(Base):
    ROOT_ELEMENT = 'action'
    KEYS = ['id', 'async', 'status', 'self', 'grace']

class GracePeriod(Base):
    ROOT_ELEMENT = 'grace_period'
    KEYS = ['expiry', 'absolute']

class Encoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Base):
            return obj.as_dict()
        return json.JSONEncoder.default(self, obj)

def parseAction(doc):
    return Action(json.loads(doc))

def parseVM(doc):
    return VM(json.loads(doc))

def parseHost(doc):
    return Host(json.loads(doc))

def parseVmCollection(doc):
    ret = []
    for vm in json.loads(doc)['vms']['vm']:
        ret.append(VM({'vm':vm}))
    return ret

def parseHostCollection(doc):
    ret = []
    for host in json.loads(doc)['hosts']['host']:
        ret.append(Host({'host':host}))
    return ret
