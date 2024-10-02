# -*- coding: utf-8 -*-
from pyrsistent import m

from core.users.signup.signup import Signup

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
    # Utilisation de model_copy qui a été vérifié comme fonctionnel
    new_signup = signup.model_copy(update={"email": "janedoe@example.com"})

    print("Original signup:", signup)
    print("New signup:", new_signup)

    # Démonstration de la conversion retour vers Pyrsistent
    persistent_data = new_signup.to_persistent()
    print("Données Pyrsistent:", persistent_data)