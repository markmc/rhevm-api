#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import sys
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


class mybuild(build):

    def _topdir(self):
        if hasattr(self, 'c_topdir'):
            return self.c_topdir
        fname = __file__
        base, tail = os.path.split(fname)
        while base != '/':
            testname = os.path.join(base, 'setup.py')
            if os.access(testname, os.R_OK):
                break
            base, tail = os.path.split(base)
        else:
            raise DistutilsError, 'Could not determine source directory.'
        self.c_topdir = base
        return self.c_topdir

    def _store_version(self):
        srcdir = self._topdir()
        setup = os.path.join(srcdir, 'setup.py')
        version = os.path.join(srcdir, 'lib', 'rhev', '_version.py')
        try:
            st1 = os.stat(setup)
        except OSError:
            raise DistutilsError, 'Could not locate setup.py'
        try:
            st2 = os.stat(version)
        except OSError:
            st2 = None
        if st2 and st1.st_mtime < st2.st_mtime:
            print 'version file up to date'
            return
        contents = '# This is a geneated file - do not edit!\n'
        info = tuple(map(int, version_info['version'].split('.')))
        contents += 'version = %s\n' % repr(info)
        fout = file(version, 'w')
        fout.write(contents)
        fout.close()
        print 'version file created at %s' % version

    def _generate_schema(self):
        srcdir = self._topdir()
        xsd = os.path.join(srcdir, 'api.xsd')
        libdir = os.path.join(srcdir, 'lib', 'rhev')
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
            print 'schema up to date'
            return
        try:
            pipe = Popen(('pyxbgen', '-m', '_schema', '--binding-root',
                          libdir, xsd), stdout=PIPE, stderr=PIPE)
            pipe.communicate()
        except OSError:
            raise DistutilsError, 'Could not find pyxbgen.'
        if pipe.returncode:
            raise DistutilsError, 'Could not generate schema'
        print 'schema generated as %s' % schema

    def run(self):
        self._store_version()
        self._generate_schema()
        build.run(self)


setup(
    package_dir = { '': 'lib' },
    packages = [ 'rhev', 'rhev.test' ],
    cmdclass = { 'build': mybuild },
    install_requires = [ 'PyXB >= 1.1.0' ],
    test_suite = 'nose.collector',
    entry_points = {
        'nose.plugins.0.10': [ 'deploader = rhev.test.loader:DepLoader' ]
    },
    **version_info
)
