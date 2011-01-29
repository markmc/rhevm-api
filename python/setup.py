#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import sys
import os
import os.path 

from setuptools import setup
from distutils.command.build import build
from distutils.errors import DistutilsError
from subprocess import Popen, PIPE

version_info = {
    'name': 'python-rhev',
    'version': '0.9',
    'description': 'A Python API for Red Hat Enterprise Virtualization',
    'author': 'Geert Jansen',
    'author_email': 'gjansen@redhat.com',
    'url': 'http://bitbucket.org/geertj/python-rhev',
    'license': 'MIT',
    'classifiers': [
        'Development Status :: 3 - Alpha',
        'Environment :: Console',
        'Intended Audience :: System Administrators',
        'License :: OSI Approved :: MIT License',
        'Operating System :: OS Independent',
        'Programming Language :: Python' ],
}

topdir = os.path.split(os.path.abspath(__file__))[0]
if topdir == os.getcwd():
    topdir = ''


class mybuild(build):

    def _generate_schema(self):
        xsd = os.path.normpath(os.path.join(topdir,
                    '../api/src/main/resources/api.xsd'))
        libdir = os.path.join(topdir, 'lib', 'rhev')
        schema = os.path.join(libdir, '_schema.py')
        try:
            st1 = os.stat(xsd)
        except OSError:
            raise DistutilsError, 'Could not find XMLSchema.'
        try:
            st2 = os.stat(schema)
        except OSError:
            st2 = None
        if st2 and st1.st_mtime < st2.st_mtime:
            print 'schema is up to date'
            return
        try:
            pipe = Popen(('pyxbgen', '-m', '_schema', '--binding-root',
                          libdir, xsd), stdout=PIPE, stderr=PIPE)
            pipe.communicate()
        except OSError:
            raise DistutilsError, 'Could not find pyxbgen.'
        if pipe.returncode:
            raise DistutilsError, 'Could not generate schema'
        print 'xmlschema input: %s' % xsd
        print 'schema generated as: %s' % schema

    def run(self):
        self._generate_schema()
        build.run(self)


setup(
    package_dir = { '': os.path.join(topdir, 'lib') },
    packages = [ 'rhev', 'rhev.test' ],
    cmdclass = { 'build': mybuild },
    install_requires = [ 'PyXB >= 1.1.0' ],
    test_suite = 'nose.collector',
    entry_points = {
        'nose.plugins.0.10': [ 'deploader = rhev.test.loader:DepLoader' ]
    },
    **version_info
)
