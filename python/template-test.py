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
from testutils import *

opts = parseOptions()

links = http.HEAD_for_links(opts)

print links

for fmt in [xmlfmt]:
   t = TestUtils(opts, fmt)

   print "=== ", fmt.MEDIA_TYPE, " ==="

   for template in t.get(links['templates'], fmt.parseTemplateCollection):
      print t.get(template.href, fmt.parseTemplate)
