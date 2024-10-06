from typing import Dict, Any

from pydantic import BaseModel
from pyrsistent import PClass, field


class Role(BaseModel):
    """Immutable Role class using Pydantic"""
    name: str = field(type=str)

    def to_dict(self) -> Dict[str, Any]:
        return {"name": self.name}
