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

import httplib
import base64
from testutils import debug

def open_connection(opts):
    if opts['scheme'] == 'https':
        cnx = httplib.HTTPSConnection(opts['host'], opts['port'])
    else:
        cnx = httplib.HTTPConnection(opts['host'], opts['port'])
    if opts['debug']:
        cnx.set_debuglevel(1)
    return cnx

def basic_auth(opts):
    credentials = base64.encodestring('%s:%s' % (opts['user'], opts['secret']))[:-1]
    return "Basic %s" % credentials

def basic_headers(opts):
    headers = {}
    if not (opts['user'] is None or opts['secret'] is None):
        headers['Authorization'] = basic_auth(opts)
    return headers

#
# Dumb attempt to parse e.g. <http://foo/>; rel=bar; type=text/plain
#
def parse_link(s, links):
    url = s[s.find('<')+1:s.find('>')]
    s = s[s.find(';')+1:]
    rel = s[s.find('rel=')+4:]
    if rel.find(';') != -1:
        rel = rel[:s.find(';')]
    links[rel] = url
    return links

def HEAD_for_links(opts):
    cnx = open_connection(opts)
    try:
        cnx.request('HEAD', opts['uri'], headers = basic_headers(opts))
        links = {}
        for s in cnx.getresponse().getheader('Link').split(','):
            parse_link(s, links)
        return links
    finally:
        cnx.close()

def GET(opts, uri, type = None):
    cnx = open_connection(opts)
    try:
        headers = basic_headers(opts)
        if not type is None:
            headers['Accept'] = type
        cnx.request('GET', uri, headers = headers)
        ret = { 'status' : 0, 'body' : None }
        resp = cnx.getresponse()
        ret['status'] = resp.status
        ret['body'] = resp.read()
        debug(opts, "body: %s", ret['body'])
        return ret
    finally:
        cnx.close()

def POST(opts, uri, body = None, type = None):
    cnx = open_connection(opts)
    try:
        headers = basic_headers(opts)
        if not type is None:
            headers['Content-type'] = type
            headers['Accept'] = type
        cnx.request('POST', uri, body, headers = headers)
        ret = { 'status' : 0, 'body' : None }
        resp = cnx.getresponse()
        ret['status'] = resp.status
        ret['body'] = resp.read()
        debug(opts, "body: %s", ret['body'])
        return ret
    finally:
        cnx.close()

def PUT(opts, uri, body, type = None):
    cnx = open_connection(opts)
    try:
        headers = basic_headers(opts)
        if not type is None:
            headers['Content-type'] = type
            headers['Accept'] = type
        cnx.request('PUT', uri, body, headers = headers)
        ret = { 'status' : 0, 'body' : None }
        resp = cnx.getresponse()
        ret['status'] = resp.status
        ret['body'] = resp.read()
        debug(opts, "body: %s", ret['body'])
        return ret
    finally:
        cnx.close()

def DELETE(opts, uri):
    cnx = open_connection(opts)
    try:
        cnx.request('DELETE', uri, headers = basic_headers(opts))
        ret = { 'status' : 0, 'body' : None }
        resp = cnx.getresponse()
        ret['status'] = resp.status
        return ret
    finally:
        cnx.close()
