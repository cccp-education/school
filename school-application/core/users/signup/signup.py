# -*- coding: utf-8 -*-
from pyrsistent import m, PMap
from pydantic import BaseModel
from typing import Dict, Any

class Signup(BaseModel):
    login: str
    password: str
    repassword: str
    email: str

    @classmethod
    def from_persistent(cls, data: PMap) -> 'Signup':
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

    def to_persistent(self) -> PMap:
        # Utilise model_dump() pour Pydantic v2
        return m(**self.model_dump())

