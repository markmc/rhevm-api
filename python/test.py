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

#
# $> yum install -y python-suds
#

BASE_URL = 'http://localhost:8080/rhevm-api/'
WSDL_SUFFIX = '?wsdl'
IMPL_NAME = "PowerShell" # or "Dummy"

def get_url(type):
    return BASE_URL + IMPL_NAME + type + WSDL_SUFFIX

from suds.client import Client

vmsClient = Client(get_url('VMs'), cache = None)
print vmsClient

vm = vmsClient.factory.create('VM')
vm.name = "foo"
vm = vmsClient.service.add(vm)

if not vm.id is None:
    vm.name = "foo" + vm.id
vmsClient.service.update(vm)

print vmsClient.service.list()

hostsClient = Client(get_url('Hosts'))
print hostsClient

host = hostsClient.factory.create('Host')
host = hostsClient.service.add(host)
host = hostsClient.service.update(host)

print hostsClient.service.list()


