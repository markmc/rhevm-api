#!/usr/bin/env python
#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

import os
import sys

from xml.etree import ElementTree as etree
from xml.etree.ElementTree import Element, SubElement

import httplib as http
from httplib import HTTPConnection, HTTPSConnection
from urlparse import urlparse
from urllib import quote as urlencode

from parser import parse_args


def connect(url):
    """Connect to RHEV-M and return the HTTPConnection."""
    parsed = urlparse(url)
    scheme, netloc, path = parsed[:3]
    if scheme == 'https':
        factory = HTTPSConnection
    elif scheme == 'http':
        factory = HTTPConnection
    else:
        raise ValueError, 'Unknown scheme: %s' % scheme
    if ':' in netloc:
        host, port = netloc.split(':')
    elif scheme == 'https':
        host = netloc
        port = http.HTTPS_PORT
    elif scheme == 'http':
        host = netloc
        port = http.HTTP_PORT
    connection = factory(host, port)
    connection.connect()
    if not path.endswith('/'):
        path += '/'
    return connection, path

def get_headers(username, password):
    """Get request headers."""
    headers = {}
    auth = '%s:%s' % (username, password)
    auth = 'Basic %s' % auth.encode('base64').rstrip()
    headers['Authorization'] = auth
    headers['Accept'] = 'application/xml'
    headers['Content-Type'] = 'application/xml'
    return headers

def make_request(connection, method, url, headers=None, body=None):
    """Make a HTTP request and return a (status, header, body) tuple."""
    if headers is None:
        headers = {}
    if body is None:
        body = ''
    elif isinstance(body, etree.Element):
        body = etree.tostring(body)
    connection.request(method, url, body, headers)
    response = connection.getresponse()
    status = response.status
    body = response.read()
    clen = int(response.getheader('Content-Length'))
    ctype = response.getheader('Content-Type')
    if clen > 0 and ctype == 'application/xml':
        body = etree.fromstring(body)
    else:
        body = ''
    return status, body

def expand_template(s, **kwargs):
    """Expand {name} string template."""
    for key in kwargs:
        s = s.replace('{%s}' % key, urlencode(kwargs[key]))
    return s


opts, args = parse_args()

api, entrypoint = connect(opts.url)
headers = get_headers(opts.username, opts.password)

# Retrieve the entry point to get links
status, links = make_request(api, 'GET', entrypoint, headers)
if status != http.OK:
    sys.stderr.write('error: unexpected response code: %s\n' % status)
    sys.exit(1)

# Find the cluster
link = links.find('link[@rel="clusters/search"]')
url = expand_template(link.attrib['href'], query='name=%s' % opts.cluster)
status, body = make_request(api, 'GET', url, headers)
if status != http.OK:
    sys.stderr.write('error: unexpected response code: %s\n' % status)
    sys.exit(1)
cluster = body.find('cluster')
if cluster is None:
    sys.stderr.write('error: cluster not found: %s\n' % opts.cluster)
    sys.exit(1)
clusterid = cluster.attrib['id']

# Find the template
link = links.find('link[@rel="templates/search"]')
url = expand_template(link.attrib['href'], query='name=%s' % opts.template)
status, body = make_request(api, 'GET', url, headers)
if status != http.OK:
    sys.stderr.write('error: unexpected response code: %s\n' % status)
    sys.exit(1)
template = body.find('template')
if template is None:
    sys.stderr.write('error: template not found: %s\n' % opts.template)
    sys.exit(1)
templateid = template.attrib['id']

# Find the network. No "search" is available so list them all.
link = links.find('link[@rel="networks"]')
url = link.attrib['href']
status, body = make_request(api, 'GET', url, headers)
if status != http.OK:
    sys.stderr.write('error: unexpected response code: %s\n' % status)
    sys.exit(1)
networks = body.findall('network')
for network in networks:
    if network.find('name').text == opts.network:
        break
else:
    sys.stderr.write('error: network not found: %s\n' % opts.network)
    sys.exit(1)
networkid = network.attrib['id']

# Prepare the XML and create the VM
vm = Element('vm')
name = SubElement(vm, 'name')
name.text = args[0]
memory = SubElement(vm, 'memory')
memory.text = str(opts.memory * 1024**2)
cluster = SubElement(vm, 'cluster')
cluster.attrib['id'] = clusterid
template = SubElement(vm, 'template')
template.attrib['id'] = templateid
link = links.find('link[@rel="vms"]')
url = link.attrib['href']
status, vm = make_request(api, 'POST', url, headers, vm)
if status not in (http.CREATED, http.ACCEPTED):
    sys.stderr.write('error: failed to create vm: status %s\n' % status)
    sys.exit(1)

# Add the nic
nic = Element('nic')
name = SubElement(nic, 'name')
name.text = 'eth0'
type = SubElement(nic, 'type')
type.text = 'PV'
network = SubElement(nic, 'network')
network.attrib['id'] = networkid
link = vm.find('link[@rel="nics"]')
url = link.attrib['href']
status, nic = make_request(api, 'POST', url, headers, nic)
if status not in (http.CREATED, http.ACCEPTED):
    sys.stderr.write('error: failed to add nic: status %s\n' % status)
    sys.exit(1)

# Add the disk
disk = Element('disk')
size = SubElement(disk, 'size')
size.text = str(opts.disk * 1024**3)
format = SubElement(disk, 'format')
format.text = 'COW'
sparse = SubElement(disk, 'sparse')
sparse.text = 'true'
interface = SubElement(disk, 'interface')
interface.text = 'VIRTIO'
link = vm.find('link[@rel="disks"]')
url = link.attrib['href']
status, disk = make_request(api, 'POST', url, headers, disk)
if status not in (http.CREATED, http.ACCEPTED):
    sys.stderr.write('error: failed to add disk: status %s\n' % status)
    sys.exit(1)

print 'created vm: "%s", with %dMB RAM, %dGB disk and 1 NIC' % \
        (args[0], opts.memory, opts.disk)
