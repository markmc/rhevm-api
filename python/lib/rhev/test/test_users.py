#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

from rhev import *
from rhev.test import util
from rhev.test.base import BaseTest
from rhev.test.loader import depends


class TestUsers(BaseTest):

    def test_getall(self):
        users = self.api.getall(schema.User)
        assert isinstance(users, schema.Users)
        assert len(users) > 0
        self.store.user = users[0]

    def test_get(self):
        user = self.store.user
        user2 = self.api.get(schema.User, id=user.id)
        assert isinstance(user2, schema.User)
        assert user2.id == user.id

    def test_reload(self):
        user = self.store.user
        user2 = self.api.reload(user)
        assert isinstance(user2, schema.User)
        assert user2.id == user.id

    def test_attributes(self):
        user = self.api.reload(self.store.user)
        assert isinstance(user, schema.User)
        assert util.is_str_uuid(user.id)
        assert util.is_str_href(user.href)
        assert util.is_str(user.name) and len(user.name) > 0
        assert user.description is None or util.is_str(user.description)
