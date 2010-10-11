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

   for tag in t.get(links['tags']):
      t.get(tag.href)

   tag = fmt.Tag()
   tag.name = randomName("tag")

   tag = t.create(links['tags'], tag)

   tag.name += "u"
   tag = t.update(tag.href, tag, 200)

   for vm in t.get(links['vms']):
      for child in t.get(vm.link['tags'].href):
         t.get(child.href)
      child = fmt.Tag()
      child.id = tag.id # add by id
      child = t.create(vm.link['tags'].href, child)
      t.delete(child.href)

   for host in t.get(links['hosts']):
      for child in t.get(host.link['tags'].href):
         t.get(child.href)
      child = fmt.Tag()
      child.name = tag.name # add by name
      child = t.create(host.link['tags'].href, child)
      t.delete(child.href)

   t.delete(tag.href)
