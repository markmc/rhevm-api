#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

from rhev import schema
from rhev.error import Error
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
        self.api.retries = 1
        assert_raises(Error, self.api.ping)
