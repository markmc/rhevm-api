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
from optparse import OptionParser


parser = OptionParser(usage='%prog [options] <name>',
                      description='create a VM using the SDK')
parser.add_option('-U', '--url', help='the API entry point URL')
parser.add_option('-u', '--username', help='the user to connect as')
parser.add_option('-p', '--password', help='the user\'s password')
parser.add_option('-m', '--memory', type='int', help='memory size in MB')
parser.add_option('-D', '--disk', type='int', help='disk size in GB')
parser.add_option('-c', '--cluster', help='the cluster to add the VM to')
parser.add_option('-t', '--template', help='base the VM off this template')
parser.add_option('-N', '--network', help='the network to connect to')
parser.add_option('-d', '--debug', action='store_true',
                  help='enable debugging')
for key in ('url', 'username', 'password'):
    name = 'RHEV_%s' % key.upper()
    parser.set_default(key, os.environ.get(name))
parser.set_default('cluster', 'Default')
parser.set_default('template', 'Blank')
parser.set_default('memory', 512)
parser.set_default('disk', 8)
parser.set_default('network', 'rhevm')

opts, args = parser.parse_args()
for key in ('url', 'username', 'password'):
    if getattr(opts, key) is None:
        name = 'RHEV_%s' % key.upper()
        parser.error('please specify --%s or set $%s' % (key, name))
if len(args) != 1:
    parser.print_usage()
    parser.exit()


if opts.debug:
    logging.basicConfig(level=logging.DEBUG)

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
