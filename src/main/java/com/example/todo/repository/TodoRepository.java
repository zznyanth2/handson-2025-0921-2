package com.example.todo.repository;

import com.example.todo.model.Status;
import com.example.todo.model.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {
    Page<Todo> findByStatusAndDeletedAtIsNull(Status status, Pageable pageable);
    Page<Todo> findByDeletedAtIsNull(Pageable pageable);
}
