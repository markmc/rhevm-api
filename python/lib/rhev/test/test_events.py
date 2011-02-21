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


class TestEvents(BaseTest):

    def test_getall(self):
        events = self.api.getall(schema.Event)
        assert isinstance(events, schema.Events)
        assert len(events) > 0
        self.store.event = events[0]

    def test_get(self):
        event = self.store.event
        event2 = self.api.get(schema.Event, id=event.id)
        assert isinstance(event2, schema.Event)
        assert event2.id == event.id

    def test_reload(self):
        event = self.store.event
        event2 = self.api.reload(event)
        assert isinstance(event2, schema.Event)
        assert event2.id == event.id

    def test_attributes(self):
        event = self.api.reload(self.store.event)
        assert isinstance(event, schema.Event)
        assert util.is_str(event.description) and len(event.description) > 1
        assert util.is_int(event.code)
        assert util.is_str(event.severity)
        # BUG: #242: log_time missing
