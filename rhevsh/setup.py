#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

import os
import sys

from distutils.command.build import build
from setuptools import setup, Command


version_info = {
    'name': 'rhevsh',
    'version': '0.9-SNAPSHOT',
    'description': 'A command-line interface to Red Hat Enterprise'
                   ' Virtualization',
    'author': 'Geert Jansen',
    'author_email': 'gjansen@redhat.com',
    'url': 'http://fedorahosted.org/rhevm-api',
    'license': 'MIT',
    'classifiers': [
        'Development Status :: 4 - Beta',
        'Environment :: Console',
        'Intended Audience :: System Administrators',
        'License :: OSI Approved :: MIT License',
        'Operating System :: OS Independent',
        'Programming Language :: Python :: 2.4' ],
}


setup(
    package_dir = { '': 'lib' },
    packages = [ 'rhevsh', 'rhevsh.command', 'rhevsh.format',
                 'rhevsh.platform', 'rhevsh.platform.posix',
                 'rhevsh.platform.windows' ],
    install_requires = [ 'python-cli >= 1.1', 'python-rhev >= 0.9', ],
    entry_points = { 'console_scripts': [ 'rhevsh = rhevsh.main:main' ] },
    **version_info
)
