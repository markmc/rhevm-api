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

def makeAction(async, expiry):
    action = fmt.Action()
    action.async = async
    grace = fmt.GracePeriod()
    grace.expiry = expiry
    grace.absolute = 'false'
    action.grace = grace.dump()
    return action

def expectedStatusCode(code, expected):
    assert code == expected, "Expected %(e)d status, got %(c)d" % {'e': expected, 'c': code}

def unexpectedActionStatus(status, unexpected):
    assert status != unexpected, "Unexpected %s action status" % unexpected

def expectedActionStatus(status, expected):
    assert status == expected, "Expected %(e)s, got %(s)s" % {'e': expected, 's': status}

def testGet(href):
    ret = http.GET(opts, href, fmt.MEDIA_TYPE)
    expectedStatusCode(ret['status'], 200)
    return ret['body']

def testCreate(entity, name, links, parse):
    entity.name = name
    ret = http.POST(opts, links, entity.dump(), fmt.MEDIA_TYPE)
    parsed = parse(ret['body'])
    expectedStatusCode(ret['status'], 201)
    return parsed

def testAsyncAction(href, verb):
    ret = http.POST(opts, href + "/" + verb, makeAction('true', '5000').dump(), fmt.MEDIA_TYPE)
    print ret['body']
    expectedStatusCode(ret['status'], 202)
    resp_action = fmt.parseAction(ret['body'])
    unexpectedActionStatus(resp_action.status, "COMPLETE")
    for i in range(1, 3):
        time.sleep(1)
        resp = http.GET(opts, resp_action.href, fmt.MEDIA_TYPE)
        print resp['body']
        expectedStatusCode(resp['status'], 200)
        resp_action = fmt.parseAction(resp['body'])
        unexpectedActionStatus(resp_action.status, "COMPLETE")

    time.sleep(4)
    resp = http.GET(opts, resp_action.href, fmt.MEDIA_TYPE)
    print resp['body']
    expectedStatusCode(resp['status'], 200)
    resp_action = fmt.parseAction(resp['body'])
    expectedActionStatus(resp_action.status, "COMPLETE")

def testSyncAction(href, verb):
    ret = http.POST(opts, href + "/" + verb, makeAction('false', '10').dump(), fmt.MEDIA_TYPE)
    print ret['body']
    expectedStatusCode(ret['status'], 200)
    resp_action = fmt.parseAction(ret['body'])
    expectedActionStatus(resp_action.status, "COMPLETE")

def testUpdate(name, id, entity, expected):
    entity.name = name
    entity.id = id
    ret = http.PUT(opts, entity.href, entity.dump(), fmt.MEDIA_TYPE)
    print ret['body']
    expectedStatusCode(ret['status'], expected)

def testDelete(href):
    status = http.DELETE(opts, href)
    print status
    expectedStatusCode(status, 204)

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

    for host in fmt.parseHostCollection(testGet(links['hosts'])):
        print fmt.parseHost(testGet(host.href))

    for vm in fmt.parseVmCollection(testGet(links['vms'])):
        print fmt.parseVM(testGet(vm.href))

    foo_vm = testCreate(fmt.VM(), 'foo', links['vms'], fmt.parseVM)

    testAsyncAction(foo_vm.href, "start")

    testSyncAction(foo_vm.href, "stop")

    bar_host = testCreate(fmt.Host(), 'bar', links['hosts'], fmt.parseHost)

    testAsyncAction(bar_host.href, "fence")

    testAsyncAction(bar_host.href, "approve")

    print testGet(foo_vm.href)

    testUpdate('bar', foo_vm.id, foo_vm, 200)

    testUpdate('wonga', 'snafu', foo_vm, 409)

    testUpdate('foo', bar_host.id, bar_host, 200)

    testUpdate('ping', 'pong', bar_host, 409)

    testDelete(foo_vm.href)

    testDelete(bar_host.href)
