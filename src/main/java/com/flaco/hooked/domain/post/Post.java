package com.flaco.hooked.domain.post;

import com.flaco.hooked.domain.categoria.Categoria;
import com.flaco.hooked.domain.usuario.Usuario;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Setter@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;
    @Column(columnDefinition = "TEXT")
    private String contenido;
    private String fotoLink;
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
    private Integer likeCount = 0;

    //Esta es la relación de quien crea el post
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    //Relación con categoria, momentaneamente una sola
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @PrePersist
    public void prePersist(){
        fechaCreacion = LocalDateTime.now();
    }
}
