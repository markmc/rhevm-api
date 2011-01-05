#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import sys
import os.path
import time

from ConfigParser import ConfigParser, NoOptionError

from rhev.object import create
from rhev.error import Error
from rhev.connection import Connection
from rhev.test import util


class BaseTest(object):
    """Base class for tests."""

    @classmethod
    def _find_test_config(cls):
        testname = os.environ.get('TEST_CONFIG')
        if testname is None:
            fname = __file__
            base, tail = os.path.split(fname)
            while base != '/':
                testname = os.path.join(base, 'test.conf')
                if os.access(testname, os.R_OK):
                    break
                base, tail = os.path.split(base)
            else:
                raise Error, 'test.conf not found'
        return testname

    @classmethod
    def get_config(self, key, section=None, default=None):
        if section is None:
            section = 'test'
        try:
            return self.config.get(section, key)
        except NoOptionError:
            return default

    @classmethod
    def _connect(cls):
        url = cls.get_config('url')
        username = cls.get_config('username')
        password = cls.get_config('password')
        api = create(Connection, url=url,username=username, password=password)
        api.verbosity = 10
        return api

    @classmethod
    def setUpClass(cls):
        cls.store = util.store()
        util.setup_logging(debug=True)
        cfgname = cls._find_test_config()
        if not os.access(cfgname, os.R_OK):
            raise Error, 'test.conf file missing'
        defaults = { 'keep_connection': 'false' }
        cls.config = ConfigParser(allow_no_value=True)
        cls.config.read(cfgname)
        cls.api = cls._connect()
        cls.setup()

    @classmethod
    def setup(cls):
        """Override in a subclass."""

    @classmethod
    def tearDownClass(cls):
        cls.teardown()
        cls.api.close()

    @classmethod
    def teardown(cls):
        """Overrride in a subclass."""

    def setUp(self):
        pass
        #self.store = self.__class__._store

    def wait_for_status(self, resource, status, timeout=600):
        start = time.time()
        delay = 1
        while time.time() < start + timeout:
            obj = self.api.reload(resource)
            if obj.status == status:
                resource.status = obj.status
                return True
            time.sleep(delay)
            delay = min(10, delay*2)
        return False

    def retry_action(self, resource, action, params=None, status='COMPLETE',
                   timeout=100):
        start = time.time()
        while time.time() < start + timeout:
            result = self.api.action(resource, action, params)
            if result.status == status:
                return True
            time.sleep(10)
        return False
