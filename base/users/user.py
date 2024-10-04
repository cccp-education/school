# # -*- coding: utf-8 -*-
# from uuid import UUID, uuid4
# from typing import Optional, Set, FrozenSet
# from pydantic import BaseModel, Field, EmailStr, constr
# from pyrsistent import PClass, field, pset
#
# # Constants
# ANONYMOUS_USER = "ANONYMOUS"
# EMPTY_STRING = ""
# LOGIN_REGEX = r"^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$"
#
# class Role(PClass):
#     """Immutable Role class using Pyrsistent"""
#     name: str = field(type=str)
#
# class User(BaseModel):
#     """User model with validation using Pydantic"""
#     id: Optional[UUID] = None
#     login: constr(regex=LOGIN_REGEX, min_length=1, max_length=50)
#     password: constr(min_length=60, max_length=60) = Field(default=EMPTY_STRING, exclude=True)
#     email: EmailStr = Field(default=EMPTY_STRING, max_length=254)
#     roles: FrozenSet[Role] = Field(
#         default_factory=lambda: frozenset({Role(name=ANONYMOUS_USER)}),
#         exclude=True
#     )
#     lang_key: constr(min_length=2, max_length=10) = Field(default=EMPTY_STRING)
#     version: int = Field(default=-1, exclude=True)
#
#     class Config:
#         frozen = True  # Make the model immutable
#         arbitrary_types_allowed = True  # Allow custom types like Role
#
#     def __init__(self, **data):
#         # Convert mutable set to frozenset for roles if provided
#         if "roles" in data and not isinstance(data["roles"], frozenset):
#             data["roles"] = frozenset(data["roles"])
#         super().__init__(**data)
#
#     @classmethod
#     def create_anonymous(cls) -> "User":
#         """Factory method to create an anonymous user"""
#         return cls(
#             id=uuid4(),
#             login="anonymous",
#             roles=frozenset({Role(name=ANONYMOUS_USER)})
#         )

from uuid import UUID, uuid4
from typing import Dict, ClassVar, Any, Optional, FrozenSet
from pydantic import BaseModel, Field, EmailStr, constr, field_validator
from pyrsistent import m, PMap
import json
import yaml
import xml.etree.ElementTree as ET
import re
import xmlschema
from datetime import datetime

from users.role import Role

# Constants
ANONYMOUS_USER = "ANONYMOUS"
EMPTY_STRING = ""


class User(BaseModel):
    """User model with validation using Pydantic and complete serialization capabilities"""
    id: Optional[UUID] = None
    login: str
    password: constr(min_length=60, max_length=60) = Field(default=EMPTY_STRING, exclude=True)
    email: EmailStr = Field(default=EMPTY_STRING, max_length=254)
    roles: FrozenSet[Role] = Field(
        default_factory=lambda: frozenset({Role(name=ANONYMOUS_USER)}),
        exclude=True
    )
    lang_key: constr(min_length=2, max_length=10) = Field(default=EMPTY_STRING)
    version: int = Field(default=-1, exclude=True)

    # Validation constants
    LOGIN_REGEX: ClassVar[str] = r"^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$"
    LOGIN_MAX_LENGTH: ClassVar[int] = 50
    EMAIL_MAX_LENGTH: ClassVar[int] = 254

    class Config:
        frozen = True
        arbitrary_types_allowed = True

    def __init__(self, **data):
        if "roles" in data and not isinstance(data["roles"], frozenset):
            data["roles"] = frozenset(data["roles"])
        super().__init__(**data)

    @field_validator('login')
    def validate_login(cls, v):
        if not v:
            raise ValueError("Le login ne peut pas être vide")
        if len(v) > cls.LOGIN_MAX_LENGTH:
            raise ValueError(f"Le login ne peut pas dépasser {cls.LOGIN_MAX_LENGTH} caractères")
        if not re.match(cls.LOGIN_REGEX, v):
            raise ValueError("Format de login invalide")
        if v.count('@') > 1:
            raise ValueError("Le login ne peut pas contenir plus d'un @")
        return v

    @classmethod
    def create_anonymous(cls) -> "User":
        """Factory method to create an anonymous user"""
        return cls(
            id=uuid4(),
            login="anonymous",
            roles=frozenset({Role(name=ANONYMOUS_USER)})
        )

    def to_dict(self) -> Dict[str, Any]:
        """Convert User instance to dictionary"""
        return {
            "id": str(self.id) if self.id else None,
            "login": self.login,
            "email": self.email,
            "langKey": self.lang_key,
            "roles": [role.to_dict() for role in self.roles],
            "version": self.version,
            "timestamp": datetime.now().isoformat()
        }

    @classmethod
    def from_persistent(cls, data: PMap) -> 'User':
        """Create User instance from Pyrsistent PMap"""
        if isinstance(data, PMap):
            # Convert PMap to dict with explicit type conversion
            user_dict = {
                'id': UUID(data['id']) if data.get('id') else None,
                'login': str(data['login']),
                'email': str(data.get('email', '')),
                'lang_key': str(data.get('lang_key', '')),
                'roles': frozenset(Role(name=str(r['name'])) for r in data.get('roles', []))
            }
            return cls(**user_dict)
        raise ValueError("Input data must be of type PMap")

    def to_persistent(self) -> PMap:
        """Convert User instance to Pyrsistent PMap"""
        return m(**self.to_dict())

    def to_json(self, pretty: bool = False) -> str:
        """Convert User instance to JSON string"""
        dict_data = self.to_dict()
        if pretty:
            return json.dumps(dict_data, indent=2, ensure_ascii=False)
        return json.dumps(dict_data, ensure_ascii=False)

    def to_yaml(self) -> str:
        """Convert User instance to YAML string"""
        dict_data = self.to_dict()
        return yaml.dump(dict_data, allow_unicode=True, sort_keys=False)

    def to_xml(self, root_element: str = "user", pretty: bool = False) -> str:
        """Convert User instance to XML string"""
        root = ET.Element(root_element)

        def add_element(parent: ET.Element, name: str, value: Any):
            if value is not None:
                elem = ET.SubElement(parent, name)
                elem.text = str(value)

        data = self.to_dict()
        for key, value in data.items():
            if key == "roles":
                roles_elem = ET.SubElement(root, "roles")
                for role in self.roles:
                    role_elem = ET.SubElement(roles_elem, "role")
                    add_element(role_elem, "name", role.name)
            else:
                add_element(root, key, value)

        xml_str = ET.tostring(root, encoding="unicode")
        if pretty:
            import xml.dom.minidom
            return xml.dom.minidom.parseString(xml_str).toprettyxml(indent="  ")
        return xml_str

    def to_schema(self, schema_type: str = "json") -> Dict:
        """Generate a JSON or YAML schema"""
        schema = {
            "type": "object",
            "properties": {
                "id": {"type": "string", "format": "uuid"},
                "login": {"type": "string", "pattern": self.LOGIN_REGEX},
                "email": {"type": "string", "format": "email"},
                "langKey": {"type": "string"},
                "roles": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "name": {"type": "string"}
                        }
                    }
                },
                "version": {"type": "integer"},
                "timestamp": {"type": "string", "format": "date-time"}
            }
        }

        if schema_type == "yaml":
            return yaml.dump(schema)
        return schema

    def to_dtd(self, root_element: str = "user") -> str:
        """Generate a DTD (Document Type Definition)"""
        dtd_elements = [
            f"<!ELEMENT {root_element} (id, login, email, langKey, roles, version, timestamp)>",
            "<!ELEMENT id (#PCDATA)>",
            "<!ELEMENT login (#PCDATA)>",
            "<!ELEMENT email (#PCDATA)>",
            "<!ELEMENT langKey (#PCDATA)>",
            "<!ELEMENT roles (role*)>",
            "<!ELEMENT role (name)>",
            "<!ELEMENT name (#PCDATA)>",
            "<!ELEMENT version (#PCDATA)>",
            "<!ELEMENT timestamp (#PCDATA)>"
        ]
        return "\n".join(dtd_elements)

    def to_xsd(self, root_element: str = "user") -> str:
        """Generate an XML Schema (XSD)"""
        xsd_schema = f"""<?xml version="1.0" encoding="UTF-8"?>
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xs:element name="{root_element}">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="id" type="xs:string" minOccurs="0"/>
                <xs:element name="login" type="xs:string"/>
                <xs:element name="email" type="xs:string"/>
                <xs:element name="langKey" type="xs:string"/>
                <xs:element name="roles">
                    <xs:complexType>
                        <xs:sequence>
                            <xs:element name="role" minOccurs="0" maxOccurs="unbounded">
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name="name" type="xs:string"/>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
                <xs:element name="version" type="xs:integer"/>
                <xs:element name="timestamp" type="xs:string"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>
</xs:schema>"""

        # Validate the generated schema
        try:
            xmlschema.XMLSchema(xsd_schema)
        except Exception as e:
            raise ValueError(f"Generated XSD schema is invalid: {str(e)}")

        return xsd_schema