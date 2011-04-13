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
from rhevsh import metadata

from cli.command import Command
from cli.error import CommandError


class RhevCommand(Command):
    """Base class for RHEV commands."""

    def check_connection(self):
        """ensure we have a connection."""
        if self.context.connection is None:
            self.error('not connected', help='try \'help connect\'')
        return self.context.connection

    def resolve_base(self, options):
        """resolve a base object from a set of '--typeid value' options."""
        connection = self.check_connection()
        path = {}
        for opt in options:
            if not opt.endswith('id'):
                continue
            typename = opt[2:-2]
            info = schema.type_info(typename)
            if info is None:
                self.error('unknown type: %s' % typename)
            path[info[3]] = (options[opt], info)
        base = connection.get(schema.API)
        info = schema.type_info(type(base))
        while path:
            links = connection.get_links(base)
            for link in links:
                if link in path:
                    break
            else:
                self.error('unable to resolve at: %s' % info[2])
            id, info = path.pop(link)
            base = self.get_object(info[0], id, base)
            if base is None:
                self.error('object does not exist at: %s' % info[2])
        return base

    def create_object(self, typ, options):
        """Create a new object of type `typ' based on the command-line
        options in `options'."""
        obj = schema.new(typ)
        fields = metadata.get_fields(typ, 'C')
        for field in fields:
            key = '--%s' % field.name
            if key in options:
                field.set(obj, options[key], self.context)
        return obj

    def update_object(self, obj, options):
        """Create a new binding type of type `typ', and set its attributes
        with values from `options'."""
        fields = metadata.get_fields(type(obj), 'U')
        for field in fields:
            key = '--%s' % field.name
            if key in options:
                field.set(obj, options[key], self.context)
        return obj

    def read_object(self):
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

    def get_object(self, typ, id, base):
        """Return an object by id or name."""
        self.check_connection()
        connection = self.context.connection
        # Try by id
        try:
            obj = connection.get(typ, id=id, base=base)
        except rhev.Error:
            obj = None  # work around RHEV issue #120
        if obj is not None:
            return obj
        # Try by name: either search, or filter
        info = schema.type_info(typ)
        if base is None:
            base = connection.get(schema.API)
        links = connection.get_links(base)
        rel = '%s/search' % info[3]
        if rel in links:
            obj = connection.get(typ, name=id, base=base)
        else:
            obj = connection.get(typ, base=base, filter={'name': id})
        return obj

    def _get_types(self, plural):
        """INTERNAL: return a list of types."""
        connection = self.check_connection()
        links = connection.get_links(connection.api())
        types = [ schema.type_info(link) for link in links ]
        ix = 2 + int(plural)
        types = [ info[ix] for info in types if info and info[ix] ]
        return types

    def get_singular_types(self):
        """Return a list of singular types."""
        return self._get_types(False)

    def get_plural_types(self):
        """Return a list of plural types."""
        return self._get_types(True)

    def get_options(self, typ, action=None):
        """Return a list of options for typ/action."""
        flag = self.name[0].upper()
        fields = metadata.get_fields(typ, flag, action=action)
        options = [ '--%-20s %s' % (field.name, field.description)
                    for field in fields ]
        return options
