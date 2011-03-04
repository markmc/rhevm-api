#!/usr/bin/env python
#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

import os
import sys
import logging

from rhev import *
from parser import parse_args


opts, args = parse_args()

api = Connection(opts.url, opts.username, opts.password)
try:
    api.ping()
except Error, e:
    sys.stderr.write('could not connect: %s\n' % str(e))
    sys.exit(1)

cluster = api.get(schema.Cluster, name=opts.cluster)
if cluster is None:
    sys.stderr.write('error: cluster not found: %s\n' % opts.cluster)
    sys.exit(1)
template = api.get(schema.Template, name=opts.template)
if template is None:
    sys.stderr.write('error: template not found: %s\n' % opts.template)
    sys.exit(1)
network = api.get(schema.Network, base=cluster,
                        filter={'name': opts.network})
if network is None:
    sys.stderr.write('error: network not found: %s\n' % opts.network)
    sys.exit(1)


try:
    vm = schema.new(schema.VM, name=args[0])
    vm.memory = opts.memory * 1024**2
    vm.cluster = schema.ref(cluster)
    vm.template = schema.ref(template)
    vm = api.create(vm)

    nic = schema.new(schema.NIC)
    nic.name = 'eth0'
    nic.type = 'PV'
    nic.network = schema.ref(network)
    api.create(nic, base=vm)

    disk = schema.new(schema.Disk)
    disk.size = opts.disk * 1024**3
    disk.format = 'COW'
    disk.sparse = True
    disk.interface = 'VIRTIO'
    api.create(disk, base=vm)

    print 'created vm: "%s", with %dMB RAM, %dGB disk and 1 NIC' % \
            (args[0], opts.memory, opts.disk)

except Error, e:
    sys.stderr.write('error: could not create vm: %s\n' % str(e))
    sys.exit(1)
