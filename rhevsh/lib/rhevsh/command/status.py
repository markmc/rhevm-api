#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

from rhevsh.command.command import RhevCommand


class StatusCommand(RhevCommand):

    name = 'status'
    description = 'show status'
    helptext = """\
        == Usage ==

        status

        == Description ==

        Show the exist status of the last command and the staus of the
        connection to RHEV manager.
        """

    def execute(self):
        context = self.context
        stdout = context.terminal.stdout
        status = context.status
        if status is not None:
            sstatus = str(status)
            for sym in dir(context):
                if sym[0].isupper() and getattr(context, sym) == status:
                    sstatus += ' (%s)' % sym
        else:
            sstatus = 'N/A'
        stdout.write('last command status: %s\n' % sstatus)
        if context.connection:
            sstatus = 'connected to %s' % context.connection.url
        else:
            sstatus = 'not connected'
        stdout.write('connection: %s\n' % sstatus)
