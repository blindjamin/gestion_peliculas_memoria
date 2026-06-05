package cl.usm.gestionPeliculasMemoria.repositories;

import cl.usm.gestionPeliculasMemoria.entities.Pelicula;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeliculasRepositoryImplTest {

    private PeliculasRepositoryImpl repo;

    @BeforeEach
    void init() {
        repo = new PeliculasRepositoryImpl();
    }

    @Test
    void insertar_pelicula_correctamente() {
        Pelicula p = new Pelicula("tt001", "Matrix", "Wachowski", null, null);
        Pelicula res = repo.insert(p);

        assertNotNull(res);
        assertEquals("tt001", res.getId());
        assertEquals("Matrix", res.getTitulo());
    }

    @Test
    void insertar_con_id_nulo_debe_lanzar_excepcion() {
        Pelicula p = new Pelicula(null, "Sin id", "Alguien", null, null);
        assertThrows(IllegalArgumentException.class, () -> repo.insert(p));
    }

    @Test
    void no_se_puede_insertar_id_duplicado() {
        repo.insert(new Pelicula("tt002", "Inception", "Nolan", null, null));
        Pelicula duplicada = new Pelicula("tt002", "Otro titulo", "Otro", null, null);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> repo.insert(duplicada)
        );
        assertTrue(ex.getMessage().contains("tt002"));
    }

    @Test
    void duplicado_case_insensitive_tambien_falla() {
        repo.insert(new Pelicula("ID001", "Pelicula A", "Director A", null, null));
        assertThrows(IllegalArgumentException.class,
            () -> repo.insert(new Pelicula("id001", "Pelicula B", "Director B", null, null)));
    }

    @Test
    void lista_vacia_al_inicio() {
        List<Pelicula> lista = repo.findAll();
        assertTrue(lista.isEmpty());
    }

    @Test
    void findAll_devuelve_todas() {
        repo.insert(new Pelicula("tt003", "Pulp Fiction", "Tarantino", null, null));
        repo.insert(new Pelicula("tt004", "Fight Club", "Fincher", null, null));

        assertEquals(2, repo.findAll().size());
    }

    @Test
    void findAll_no_retorna_la_misma_referencia() {
        repo.insert(new Pelicula("tt005", "Interstellar", "Nolan", null, null));
        assertNotSame(repo.findAll(), repo.findAll());
    }

    @Test
    void buscar_por_id_nulo_retorna_null() {
        assertNull(repo.findById(null));
    }

    @Test
    void buscar_pelicula_existente() {
        repo.insert(new Pelicula("tt006", "The Dark Knight", "Nolan", null, null));
        Pelicula encontrada = repo.findById("tt006");

        assertNotNull(encontrada);
        assertEquals("The Dark Knight", encontrada.getTitulo());
    }

    @Test
    void buscar_id_que_no_existe() {
        Pelicula res = repo.findById("id-inventado");
        assertNull(res);
    }

    @Test
    void busqueda_es_case_insensitive() {
        repo.insert(new Pelicula("TT007", "12 Angry Men", "Lumet", null, null));

        Pelicula res = repo.findById("tt007");

        assertNotNull(res);
        assertEquals("TT007", res.getId());
    }
}
