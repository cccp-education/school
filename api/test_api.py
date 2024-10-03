# -*- coding: utf-8 -*-
# noinspection PyUnresolvedReferences
import unittest
from assertpy import assert_that


class SchoolTestCase(unittest.TestCase):
    @staticmethod
    def test_something():
        assert_that(True).is_not_equal_to(False)  # add assertion here


if __name__ == '__main__':
    unittest.main()
