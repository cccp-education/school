# -*- coding: utf-8 -*-
import unittest
from assertpy import assert_that

import unittest
from fastapi.testclient import TestClient
from pydantic import ValidationError

from api import api
# Assuming a Signup model with appropriate fields like login, password, etc.
from users.signup import Signup  # Replace with the actual path to your model


class ApiTestCase(unittest.TestCase):
    @staticmethod
    def test_something():
        assert_that(True).is_not_equal_to(False)  # add assertion here

    def setUp(self) -> None:
        self.client = TestClient(api)

    def test_signup_valid_data(self):
        valid_data = {
            "login": "johndoe",
            "email":"johndoe@example.com",
            "password": "strong_password123",
            "repassword":"secret",
        }
        response = self.client.post("/users/signup", json=valid_data)
        self.assertEqual(response.status_code, 201)  # Created status code

    def test_signup_missing_login(self):
        invalid_data = {
            "password": "strong_password123",
            # Other required fields for Signup (except login)
        }
        with self.assertRaises(ValidationError):
            self.client.post("/users/signup", json=invalid_data)

    def test_signup_invalid_login_format(self):
        invalid_data = {
            "login": "invalid_username",  # Missing "@" or "."
            "password": "strong_password123",
            # Other required fields for Signup
        }
        with self.assertRaises(ValidationError):
            self.client.post("/users/signup", json=invalid_data)

    def test_signup_short_password(self):
        invalid_data = {
            "login": "valid_username@example.com",
            "password": "short",  # Less than minimum password length
            # Other required fields for Signup
        }
        with self.assertRaises(ValidationError):
            self.client.post("/users/signup", json=invalid_data)

    # Add more tests for other edge cases like duplicate logins, etc.

if __name__ == '__main__':
    unittest.main()