# -*- coding: utf-8 -*-
"""
School FastAPI application.
"""

from typing import Union

import uvicorn
from fastapi import FastAPI

api = FastAPI()

@api.post("/users/signup/{signup}")
async def signup(signup_id: int, q: Union[str, None] = None):
    return {"signup_id": signup_id, "q": q}

if __name__ == "__main__":
    uvicorn.run('api:api', reload=True, port=8888)
