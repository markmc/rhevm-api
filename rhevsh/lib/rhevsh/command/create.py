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


class CreateCommand(RhevCommand):

    name = 'create'
    aliases = ('add',)
    description = 'create a new object'
    args_check = 1
    valid_options = [ ('*', str) ]

    helptext0 = """\
        == Usage ==

        create <type> [base identifiers] [attribute options]

        == Description ==

        Create a new object with type <type>.

        == Available Types ==

        The following types of objects can be created:

          $types

        == Base Identifiers ==

        Some objects can only be created inside other objects. For example a
        disk can only be created inside a virtual machine. In this case, one
        or more base identifier options need to be given to identify the
        containing object. These options have the form --<type>id <id>.

        == Attribute Options ==

        Attribute options specifiy values for attributes of the to be created
        object.

        Attributes for the new object can be specified in one of two ways.

          * Using command-line options. For example, "--description foo"
            would create the object with a description of "foo".
          * By providing pre-formatted input, in the format specified by the
            'input_format' configuration variable. In this case the input
            needs to be provided using the '<<' input redirection operator.

        Type 'help create <type>' to see an overview of which attributes are
        available for a given type.

        == Examples ==

        This create a new virtual machine in the Default cluster based on the
        Blank template:

          $ create vm --name myvm --memory 512000000 --type SERVER \\
                      --cluster-name Default --template-name Blank

        This example does the same but now using pre-formatted input:

          $ create vm << EOM
          > <vm>
          >   <name>myvm</name>
          >   <memory>512000000</memory>
          >   <type>SERVER</type>
          >   <cluster><name>Default</name></cluster>
          >   <template><name>Blank</name></template>
          > </vm>
          > EOM

        == Return Values ==

          $statuses
        """

    helptext1 = """\
        == Usage ==

        create <type> [base identifiers] [attribute options]

        == Description ==

        Create a new object with type $type. See 'help create' for generic
        help on creating objects.

        == Attribute Options ==

        The following options are available for objects with type $type:

          $options

        == Return Values ==

          $statuses
        """

    def execute(self):
        """Execute the "create" command."""
        self.check_connection()
        connection = self.context.connection
        stdout = self.context.terminal.stdout
        typename = self.arguments[0]
        typ = self.resolve_singular_type(typename)
        base = self.resolve_base(self.options)
        obj = self.read_input()
        if obj is None:
            obj = schema.new(typ)
            obj = self.update_object(obj, self.options)
        connection.create(obj, base=base)

    def show_help(self):
        """Show help for "create"."""
        subst = {}
        args = self.arguments
        if len(args) == 0:
            helptext = self.helptext0
            types = self.get_singular_types()
            subst['types'] = self.format_list(types)
        elif len(args) == 1:
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
