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

import sys
import getopt
import http
import random
import template_parser
import time

random.seed()

def parseOptions():
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

    return opts

def randomName(prefix):
    return prefix + str(random.randint(0, 1000))

def expectedStatusCode(code, expected):
    assert code == expected, "Expected %(e)d status, got %(c)d" % {'e': expected, 'c': code}

def unexpectedActionStatus(status, unexpected):
    assert status != unexpected, "Unexpected %s action status" % unexpected

def expectedActionStatus(status, expected):
    assert status == expected, "Expected %(e)s, got %(s)s" % {'e': expected, 's': status}

def expectedCollectionSize(collection, expected):
    assert len(collection) == expected, "Expected collection of size %(e)d, got %(s)d" % {'e': expected, 's': len(collection)}

def expectedActionLink(actions, verb):
    assert verb in actions, "Expected action verb %s, got %s" % (verb, actions.keys())

class TestUtils:
    def __init__(self, opts, fmt):
        self.opts = opts
        self.fmt = fmt

    def get(self, href, parse):
        ret = http.GET(self.opts, href, self.fmt.MEDIA_TYPE)
        parsed = parse(ret['body'])
        expectedStatusCode(ret['status'], 200)
        return parsed

    def query(self, href, constraint, parse):
        t = template_parser.URITemplate(href)
        qhref = t.sub({"query": constraint})
        ret = http.GET(self.opts, qhref, self.fmt.MEDIA_TYPE)
        parsed = parse(ret['body'])
        expectedStatusCode(ret['status'], 200)
        return parsed

    def create(self, href, entity, parse):
        ret = http.POST(self.opts, href, entity.dump(), self.fmt.MEDIA_TYPE)
        parsed = parse(ret['body'])
        expectedStatusCode(ret['status'], 201)
        return parsed

    def update(self, href, entity, expected):
        ret = http.PUT(self.opts, href, entity.dump(), self.fmt.MEDIA_TYPE)
        print ret['body']
        expectedStatusCode(ret['status'], expected)

    def delete(self, href):
        status = http.DELETE(self.opts, href)
        expectedStatusCode(status, 204)

    def makeAction(self, async, expiry):
        action = self.fmt.Action()
        action.async = async
        action.grace_period = self.fmt.GracePeriod()
        action.grace_period.expiry = expiry
        action.grace_period.absolute = 'false'
        return action

    def asyncAction(self, actions, verb):
        actionsDict = actions.asDict()
        expectedActionLink(actionsDict, verb)
        ret = http.POST(self.opts,
                        actionsDict[verb],
                        self.makeAction('true', '5000').dump(),
                        self.fmt.MEDIA_TYPE)
        print ret['body']
        expectedStatusCode(ret['status'], 202)
        resp_action = self.fmt.parseAction(ret['body'])
        unexpectedActionStatus(resp_action.status, "COMPLETE")
        for i in range(1, 3):
            time.sleep(1)
            resp = http.GET(self.opts, resp_action.href, self.fmt.MEDIA_TYPE)
            print resp['body']
            expectedStatusCode(resp['status'], 200)
            resp_action = self.fmt.parseAction(resp['body'])
            unexpectedActionStatus(resp_action.status, "COMPLETE")

        time.sleep(4)
        resp = http.GET(self.opts, resp_action.href, self.fmt.MEDIA_TYPE)
        print resp['body']
        expectedStatusCode(resp['status'], 200)
        resp_action = self.fmt.parseAction(resp['body'])
        expectedActionStatus(resp_action.status, "COMPLETE")

    def syncAction(self, actions, verb):
        actionsDict = actions.asDict()
        expectedActionLink(actionsDict, verb)
        ret = http.POST(self.opts,
                        actionsDict[verb],
                        self.makeAction('false', '10').dump(),
                        self.fmt.MEDIA_TYPE)
        print ret['body']
        expectedStatusCode(ret['status'], 200)
        resp_action = self.fmt.parseAction(ret['body'])
        expectedActionStatus(resp_action.status, "COMPLETE")
