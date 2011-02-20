#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010-2011 by the python-rhev authors. See
# the file "AUTHORS" for a complete overview.

import socket
import logging

import rhev
from rhevsh.command.command import RhevCommand


class ConnectCommand(RhevCommand):

    name = 'connect'
    description = 'connect to a RHEV manager'
    args_check = (0, 3)

    helptext = """\
        == Usage ==

        connect
        connect <url> <username> <password>

        == Description ==

        Connect to a RHEV manager. This command has two forms. In the first
        form, no arguments are provided, and the connection details are read
        from their respective configuration variables (see 'show'). In
        the second form, the connection details are provided as arguments.

        The arguments are:

         * url          - The URL to connect to.
         * username     - The user to connect as. Important: this needs to be
                          in the user@domain format.
         * password     - The password to use.
        """

    def execute(self):
        settings = self.context.settings
        stdout = self.context.terminal.stdout
        if self.context.connection is not None:
            stdout.write('already connected\n')
            return
        if len(self.arguments) == 3:
            url, username, password = self.arguments
        else:
            url = settings.get('url')
            if not url:
                self.error('missing configuration variable: url')
            username = settings.get('username')
            if not username:
                self.error('missing configuration variable: username')
            password = settings.get('password')
            if not password:
                self.error('missing configuration variable: password')
        connection = rhev.Connection(url, username, password)
        if settings['verbose']:
            level = 10
        else:
            level = 1
        connection.verbosity = level
        try:
            connection.connect()
            connection.ping()
        except socket.error, e:
            self.error(e.strerror.lower())
        except rhev.Error, e:
            self.error(str(e))
        stdout.write('connected to RHEV manager at %s\n' % url)
        self.context.connection = connection
