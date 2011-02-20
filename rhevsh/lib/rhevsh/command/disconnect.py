#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

from rhev import Connection, Error as RhevError
from rhevsh.command.command import RhevCommand


class DisconnectCommand(RhevCommand):

    name = 'disconnect'
    description = 'disconnect from RHEV manager'
    helptext = """\
        == Usage ==

        disconnect

        == Description ==

        Disconnect an active connection to RHEV manager, if any. This method
        can be called multiple times. It is not an error to disconnect when
        not connected.
        """

    def execute(self):
        stdout = self.context.terminal.stdout
        connection = self.context.connection
        if connection is None:
            stdout.write('not connected\n')
            return
        try:
            connection.close()
        except RhevError, e:
            pass
        stdout.write('disconnected from RHEV manager\n')
        self.context.connection = None
