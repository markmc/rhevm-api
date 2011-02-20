#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

from rhev import schema
from cli.error import CommandError
from rhevsh.format.format import Formatter


class TextFormatter(Formatter):
    """Text formatter."""

    name = 'text'

    def _get_fields(self, tag):
        fields = self.context.settings.get('fields.%s' % tag)
        if fields is None:
            fields = self.context.settings.get('fields')
        if fields is None:
            raise CommandError, 'required config variable not set: fields'
        fields = fields.split(',')
        return fields

    def _get_value(self, resource, field):
        value = resource
        path = field.split('.')
        for pa in path:
            value = getattr(value, pa, None)
            if value is None:
                break
        if value is None:
            value = ''
        else:
            value = str(value)
        return value

    def _filter_fields(self, fields, resource):
        filtered = []
        for field in fields:
            try:
                self._get_value(resource, field)
            except AttributeError:
                pass
            else:
                filtered.append(field)
        return filtered

    def _format_resource(self, resource):
        context = self.context
        settings = context.settings
        stdout = context.terminal.stdout
        fields = self.context.command.get_attributes(type(resource))
        width0 = 0
        for field in fields:
            width0 = max(width0, len(field))
        format0 = '%%-%ds' % width0
        if stdout.isatty() and not settings['wide']:
            width1 = context.terminal.width - width0 - 2
            format1 = '%%-%d.%ds' % (width1, width1)
        else:
            width1 = sys.maxint
            format1 = '%s'
        stdout.write('\n')
        for field in fields:
            value = self._get_value(resource, field)
            if not value:
                continue
            stdout.write(format0 % field)
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

    def _format_collection(self, collection):
        context = self.context
        settings = context.settings
        stdout = context.terminal.stdout
        info = schema.type_info(type(collection))
        rel = info[2]
        fields = self._get_fields(rel)
        fields = self._filter_fields(fields, info[0])
        # Calculate the width of each column
        if stdout.isatty() and not settings['wide']:
            widths = [ len(f) for f in fields ]
            for resource in collection:
                for i in range(len(fields)):
                    value = self._get_value(resource, fields[i])
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
                remainders = [ (fwidths[i] - widths[i], i)
                               for i in range(len(fields)) ]
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
                stdout.write(formats[i] % fields[i])
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
            values = [ self._get_value(resource, field) for field in fields ]
            while sum([len(v) for v in values]) > 0:
                for i in range(len(fields)):
                    stdout.write(formats[i] % values[i])
                    values[i] = values[i][widths[i]:]
                    if i != len(fields)-1:
                        stdout.write('  ')
                stdout.write('\n')
        stdout.write('\n')

    def format(self, context, result):
        self.context = context
        if isinstance(result, schema.BaseResource):
            self._format_resource(result)
        elif isinstance(result, schema.BaseResources):
            self._format_collection(result)
