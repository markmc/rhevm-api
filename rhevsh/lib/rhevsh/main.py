#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

import os
import sys

from rhevsh.object import create
from rhevsh.options import RhevshOptionParser
from rhevsh.context import RhevshExecutionContext


def copy_environment_vars(context):
    """Copy environment variables into configuration variables in the
    execution context."""
    for var in ('url', 'username', 'password'):
        envvar = 'RHEV_%s' % var.upper()
        if envvar in os.environ:
            try:
                context.settings[var] = os.environ[envvar]
            except ValueError, e:
                sys.stderr.write('error: %s\n' % str(e))
                return False
    return True


def copy_cmdline_options(options, context, parser):
    """Copy command-line options into configuration variables in the
    execution context."""
    for opt in parser.option_list:
        if not opt.dest:
            continue
        value = getattr(options, opt.dest)
        if value is None:
            continue
        try:
            context.settings[opt.dest] = value
        except KeyError:
            pass
        except ValueError, e:
            sys.stderr.write('error: %s\n' % str(e))
            return False
    return True


def main():
    """Entry point for rhevsh."""
    parser = create(RhevshOptionParser)
    opts, args = parser.parse_args()

    context = create(RhevshExecutionContext)
    if not copy_environment_vars(context):
        sys.exit(1)
    if not copy_cmdline_options(opts, context, parser):
        sys.exit(1)

    if opts.help_commands:
        args = ['help']

    if opts.filter:
        try:
            sys.stdin = file(opts.filter)
        except IOError, e:
            sys.stderr.write('error: %s\n' % e.strerror)
            sys.exit(1)

    if opts.connect or len(args) > 0:
        context.execute_string('connect\n')

    if len(args) == 0:
        context.execute_loop()
    else:
        command = ' '.join(args)
        if opts.read_input:
            buf = sys.stdin.read()
            command += '<<EOM\n%s\nEOM' % buf
        command += '\n'
        context.execute_string(command)

    sys.exit(context.status)
