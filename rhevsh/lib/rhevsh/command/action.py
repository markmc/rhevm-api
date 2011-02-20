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


class ActionCommand(RhevCommand):

    name = 'action'
    description = 'execute an action on an object'
    usage = 'action <type> <action> <id> [options]'
    args_check = 3
    valid_options = [ ('*', str) ]

    helptext0 = """\
        == Usage ==

        action <type> <id> <action> [base identifiers] [attribute options]

        == Description ==

        Executes the an action on an object. This command requires the
        following arguments:

          * type        - The type to operate on
          * id          - The name or id identifying the object
          * action      - The action to take

        For more specific help on the available actions and options, use
        'help action <type> <id>'

        == Available types ==

        The <type> parameter must be one of the following:

          $types

        == Return values ==

        This command will return one of the following statuses. To see the
        exit status of the last command, type 'status'.

          $statuses
        """

    helptext1 = """\
        == Usage ==

        action <type> <id> <action> [object identifiers] [attribute options]

        == Description ==

        Executes the an action on an object. This command requires the
        following arguments:

          * type        - The type to operate on
          * id          - The name or id identifying the object
          * action      - The action to take

        == Available actions ==

        The following actions are available for the specified $type object:

          $actions

        == Object Identifiers ==

        Some objects can only exist inside other objects. For example, a disk
        can only exist in the content of a virtual machine. In this case, one
        or more object identifiers needs to be provided to identify the
        containing object.

        An object identifier is an option of the form '--<type>id <id>'. This
        would identify an object with type <type> and id <id>. See the
        examples section below for a few examples.

        == Attribute Options ==

        The following attribute options are understood. Note: this lists all
        available attribute options for actions. Not every action supports
        every object!

          $options

        == Examples ==

        This example migrates a vm named "vm0" to the host named "host1":

          $ action vm vm0 migrate --host-name host1

        This example detaches a host nic with id '12345' from host '0':

          $ action nic 12345 detach --hostid 0
    
        == Return values ==

        This command will exit with one of the following statuses. To see the
        exit status of the last command, type 'status'.

          $statuses
        """

    def execute(self):
        """Execute the action command."""
        self.check_connection()
        typename, name, action = self.arguments
        typ = self.resolve_singular_type(typename)
        base = self.resolve_base(self.options)
        obj = self.get_object(typ, name, base)
        actionobj = schema.new(schema.Action)
        actionobj = self.update_object(actionobj, self.options)
        connection = self.context.connection
        try:
            result = connection.action(obj, action, actionobj)
        except rhev.Error, e:
            self.error(str(e))
        if result.status != 'COMPLETE':
            self.error('action status: %s' % result.status)

    def show_help(self):
        """Show help for the action command."""
        subst = {}
        args = self.arguments
        connection = self.context.connection
        if len(args) < 2:
            helptext = self.helptext0
            types = self.get_singular_types()
            subst['types'] = self.format_list(types)
        elif len(args) >= 2:
            helptext = self.helptext1
            typ = self.resolve_singular_type(args[0])
            subst['type'] = args[0]
            subst['id'] = args[1]
            if connection is None:
                subst['action'] = 'Not connected, cannot list actions.'
            else:
                base = self.resolve_base(self.options)
                obj = self.get_object(typ, args[1], base)
                if obj is None:
                    subst['actions'] = 'No such object, cannot list actions.'
                else:
                    actions = self.get_actions(obj)
                    subst['actions'] = self.format_list(actions)
            options = self.get_attribute_options(schema.Action)
            subst['options'] = self.format_list(options, bullet='')
        statuses = self.get_statuses()
        subst['statuses'] = self.format_list(statuses)
        stdout = self.context.terminal.stdout
        helptext = self.format_help(helptext, subst)
        stdout.write(helptext)
