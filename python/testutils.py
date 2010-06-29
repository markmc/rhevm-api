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
import urlparse
import copy

random.seed()

def parseOptions():
    opts = {
        'host' : 'localhost',
        'port' : 8080,
        'impl' : "mock",
        'debug' : False,
        'user' : None,
        'secret' : None,
        }

    oargs = []
    if len(sys.argv) > 1:
        options, oargs = getopt.getopt(sys.argv[1:], "h:p:i:u:s:d", ["host=", "port=", "impl=", "user=", "secret=", "debug"])
        for opt, a in options:
            if opt in ("-h", "--host"):
                opts['host'] = a
            if opt in ("-p", "--port"):
                opts['port'] = a
            if opt in ("-i", "--impl"):
                opts['impl'] = a
            if opt in ("-u", "--user"):
                opts['user'] = a
            if opt in ("-s", "--secret"):
                opts['secret'] = a
            if opt in ("-d", "--debug"):
                opts['debug'] = True

    opts['uri'] = 'http://%(host)s:%(port)s/rhevm-api-%(impl)s/' % opts
    opts['oargs'] = oargs

    return opts

def debug(opts, fmt, *args):
    if opts['debug']:
        print fmt % args

def randomName(prefix):
    return prefix + str(random.randint(0, 1000))

def expectedStatusCode(resp, expected):
    assert resp['status'] == expected, "Expected %d status, got %d; body is '%s'" % (expected, resp['status'], resp['body'])

def unexpectedActionStatus(status, unexpected):
    assert status != unexpected, "Unexpected %s action status" % unexpected

def expectedActionStatus(status, expected):
    assert status == expected, "Expected %(e)s, got %(s)s" % {'e': expected, 's': status}

def expectedCollectionSize(collection, expected):
    assert len(collection) == expected, "Expected collection of size %(e)d, got %(s)d" % {'e': expected, 's': len(collection)}

def expectedActionLink(actions, verb):
    assert verb in actions.link, "Expected action verb %s, got %s" % (verb, actions.link.keys())

class TestUtils:
    def __init__(self, opts, fmt):
        self.opts = opts
        self.fmt = fmt

    def abs(self, href):
        return urlparse.urljoin(self.opts['uri'], href);

    def get(self, href):
        ret = http.GET(self.opts, self.abs(href), self.fmt.MEDIA_TYPE)
        expectedStatusCode(ret, 200)
        return self.fmt.parse(ret['body'])

    def unauth(self, href):
        unauth_opts = copy.deepcopy(self.opts)
        unauth_opts['user'] = None
        unauth_opts['secret'] = None
        ret = http.GET(unauth_opts, self.abs(href), self.fmt.MEDIA_TYPE)
        expectedStatusCode(ret, 401)

    def query(self, href, constraint):
        t = template_parser.URITemplate(self.abs(href))
        qhref = t.sub({"query": constraint})
        ret = http.GET(self.opts, qhref, self.fmt.MEDIA_TYPE)
        expectedStatusCode(ret, 200)
        return self.fmt.parse(ret['body'])

    def create(self, href, entity):
        ret = http.POST(self.opts, self.abs(href), entity.dump(), self.fmt.MEDIA_TYPE)
        expectedStatusCode(ret, 201)
        return self.fmt.parse(ret['body'])

    def update(self, href, entity, expected):
        ret = http.PUT(self.opts, self.abs(href), entity.dump(), self.fmt.MEDIA_TYPE)
        expectedStatusCode(ret, expected)
        return self.fmt.parse(ret['body'])

    def delete(self, href):
        ret = http.DELETE(self.opts, self.abs(href))
        expectedStatusCode(ret, 204)

    def makeAction(self, async, expiry, **params):
        action = self.fmt.Action()
        action.async = async
        action.grace_period = self.fmt.GracePeriod()
        action.grace_period.expiry = expiry
        action.grace_period.absolute = 'false'
        for p in params:
            setattr(action, p, params[p])
        return action

    def asyncAction(self, actions, verb, **params):
        expectedActionLink(actions, verb)
        ret = http.POST(self.opts,
                        self.abs(actions.link[verb].href),
                        self.makeAction('true', '5000', **params).dump(),
                        self.fmt.MEDIA_TYPE)
        expectedStatusCode(ret, 202)
        resp_action = self.fmt.parse(ret['body'])
        unexpectedActionStatus(resp_action.status, "COMPLETE")
        for i in range(1, 3):
            time.sleep(1)
            resp = http.GET(self.opts, self.abs(resp_action.href), self.fmt.MEDIA_TYPE)
            expectedStatusCode(resp, 200)
            resp_action = self.fmt.parse(resp['body'])
            unexpectedActionStatus(resp_action.status, "COMPLETE")

        time.sleep(4)
        resp = http.GET(self.opts, self.abs(resp_action.href), self.fmt.MEDIA_TYPE)
        expectedStatusCode(resp, 200)
        resp_action = self.fmt.parse(resp['body'])
        expectedActionStatus(resp_action.status, "COMPLETE")

    def syncAction(self, actions, verb, **params):
        expectedActionLink(actions, verb)
        ret = http.POST(self.opts,
                        self.abs(actions.link[verb].href),
                        self.makeAction('false', '10', **params).dump(),
                        self.fmt.MEDIA_TYPE)
        expectedStatusCode(ret, 200)
        resp_action = self.fmt.parse(ret['body'])
        expectedActionStatus(resp_action.status, "COMPLETE")

    def find(self, href, name):
        results = filter(lambda r: r.name == name,
                         self.get(self.abs(href)))
        if len(results) == 0:
            raise RuntimeError("'%s' not found" % name)
        return results[0]
