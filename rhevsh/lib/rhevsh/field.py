#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

from rhev import schema


class Field(object):
    """Base class for display fields."""

    def __init__(self, name, description, flags, attribute=None):
        self.name = name
        self.description = description
        self.flags = flags
        self.attribute = attribute or name

    def get(self, obj, context):
        """Retrieve an attrbiute."""
        raise NotImplementedError

    def set(self, obj, value, context):
        """Set an attribute."""
        raise NotImplementedError

    def _resolve_parent(self, obj, attr):
        """INTERNAL: Resolve a dotted attribute name `attr' inside `obj'
        until its parent is reached, creating classes on the fly where
        required."""
        baseobj = obj
        basetype = type(obj)
        walked = []
        path = attr.split('.')
        for pa in path[:-1]:
            walked.append(pa)
            try:
                subobj = getattr(baseobj, pa) 
            except AttributeError:
                m = 'no such attribute: %s' % '.'.join(walked)
                raise ValueError, m
            if subobj is None:
                prop = getattr(type(baseobj), pa)
                subtype = schema.subtype(subtype)
                if issubclass(subtype, schema.ComplexType):
                    setattr(baseobj, pa, schema.new(subtype))
                    subobj = getattr(baseobj, pa)
            baseobj = subobj
        return baseobj, path[-1]


class StringField(Field):
    """A text field."""

    def get(self, obj, context):
        obj, attr = self._resolve_parent(obj, self.attribute)
        return getattr(obj, attr, None) or ''

    def set(self, obj, value, context):
        obj, attr = self._resolve_parent(obj, self.attribute)
        setattr(obj, attr, value)


class IntegerField(Field):
    """An integer field."""

    def __init__(self, name, description, flags, attribute=None,
                 min=None, max=None, scale=None):
        super(IntegerField, self).__init__(name, description, flags)
        self.min = None
        self.max = None
        self.scale = scale

    def get(self, obj, context):
        obj, attr = self._resolve_parent(obj, self.attribute)
        value = getattr(obj, attr, None)
        if value is not None:
            if self.scale is not None:
                value //= self.scale
            value = str(value)
        else:
            value = ''
        return value

    def set(self, obj, value, context):
        obj, attr = self._resolve_parent(obj, self.attribute)
        value = int(value)
        if self.scale is not None:
            value *= self.scale
        setattr(obj, attr, value)


class BooleanField(Field):
    """A boolean field."""

    def get(self, obj, context):
        obj, attr = self._resolve_parent(obj, self.attribute)
        value = getattr(obj, attr, None)
        if value is not None:
            if value:
                value = 'True'
            else:
                value = 'False'
        else:
            value = ''
        return value

    def set(self, obj, value, context):
        obj, attr = self._resolve_parent(obj, self.name)
        if value is None or value.lower() in ('true', 'on'):
            value = True
        else:
            value = False
        setattr(obj, attr, value)


class ReferenceField(Field):
    """A field referencing another resource."""

    def get(self, obj, context):
        obj, attr = self._resolve_parent(obj, self.attribute)
        value = getattr(obj, attr)
        if value.name:
            return name
        return value.id

    def set(self, obj, value, context):
        obj, attr = self._resolve_parent(obj, self.attribute)
        subtyp = schema.subtype(getattr(type(obj), attr))
        refobj = context.command.get_object(subtyp, value, None)
        if not refobj:
            context.command.error('%s not found: %s' % (self.name, value))
        setattr(obj, attr, schema.ref(refobj))
