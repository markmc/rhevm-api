#!/usr/bin/env python

# Copyright (C) 2010 Red Hat, Inc.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

import http
import xmlfmt
import yamlfmt
import jsonfmt
import copy
from testutils import *

opts = parseOptions()

links = http.HEAD_for_links(opts)

for fmt in [xmlfmt]:
    t = TestUtils(opts, fmt)

    print "=== ", fmt.MEDIA_TYPE, " ==="

    global_roles = {}
    for role in t.get(links['roles']):
        global_role = t.get(role.href)
        global_roles[role.id] = global_role

    assigned_roles = {}
    unassigned_roles = copy.deepcopy(global_roles)

    users = t.get(links['users'])
    user = t.get(users[0].href)
    for role in t.get(user.link['roles'].href):
        assigned_role = t.get(role.href)
        assigned_roles[assigned_role.id] = assigned_role
        del unassigned_roles[assigned_role.id]

    for r in unassigned_roles:
        t.create(user.link['roles'].href, unassigned_roles[r])

    expanded_roles = {}
    for role in t.get(user.link['roles'].href):
        expanded_role = t.get(role.href)
        expanded_roles[expanded_role.id] = expanded_role

    assert len(expanded_roles) == len(global_roles), "Expected all roles to be assigned to user"

    for r in expanded_roles:
        if r in unassigned_roles:
            t.delete(expanded_roles[r].href)

    final_roles = {}
    for role in t.get(user.link['roles'].href):
        final_role = t.get(role.href)
        final_roles[final_role.id] = final_role

    assert len(final_roles) == len(assigned_roles), "Expected initial roles to be re-assigned to user"

    for r in assigned_roles:
        assert r in final_roles, "Expected initial role %s to be re-assigned to user" % r.id
