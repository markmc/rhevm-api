#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

from rhev import schema
from rhev.object import create
from rhev.error import *
from rhev.test.base import BaseTest
from rhev.test import util

from nose.tools import assert_raises


class TestError(BaseTest):

    def test_prepare(self):
        dc = schema.new(schema.DataCenter)
        dc.name = util.random_name('dc')
        dc.storage_type = 'NFS'
        dc = self.api.create(dc)
        assert dc is not None
        self.store.dc = dc

    def test_error(self):
        error = Error('foo')
        assert str(error) == 'foo'
        error = Error('foo', arg='value', arg2='value2')
        assert str(error) == 'foo'
        assert error.arg == 'value'
        assert error.arg2 == 'value2'

    def test_create(self):
        error = create(Error, 'foo')
        assert isinstance(error, Error)
        assert str(error) == 'foo'

    def test_illegal_action(self):
        dc = self.store.dc
        assert_raises(IllegalAction, self.api.action, dc, 'foo')

    def test_finalize(self):
        dc = self.store.dc
        self.api.delete(dc)
