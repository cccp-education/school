# -*- coding: utf-8 -*-
from pydantic import BaseModel
from pyrsistent import m, PMap


class Signup(BaseModel):
    login: str
    password: str
    repassword: str
    email: str
    
    @classmethod
    def from_map(cls, data: PMap) -> 'Signup':
        if isinstance(data, PMap):
            # Convertit explicitement chaque clé-valeur pour les données Pyrsistent
            signup_dict = {
                'login': str(data['login']),
                'password': str(data['password']),
                'repassword': str(data['repassword']),
                'email': str(data['email'])
            }
            return cls(**signup_dict)
        raise ValueError("Les données d'entrée doivent être de type PMap")
    
    def to_map(self) -> PMap:
        # Convertit l'instance en PMap (dictionnaire immuable Pyrsistent)
        return m(**self.model_dump())

if __name__ == "__main__":
    # Création d'un objet avec des données Pyrsistent
    # Utilisation de m() avec des arguments nommés
    signup_data = m(
        login="johndoe",
        password="secret",
        repassword="secret",
        email="johndoe@example.com"
    )
    
    # Création d'une instance de Signup à partir des données Pyrsistent
    signup = Signup.from_map(signup_data)
    
    # Création d'une nouvelle instance avec un email modifié
    new_signup = signup.model_copy(update={"email": "janedoe@example.com"})
    
    print("Original signup:", signup)
    print("New signup:", new_signup)
    
    # Démonstration de la conversion retour vers Pyrsistent
    persistent_data = new_signup.to_map()
    print("Données Pyrsistent:", persistent_data)