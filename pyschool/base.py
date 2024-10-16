# -*- coding: utf-8 -*-
from pyrsistent import m

from users.signup import Signup

if __name__ == "__main__":
    # Création d'un objet avec des données Pyrsistent
    signup_data = m(
        login="johndoe",
        password="secret",
        repassword="secret",
        email="johndoe@example.com"
    )

    # Création d'une instance de Signup à partir des données Pyrsistent
    signup = Signup.from_persistent(signup_data)

    # Création d'une nouvelle instance avec un email modifié
    # Utilisation de copy qui a été vérifié comme fonctionnel
    new_signup = signup.copy(update={"email": "janedoe@example.com"})

    print("Original signup:", signup)
    print("New signup:", new_signup)

    # Démonstration de la conversion retour vers Pyrsistent
    persistent_data = new_signup.to_persistent()
    print("Données Pyrsistent:", persistent_data)

    print(f"to_json : {new_signup.to_json()}")

    print(f"to_xml : {new_signup.to_xml()}")

    print(f"to_schema json : {new_signup.to_schema()}")

    print(f"to_schema yaml : {new_signup.to_schema("yaml")}")

    print(f"to_dtd : {new_signup.to_dtd()}")

    print(f"to_xsd : {new_signup.to_xsd()}")


