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

(name, address, path, host) = (None, None, None, None)
if len(opts['oargs']) >= 3:
   (name, address, path, host) = opts['oargs'][0:4]

links = http.HEAD_for_links(opts)

print links

for fmt in [xmlfmt]:
    t = TestUtils(opts, fmt)

    print "=== ", fmt.MEDIA_TYPE, " ==="

    for dom in t.get(links['storagedomains'], fmt.parseStorageDomainCollection):
        print t.get(dom.href, fmt.parseStorageDomain)

    if name is None:
        continue

    dom = fmt.StorageDomain()
    dom.name = name
    dom.type = 'DATA'
    dom.storage = fmt.Storage()
    dom.storage.type = 'NFS'
    dom.storage.address = address
    dom.storage.path = path
    dom = t.create(links['storagedomains'], dom, fmt.parseStorageDomain)

    def find_host(t, name):
       hosts = filter(lambda h: h.name == name,
                      t.get(links['hosts'], fmt.parseHostCollection))
       if len(hosts) == 0:
          raise RuntimeError(host + " not found")
       return hosts[0]

    h = fmt.Host()
    h.id = find_host(t, host).id

    t.syncAction(dom.actions, "initialize", host=h)

    dom = t.get(dom.href, fmt.parseStorageDomain)

    t.syncAction(dom.actions, "teardown", host=h)

    t.delete(dom.href)
