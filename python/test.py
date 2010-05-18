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
import sys
import getopt
import time

opts = {
    'host' : 'localhost',
    'port' : 8080,
    'impl' : "mock",
}

if len(sys.argv) > 1:
   options, oargs = getopt.getopt(sys.argv[1:], "h:p:i:", ["host=", "port=", "impl="])
   for opt, a in options:
       if opt in ("-h", "--host"):
           opts['host'] = a
       if opt in ("-p", "--port"):
           opts['port'] = a
       if opt in ("-i", "--impl"):
           opts['impl'] = a


opts['uri'] = 'http://%(host)s:%(port)s/rhevm-api-%(impl)s/' % opts

links = http.HEAD_for_links(opts)

for fmt in [xmlfmt]:
    print "=== ", fmt.MEDIA_TYPE, " ==="

    for host in fmt.parseHostCollection(http.GET(opts, links['hosts'], fmt.MEDIA_TYPE)['body']):
        print fmt.parseHost(http.GET(opts, host.href, fmt.MEDIA_TYPE)['body'])

    for vm in fmt.parseVmCollection(http.GET(opts, links['vms'], fmt.MEDIA_TYPE)['body']):
        print fmt.parseVM(http.GET(opts, vm.href, fmt.MEDIA_TYPE)['body'])

    foo_vm = fmt.VM()
    foo_vm.name = 'foo'
    ret = http.POST(opts, links['vms'], foo_vm.dump(), fmt.MEDIA_TYPE)
    foo_vm = fmt.parseVM(ret['body'])
    assert ret['status'] is 201, "Expected 201 status, got %d" % ret['status']

    bar_host = fmt.Host()
    bar_host.name = 'bar'
    ret = http.POST(opts, links['hosts'], bar_host.dump(), fmt.MEDIA_TYPE)
    bar_host = fmt.parseHost(ret['body'])
    assert ret['status'] is 201, "Expected 201 status, got %d" % ret['status']

    foo_action = fmt.Action()
    foo_action.async = 'true'
    foo_grace = fmt.GracePeriod()
    foo_grace.expiry = '5000'
    foo_grace.absolute = 'false'
    foo_action.grace = foo_grace.dump()
    ret = http.POST(opts, foo_vm.href + "/start", foo_action.dump(), fmt.MEDIA_TYPE)
    print ret['body']
    assert ret['status'] is 202, "Expected 202 status, got %d" % ret['status']
    resp_action = fmt.parseAction(ret['body'])
    assert resp_action.status != "COMPLETE", "Unexpected COMPLETE action status"
    for i in range(1, 3):
        time.sleep(1)
        resp = http.GET(opts, resp_action.href, fmt.MEDIA_TYPE)
        print resp['body']
        resp_action = fmt.parseAction(resp['body'])
        assert resp_action.status != "COMPLETE", "Unexpected COMPLETE action status"
    time.sleep(4)
    resp = http.GET(opts, resp_action.href, fmt.MEDIA_TYPE)
    print resp['body']
    resp_action = fmt.parseAction(resp['body'])
    assert resp_action.status == "COMPLETE", "Expected COMPLETE, got %d" % resp_action.status

    foo_grace.expiry = '10'
    foo_action.async = 'false'
    foo_action.grace = foo_grace.dump()
    ret = http.POST(opts, foo_vm.href + "/start", foo_action.dump(), fmt.MEDIA_TYPE)
    print ret['body']
    assert ret['status'] is 200, "Expected 200 status, got %d" % ret['status']
    resp_action = fmt.parseAction(ret['body'])
    assert resp_action.status == "COMPLETE", "Expected COMPLETE, got %d" % resp_action.status

    print http.GET(opts, foo_vm.href, type = fmt.MEDIA_TYPE)['body']

    foo_vm.name = 'bar'
    print http.PUT(opts, foo_vm.href, foo_vm.dump(), fmt.MEDIA_TYPE)['body']

    foo_vm.name = 'wonga'
    foo_vm.id = 'snafu'
    ret = http.PUT(opts, foo_vm.href, foo_vm.dump(), fmt.MEDIA_TYPE)
    print ret['body']
    assert ret['status'] == 409, "Expected 409 status, got %d" % ret['status']

    bar_host.name = 'foo'
    print http.PUT(opts, bar_host.href, bar_host.dump(), fmt.MEDIA_TYPE)['body']

    print http.DELETE(opts, foo_vm.href)
    print http.DELETE(opts, bar_host.href)
