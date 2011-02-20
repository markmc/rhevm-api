#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

import sys
import logging
import textwrap

from cli.context import ExecutionContext
import rhev
from rhevsh.settings import RhevshSettings
from rhevsh.command import *
from rhevsh.format import *
from rhevsh.object import create


class RhevshExecutionContext(ExecutionContext):

    Settings = RhevshSettings

    welcome = textwrap.dedent("""\
        Welcome to Red Hat Enterprise Virtualization.

        Type:  'help' for help with commands
               'exit' to leave this interactive shell
        """)

    REMOTE_ERROR = 10
    NOT_FOUND = 11

    def __init__(self):
        super(RhevshExecutionContext, self).__init__()
        self.connection = None
        self._setup_logging()
        self._set_debug('debug', self.settings['debug'])
        self._set_verbose('verbose', self.settings['verbose'])
        self._set_formatter('output_format', self.settings['output_format'])
        self.settings.add_callback(self._set_debug, 'debug')
        self.settings.add_callback(self._set_verbose, 'verbose')
        self.settings.add_callback(self._set_formatter, 'output_format')

    def _setup_logging(self):
        """INTERNAL: configure logging."""
        logger = logging.getLogger()
        handler = logging.StreamHandler(sys.stdout)
        formatter = logging.Formatter('%(levelname)s: %(message)s')
        handler.setFormatter(formatter)
        logger.addHandler(handler)
        logger.setLevel(logging.DEBUG)
        self._logger = logger

    def _set_debug(self, key, value):
        if value:
            level = logging.DEBUG
        else:
            level = logging.INFO
        self._logger.setLevel(level)

    def _set_verbose(self, key, value):
        if self.connection is None:
            return
        if value:
            level = 10
        else:
            level = 1
        self.connection.verbosity = level

    def _set_formatter(self, key, value):
        self.formatter = create(Formatter, value)

    def handle_exception(self, e):
        if isinstance(e, rhev.Error):
            msg = 'error: rhev: %s\n' % str(e)
            if hasattr(e, 'detail'):
                msg += 'detail: %s\n' % e.detail
            sys.stderr.write(msg)
            self.status = self.REMOTE_ERROR
        else:
            super(RhevshExecutionContext, self).handle_exception(e)

    def setup_commands(self):
        super(RhevshExecutionContext, self).setup_commands()
        self.add_command(ActionCommand)
        self.add_command(CreateCommand)
        self.add_command(ConnectCommand)
        self.add_command(CreateCommand)
        self.add_command(DeleteCommand)
        self.add_command(DisconnectCommand)
        self.add_command(HelpCommand)
        self.add_command(ListCommand)
        self.add_command(PingCommand)
        self.add_command(ShowCommand)
        self.add_command(StatusCommand)
        self.add_command(UpdateCommand)
