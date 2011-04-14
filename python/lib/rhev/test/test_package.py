#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import inspect

import rhev
from rhev.test.base import BaseTest


class TestPackage(BaseTest):

    def test_interface(self):
        assert hasattr(rhev, 'Connection')
        assert inspect.isclass(rhev.Connection)
        assert hasattr(rhev, 'Error')
        assert inspect.isclass(rhev.Error)
        assert hasattr(rhev, 'schema')
        assert inspect.ismodule(rhev.schema)
