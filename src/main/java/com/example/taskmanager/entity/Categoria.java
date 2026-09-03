package com.example.taskmanager.entity;

import jakarta.persistence.*;
import java.util.Locale;
import lombok.*;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String nomeNormalizado;

    public Categoria(Long id, String nome) {
        this(id, nome, nome.trim().toLowerCase(Locale.ROOT));
    }
}
