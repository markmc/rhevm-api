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
    'version': '0.9',
    'description': 'A command-line interface to Red Hat Enterprise'
                   ' Virtualization',
    'author': 'Geert Jansen',
    'author_email': 'gjansen@redhat.com',
    'url': 'http://bitbucket.org/geertj/rhevsh',
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
    packages = [ 'rhevsh', 'rhevsh.command', 'rhevsh.format' ],
    # XXX: this is a horrible hack. Add pyxbbase as a dependency here, even
    # though it's only a transitive dependency. This ensures it is installed
    # before python-rhev. The python-rhev setup script requires pyxb to be
    # installed, and normally setuptools has a install_requires argument for
    # that. However, there's a bug with that.. For now, this workaround allows
    # a 1-line install of rhevsh via "easy_install rhevsh". Note also that
    # dependencies seem to be processed starting at the end of
    # install_requires. The real fix is to investigate why install_requires
    # doesn't work for python-rhev.
    install_requires = [ 'python-cli >= 1.1', 'python-rhev >= 0.9', 'pyxbbase' ],
    entry_points = { 'console_scripts': [ 'rhevsh = rhevsh.main:main' ] },
    **version_info
)
