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

import http
import xmlfmt
import yamlfmt
import jsonfmt
import sys
import getopt
import time
from testutils import *

opts = parseOptions()

(host, address, path) = (None, None, None)
if len(opts['oargs']) >= 3:
   (host, address, path) = opts['oargs'][0:3]

links = http.HEAD_for_links(opts)

def find_data_center(t, name):
   datacenters = filter(lambda d: d.name == name, t.get(links['datacenters']))
   if len(datacenters) == 0:
      raise RuntimeError("data center '%s' not found" % name)
   return datacenters[0]

for fmt in [xmlfmt]:
    t = TestUtils(opts, fmt)

    print "=== ", fmt.MEDIA_TYPE, " ==="

    for dom in t.get(links['storagedomains']):
       t.get(dom.href)

    dc = find_data_center(t, 'Default')

    for dom in t.get(dc.link['storagedomains'].href):
       t.get(dom.href)

    if host is None:
       continue

    h = fmt.Host()
    h.name = host

    dom = fmt.StorageDomain()
    dom.name = randomName("sd")
    dom.type = 'EXPORT'
    dom.storage = fmt.Storage()
    dom.storage.type = 'NFS'
    dom.storage.address = address
    dom.storage.path = path
    dom.host = h
    dom = t.create(links['storagedomains'], dom)

    attachDom = fmt.StorageDomain()
    attachDom.id = dom.id
    attachDom = t.create(dc.link['storagedomains'].href, attachDom)

    t.syncAction(attachDom.actions, "activate")

    t.syncAction(attachDom.actions, "deactivate")

    t.delete(attachDom.href)

    t.syncAction(dom.actions, "teardown", host=h)

    t.delete(dom.href)

    dom = fmt.StorageDomain()
    dom.type = 'EXPORT'
    dom.storage = fmt.Storage()
    dom.storage.type = 'NFS'
    dom.storage.address = address
    dom.storage.path = path
    dom.host = h
    dom = t.create(links['storagedomains'], dom)

    t.syncAction(dom.actions, "teardown", host=h)

    t.delete(dom.href)
