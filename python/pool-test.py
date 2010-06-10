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

(cluster, template) = (None, None)
if len(opts['oargs']) >= 2:
   (cluster, template) = opts['oargs'][0:2]

links = http.HEAD_for_links(opts)

for fmt in [xmlfmt]:
   t = TestUtils(opts, fmt)

   print "=== ", fmt.MEDIA_TYPE, " ==="

   for pool in t.get(links['vmpools']):
      t.get(pool.href)

   if cluster is None:
      continue

   pool = fmt.VmPool()
   pool.name = randomName('foo')
   pool.size = "2"
   pool.cluster = fmt.Cluster()
   pool.cluster.id = t.find(links['clusters'], cluster).id
   pool.template = fmt.Template()
   pool.template.id = t.find(links['templates'], template).id
   pool = t.create(links['vmpools'], pool)

   vms_in_pool = []
   for vm in t.get(links['vms']):
      if not hasattr(vm, "vmpool"):
         continue
      if vm.vmpool.id == pool.id:
         vms_in_pool.append(vm)

   assert len(vms_in_pool) == 2, "Expected 2 VMs with pool ID '" + pool.id + "', got " + str(len(vms_in_pool))

   for vm in vms_in_pool:
      t.syncAction(vm.actions, "detach")
      t.delete(vm.href)

   t.delete(pool.href)
