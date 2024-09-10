import os
from unittest import TestCase, main

from assertpy import assert_that
from langchain_openai import ChatOpenAI

from assistant_utils import ASSISTANT_ENV, set_environment

from datasets import load_dataset

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
        print(dataset["train"][100])
        print(
            """"{'text': "Terrible movie. Nuff Said.<br /><br />
            These Lines are Just Filler. The movie was bad.
            Why I have to expand on that I don't know.
            This is already a waste of my time.
            I just wanted to warn others. Avoid this movie.
            The acting sucks and the writing is just moronic.
            Bad in every way.
            The only nice thing about the movie are Deniz Akkaya's breasts.
            Even that was ruined though by a terrible and unneeded rape scene.
            The movie is a poorly contrived and totally unbelievable piece of garbage.
            <br /><br />
            OK now I am just going to rag on IMDb for this stupid rule
            of 10 lines of text minimum.
            First I waste my time watching this offal.
            Then feeling compelled to warn others I create an account with IMDb
            only to discover that I have to write a friggen essay
            on the film just to express how bad I think it is. Totally unnecessary.",
            'label': 0}"""
        )

        if __name__ == '__main__':
            main()
