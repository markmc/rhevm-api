#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

import os
from cli.error import Error
from rhevsh.platform import util


def launch_vnc_viewer(host, port, ticket, debug=False):
    """Launch a VNC viewer on host::port with `password'."""
    display = os.environ.get('DISPLAY')
    if display is None:
        raise Error, 'not running in a GUI, cannot start a VNC viewer'
    cmd = util.which('vncviewer')
    if cmd is None:
        raise Error, 'vncviewer: command not found'
    args = ['vncviewer', '%s::%s' % (host, port), '-passwdInput' ]
    pid, pstdin = util.spawn(cmd, args, debug)
    os.write(pstdin, ticket)
    os.close(pstdin)
