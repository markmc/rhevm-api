#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

from rhev.object import create
from rhev.error import Error
from rhev.test.base import BaseTest


class TestError(BaseTest):

    def test_error(self):
        error = Error('foo')
        assert error.message == 'foo'
        error = Error('foo', arg='value', arg2='value2')
        assert error.message == 'foo'
        assert error.arg == 'value'
        assert error.arg2 == 'value2'

    def test_create(self):
        error = create(Error, 'foo')
        assert isinstance(error, Error)
        assert error.message == 'foo'
