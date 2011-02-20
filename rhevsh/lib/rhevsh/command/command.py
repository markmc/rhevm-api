#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

from string import Template

import rhev
from rhev import schema

from cli.command import Command
from cli.error import CommandError


class RhevCommand(Command):
    """Base class for RHEV commands."""

    def check_connection(self):
        """ensure we have a connection."""
        if self.context.connection is None:
            self.error('not connected', help='try \'help connect\'')

    def resolve_type(self, name):
        """return a rhev.schema.* mapping type for a type name."""
        info = schema.type_info(name)
        if info is None:
            plural = schema.plural(name)
            if plural is None:
                self.error('no such type: %s' % name)
            info = schema.type_info(name)
            assert info is not None
            return info[0]._SupersedingClass()
        return info[1]._SupersedingClass()

    def resolve_plural_type(self, name):
        """resolve a plural type only."""
        info = schema.type_info(name)
        if info is None:
            self.error('no such type: %s' % name)
        return info[1]._SupersedingClass()

    def resolve_singular_type(self, name):
        """return a singular type only."""
        plural = schema.plural(name)
        if plural is None:
            self.error('no such type: %s' % name)
        info = schema.type_info(plural)
        assert info is not None
        return info[0]._SupersedingClass()

    def resolve_base(self, options):
        """resolve a base object from a set of '--typeid value' options."""
        self.check_connection()
        path = []
        for opt in options:
            if not opt.endswith('id'):
                continue
            typename = opt[2:-2]
            typ = self.resolve_singular_type(typename)
            if typ is None:
                self.error('unknown type: %s' % typename)
            path.append((typ, typename, options[opt]))
        base = None
        for (typ,typename,id) in path:
            try:
                base = self.context.connection.get(typ, id=id, base=base)
            except rhev.Error:
                base = None  # work around RHEV issue #120
            if base is None:
                base = self.context.connection.get(typ, name=id, base=base)
                if base is None:
                    self.error('could not locate %s: %s' %  (typename, id))
                    return
        return base

    def update_object(self, obj, options):
        """Create a new binding type of type `typ', and set its attributes
        with values from `options'."""
        attrs = [ opt for opt in options if not opt.endswith('id') ]
        attrs.sort()
        for attr in attrs:
            baseobj = obj
            basetype = type(obj)
            walked = []
            path = attr[2:].split('-')
            for pa in path[:-1]:
                walked.append(pa)
                try:
                    subobj = getattr(baseobj, pa) 
                    subtype = getattr(basetype, pa)
                except AttributeError:
                    self.error('no such attribute: %s' % '.'.join(walked))
                if subobj is None:
                    subtype = schema.subtype(subtype)
                    if issubclass(subtype, schema.ComplexType):
                        setattr(baseobj, pa, schema.new(subtype))
                        subobj = getattr(baseobj, pa)
                baseobj = subobj
                basetype = subtype
            if not hasattr(basetype, path[-1]):
                self.error('no such attribute: %s' % attr)
            setattr(baseobj, path[-1], options[attr])
        return obj 

    def read_input(self):
        """If input was provided via stdin, then parse it and return a binding
        instance."""
        stdin = self.context.terminal.stdin
        # REVISE: this is somewhat of a hack (this detects a '<<' redirect by
        # checking if stdin is a StringIO)
        if not hasattr(stdin, 'len'):
            return
        buf = stdin.read()
        try:
            obj = schema.create_from_xml(buf)
        except rhev.ParseError:
            self.error('could not parse input')
        return obj

    def get_singular_types(self):
        """Return a list of valid top-level singular types."""
        types = []
        for info in schema._mapping_data:
            if info[1] and info[2]:
                name = schema.singular(info[2])
                types.append(name)
        return types

    def get_plural_types(self):
        """Return a list of valid top-level plural types."""
        types = []
        for info in schema._mapping_data:
            if info[1] and info[2]:
                types.append(info[2])
        return types

    def get_attributes(self, typ, prefix=''):
        """Return a list of valid attributes for a type."""
        attrs = []
        for elem in typ._ElementMap:
            name = elem.localName()
            if name in ('actions', 'link', 'fault'):
                continue
            prop = getattr(typ, name)
            if not isinstance(prop, property):
                continue
            subtype = schema.subtype(prop)
            if issubclass(subtype, schema.ComplexType):
                info = schema.type_info(subtype)
                if info is None:
                    attrs += self.get_attributes(subtype, prefix + name + '.')
                elif info[3]:
                    attrs.append('%s%s.id' % (prefix, name))
                    attrs.append('%s%s.name' % (prefix, name))
            elif issubclass(subtype, schema.SimpleType):
                attrs.append('%s%s' % (prefix, name))
        for attr in typ._AttributeMap:
            if not prefix and attr in ('id', 'href'):
                continue
            name = attr.localName()
            attrs.append('%s%s' % (prefix, name))
        return attrs

    def get_attribute_options(self, typ):
        """Return a list of valid data binding options for a type."""
        attrs = self.get_attributes(typ)
        options = []
        for attr in attrs:
            option = '--%s' % attr.replace('.', '-')
            options.append(option)
        return options

    def get_object(self, typ, id, base):
        """Return an object by id or name."""
        self.check_connection()
        connection = self.context.connection
        try:
            obj = connection.get(typ, id=id, base=base)
        except rhev.Error:
            obj = None  # work around RHEV issue #120
        if obj is None:
            obj = connection.get(typ, name=id, base=base)
        return obj

    def get_actions(self, obj):
        """Return the actions supported for the object `obj'."""
        actions = []
        if hasattr(obj, 'actions') and obj.actions:
            for link in obj.actions.link:
                actions.append(link.rel)
        return actions
