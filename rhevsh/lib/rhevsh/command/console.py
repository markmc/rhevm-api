#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

import os
import os.path
from subprocess import Popen, PIPE

from rhev import schema, Error
from rhevsh.platform import vnc, spice
from rhevsh.command.command import RhevCommand


class ConsoleCommand(RhevCommand):

    name = 'console'
    description = 'open a console to a VM'
    args_check = 1

    helptext = """\
        == Usage ==

        console <vm>

        == Description ==

        This opens up a graphical console to a virtual machine. Depending on
        the virtual machine display, this will fire up an external VNC or
        SPICE client.
        """

    def execute(self):
        connection = self.check_connection()
        stdout = self.context.terminal.stdout
        args = self.arguments
        vm = self.get_object(schema.VM, args[0])
        if vm is None:
            self.error('no such vm: %s' % args[0])
        if vm.status not in ('POWERING_UP', 'UP', 'REBOOT_IN_PROGRESS'):
            self.error('vm is not up')
        proto = vm.display.type
        host = vm.display.address
        port = vm.display.port
        action = connection.action(vm, 'ticket')
        if action.status != 'COMPLETE':
            self.error('could not set a ticket for the vm')
        ticket = action.ticket.value_
        debug = self.context.settings['debug']
        if proto == 'vnc':
            vnc.launch_vnc_viewer(host, port, ticket, debug)
        elif proto == 'spice':
            certurl = 'https://%s/ca.crt' % connection.host
            spice.launch_spice_client(host, port, ticket, certurl, vm.name, debug)
        else:
            self.error('unsupported display protocol: %s' % proto)
