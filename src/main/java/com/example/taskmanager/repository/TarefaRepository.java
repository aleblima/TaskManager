package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    @Query("SELECT t FROM Tarefa t " +
            "JOIN FETCH t.usuario " +
            "JOIN FETCH t.categoria " +
            "WHERE t.usuario.id = :usuarioId")
    List<Tarefa> findByUsuarioId(@Param("usuarioId")Long usuarioId);

    @Query("SELECT t FROM Tarefa t " +
            "JOIN FETCH t.usuario " +
            "JOIN FETCH t.categoria " +
            "WHERE t.id = :id")
    Optional<Tarefa> findByIdComRelacionamentos(@Param("id")Long id);

}
