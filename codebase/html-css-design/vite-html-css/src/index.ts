import "./styles.scss";
import "./style.css";
import footer from "./footer";
import { pokemons, display_pokemons, pokemon_navigation } from "./pokedex";

footer();
pokemon_navigation();
display_pokemons(pokemons);

export { sum } from "./sum";
