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

class iScsiTests(base.TestCase):
    def setUp(self):
        base.TestCase.setUp(self)

    def _create_storage_domain_from_lun(self, host, **kwargs):
        storage_domain = self.fmt.StorageDomain()
        storage_domain.name = randomName('iscsi')
        storage_domain.type = 'DATA'
        storage_domain.storage = self.fmt.Storage()
        storage_domain.storage.type = 'ISCSI'
        storage_domain.storage.logical_unit = self.fmt.LogicalUnit()
        if 'portal' in kwargs and 'target' in kwargs:
            storage_domain.storage.logical_unit.address = kwargs['portal']
            storage_domain.storage.logical_unit.target = kwargs['target']
        storage_domain.storage.logical_unit.id = kwargs['lun']
        storage_domain.host = host

        return self.t.create(self.links['storagedomains'], storage_domain)

    def _delete_storage_domain(self, storage_domain, host):
        self.t.syncAction(storage_domain.actions, "teardown", host=host)
        self.t.delete(storage_domain.href)

    def _run_iscsi_tests(self, host, **kwargs):
        storage_domain = self._create_storage_domain_from_lun(host, **kwargs)
        self._delete_storage_domain(storage_domain, host)

    def test_lun_create_update_delete(self):
        host = self.fmt.Host()
        host.id = self.host.id

        self._run_iscsi_tests(host,
                              lun = self.config.iscsi_luns[0])

    def test_lun_create_update_delete(self):
        host = self.fmt.Host()
        host.id = self.host.id

        self._run_iscsi_tests(host,
                              lun = self.config.iscsi_luns[1],
                              portal = self.config.iscsi_portal,
                              target = self.config.iscsi_target)
