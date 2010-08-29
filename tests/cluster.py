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

class ClusterTests(base.TestCase):
    def setUp(self):
        base.TestCase.setUp(self)

    def test_list_and_get(self):
        clusters = self.t.get(self.links['clusters'])
        for cluster in clusters:
            self.t.get(cluster.href)

    def test_query(self):
        clusters = self.t.get(self.links['clusters'])
        for cluster in clusters:
            self.t.query(self.links['clusters/search'], 'name=' + cluster.name[0:-1] + '*')

    def test_create_update_delete(self):
        cluster = self.fmt.Cluster()
        cluster.name = randomName('c')
        cluster.cpu = self.fmt.CPU()
        cluster.cpu.id = self.config.default_cpu
        cluster.data_center = self.fmt.DataCenter()
        cluster.data_center.id = self.data_center.id

        cluster = self.t.create(self.links['clusters'], cluster)

        cluster.name += 'u'
        self.t.update(cluster.href, cluster, 200)

        simpleUpdate = self.fmt.Cluster()
        simpleUpdate.name = cluster.name + 'su'
        self.t.update(cluster.href, simpleUpdate, 200)

        self.t.delete(cluster.href)
