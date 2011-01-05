#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import inspect

from nose import SkipTest
from nose.case import MethodTestCase, FunctionTestCase, Test
from nose.plugins.base import Plugin


def depends(*tests):
    def _f(func):
        func.depends = list(tests)
        return func
    return _f

def _skip_test(reason):
    def _f():
        raise SkipTest(reason)
    return _f

class DepLoader(Plugin):
    """A nose plugin that can annotate tests with dependencies to other tests.
    A dependent test is run only if its depedencies succeed. Furthermore the
    tests are run in the order in which they appear in the source file, so
    that the programmer can order dependent tests appropriately."""

    name = 'deploader'

    def configure(self, options, config):
        self.enabled = True

    def prepareTestLoader(self, loader):
        self.loader = loader

    def _get_func(self, test):
        if not hasattr(test, 'test'):
            func = None
        elif hasattr(test.test, 'method'):
            func = test.test.method.im_func
        elif hasattr(test.test, 'func_code'):
            func = test.test
        else:
            func = None
        return func
        
    def addError(self, test, err):
        func = self._get_func(test)
        if func is None:
            return
        func.status = 'error'

    def addSkip(self, test):
        func = self._get_func(test)
        if func is None:
            return
        func.status = 'skipped'

    def addFailure(self, test, err):
        func = self._get_func(test)
        if func is None:
            return
        func.status = 'failure'

    def addSuccess(self, test):
        func = self._get_func(test)
        if func is None:
            return
        func.status = 'success'

    def describeTest(self, test):
        # Generated tests are not correctly formatted in Nose. They refer
        # to the generator name and not the test method name.
        if not hasattr(test.test, 'method'):
            return
        method = test.test.method
        desc = '%s:%s.%s' % (method.im_class.__module__,
                method.im_class.__name__, method.__name__)
        arg = test.test.arg
        if arg:
            desc += str(arg)
        return desc
    
    def beforeTest(self, test):
        func = self._get_func(test)
        if func is None:
            return
        if not hasattr(func, 'depends'):
            return
        for t in func.depends:
            status = getattr(t, 'status', 'success')
            if status == 'success':
                continue
            name = t.__name__
            test.test._skip_test = _skip_test('due to dependency: %s' % name)
            test.test._testMethodName = '_skip_test'
            func.status = 'skipped'
            break

    def wantMethod(self, method):
        return False  # only load via this plugin

    def _sort_on_lineno(self, t1, t2):
        return cmp(t1.im_func.func_code.co_firstlineno,
                   t2.im_func.func_code.co_firstlineno)

    def loadTestsFromTestClass(self, cls):
        tests = []
        for name in dir(cls):
            obj = getattr(cls, name)
            if not inspect.ismethod(obj) or \
                    name.startswith('_') or \
                    not self.loader.selector.matches(name):
                continue
            tests.append(obj)
        tests.sort(self._sort_on_lineno)
        tests = [self.loader.makeTest(t) for t in tests ]
        return tests
