#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import os

from rhev import schema
from rhev.error import Error
from rhev.connection import Connection
from rhev.object import create, singleton, instance
from rhev.test.base import BaseTest

from nose.tools import assert_raises


class TestObject(BaseTest):

    def test_prepare(self):
        args = { 'url': 'http://example.com/api',
                 'username': 'user', 'password': 'passw0rd'}
        for key in args:
            try:
                del os.environ['RHEV_%s' % key.upper()]
            except KeyError:
                pass
        self.store.args = args

    def test_create(self):
        args = self.store.args
        obj = create(Connection, **args)
        assert isinstance(obj, Connection)
        assert_raises(Error, create, Connection)
        for key in args:
            os.environ['RHEV_%s' % key.upper()] = args[key]
        obj = create(Connection)
        assert isinstance(obj, Connection)
        obj = create(schema.VM)
        assert isinstance(obj, schema.VM)
        obj = create(Error, 'error')
        assert isinstance(obj, Error)
        obj = create(list)
        assert isinstance(obj, list)

    def test_singleton(self):
        obj = singleton(Connection)
        assert isinstance(obj, Connection)
        obj2 = instance(Connection)
        assert obj is obj2
        obj3 = singleton(Connection)
        assert obj3 is not obj2
        obj4 = instance(Connection)
        assert obj4 is obj3
        obj5 = instance(Connection)
        assert obj5 is obj3
