# -*- coding: utf-8 -*-

import json

from datasets import load_dataset

from data_test import display_placeholders

dataset = load_dataset("imdb")


def format_dataset_to_json(dataset, index, keys=None):
    """
    Formats a specific element from a Hugging Face dataset to JSON.

    Args:
        dataset: The Hugging Face dataset.
        index: The index of the element to format.
        keys: A list of keys to include in the JSON output. If None, all keys will be included.

    Returns:
        A JSON string representing the formatted element.
    """
    data = dataset["train"][index]  # Assuming it's a dictionary
    # Create a new dictionary with specified or all keys
    if keys:
        dataset_dict = {key: data.get(key) for key in keys}
    else:
        dataset_dict = {key: value for key, value in data.items()}
    # Convert the dictionary to JSON
    json_data: str = json.dumps(dataset_dict)
    return json_data


if __name__ == '__main__':
    display_placeholders()
    json_string = format_dataset_to_json(
        dataset, 100,
        keys=["text", "label"]
    )
    print(json_string)
