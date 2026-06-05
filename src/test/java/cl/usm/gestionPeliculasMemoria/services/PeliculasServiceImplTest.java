package cl.usm.gestionPeliculasMemoria.services;

import cl.usm.gestionPeliculasMemoria.entities.Pelicula;
import cl.usm.gestionPeliculasMemoria.repositories.PeliculasRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeliculasServiceImplTest {

    @Mock
    private PeliculasRepository peliculasRepository;

    @InjectMocks
    private PeliculasServiceImpl service;

    @Test
    void crear_pelicula_asigna_token() {
        Pelicula p = new Pelicula("tt001", "Matrix", "Wachowski", null, null);
        when(peliculasRepository.insert(any())).thenAnswer(inv -> inv.getArgument(0));

        Pelicula res = service.createPelicula(p);

        assertNotNull(res.getTokenDescarga());
        assertEquals(10, res.getTokenDescarga().length());
    }

    @Test
    void crear_pelicula_llama_al_repositorio() {
        Pelicula p = new Pelicula("tt002", "Inception", "Nolan", null, null);
        when(peliculasRepository.insert(any())).thenReturn(p);

        service.createPelicula(p);

        verify(peliculasRepository, times(1)).insert(p);
    }

    @Test
    void crear_pelicula_retorna_lo_que_guarda_el_repo() {
        Pelicula p = new Pelicula("tt003", "Pulp Fiction", "Tarantino", null, null);
        when(peliculasRepository.insert(any())).thenReturn(p);

        Pelicula res = service.createPelicula(p);

        assertEquals("tt003", res.getId());
    }

    @Test
    void si_el_repo_falla_retorna_null() {
        Pelicula p = new Pelicula("tt004", "Fight Club", "Fincher", null, null);
        when(peliculasRepository.insert(any())).thenThrow(new RuntimeException("fallo"));

        assertNull(service.createPelicula(p));
    }

    @Test
    void getAll_devuelve_lo_que_trae_el_repo() {
        List<Pelicula> lista = List.of(
            new Pelicula("tt005", "Interstellar", "Nolan", null, null),
            new Pelicula("tt006", "The Dark Knight", "Nolan", null, null)
        );
        when(peliculasRepository.findAll()).thenReturn(lista);

        List<Pelicula> res = service.getAll();

        assertEquals(2, res.size());
        verify(peliculasRepository).findAll();
    }

    @Test
    void getAll_con_repo_vacio() {
        when(peliculasRepository.findAll()).thenReturn(List.of());
        assertTrue(service.getAll().isEmpty());
    }

    @Test
    void findById_delega_en_repo() {
        Pelicula p = new Pelicula("tt007", "12 Angry Men", "Lumet", null, null);
        when(peliculasRepository.findById("tt007")).thenReturn(p);

        Pelicula res = service.findById("tt007");

        assertNotNull(res);
        assertEquals("tt007", res.getId());
        verify(peliculasRepository).findById("tt007");
    }

    @Test
    void findById_id_que_no_existe_retorna_null() {
        when(peliculasRepository.findById("nope")).thenReturn(null);
        assertNull(service.findById("nope"));
    }

    @Test
    void filter_por_titulo() {
        List<Pelicula> todas = List.of(
            new Pelicula("tt008", "The Godfather", "Coppola", null, null),
            new Pelicula("tt009", "Goodfellas", "Scorsese", null, null)
        );
        when(peliculasRepository.findAll()).thenReturn(todas);

        List<Pelicula> res = service.filter("Godfather");

        assertEquals(1, res.size());
        assertEquals("tt008", res.get(0).getId());
    }

    @Test
    void filter_por_id() {
        List<Pelicula> todas = List.of(
            new Pelicula("tt010", "Parasite", "Bong", null, null),
            new Pelicula("tt011", "Oldboy", "Park", null, null)
        );
        when(peliculasRepository.findAll()).thenReturn(todas);

        List<Pelicula> res = service.filter("tt011");

        assertEquals(1, res.size());
        assertEquals("Oldboy", res.get(0).getTitulo());
    }

    @Test
    void filter_ignora_mayusculas() {
        when(peliculasRepository.findAll()).thenReturn(
            List.of(new Pelicula("TT012", "Interstellar", "Nolan", null, null))
        );

        assertEquals(1, service.filter("INTERSTELLAR").size());
        assertEquals(1, service.filter("tt012").size());
    }

    @Test
    void filter_sin_resultados() {
        when(peliculasRepository.findAll()).thenReturn(
            List.of(new Pelicula("tt013", "Avatar", "Cameron", null, null))
        );

        assertTrue(service.filter("Batman").isEmpty());
    }
}
