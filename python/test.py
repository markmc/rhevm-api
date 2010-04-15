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

opts = {
    'host' : 'localhost',
    'port' : 8080,
    'impl' : "dummy",
}
opts['uri'] = 'http://%(host)s:%(port)s/rhevm-api-%(impl)s-war/' % opts

import http
import xmlfmt
import yamlfmt
import jsonfmt

links = http.HEAD_for_links(opts)

for fmt in [xmlfmt, yaml, jsonfmt]:
    print "=== ", fmt.NAME, " ==="

    for host in fmt.parseHostCollection(http.GET(opts, links['hosts'], fmt.HOST_MEDIA_TYPE)):
        print fmt.parseHost(http.GET(opts, host.link.href, fmt.HOST_MEDIA_TYPE))

    for vm in fmt.parseVmCollection(http.GET(opts, links['vms'], fmt.VM_MEDIA_TYPE)):
        print fmt.parseVM(http.GET(opts, vm.link.href, fmt.VM_MEDIA_TYPE))

    foo_vm = fmt.VM()
    foo_vm.name = 'foo'
    foo_vm = fmt.parseVM(http.POST(opts, links['vms'], foo_vm.dump(), fmt.VM_MEDIA_TYPE))

    bar_host = fmt.Host()
    bar_host.name = 'bar'
    bar_host = fmt.parseHost(http.POST(opts, links['hosts'], bar_host.dump(), fmt.HOST_MEDIA_TYPE))

    print http.POST(opts, foo_vm.link.href + "/start", type = fmt.VM_MEDIA_TYPE)
    print http.GET(opts, foo_vm.link.href, type = fmt.VM_MEDIA_TYPE)

    foo_vm.name = 'bar'
    print http.PUT(opts, foo_vm.link.href, foo_vm.dump(), fmt.VM_MEDIA_TYPE)
    bar_host.name = 'foo'
    print http.PUT(opts, bar_host.link.href, bar_host.dump(), fmt.HOST_MEDIA_TYPE)

    print http.DELETE(opts, foo_vm.link.href)
    print http.DELETE(opts, bar_host.link.href)
