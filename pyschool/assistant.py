# -*- coding: utf-8 -*-

import asyncio
# import os

from langchain.callbacks.manager import AsyncCallbackManager
from langchain.callbacks.streaming_stdout import StreamingStdOutCallbackHandler
from langchain.chains import LLMChain
from langchain.prompts import PromptTemplate
# from langchain_community.llms import HuggingFaceHub
from langchain_community.llms import Ollama

from data_test import display_placeholders


async def ollama():
    # Initialiser Ollama
    llm = Ollama(
        base_url="http://localhost:11434",  # Assurez-vous que cette URL correspond à votre configuration Ollama
        model="llama3.2:3b",  # Ou un autre modèle que vous avez installé
        callback_manager=AsyncCallbackManager([StreamingStdOutCallbackHandler()]),
    )

    # Créer un template de prompt
    prompt = PromptTemplate(
        input_variables=["question"],
        template="Répondez à la question suivante : {question}"
    )

    # Créer une chaîne LLM
    chain = LLMChain(llm=llm, prompt=prompt)

    # Boucle principale du chatbot
    while True:
        user_input = input("Vous: ")
        if user_input.lower() in ['quit', 'exit', 'bye']:
            print("Chatbot: Au revoir !")
            break

        # Appel asynchrone à la chaîne
        response = await chain.arun(question=user_input)
        print(f"Chatbot: {response.strip()}")


# Assurez-vous d'avoir défini votre token Hugging Face comme variable d'environnement
# os.environ["HUGGINGFACEHUB_API_TOKEN"] = "votre_token_ici"

# async def huggingface_chat():
#     # Initialiser le modèle Hugging Face
#     llm = HuggingFaceHub(
#         repo_id="google/flan-t5-xxl",  # Vous pouvez changer le modèle selon vos besoins
#         model_kwargs={"temperature": 0.5, "max_length": 512},
#         huggingfacehub_api_token=os.environ["HUGGINGFACEHUB_API_TOKEN"],
#         callback_manager=AsyncCallbackManager([StreamingStdOutCallbackHandler()]),
#     )
#
#     # Créer un template de prompt
#     prompt = PromptTemplate(
#         input_variables=["question"],
#         template="Répondez à la question suivante : {question}"
#     )
#
#     # Créer une chaîne LLM
#     chain = LLMChain(llm=llm, prompt=prompt)
#
#     # Boucle principale du chatbot
#     while True:
#         user_input = input("Vous: ")
#         if user_input.lower() in ['quit', 'exit', 'bye']:
#             print("Chatbot: Au revoir !")
#             break
#
#         # Appel asynchrone à la chaîne
#         response = await chain.arun(question=user_input)
#         print(f"Chatbot: {response.strip()}")


if __name__ == '__main__':
    display_placeholders()
    asyncio.run(ollama())
    # asyncio.run(huggingface_chat())
