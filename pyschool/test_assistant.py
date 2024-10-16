# -*- coding: utf-8 -*-
import os
from unittest import TestCase, main

from assertpy import assert_that
from datasets import load_dataset
from langchain_openai import ChatOpenAI

from dataset import format_dataset_to_json
from assistant_utils import ASSISTANT_ENV, set_environment
from config import OPENAI_API_KEY


class AssistantTestCase(TestCase):

    @staticmethod
    def test_canary():
        print("canary")

    # # avec la boucle for
    # @staticmethod
    # def test_environment_contains_assistant_env_after_set_environment():
    #     for key, value in ASSISTANT_ENV.items():
    #         assert_that(os.environ).does_not_contain(key)
    #     set_environment()
    #     # Vérifier que chaque clé de ASSISTANT_ENV est présente dans os.environ avec la valeur correcte
    #     for key, value in ASSISTANT_ENV.items():
    #         assert_that(os.environ).contains_key(key)
    #         assert_that(os.environ[key]).is_equal_to(value)

    @staticmethod
    def test_environment_contains_assistant_env_after_set_environment_functionnal():
        # Vérifier que aucune clé de ASSISTANT_ENV est présente dans os.environ avant set_environment
        assert_that(list(map(lambda key: key in os.environ,
                             ASSISTANT_ENV.keys()))
                    ).is_equal_to([False] * len(ASSISTANT_ENV))

        set_environment()
        # Vérifier que chaque clé de ASSISTANT_ENV est présente dans os.environ avec la valeur correcte
        assert_that(list(map(lambda key: os.environ[key] == ASSISTANT_ENV[key],
                             ASSISTANT_ENV.keys()))
                    ).is_equal_to([True] * len(ASSISTANT_ENV))

    @staticmethod
    def test_openapi_chat_connection():
        if OPENAI_API_KEY not in os.environ:
            set_environment()
        assert_that(OPENAI_API_KEY).is_not_empty()
        assert_that(ASSISTANT_ENV).contains_key("OPENAI_API_KEY")
        ChatOpenAI()

    @staticmethod
    def test_dataset():
        dataset = load_dataset("imdb")
        assert_that(format_dataset_to_json(
            dataset,
            100, keys=["text", "label"])
        ).is_not_empty()


if __name__ == '__main__':
    main()
