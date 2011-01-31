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
        self.url = url
        username = self.api.username
        self.api.username = None
        assert_raises(Error, self.api.connect)
        self.api.username = username
        password = self.api.password
        self.api.password = None
        assert_raises(Error, self.api.connect)
        self.api.password = password
