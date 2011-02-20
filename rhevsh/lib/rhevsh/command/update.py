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

          $ update vm myvm --name newname --memory 1024000000

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
        self.check_connection()
        connection = self.context.connection
        typename, id = self.arguments
        typ = self.resolve_singular_type(typename)
        base = self.resolve_base(self.options)
        obj = self.get_object(typ, id, base)
        # Trac issue #179: don't set fields that already exist
        obj = schema.href(obj)
        obj = self.update_object(obj, self.options)
        connection.update(obj)

    def show_help(self):
        """Show help for "update"."""
        subst = {}
        args = self.arguments
        if len(args) == 0:
            helptext = self.helptext0
            types = self.get_singular_types()
            subst['types'] = self.format_list(types)
        else:
            helptext = self.helptext1
            typ = self.resolve_singular_type(args[0])
            subst['type'] = args[0]
            options = self.get_attribute_options(typ)
            subst['options'] = self.format_list(options)
        statuses = self.get_statuses()
        subst['statuses'] = self.format_list(statuses)
        stdout = self.context.terminal.stdout
        helptext = self.format_help(helptext, subst)
        stdout.write(helptext)
