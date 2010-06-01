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

(name, address, root_password) = (None, None, None)
if len(opts['oargs']) >= 3:
   (name, address, root_password) = opts['oargs'][0:3]

links = http.HEAD_for_links(opts)

print links

for fmt in [xmlfmt]:
   t = TestUtils(opts, fmt, root_password)

   print "=== ", fmt.MEDIA_TYPE, " ==="

   for host in t.get(links['hosts'], fmt.parseHostCollection):
      print t.get(host.href, fmt.parseHost)

   if name is None:
      continue

   host = fmt.Host()
   host.name = name
   host.address = address
   host = t.create(links['hosts'], host, fmt.parseHost)

   def waitFor(host, status):
      host = t.get(host.href, fmt.parseHost)
      while host.status != status:
         print "waiting to go to " + status + " currently " + host.status
         time.sleep(1)
         host = t.get(host.href, fmt.parseHost)
         continue

   t.syncAction(host.actions, "deactivate")
   waitFor(host, "MAINTENANCE")
   t.syncAction(host.actions, "install");
   waitFor(host, "MAINTENANCE")
   t.syncAction(host.actions, "activate")
   waitFor(host, "UP")
   t.syncAction(host.actions, "deactivate")
   waitFor(host, "MAINTENANCE")

   t.delete(host.href)
