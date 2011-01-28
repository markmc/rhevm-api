#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import sys
from rhev import create, schema
from rhev import Connection as RhevConnection
from rhev import Error as RhevError

try:
    api = create(RhevConnection)

    datacenter = api.get(schema.DataCenter, name='default')
    cluster = api.get(schema.Cluster, name='default')
    template = api.get(schema.Template, name='blank')
    network = api.get(schema.Network, base=cluster,
                      filter={'name': 'rhevm'})

    vm = schema.new(schema.VM)
    vm.name = 'myvm'
    vm.memory = 2 * 1024**3
    vm.os = 'Windows 2008 R2'
    vm.datacenter = schema.ref(datacenter)
    vm.cluster = schema.ref(cluster)
    vm.template = schema.ref(template)
    vm = api.create(vm)

    disk = create(schema.Disk)
    disk.size = 16 * 1024**3
    disk.type = 'SYSTEM'
    disk.format = 'COW'
    disk.sparse = True
    disk.interface = 'VIRTIO'
    api.create(disk, base=vm)

    nic = create(schema.NIC)
    nic.name = 'eth0'
    nic.network = network
    nic.type = 'PV'
    api.create(nic, base=vm)

    api.action(vm, 'start')

    print 'VM \'myvm\' created and started up'

except RhevError, e:
    print 'Error: %s' % str(e)
    if hasattr(e, 'detail'):
        print e.detail
