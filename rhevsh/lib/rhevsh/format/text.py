#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

from rhev import schema
from cli.error import CommandError

from rhevsh import metadata
from rhevsh.format.format import Formatter


class TextFormatter(Formatter):
    """Text formatter."""

    name = 'text'

    def _get_fields(self, typ, flag, scope=None):
        info = schema.type_info(typ)
        assert info is not None
        override = self.context.settings.get('fields.%s' % info[2])
        if override is None:
            override = self.context.settings.get('fields')
        if override is None:
            fields = metadata.get_fields(typ, flag, scope)
        else:
            override = override.split(',')
            fields = metadata.get_fields(typ, '')
            fields = filter(lambda f: f.name in override, fields)
        return fields

    def _format_resource(self, resource, scope=None):
        context = self.context
        settings = context.settings
        stdout = context.terminal.stdout
        fields = self._get_fields(type(resource), 'S', scope)
        width0 = 0
        for field in fields:
            width0 = max(width0, len(field.name))
        format0 = '%%-%ds' % width0
        if stdout.isatty() and not settings['wide']:
            width1 = context.terminal.width - width0 - 2
            format1 = '%%-%d.%ds' % (width1, width1)
        else:
            width1 = sys.maxint
            format1 = '%s'
        stdout.write('\n')
        for field in fields:
            value = field.get(resource, self.context)
            if not value:
                continue
            stdout.write(format0 % field.name)
            stdout.write('  ')
            stdout.write(format1 % value)
            stdout.write('\n')
            value = value[width1:]
            while len(value) > 0:
                stdout.write(width1 * ' ')
                stdout.write(format1 % value)
                stdout.write('\n')
                value = value[width1:]
        stdout.write('\n')

    def _format_collection(self, collection, scope=None):
        context = self.context
        settings = context.settings
        stdout = context.terminal.stdout
        info = schema.type_info(type(collection))
        fields = self._get_fields(info[0], 'L', scope)
        # Calculate the width of each column
        if stdout.isatty() and not settings['wide']:
            widths = [ len(f.name) for f in fields ]
            for resource in collection:
                for i in range(len(fields)):
                    value = fields[i].get(resource, self.context)
                    widths[i] = max(widths[i], len(value))
            total = sum(widths) + 2*len(fields)
            # Now downsize if it doesn't fit
            if total > context.terminal.width:
                fraction = 1.0 * context.terminal.width / total
                fwidths = [0] * len(fields)
                # Pass 1: round down to nearest integer
                for i in range(len(fields)):
                    fwidths[i] = widths[i] * fraction
                    widths[i] = int(fwidths[i])
                # Pass 2: allocate fractional leftovers to columns
                available = context.terminal.width - 2*len(fields) - sum(widths)
                remainders = [ (fwidths[i] - widths[i], i) for i in range(len(fields)) ]
                remainders.sort(reverse=True)
                for i in range(min(len(fields), available)):
                    widths[remainders[i][1]] += 1
            formats = ['%%-%d.%ds' % (w, w) for w in widths ]
        else:
            widths = [ sys.maxint ] * len(fields)
            formats = [ '%s' ] * len(fields)
        if settings['header']:
            # Header
            for i in range(len(fields)):
                stdout.write(formats[i] % fields[i].name)
                if i != len(fields)-1:
                    stdout.write('  ')
            stdout.write('\n')
            # Horizontal line
            for i in range(len(fields)):
                stdout.write('-' * widths[i])
                if i != len(fields)-1:
                    stdout.write('  ')
            stdout.write('\n')
        # Data elements
        for resource in collection:
            values = [ field.get(resource, self.context) for field in fields ]
            while sum([len(v) for v in values]) > 0:
                for i in range(len(fields)):
                    stdout.write(formats[i] % values[i])
                    values[i] = values[i][widths[i]:]
                    if i != len(fields)-1:
                        stdout.write('  ')
                stdout.write('\n')
        stdout.write('\n')

    def format(self, context, result, scope=None):
        self.context = context
        if isinstance(result, schema.BaseResource):
            self._format_resource(result, scope)
        elif isinstance(result, schema.BaseResources):
            self._format_collection(result, scope)
