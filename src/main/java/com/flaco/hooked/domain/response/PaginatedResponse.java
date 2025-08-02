package com.flaco.hooked.domain.response;

import org.springframework.data.domain.Page;
import java.util.List;

public class PaginatedResponse<T> {

    private List<T> contenido;
    private int paginaActual;
    private int totalPaginas;
    private long totalElementos;
    private int tamanoPagina;
    private boolean esUltimaPagina;
    private boolean esPrimeraPagina;
    private boolean estaVacia;

    public PaginatedResponse() {}

    public PaginatedResponse(Page<T> page) {
        this.contenido = page.getContent();
        this.paginaActual = page.getNumber();
        this.totalPaginas = page.getTotalPages();
        this.totalElementos = page.getTotalElements();
        this.tamanoPagina = page.getSize();
        this.esUltimaPagina = page.isLast();
        this.esPrimeraPagina = page.isFirst();
        this.estaVacia = page.isEmpty();
    }

    // GETTERS Y SETTERS
    public List<T> getContenido() {
        return contenido;
    }

    public void setContenido(List<T> contenido) {
        this.contenido = contenido;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public void setPaginaActual(int paginaActual) {
        this.paginaActual = paginaActual;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public void setTotalElementos(long totalElementos) {
        this.totalElementos = totalElementos;
    }

    public int getTamanoPagina() {
        return tamanoPagina;
    }

    public void setTamanoPagina(int tamanoPagina) {
        this.tamanoPagina = tamanoPagina;
    }

    public boolean isEsUltimaPagina() {
        return esUltimaPagina;
    }

    public void setEsUltimaPagina(boolean esUltimaPagina) {
        this.esUltimaPagina = esUltimaPagina;
    }

    public boolean isEsPrimeraPagina() {
        return esPrimeraPagina;
    }

    public void setEsPrimeraPagina(boolean esPrimeraPagina) {
        this.esPrimeraPagina = esPrimeraPagina;
    }

    public boolean isEstaVacia() {
        return estaVacia;
    }

    public void setEstaVacia(boolean estaVacia) {
        this.estaVacia = estaVacia;
    }

    // METODO POR SI ALGO NO FUNCIONA
    @Override
    public String toString() {
        return "PaginatedResponse{" +
                "contenido=" + (contenido != null ? contenido.size() + " elementos" : "null") +
                ", paginaActual=" + paginaActual +
                ", totalPaginas=" + totalPaginas +
                ", totalElementos=" + totalElementos +
                ", tamanoPagina=" + tamanoPagina +
                ", esUltimaPagina=" + esUltimaPagina +
                ", esPrimeraPagina=" + esPrimeraPagina +
                '}';
    }
}