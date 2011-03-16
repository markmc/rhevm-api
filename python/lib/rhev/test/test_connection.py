#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

from rhev import schema
from rhev.error import Error
from rhev.test import util
from rhev.test.base import BaseTest

from nose.tools import assert_raises


class TestConnection(BaseTest):

    def test_retry(self):
        dc = self.api.get(schema.DataCenter)
        assert dc is not None
        # Close the socket below from under the API
        self.api._connection.sock.close()
        dc = self.api.reload(dc)
        assert dc is not None

    def test_ping(self):
        self.api.ping()
        # Close the socket below from under the API
        self.api._connection.sock.close()
        retries = self.api.retries
        self.api.retries = 1
        assert_raises(Error, self.api.ping)
        self.api.retries = retries

    def test_wait(self):
        vm = schema.new(schema.VM)
        vm.name = util.random_name('vm')
        vm.type = 'SERVER'
        vm.memory = 512*1024*1024
        vm.cluster = schema.new(schema.Cluster)
        vm.cluster.name = self.get_config('cluster')
        vm.template = schema.new(schema.Template)
        vm.template.name = self.get_config('template')
        vm = self.api.create(vm)
        assert isinstance(vm, schema.VM)
        disk = schema.new(schema.Disk)
        disk.format = 'COW'
        disk.size = 2024**3
        disk.sparse = True
        disk = self.api.create(disk, base=vm)
        assert isinstance(disk, schema.Disk)
        assert self.api.wait(vm, exists=True)
        assert self.api.wait(vm, status='DOWN')
        self.api.action(vm, 'start')
        assert self.api.wait(vm, status='UP')
        self.api.action(vm, 'stop')
        assert self.api.wait(vm, status='DOWN')
        self.api.delete(vm)
        #assert self.api.wait(vm, exists=False)  # BUG

    def test_methods(self):
        methods = self.api.get_methods(schema.DataCenters)
        assert 'GET' in methods
        assert 'POST' in methods
        assert 'PUT' not in methods
        assert 'DELETE' not in methods
        dc = self.api.get(schema.DataCenter)
        methods = self.api.get_methods(dc)
        assert 'GET' in methods
        assert 'POST' not in methods
        assert 'PUT' in methods
        #assert 'DELETE' in methods  # BUG

    def test_actions(self):
        vm = self.api.get(schema.VM)
        if vm is None:
            raise SkipTest, 'No VM found'
        actions = self.api.get_actions(vm)
        assert 'start' in actions
        assert 'stop' in actions
        assert 'suspend' in actions
        assert 'shutdown' in actions
        assert 'export' in actions
        assert 'detach' in actions
        assert 'migrate' in actions
        assert 'ticket' in actions

    def test_illegal_url(self):
        self.api.close()
        assert_raises(Error, self.api.connect,
                      '10.0.0.1', 'user@domain', 'pass')
        assert_raises(Error, self.api.connect,
                      'http://10.0.0.1', 'user@domain', 'pass')

    def test_illegal_username(self):
        self.api.close()
        assert_raises(Error, self.api.connect,
                      'http://10.0.0.1/foo', 'user', 'pass')

    def test_connect_missing_arguments(self):
        self.api.close()
        url = self.api.url
        self.api.url = None
        assert_raises(Error, self.api.connect)
        self.api.url = url
        username = self.api.username
        self.api.username = None
        assert_raises(Error, self.api.connect)
        self.api.username = username
        password = self.api.password
        self.api.password = None
        assert_raises(Error, self.api.connect)
        self.api.password = password
