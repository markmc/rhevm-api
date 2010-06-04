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

links = http.HEAD_for_links(opts)

for fmt in [xmlfmt]:
   t = TestUtils(opts, fmt)

   print "=== ", fmt.MEDIA_TYPE, " ==="

   for vm in t.get(links['vms']):
      t.get(vm.href)

   vm = fmt.VM()
   vm.name = randomName('foo')
   vm.template = fmt.Template()
   vm.template.id = t.find(links['templates'], 'Blank').id
   vm.cluster = fmt.Cluster()
   vm.cluster.id = t.find(links['clusters'], 'Default').id
   vm = t.create(links['vms'], vm)

   interface = fmt.Interface()
   interface.name = 'eth0'
   interface.network = fmt.Network()
   interface.network.id = t.find(links['networks'], 'rhevm').id
   t.syncAction(vm.actions, "adddevice", interface=interface)

   disk = fmt.Disk()
   disk.size = 10737418240
   t.syncAction(vm.actions, "adddevice", disk=disk)

   vm = t.get(vm.href)

   disk = fmt.Disk()
   disk.id = vm.devices.disk.id
   t.syncAction(vm.actions, "removedevice", disk=disk)

   interface = fmt.Interface()
   interface.id = vm.devices.interface.id
   t.syncAction(vm.actions, "removedevice", interface=interface)

   t.delete(vm.href)
