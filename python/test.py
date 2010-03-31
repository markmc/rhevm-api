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
    'impl' : "dummy"
}
URL = 'http://%(host)s:%(port)s/rhevm-api-%(impl)s-war/' % opts

import httplib

#
# Dumb attempt to parse e.g. <http://foo/>; rel=bar; type=text/plain
#
def parse_link(s, links):
    url = s[s.find('<')+1:s.find('>')]
    s = s[s.find(';')+1:]
    links[s[s.find('rel=')+4:s.find(';')]] = url
    return links

def query_urls(base_url):
    cnx = httplib.HTTPConnection(opts['host'], opts['port'])
    try:
        cnx.request('HEAD', base_url)
        links = {}
        for s in cnx.getresponse().getheader('Link').split(','):
            parse_link(s, links)
        return links
    finally:
        cnx.close()

links = query_urls(URL)

import xml.dom
import xml.dom.minidom

def getText(nodelist):
    rc = ""
    for node in nodelist:
        if node.nodeType == node.TEXT_NODE:
            rc = rc + node.data
    return rc

def parseNode(node):
    ret = {}
    for n in node.attributes.keys():
        n = node.attributes[n]
        ret[n.nodeName] = n.nodeValue
    for n in node.childNodes:
        if n.nodeType != n.ELEMENT_NODE:
            continue
        ret[n.nodeName] = getText(n.childNodes)
    return ret

def parse(doc, entity):
    return parseNode(xml.dom.minidom.parseString(doc).getElementsByTagName(entity)[0])

def parseCollection(doc, entity):
    ret = []
    collection = xml.dom.minidom.parseString(doc).getElementsByTagName('collection')[0]
    for n in collection.childNodes:
        if n.nodeType != n.ELEMENT_NODE or n.nodeName != entity:
            continue
        ret.append(parseNode(n))
    return ret

def GET(url):
    cnx = httplib.HTTPConnection(opts['host'], opts['port'])
    try:
        cnx.request('GET', url)
        return cnx.getresponse().read()
    finally:
        cnx.close()

for host in parseCollection(GET(links['hosts']), 'host'):
    print parse(GET(host['href']), 'host')

for vm in parseCollection(GET(links['vms']), 'vm'):
    print parse(GET(vm['href']), 'vm')

def POST(url, body = None):
    cnx = httplib.HTTPConnection(opts['host'], opts['port'])
    try:
        #if body is None:
        #    cnx.request('POST', url)
        #else:
        cnx.request('POST', url, body, {'Content-type': 'application/xml'})
        resp = cnx.getresponse()
        if resp.getheader('location') is None:
            return resp.status
        else:
            return resp.getheader('location')
    finally:
        cnx.close()

foo_vm = POST(links['vms'], '<vm><name>foo</name></vm>')
bar_host = POST(links['hosts'], '<host><name>bar</name></host>')

print POST(foo_vm + "/start")
print GET(foo_vm)

def PUT(url, body):
    cnx = httplib.HTTPConnection(opts['host'], opts['port'])
    try:
        cnx.request('PUT', url, body, {'Content-type': 'application/xml'})
        return cnx.getresponse().read()
    finally:
        cnx.close()

print PUT(foo_vm, '<vm><name>bar</name></vm>')
print PUT(bar_host, '<host><name>foo</name></host>')

def DELETE(url):
    cnx = httplib.HTTPConnection(opts['host'], opts['port'])
    try:
        cnx.request('DELETE', url)
        return cnx.getresponse().status
    finally:
        cnx.close()

print DELETE(foo_vm)
print DELETE(bar_host)
