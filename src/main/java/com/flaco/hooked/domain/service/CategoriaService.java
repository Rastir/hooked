package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.categoria.Categoria;
import com.flaco.hooked.domain.categoria.CategoriaRepository;
import com.flaco.hooked.domain.request.ActualizarCategoriaRequest;
import com.flaco.hooked.domain.request.CrearCategoriaRequest;
import com.flaco.hooked.domain.response.CategoriaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Crear (CREATE)
    public CategoriaResponse crearCategoria(CrearCategoriaRequest request) {
        //Validación
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return convertirACategoriaResponse(categoriaGuardada);
    }

    //Obtener todas las categorias (READ)
    @Transactional(readOnly = true)
    public List<CategoriaResponse> obtenerTodasLasCategorias(){
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirACategoriaResponse)
                .collect(Collectors.toList());
    }

    //Obtener categorias por Id (READ)
    @Transactional(readOnly = true)
    public CategoriaResponse obtenerCategoriaPorId(Long id){
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con ID : " + id));

        return convertirACategoriaResponse(categoria);
    }

    //Actualizar categoría (UPDATE)
    public CategoriaResponse actualizarCategoria(Long id, ActualizarCategoriaRequest request){
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con el ID : " + id));

        //Validar cambio de nombre
        if (request.getNombre() != null && request.getNombre().equals(categoria.getNombre())){
            if(categoriaRepository.existsByNombreIgnoreCase(request.getNombre())){
                throw new RuntimeException("Ya existe una categoría con el nombre : " + request.getNombre());
            }
        }

        //parcialidad de los cambios, si hay
        if(request.getNombre() != null){
            categoria.setNombre(request.getNombre());
        }
        if(request.getDescripcion() != null){
            categoria.setDescripcion(request.getDescripcion());
        }

        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        return convertirACategoriaResponse(categoriaActualizada);
    }

    //Eliminar categoría (DELETE)
    public void eliminarCategoria(Long id){
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con el ID : " + id));

        //Verificar que no tenga post asociados
        if(categoria.getPosts() != null && !categoria.getPosts().isEmpty()){
            throw new RuntimeException("No se puede eliminar la categoría porque tiene " +
                    categoria.getPosts().size() + " posts asociados");
        }

        categoriaRepository.delete(categoria);
    }

    //Funcionalidades extra

    //Verificar si existe la categoría
    @Transactional(readOnly = true)
    public boolean existeCategoria(Long id){
        return categoriaRepository.existsById(id);
    }

    //Buscar por nombre
    @Transactional(readOnly = true)
    public List<CategoriaResponse> buscarPorNombre(String nombre){
        return categoriaRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirACategoriaResponse)
                .collect(Collectors.toList());
    }

    //Traer las categorías que tengan posts
    @Transactional(readOnly = true)
    public List<CategoriaResponse> obtenerCategoriasConPosts(){
        return categoriaRepository.findCategoriasWithPosts()
                .stream()
                .map(this::convertirACategoriaResponse)
                .collect(Collectors.toList());
    }

    //Cuenta la cantidad de categorías
    @Transactional(readOnly = true)
    public long contarCategorias(){
        return categoriaRepository.count();
    }

    private CategoriaResponse convertirACategoriaResponse(Categoria categoria){
        CategoriaResponse response = new CategoriaResponse();
        response.setId(categoria.getId());
        response.setNombre(categoria.getNombre());
        response.setDescripcion(categoria.getDescripcion());
        response.setTotalPosts(categoria.getPosts() != null ? categoria.getPosts().size() : 0);
        return response;
    }
}
