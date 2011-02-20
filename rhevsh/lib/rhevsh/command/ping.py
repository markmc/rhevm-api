#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

from rhevsh.command.command import RhevCommand


class PingCommand(RhevCommand):

    name = 'ping'
    description = 'test the connection'
    helptext = """\
        == Usage ==

        ping

        == Description ==

        Test the connection to the RHEV manager. This command will go out to
        the RHEV manager and retrieve a remote resource. If it succeeds, you
        know that the URL, username and password are working.
        """

    def execute(self):
        self.check_connection()
        connection = self.context.connection
        stdout = self.context.terminal.stdout
        try:
            connection.ping()
        except RhevError:
            stdout.write('error: could NOT reach RHEV manager\n')
        else:
            stdout.write('success: RHEV manager could be reached OK\n')
