# -*- coding: utf-8 -*-
import unittest

# from assertpy import assert_that
from rich import print

INSTALL_PATH = ""


class AssistantTestCase(unittest.TestCase):

    def test_greetings(self):
        print(f"test cli : greetings!")
        # result = runner.invoke(cli)
        # assert_that(result.exit_code).is_equal_to(0)
        # assert_that(result.stdout).contains(status())

if __name__ == '__main__':
    unittest.main()
