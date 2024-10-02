# -*- coding: utf-8 -*-
"""
School FastAPI application.
"""

from typing import Union

import uvicorn
from fastapi import FastAPI

api = FastAPI()


@api.get("/")
async def read_root():
    return {"Hello": "World"}


@api.get("/items/{item_id}")
async def read_item(item_id: int, q: Union[str, None] = None):
    return {"item_id": item_id, "q": q}


if __name__ == "__main__":
    uvicorn.run(
        'api:api',
        reload=True,
        port=8888
    )
