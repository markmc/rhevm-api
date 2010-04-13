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

links = http.HEAD_for_links(opts)

for host in xmlfmt.parseHostCollection(http.GET(opts, links['hosts'])):
    print xmlfmt.parseHost(http.GET(opts, host.self))

for vm in xmlfmt.parseVmCollection(http.GET(opts, links['vms'])):
    print xmlfmt.parseVM(http.GET(opts, vm.self))

foo_vm = xmlfmt.parseVM(http.POST(opts, links['vms'], '<vm><name>foo</name></vm>'), 'application/xml')
bar_host = xmlfmt.parseHost(http.POST(opts, links['hosts'], '<host><name>bar</name></host>', 'application/xml'))

print http.POST(opts, foo_vm.self + "/start", type = 'application/xml')
print http.GET(opts, foo_vm.self)

print http.PUT(opts, foo_vm.self, '<vm><name>bar</name></vm>', 'application/xml')
print http.PUT(opts, bar_host.self, '<host><name>foo</name></host>', 'application/xml')

print http.DELETE(opts, foo_vm.self)
print http.DELETE(opts, bar_host.self)
