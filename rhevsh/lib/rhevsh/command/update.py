#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

import rhev
from rhev import schema
from rhevsh.command.command import RhevCommand


class UpdateCommand(RhevCommand):

    name = 'update'
    description = 'update an object'
    args_check = 2
    valid_options = [ ('*', str) ]

    helptext0 = """\
        == Usage ==

        update <type> <id> [base identifiers] [attribute options]

        == Description ==

        Update an existing object. This command requires the following
        arguments:

          * type        The type of object to delete.
          * id          The identifier of the object to delete

        == Available Types ==

        The following object types are available:

          $types

        == Base Identifiers ==

        Some objects can only exist inside other objects. For example a disk
        can only exist as part of a virtual machine. In this case you want to
        update such an object, one or more base identifier options need to be
        given to identify the containing object. These options have the form
        --<type>id <id>.

        == Attribute Options ==

        Attribute options specifiy values for attributes of the object that is
        to be updated.

        Type 'help update <type>' to see an overview of which attributes are
        available for a specific type.

        == Examples ==

        This updates a virtual machine with name "myvm":

          $ update vm myvm --name newname --memory 1024

        == Return Values ==

          $statuses
        """

    helptext1 = """\
        == Usage ==

        update <type> <id> [base identifiers] [attribute options]

        == Description ==

        Update an existing object. This command requires the following
        arguments:

          * type        The type of object to delete.
          * id          The identifier of the object to delete

        See 'help create' for generic help on creating objects.

        == Attribute Options ==

        The following options are available for objects with type $type:

          $options

        == Return Values ==

          $statuses
        """

    def execute(self):
        """Execute the "update" command."""
        args = self.arguments
        opts = self.options
        connection = self.check_connection()
        base = self.resolve_base(opts)
        info = schema.type_info(args[0], base=base)
        if info is None:
            self.error('no such type: %s' % args[0])
        for link in base.link:
            if link.rel == info[3]:
                break
        else:
            self.error('type does not exist here: %s' % args[0])
        obj = self.get_object(info[0], args[1], base)
        if obj is None:
            self.error('object does not exist')
        # Trac issue #179: don't set fields that already exist
        obj = schema.href(obj)
        self.update_object(obj, self.options)
        connection.update(obj)

    def show_help(self):
        """Show help for "update"."""
        args = self.arguments
        opts = self.options
        connection = self.check_connection()
        subst = {}
        if len(args) < 2:
            helptext = self.helptext0
            types = self.get_singular_types()
            subst['types'] = self.format_list(types)
        elif len(args) == 2:
            info = schema.type_info(args[0])
            if info is None:
                self.error('no such type: %s' % args[0])
            base = self.resolve_base(opts)
            obj = self.get_object(info[0], args[1], base=base)
            if obj is None:
                self.error('no such %s: %s' % (args[0], args[1]))
            methods = connection.get_methods(obj)
            if 'PUT' not in methods:
                self.error('type cannot be updated: %s' % args[0])
            helptext = self.helptext1
            subst['type'] = args[0]
            options = self.get_options(info[0], 'U')
            subst['options'] = self.format_list(options)
        statuses = self.get_statuses()
        subst['statuses'] = self.format_list(statuses)
        helptext = self.format_help(helptext, subst)
        stdout = self.context.terminal.stdout
        stdout.write(helptext)
