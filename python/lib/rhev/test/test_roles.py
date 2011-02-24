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


class TestRoles(BaseTest):

    def test_getall(self):
        roles = self.api.getall(schema.Role)
        assert isinstance(roles, schema.Roles)
        assert len(roles) > 0
        self.store.role = roles[0]

    def test_get(self):
        role = self.store.role
        role2 = self.api.get(schema.Role, id=role.id)
        assert isinstance(role2, schema.Role)
        assert role2.id == role.id

    def test_reload(self):
        role = self.store.role
        role2 = self.api.reload(role)
        assert isinstance(role2, schema.Role)
        assert role2.id == role.id

    def test_attributes(self):
        role = self.api.reload(self.store.role)
        assert isinstance(role, schema.Role)
        assert util.is_str_uuid(role.id)
        assert util.is_str_href(role.href)
        assert util.is_str(role.name) and len(role.name) > 0
        assert util.is_str(role.description)
