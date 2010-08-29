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
from testutils import *

opts = parseOptions()

links = http.HEAD_for_links(opts)

for fmt in [xmlfmt]:
   t = TestUtils(opts, fmt)

   print "=== ", fmt.MEDIA_TYPE, " ==="

   v = None
   for d in t.get(links['datacenters']):
      v = d.supported_versions[0]

   d = fmt.DataCenter()
   d.name = randomName('dc')
   d.description = 'The %(name)s data center' % {'name' : d.name}
   d.storage_type = 'NFS'
   if not v is None:
      d.version = v
   else:
      d.version = fmt.Version()
      d.version.major = '2'
      d.version.minor = '2'

   d = t.create(links['datacenters'], d)

   d.description += " (UPDATED!)"
   d = t.update(d.href, d, 200)

   t.delete(d.href)
