# -*- coding: utf-8 -*-
import json
from typing import Dict, ClassVar
import re

import pandas as pd  # pip install pandas
import xmlschema  # pip install xmlschema
import yaml
from pydantic import BaseModel, validator
from pyrsistent import m, PMap

class Signup(BaseModel):
    login: str
    password: str
    repassword: str
    email: str

    # Constantes pour la validation
    LOGIN_REGEX: ClassVar[str] = r"^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$"
    LOGIN_MAX_LENGTH: ClassVar[int] = 50

    PASSWORD_REGEX: ClassVar[str] = r"!@#$%^&*(),.?\":{}|<>"
    PASSWORD_MIN_LENGTH: ClassVar[int] = 8
    PASSWORD_MAX_LENGTH: ClassVar[int] = 50

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
        return m(**self.dict())

    def to_json(self) -> str:
        """Convert a Pyrsistent Map to a JSON string."""
        return json.dumps(dict(**self.dict()))

    def to_xml(self, root_element: str = "signup") -> str:
        """Convert a Pyrsistent Map to an XML string."""
        xml_elements = []
        data = dict(**self.dict())
        for key, value in data.items():
            xml_elements.append(f"<{key}>{value}</{key}>")
        return f"<{root_element}>{''.join(xml_elements)}</{root_element}>"

    def to_schema(self, schema_type: str = "json") -> Dict:
        """Generate a JSON or YAML schema from a Pyrsistent Map."""
        schema = {
            "type": "object",
            "properties": {}
        }
        data = dict(**self.dict())
        for key, value in data.items():
            schema["properties"][key] = {"type": type(value).__name__}
        if schema_type == "yaml":
            return yaml.dump(schema)
        return schema

    def to_dtd(self, root_element: str = "signup") -> str:
        """Generate a DTD (Document Type Definition) string from the model."""
        dtd_elements = []
        data = dict(**self.dict())
        for key, value in data.items():
            dtd_elements.append(f"<!ELEMENT {key} (#PCDATA)>")
        return f"<!ELEMENT {root_element} ({' , '.join(dtd_elements)})>"

    def to_xsd(self, root_element: str = "signup") -> str:
        """Generate an XML Schema (XSD) string from the model.

        Args:
            root_element (str): The name of the root element in the schema

        Returns:
            str: The generated XSD schema as a string
        """
        # Convertir le modèle en DataFrame pandas pour faciliter la manipulation
        data = dict(**self.dict())
        df = pd.DataFrame([data])

        # Mapper les types Python vers les types XSD
        xsd_type_mapping = {
            'str': 'xs:string',
            'int': 'xs:integer',
            'float': 'xs:decimal',
            'bool': 'xs:boolean',
            'datetime': 'xs:dateTime'
        }

        # Générer les éléments du schéma
        elements = []
        for column in df.columns:
            python_type = type(data[column]).__name__
            xsd_type = xsd_type_mapping.get(python_type, 'xs:string')
            element = f"""
                <xs:element name="{column}" type="{xsd_type}">
                    <xs:annotation>
                        <xs:documentation>Field: {column}</xs:documentation>
                    </xs:annotation>
                </xs:element>"""
            elements.append(element)

        # Construire le schéma XSD complet
        xsd_schema = f"""<?xml version="1.0" encoding="UTF-8"?>
    <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
        <xs:element name="{root_element}">
            <xs:complexType>
                <xs:sequence>
                    {''.join(elements)}
                </xs:sequence>
            </xs:complexType>
        </xs:element>
    </xs:schema>"""

        # Valider le schéma généré avec xmlschema
        try:
            xmlschema.XMLSchema(xsd_schema)
        except Exception as e:
            raise ValueError(f"Le schéma XSD généré n'est pas valide : {str(e)}")

        return xsd_schema

    @validator('login')
    def validate_login(cls, v):
        if not v:
            raise ValueError("Le login ne peut pas être vide")
        if len(v) > cls.LOGIN_MAX_LENGTH:
            raise ValueError(f"Le login ne peut pas dépasser {cls.LOGIN_MAX_LENGTH} caractères")
        if not re.match(cls.LOGIN_REGEX, v):
            raise ValueError("Format de login invalide")
        if v.count('@') > 1:  # Vérifie spécifiquement le cas du double @
            raise ValueError("Le login ne peut pas contenir plus d'un @")
        return v
