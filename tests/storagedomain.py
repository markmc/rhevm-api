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

import base
from rhevm.testutils import *

class StorageDomainTests(base.TestCase):
    def setUp(self):
        base.TestCase.setUp(self)

    def test_list_and_get(self):
        storage_domains = self.t.get(self.links['storagedomains'])
        for storage_domain in storage_domains:
            self.t.get(storage_domain.href)

    def test_query(self):
        storage_domains = self.t.get(self.links['storagedomains'])
        for storage_domain in storage_domains:
            self.t.query(self.links['storagedomains/search'], 'name=' + storage_domain.name[0:-1] + '*')

    def _create_storage_domain(self, host, nfs_path):
        storage_domain = self.fmt.StorageDomain()
        storage_domain.name = randomName('sd')
        storage_domain.type = 'DATA'
        storage_domain.storage = self.fmt.Storage()
        storage_domain.storage.type = 'NFS'
        storage_domain.storage.address = self.config.nfs_address
        storage_domain.storage.path = nfs_path
        storage_domain.host = host

        return self.t.create(self.links['storagedomains'], storage_domain)

    def _update_storage_domain(self, storage_domain):
        storage_domain.name += 'u'
        self.t.update(storage_domain.href, storage_domain, 200)

        simpleUpdate = self.fmt.StorageDomain()
        simpleUpdate.name = storage_domain.name + 'su'
        self.t.update(storage_domain.href, simpleUpdate, 200)

    def _delete_storage_domain(self, storage_domain, host):
        self.t.syncAction(storage_domain.actions, "teardown", host=host)
        self.t.delete(storage_domain.href)

    def _attach_storage_domain(self, storage_domain, data_center):
        attachment = self.fmt.Attachment()
        attachment.data_center = self.fmt.DataCenter()
        attachment.data_center.id = self.data_center.id
        return self.t.create(storage_domain.link['attachments'].href, attachment)

    def _activate_storage_domain(self, attachment):
        self.t.syncAction(attachment.actions, "activate")
        return self.t.get(attachment.href)

    def _deactivate_storage_domain(self, attachment):
        self.t.syncAction(attachment.actions, "deactivate")
        return self.t.get(attachment.href)

    def _detach_storage_domain(self, attachment):
        self.t.delete(attachment.href)

    def _run_storage_domain_tests(self, host, data_center, nfs_path):
        storage_domain = self._create_storage_domain(host, nfs_path)

        attachment = self._attach_storage_domain(storage_domain, data_center)
        attachment = self._activate_storage_domain(attachment)

        # FIXME: seems updating storage domain name isn't allowed
        # self._update_storage_domain(storage_domain)

        attachment = self._deactivate_storage_domain(attachment)
        self._detach_storage_domain(attachment)

        self._delete_storage_domain(storage_domain, host)

    def test_create_update_delete(self):
        host = self.fmt.Host()
        host.id = self.host.id

        data_center = self.fmt.DataCenter()
        data_center.id = self.data_center.id

        self._run_storage_domain_tests(host, data_center, self.config.nfs_paths[0])

    def test_create_update_delete_with_names(self):
        host = self.fmt.Host()
        host.name = self.host.name

        data_center = self.fmt.DataCenter()
        data_center.name = self.data_center.name

        self._run_storage_domain_tests(host, data_center, self.config.nfs_paths[1])
