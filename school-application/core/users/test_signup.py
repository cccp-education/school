# -*- coding: utf-8 -*-
import unittest

import pytest
from assertpy import assert_that
from rich import print
import io
import sys
import re
from hypothesis import given, strategies as st
from pyrsistent import m

from core.users.signup import Signup


# Constantes pour les tests
LOGIN_REGEX = r"^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$"
PASSWORD_REGEX = r"!@#$%^&*(),.?\":{}|<>"
PASSWORD_MIN_LENGTH = 8
PASSWORD_MAX_LENGTH = 50
LOGIN_MAX_LENGTH = 50

# Stratégies Hypothesis personnalisées
def valid_login():
    return st.from_regex(LOGIN_REGEX).filter(lambda x: 1 <= len(x) <= LOGIN_MAX_LENGTH)

def valid_password():
    # Stratégie pour générer des mots de passe valides
    return st.lists(
        st.one_of(
            st.characters(whitelist_categories=('Lu',)),  # Au moins une majuscule
            st.characters(whitelist_categories=('Ll',)),  # Au moins une minuscule
            st.characters(whitelist_categories=('Nd',)),  # Au moins un chiffre
            st.characters(whitelist_characters='!@#$%^&*(),.?":{}|<>')  # Caractère spécial
        ),
        min_size=PASSWORD_MIN_LENGTH,
        max_size=PASSWORD_MAX_LENGTH
    ).map(lambda x: ''.join(x)).filter(
        lambda p: any(c.isupper() for c in p) and
                  any(c.islower() for c in p) and
                  any(c.isdigit() for c in p) and
                  any(c in '!@#$%^&*(),.?":{}|<>' for c in p)
    )

def valid_email():
    return st.emails()


class TestSignup(unittest.TestCase):

    @pytest.mark.skip(reason="This test is not ready yet")
    @given(
        login=valid_login(),
        email=valid_email(),
        password=valid_password()
    )
    def test_valid_signup(self, login, email, password):
        # Création d'un objet Signup valide
        signup_data = m(
            login=login,
            password=password,
            repassword=password,
            email=email
        )

        signup = Signup.from_persistent(signup_data)

        # Vérifications
        assert_that(signup).is_not_none()
        assert_that(signup.login).is_equal_to(login)
        assert_that(signup.password).is_equal_to(password)
        assert_that(signup.email).is_equal_to(email)
        assert_that(signup.repassword).is_equal_to(signup.password)

    @pytest.mark.skip(reason="This test is not ready yet")
    @given(st.text(min_size=1))
    def test_invalid_login(self, invalid_login):
        if re.match(LOGIN_REGEX, invalid_login) and len(invalid_login) <= LOGIN_MAX_LENGTH:
            return  # Skip valid logins

        with self.assertRaises(ValueError):
            Signup.from_persistent(m(
                login=invalid_login,
                password="ValidP@ss1",
                repassword="ValidP@ss1",
                email="test@example.com"
            ))

    @pytest.mark.skip(reason="This test is not ready yet")
    @given(
        login=valid_login(),
        email=valid_email(),
        invalid_password=st.text(min_size=1, max_size=PASSWORD_MAX_LENGTH)
    )
    def test_invalid_password(self, login, email, invalid_password):
        # Si le mot de passe généré est valide par hasard, on skip le test
        if (len(invalid_password) >= PASSWORD_MIN_LENGTH and
                any(c.isupper() for c in invalid_password) and
                any(c.islower() for c in invalid_password) and
                any(c.isdigit() for c in invalid_password) and
                any(c in PASSWORD_REGEX for c in invalid_password) and
                login not in invalid_password):
            return

        with self.assertRaises(ValueError):
            Signup.from_persistent(m(
                login=login,
                password=invalid_password,
                repassword=invalid_password,
                email=email
            ))

    @pytest.mark.skip(reason="This test is not ready yet")
    @given(
        login=valid_login(),
        email=valid_email(),
        password=valid_password()
    )
    def test_password_mismatch(self, login, email, password):
        with self.assertRaises(ValueError):
            Signup.from_persistent(m(
                login=login,
                password=password,
                repassword=password + "different",
                email=email
            ))

    @pytest.mark.skip(reason="This test is not ready yet")
    @given(
        login=valid_login(),
        password=valid_password(),
        invalid_email=st.text().filter(lambda x: '@' not in x or '.' not in x)
    )
    def test_invalid_email(self, login, password, invalid_email):
        with self.assertRaises(ValueError):
            Signup.from_persistent(m(
                login=login,
                password=password,
                repassword=password,
                email=invalid_email
            ))

class TestGreetings(unittest.TestCase):
    def setUp(self):
        self.held_output = io.StringIO()
        sys.stdout = self.held_output

    def tearDown(self):
        sys.stdout = sys.__stdout__

    def test_greetings(self):
        print("Greeting world!")
        output = self.held_output.getvalue().strip()
        assert_that(output).is_equal_to("Greeting world!")



if __name__ == '__main__':
    unittest.main()

