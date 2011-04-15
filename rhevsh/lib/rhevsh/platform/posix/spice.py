#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

import os
import urllib

from cli.error import Error
from rhevsh.platform import util


def launch_spice_client(host, port, ticket, certurl, title, debug=False):
    """Launch a VNC viewer on host::port with `password'."""
    display = os.environ.get('DISPLAY')
    if display is None:
        raise Error, 'not running in a GUI, cannot start a SPICE client'
    cmd = util.which('spicec')
    if cmd is None:
        cmd = util.which('/usr/libexec/spicec')
    if cmd is None:
        raise Error, 'spicec: command not found'
    certdir = os.path.join(util.get_home_dir(), '.spicec')
    try:
        os.stat(certdir)
    except OSError:
        os.mkdir(certdir)
    certfile = os.path.join(certdir, 'spice_truststore.pem')
    try:
        os.stat(certfile)
    except OSError:
        certtmp = '%s.%d-tmp' % (certfile, os.getpid())
        urllib.urlretrieve(certurl, certtmp)
        os.rename(certtmp, certfile)
    secport = 11800 - port
    if cmd.startswith('/usr/libexec'):
        args = [ 'spicec', host, str(port), str(secport), '--ssl-channels',
                 'smain,sinputs', '--ca-file', certfile, '-p', ticket ]
    else:
        args = [ 'spicec', '-h', host, '-p', str(port), '-s', str(secport),
                 '-w', ticket, '-t', title ]
    pid, pstdin = util.spawn(cmd, args, debug)
