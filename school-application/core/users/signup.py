# -*- coding: utf-8 -*-
import json
from typing import Dict

import pandas as pd  # pip install pandas
import xmlschema  # pip install xmlschema
import yaml
from pydantic import BaseModel
from pyrsistent import m, PMap


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

    def to_json(self) -> str:
        """Convert a Pyrsistent Map to a JSON string."""
        return json.dumps(dict(**self.model_dump()))

    def to_xml(self, root_element: str = "signup") -> str:
        """Convert a Pyrsistent Map to an XML string."""
        xml_elements = []
        data = dict(**self.model_dump())
        for key, value in data.items():
            xml_elements.append(f"<{key}>{value}</{key}>")
        return f"<{root_element}>{''.join(xml_elements)}</{root_element}>"

    def to_schema(self, schema_type: str = "json") -> Dict:
        """Generate a JSON or YAML schema from a Pyrsistent Map."""
        schema = {
            "type": "object",
            "properties": {}
        }
        data = dict(**self.model_dump())
        for key, value in data.items():
            schema["properties"][key] = {"type": type(value).__name__}
        if schema_type == "yaml":
            return yaml.dump(schema)
        return schema

    def to_dtd(self, root_element: str = "signup") -> str:
        """Generate a DTD (Document Type Definition) string from the model."""
        dtd_elements = []
        data = dict(**self.model_dump())
        for key, value in data.items():
            dtd_elements.append(f"<!ELEMENT {key} (#PCDATA)>")
        return f"<!ELEMENT {root_element} ({' , '.join(dtd_elements)})>"
