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
import time
from testutils import *

opts = parseOptions()

(host, cluster, network) = (None, None, None)
if len(opts['oargs']) >= 3:
   (host, cluster, network) = opts['oargs'][0:3]

links = http.HEAD_for_links(opts)

for fmt in [xmlfmt]:
   t = TestUtils(opts, fmt)

   print "=== ", fmt.MEDIA_TYPE, " ==="

   if host is None:
      continue

   h = t.find(links['hosts'], host)
   c = t.find(links['clusters'], cluster)

   nic = fmt.HostNIC()
   nic.name = 'bond0'
   nic.network = fmt.Network()
   nic.network.name = network
   nic.slaves = []

   slave = fmt.HostNIC()
   slave.name = 'dummy0'
   nic.slaves.append(slave)

   slave = fmt.HostNIC()
   slave.name = 'dummy1'
   nic.slaves.append(slave)

   net = t.find(links['networks'], network)

   net = t.create(c.link['networks'].href, net)

   nic = t.create(h.link['nics'].href, nic)

   t.delete(nic.href)

   t.delete(net.href)
