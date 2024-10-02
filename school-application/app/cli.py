# -*- coding: utf-8 -*-
import json
import os
from typing import Optional

import typer
from fabric import task
from typing_extensions import Annotated

# //TODO
# // # si rien alors greetings current system user
# // # sinon avec premier parametre contient install
# // # tester le nom de l'utilisateur courrant n'est pas vide
# // # tester que les dossiers sur le chemin par defaut existe, puis les fichiers concernés par l'installation.
# // # dockeriser, lancer l'image en local, executer l'image dans une aws lambda mock api,
# // # pousser sur dockerhub, et le package sur pipy

cli = typer.Typer()


def status():
    SCHOOL_GREETINGS = "Welcome to School-CLI application!"
    return SCHOOL_GREETINGS


@cli.command()
def command_processor(cmd: Annotated[Optional[str], typer.Argument()] = None):
    if cmd is None:
        typer.echo(status())
    else:
        match cmd:
            case "install":
                typer.echo(f"Installation : catalogue formations")


class DirectoryStructure:
    def __init__(self, files=None, directories=None):
        self.files = files if files is not None else []
        self.directories = directories if directories is not None else {}


class Formation:
    JSON_FILE = "patron-formation.json"
    ROOT_NODE = "formation"


def create_structure(base_dir, structure):
    if structure is None:
        return

    # Créer les fichiers
    for file_name in structure.files:
        file_path = os.path.join(base_dir, file_name)
        if not os.path.exists(file_path):
            open(file_path, 'w').close()  # Crée un nouveau fichier vide

    # Créer les répertoires et leur contenu
    for dir_name, sub_structure in structure.directories.items():
        dir_path = os.path.join(base_dir, dir_name)
        if not os.path.exists(dir_path):
            os.makedirs(dir_path)  # Crée le répertoire
        create_structure(dir_path, sub_structure)


@task
def create_patron_formation(c):
    # Lire le fichier JSON et charger la structure
    with open(Formation.JSON_FILE, 'r') as f:
        structure_dict = json.load(f)

    formation_structure = structure_dict.get(Formation.ROOT_NODE)

    # Convertir la structure en objets DirectoryStructure
    def dict_to_structure(d):
        return DirectoryStructure(
            files=d.get("files", []),
            directories={k: dict_to_structure(v) for k, v in d.get("directories", {}).items()}
        )

    formation_structure = dict_to_structure(formation_structure)

    # Créer le répertoire racine et commencer à créer l'arborescence
    root_dir = os.path.join(os.getcwd(), "build", Formation.ROOT_NODE)
    if not os.path.exists(root_dir):
        os.makedirs(root_dir)

    create_structure(root_dir, formation_structure)


if __name__ == "__main__":
    cli()
