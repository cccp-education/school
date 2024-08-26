interface Pokemon {
    id: number;
    name: string;
}

type Result<T, E> = { type: "success"; value: T } 
| { type: "error"; error: E };

interface Cursor {
    position: number;
    pokemons: Pokemon[];
}

const fetchPokemonList = async (): Promise<Result<any[], string>> => {
    return fetch(`https://pokeapi.co/api/v2/ability/?limit=358&offset=0`)
        .then(response =>
            response.ok
                ? response.json()
                : Promise.reject(`Network response was not ok: ${response.status}`)
        )
        .then(data => ({ type: "success", value: data.results }))
        .catch(error => ({ type: "error", error: error.message }));
};

const fetch_pokemons = async (): Promise<Array<Pokemon>> => {
    let pokes: Array<Pokemon> = [];
    await fetchPokemonList()
        .then(result =>
            result.type === "success"
                ? result.value.map((it: { name: string; url: string }) => [
                    parseInt(it.url.replace("https://pokeapi.co/api/v2/ability/", "").replace("/", "")),
                    it.name,
                ])
                : []
        )
        .then(
            (res): Array<Pokemon> =>
                res.map((it: Array<string | number>) => {
                    return { id: it[0], name: it[1] } as Pokemon;
                })
        )
        .then((it: Array<Pokemon>): Array<Pokemon> => {
            pokes = [...it];
            return pokes;
        })
        .catch(err => {
            console.error("Une erreur s'est produite :", err);
        });
    return pokes;
};

const display_pokemons = (pokes: Array<Pokemon>) => {
    const table = document.getElementById("data-table");
    const thead = document.getElementById("table-head");
    const tbody = document.getElementById("table-body");
    if (table && tbody && thead) {
        thead.innerHTML = `
            <tr>
                <!-- <th scope="col">Index</th> -->
                <th scope="col">Name</th>
                <th scope="col">Id</th>
            </tr>`;
        let rows = "";
        pokes.forEach((it: Pokemon) => {
            rows += `
              <tr>
                <td>${it.name}</td>
                <td>${it.id}</td>
              </tr>
            `;
        });
        tbody.innerHTML = rows;
    }
};
/*=============================================================*/

const pokemons: Array<Pokemon> = await fetch_pokemons();

console.assert(pokemons.length == 358);

const first_cursor: Cursor = {
    position: 0,
    pokemons: pokemons.slice(0, 10),
};

const last_cursor: Cursor = {
    position: Math.floor(pokemons.length / 10) * 10,
    pokemons: pokemons.slice(
        Math.floor(pokemons.length / 10) * 10,
        pokemons.length
    ),
};

const next_pokemon = (cursor: Cursor): Cursor => {
    if (cursor.position >= last_cursor.position) return last_cursor;
    else
        return {
            position: cursor.position + 10,
            pokemons: pokemons.slice(cursor.position + 10, cursor.position + 20),
        };
};

const prev_pokemon = (cursor: Cursor): Cursor => {
    const nb_paquet = pokemons.length / 10;
    const nb_paquet_rempli = Math.floor(nb_paquet);
    const nb_items_dernier_paquet = pokemons.length - nb_paquet_rempli * 10;
    const position_max = pokemons.length - nb_items_dernier_paquet;

    if (cursor.position <= 10) return first_cursor;
    if (cursor.position > position_max) return last_cursor;
    return {
        position: cursor.position - 10,
        pokemons: pokemons.slice(cursor.position - 10, cursor.position),
    };
};

const pokemon_navigation = () => {
    console.log(first_cursor);
    console.log(last_cursor);
    console.table(next_pokemon(first_cursor));
    console.table(prev_pokemon(last_cursor));
};

export { pokemons, display_pokemons, pokemon_navigation };
