#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

from cli.error import CommandError
from rhev import schema
from rhevsh.command.command import RhevCommand


class ShowCommand(RhevCommand):

    name = 'show'
    description = 'show one object'
    args_check = 2
    valid_options = [ ('*', str) ]

    helptext = """\
        == Usage ==
    
        get <type> <id> [object identifiers]

        == Description ==

        Retrieve an object and display information about it. The following
        arguments are required:

          * type        The type of object to retrieve
          * id          The object identifier

        Objects can be identified by their name and by their unique id.

        == Available Types ==

        The <type> parameter must be one of the following.

          $types

        == Object Identifiers ==

        Some objects can only exist inside other objects. For example, a disk
        can only exist in the content of a virtual machine. In this case, one
        or more object identifier opties needs to be provided to identify the
        containing object.

        An object identifier is an option of the form '--<type>id <id>'. This
        would identify an object with type <type> and id <id>. See the
        examples section below for a few examples.

        == Examples ==

        Show information about the virtual machine "myvm"

          $ show vm myvm

        Show information about the first nic of the virtual machine "myvm"

          $ show nic nic1 --vmid myvm

        == Return values ==

        This command will exit with one of the following statuses. To see the
        exit status of the last command, type 'status'.

          $statuses
        """

    def execute(self):
        """Execute "show"."""
        args = self.arguments
        opts = self.options
        self.check_connection()
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
        self.context.formatter.format(self.context, obj)

    def show_help(self):
        """Show help for "show"."""
        self.check_connection()
        subst = {}
        types = self.get_singular_types()
        subst['types'] = self.format_list(types)
        statuses = self.get_statuses()
        subst['statuses'] = self.format_list(statuses)
        helptext = self.format_help(self.helptext, subst)
        stdout = self.context.terminal.stdout
        stdout.write(helptext)
