#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

from rhev import schema
from rhev.test import util
from rhev.test.base import BaseTest
from rhev.test.loader import depends


class TestTag(BaseTest):

    def test_create(self):
        tag = schema.new(schema.Tag)
        tag.name = util.random_name('tag')
        tag2 = self.api.create(tag)
        assert isinstance(tag2, schema.Tag)
        assert tag2.id is not None
        self.store.tag = tag2

    def test_create_child(self):
        tag = self.store.tag
        child = schema.new(schema.Tag)
        child.name = util.random_name('tag')
        child.parent = schema.new(schema.TagParent)
        child.parent.tag = schema.new(schema.Tag)
        child.parent.tag.id = tag.id
        child2 = self.api.create(child)
        assert isinstance(child2, schema.Tag)
        assert child2.id is not None
        assert child2.parent.tag.id == tag.id
        self.store.child = child2

    @depends(test_create)
    def test_get(self):
        tag = self.store.tag
        tag2 = self.api.get(schema.Tag, id=tag.id)
        assert isinstance(tag2, schema.Tag)
        assert tag2.id == tag.id

    @depends(test_create)
    def test_reload(self):
        tag = self.store.tag
        tag2 = self.api.reload(tag)
        assert isinstance(tag2, schema.Tag)
        assert tag2.id == tag.id

    @depends(test_create)
    def test_getall(self):
        tag = self.store.tag
        tags = self.api.getall(schema.Tag)
        assert isinstance(tags, schema.Tags)
        assert len(tags) > 0
        assert util.contains_id(tags, tag.id)

    @depends(test_create)
    def test_update(self):
        tag = self.store.tag
        tag.description = 'foo'
        tag2 = self.api.update(tag)
        assert isinstance(tag2, schema.Tag)
        assert tag2.id == tag.id
        assert tag2.description == tag.description
        tag = self.api.get(schema.Tag, id=tag.id)
        assert isinstance(tag, schema.Tag)
        assert tag.id == tag2.id
        assert tag.description == tag2.description

    @depends(test_create)
    def test_attributes(self):
        tag = self.api.reload(self.store.tag)
        assert isinstance(tag, schema.Tag)
        assert util.is_str_int(tag.id) or util.is_str_uuid(tag.id)
        assert util.is_str_href(tag.href)
        assert util.is_str(tag.name) and tag.name
        if tag.description:
            assert util.is_str(tag.description)
        assert tag.parent is not None
        assert isinstance(tag.parent.tag, schema.Tag)
        assert util.is_str_int(tag.parent.tag.id) or \
                util.is_str_uuid(tag.parent.tag.id)

    @depends(test_create)
    def test_delete(self):
        tag = self.store.tag
        child = self.store.child
        self.api.delete(child)
        self.api.delete(tag)
        tags = self.api.getall(schema.Tag)
        assert not util.contains_id(tags, tag.id)
        assert not util.contains_id(tags, child.id)
