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
