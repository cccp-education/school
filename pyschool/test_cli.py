# -*- coding: utf-8 -*-
import unittest

from assertpy import assert_that
from rich import print
from typer.testing import CliRunner

from cli import cli, status

runner = CliRunner()
INSTALL_PATH = ""


class CLITestCase(unittest.TestCase):

    def test_greetings(self):
        print(f"test cli : greetings!")
        result = runner.invoke(cli)
        assert_that(result.exit_code).is_equal_to(0)
        assert_that(result.stdout).contains(status())

    def test_install(self):
        result = runner.invoke(cli, ["install"])
        assert_that(result.exit_code).is_equal_to(0)
        assert_that(result.stdout).contains("Installation : catalogue formations")


if __name__ == '__main__':
    unittest.main()
