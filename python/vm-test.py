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
   vm.memory = 2 * 1024 * 1024 * 1024
   vm.cpu = fmt.CPU()
   vm.cpu.topology = fmt.Topology()
   vm.cpu.topology.sockets = 1
   vm.cpu.topology.cores = 4
   vm.os = fmt.OS()
   vm.os.boot = fmt.Boot()
   vm.os.boot.dev = 'cdrom'
   vm.template = fmt.Template()
   vm.template.id = t.find(links['templates'], 'Blank').id
   vm.cluster = fmt.Cluster()
   vm.cluster.id = t.find(links['clusters'], 'Default').id
   vm = t.create(links['vms'], vm)

   nic = fmt.NIC()
   nic.name = 'eth0'
   nic.network = fmt.Network()
   nic.network.id = t.find(links['networks'], 'rhevm').id
   t.syncAction(vm.actions, "adddevice", nic=nic)

   disk = fmt.Disk()
   disk.size = 10737418240
   t.syncAction(vm.actions, "adddevice", disk=disk)

   cdrom = fmt.CdRom()
   cdrom.iso = fmt.Iso()
   cdrom.iso.id = t.get(t.find(links['datacenters'], 'Default').link['isos'].href)[0].id
   t.syncAction(vm.actions, "adddevice", cdrom=cdrom)

   vm = t.get(vm.href)
   vm.memory = 2 * 1024 * 1024 * 1024
   vm.cpu.topology.sockets = 2
   vm.cpu.topology.cores = 2
   vm.os.boot.dev = 'hd'
   t.update(vm.href, vm, 200)

   t.syncAction(vm.actions, "start")

   def waitFor(vm, status):
      vm = t.get(vm.href)
      while not hasattr(vm, 'status') or vm.status != status:
         debug(opts, "waiting to go to %s", status)
         time.sleep(1)
         vm = t.get(vm.href)
         continue
      return vm

   vm = waitFor(vm, 'RUNNING')

   t.syncAction(vm.actions, "stop")

   vm = waitFor(vm, 'SHUTOFF')

   cdrom = fmt.CdRom()
   cdrom.id = vm.devices.cdrom.id
   t.syncAction(vm.actions, "removedevice", cdrom=cdrom)

   disk = fmt.Disk()
   disk.id = vm.devices.disk.id
   t.syncAction(vm.actions, "removedevice", disk=disk)

   nic = fmt.NIC()
   nic.id = vm.devices.nic.id
   t.syncAction(vm.actions, "removedevice", nic=nic)

   t.delete(vm.href)
