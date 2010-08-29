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
    print "=== ", fmt.MEDIA_TYPE, " ==="

    t = TestUtils(opts, fmt)

    for host in t.get(links['hosts']):
        print t.get(host.href)

    for vm in t.get(links['vms']):
        print t.get(vm.href)

    template = t.get(links['templates'])[0]
    print template

    cluster = t.get(links['clusters'])[0]
    print cluster

    foo_vm = fmt.VM()
    foo_vm.name = randomName('foo')
    foo_vm.template = fmt.Template()
    foo_vm.template.id = template.id
    foo_vm.cluster = fmt.Cluster()
    foo_vm.cluster.id = cluster.id
    foo_vm = t.create(links['vms'], foo_vm)

    constraint = foo_vm.name.replace('oo', '*')
    query_vms = t.query(links['vms/search'], "name=" + constraint)
    expectedCollectionSize(query_vms, 1)

    t.asyncAction(foo_vm.actions, "start")

    t.syncAction(foo_vm.actions, "stop")

    bar_host = fmt.Host()
    bar_host.name = randomName('bar')
    bar_host = t.create(links['hosts'], bar_host)

    t.asyncAction(bar_host.actions, "approve")

    print t.get(foo_vm.href)

    foo_vm.name = randomName('bar')
    t.update(foo_vm.href, foo_vm, 200)

    foo_vm.id = 'snafu'
    foo_vm.name = 'wonga'
    t.update(foo_vm.href, foo_vm, 409)

    bar_host.name = randomName('foo')
    t.update(bar_host.href, bar_host, 200)

    bar_host.id = 'pong'
    bar_host.name = 'ping'
    t.update(bar_host.href, bar_host, 409)

    t.delete(foo_vm.href)

    t.delete(bar_host.href)

    t.unauth(links['hosts'])
