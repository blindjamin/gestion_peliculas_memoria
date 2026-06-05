package cl.usm.gestionPeliculasMemoria.controllers;

import cl.usm.gestionPeliculasMemoria.entities.Comentario;
import cl.usm.gestionPeliculasMemoria.entities.Pelicula;
import cl.usm.gestionPeliculasMemoria.services.PeliculasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PeliculasControllerTest {

    @Mock
    private PeliculasService peliculasService;

    @InjectMocks
    private PeliculasController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    // GET /peliculas

    @Test
    void get_todas_las_peliculas() throws Exception {
        List<Pelicula> lista = List.of(
            new Pelicula("tt001", "Matrix", "Wachowski", "tok1", null),
            new Pelicula("tt002", "Inception", "Nolan", "tok2", null)
        );
        when(peliculasService.getAll()).thenReturn(lista);

        mockMvc.perform(get("/peliculas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("tt001"));
    }

    @Test
    void get_peliculas_con_filtro() throws Exception {
        when(peliculasService.filter("Matrix")).thenReturn(
            List.of(new Pelicula("tt001", "Matrix", "Wachowski", "tok1", null))
        );

        mockMvc.perform(get("/peliculas").param("q", "Matrix"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titulo").value("Matrix"));
    }

    @Test
    void get_peliculas_error_interno() throws Exception {
        when(peliculasService.getAll()).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/peliculas"))
            .andExpect(status().isInternalServerError());
    }

    // POST /peliculas

    @Test
    void crear_pelicula_ok() throws Exception {
        Pelicula nueva = new Pelicula("tt003", "Parasite", "Bong", null, null);
        Pelicula creada = new Pelicula("tt003", "Parasite", "Bong", "abc1234567", null);
        when(peliculasService.createPelicula(any())).thenReturn(creada);

        mockMvc.perform(post("/peliculas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(nueva)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenDescarga").value("abc1234567"));
    }

    @Test
    void crear_pelicula_falla_en_servicio() throws Exception {
        Pelicula p = new Pelicula("tt004", "Fight Club", "Fincher", null, null);
        when(peliculasService.createPelicula(any())).thenReturn(null);

        mockMvc.perform(post("/peliculas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(p)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void crear_pelicula_sin_campos_obligatorios() throws Exception {
        Pelicula invalida = new Pelicula("", "", "", null, null);

        mockMvc.perform(post("/peliculas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(invalida)))
            .andExpect(status().isBadRequest());
    }

    // GET /peliculas/{id}

    @Test
    void buscar_pelicula_por_id_existe() throws Exception {
        Pelicula p = new Pelicula("tt005", "Oldboy", "Park", "token", null);
        when(peliculasService.findById("tt005")).thenReturn(p);

        mockMvc.perform(get("/peliculas/{id}", "tt005"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Oldboy"));
    }

    @Test
    void buscar_pelicula_no_encontrada() throws Exception {
        when(peliculasService.findById("nope")).thenReturn(null);

        mockMvc.perform(get("/peliculas/{id}", "nope"))
            .andExpect(status().isNotFound());
    }

    @Test
    void buscar_pelicula_error_en_servicio() throws Exception {
        when(peliculasService.findById(anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(get("/peliculas/{id}", "tt006"))
            .andExpect(status().isInternalServerError());
    }

    // GET /peliculas/{id}/comentarios

    @Test
    void obtener_comentarios_de_pelicula() throws Exception {
        Comentario[] comentarios = {
            new Comentario("juan", "muy buena"),
            new Comentario("maria", "la recomiendo")
        };
        when(peliculasService.findById("tt007")).thenReturn(
            new Pelicula("tt007", "Goodfellas", "Scorsese", "tok", comentarios)
        );

        mockMvc.perform(get("/peliculas/{id}/comentarios", "tt007"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].usuario").value("juan"));
    }

    @Test
    void comentarios_pelicula_sin_comentarios() throws Exception {
        when(peliculasService.findById("tt008")).thenReturn(
            new Pelicula("tt008", "Memento", "Nolan", "tok", null)
        );

        mockMvc.perform(get("/peliculas/{id}/comentarios", "tt008"))
            .andExpect(status().isOk());
    }

    @Test
    void comentarios_pelicula_no_existe() throws Exception {
        when(peliculasService.findById("nope")).thenReturn(null);

        mockMvc.perform(get("/peliculas/{id}/comentarios", "nope"))
            .andExpect(status().isNotFound());
    }

    @Test
    void comentarios_error_en_servicio() throws Exception {
        when(peliculasService.findById(anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(get("/peliculas/{id}/comentarios", "tt009"))
            .andExpect(status().isInternalServerError());
    }
}
