# -*- coding: utf-8 -*-
import os
from config import OPENAI_API_KEY, LANGCHAIN_TRACING_V2, LANGCHAIN_API_KEY

ASSISTANT_ENV = {
    "OPENAI_API_KEY": OPENAI_API_KEY,
    "LANGCHAIN_TRACING_V2": LANGCHAIN_TRACING_V2,
    "LANGCHAIN_API_KEY": LANGCHAIN_API_KEY
}


def set_environment():
    diff = {key: value for key, value in ASSISTANT_ENV.items() if key not in os.environ}
    if len(diff) > 0:
        os.environ.update(diff)
