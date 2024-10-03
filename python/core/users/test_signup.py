# -*- coding: utf-8 -*-
import io
import sys
import unittest

import pytest
from assertpy import assert_that
from hypothesis import given, strategies as st
from pyrsistent import m

from core.users.signup import Signup

if __name__ == '__main__':
    unittest.main()

# Stratégies Hypothesis améliorées
@st.composite
def valid_login_strategy(draw):
    """Génère des logins valides selon les critères définis"""
    return draw(st.from_regex(Signup.LOGIN_REGEX)
                .filter(lambda x: 1 <= len(x) <= Signup.LOGIN_MAX_LENGTH))


@st.composite
def valid_password_strategy(draw):
    """Génère des mots de passe valides selon les critères de sécurité"""
    chars = (
        st.characters(whitelist_categories=('Lu',)),  # Majuscules
        st.characters(whitelist_categories=('Ll',)),  # Minuscules
        st.characters(whitelist_categories=('Nd',)),  # Chiffres
        st.characters(whitelist_characters=Signup.PASSWORD_REGEX)  # Caractères spéciaux
    )

    # Assure au moins un caractère de chaque type
    password = [draw(char) for char in chars]

    # Complète jusqu'à la longueur minimale
    remaining_length = draw(st.integers(min_value=max(0, Signup.PASSWORD_MIN_LENGTH - len(password)),
                                        max_value=Signup.PASSWORD_MAX_LENGTH - len(password)))
    password.extend(draw(st.lists(st.one_of(*chars),
                                  min_size=remaining_length,
                                  max_size=remaining_length)))

    return ''.join(password)


class TestSignup(unittest.TestCase):

    def setUp(self):
        """Initialisation avant chaque test"""
        self.valid_data = m(
            login="john_doe",
            password="ValidP@ss1",
            repassword="ValidP@ss1",
            email="john.doe@example.com"
        )

    def test_to_json(self):
        """Test la sérialisation en JSON"""
        signup = Signup.from_persistent(self.valid_data)
        json_data = signup.to_json()

        assert_that(json_data).contains("john_doe")
        assert_that(json_data).contains("john.doe@example.com")

    def test_to_xml(self):
        """Test la sérialisation en XML"""
        signup = Signup.from_persistent(self.valid_data)
        xml_data = signup.to_xml()

        assert_that(xml_data).contains("<signup>")
        assert_that(xml_data).contains("<login>john_doe</login>")
        assert_that(xml_data).contains("</signup>")

    def test_to_schema(self):
        """Test la génération de schéma JSON"""
        signup = Signup.from_persistent(self.valid_data)
        schema = signup.to_schema()

        assert_that(schema).contains_key("properties")
        assert_that(schema["properties"]).contains_key("login")
        assert_that(schema["properties"]).contains_key("email")

    @pytest.mark.skip(reason="This test is not ready yet")
    def test_invalid_login_format(self):
        """Test le rejet des logins invalides"""
        invalid_logins = [
            "invalid@login@example.com",  # Double @
            "user name",                  # Espace non autorisé
            "a" * 51,                     # Trop long
            "",                           # Vide
            "@invalid",                   # @ au début
            "invalid@"                    # @ à la fin
        ]

        for invalid_login in invalid_logins:
            invalid_data = self.valid_data.set("login", invalid_login)
            with self.assertRaises(ValueError) as context:
                Signup.from_persistent(invalid_data)
            assert_that(str(context.exception)).contains("login")

    @pytest.mark.skip(reason="This test is not ready yet")
    def test_invalid_login_format(self):
        """Test le rejet des logins invalides"""
        invalid_data = self.valid_data.set("login", "invalid@login@example.com")

        with self.assertRaises(ValueError) as context:
            Signup.from_persistent(invalid_data)

        assert_that(str(context.exception)).contains("login")


    @pytest.mark.skip(reason="This test is not ready yet")
    @given(
        login=valid_login_strategy(),
        email=st.emails(),
        password=valid_password_strategy()
    )
    def test_valid_signup(self, login, email, password):
        """Test la création d'un signup valide"""
        signup_data = m(
            login=login,
            password=password,
            repassword=password,
            email=email
        )

        signup = Signup.from_persistent(signup_data)

        assert_that(signup.login).is_equal_to(login)
        assert_that(signup.password).is_equal_to(password)
        assert_that(signup.email).is_equal_to(email)
        assert_that(signup.repassword).is_equal_to(password)



    @pytest.mark.skip(reason="This test is not ready yet")
    def test_password_complexity(self):
        """Test les règles de complexité des mots de passe"""
        simple_password = "simple123"
        invalid_data = self.valid_data.set("password", simple_password) \
            .set("repassword", simple_password)

        with self.assertRaises(ValueError) as context:
            Signup.from_persistent(invalid_data)

        assert_that(str(context.exception)).contains("password")

    @pytest.mark.skip(reason="This test is not ready yet")
    def test_password_mismatch(self):
        """Test la correspondance des mots de passe"""
        mismatched_data = self.valid_data.set("repassword", "DifferentP@ss1")

        with self.assertRaises(ValueError) as context:
            Signup.from_persistent(mismatched_data)

        assert_that(str(context.exception)).contains("password")

    @pytest.mark.skip(reason="This test is not ready yet")
    def test_invalid_email(self):
        """Test le format d'email"""
        invalid_data = self.valid_data.set("email", "invalid-email")

        with self.assertRaises(ValueError) as context:
            Signup.from_persistent(invalid_data)

        assert_that(str(context.exception)).contains("email")



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
