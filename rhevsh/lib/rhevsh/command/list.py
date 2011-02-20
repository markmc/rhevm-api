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


class ListCommand(RhevCommand):

    name = 'list'
    aliases = ('search',)
    description = 'list or search objects'
    usage = 'list <type> [search]... [options]'
    args_check = lambda self, x: len(x) > 0
    valid_options = [ ('*', str) ]

    helptext = """\
        == Usage ==
    
        list <type> [search]... [object identifiers]

        == Description ==

        List or search for objects of a cetain type. There are two forms. If
        only <type> is provided, all objects of the specified type are
        returned. If a search query is given, it must be a valid RHEV-M search
        query. In that case objects matching the query are returned.

        == Available Types ==

        The <type> parameter must be one of the following. Note: not all types
        implement search!

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

        List all virtual machines:

          $ list vms

        Show only virtual machines that have a name that starts with "vm"

          $ list vms name=vm*

        List all disks in virtual machine 'myvm':

          $ list disks --vmid myvm

        == Return values ==

        This command will exit with one of the following statuses. To see the
        exit status of the last command, type 'status'.

          $statuses
        """

    def execute(self):
        """Execute "list"."""
        self.check_connection()
        stdout = self.context.terminal.stdout
        typename = self.arguments[0]
        typ = self.resolve_plural_type(typename)
        base = self.resolve_base(self.options)
        search = ' '.join(self.arguments[1:])
        connection = self.context.connection
        result = connection.getall(typ, base=base, search=search)
        self.context.formatter.format(self.context, result)

    def show_help(self):
        """Show help for "list"."""
        helptext = self.helptext
        subst = {}
        types = self.get_plural_types()
        subst['types'] = self.format_list(types)
        statuses = self.get_statuses()
        subst['statuses'] = self.format_list(statuses)
        stdout = self.context.terminal.stdout
        helptext = self.format_help(helptext, subst)
        stdout.write(helptext)
