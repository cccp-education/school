# -*- coding: utf-8 -*-

from datasets import load_dataset

dataset = load_dataset("imdb")

if __name__ == '__main__':
    for placeholder in [
        "foo", "bar", "baz", "qux", "quux",
        "corge", "grault", "garply", "waldo",
        "fred", "plugh", "xyzzy", "thud"
    ]: print(placeholder.title())
