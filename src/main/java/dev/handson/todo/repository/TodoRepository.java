package dev.handson.todo.repository;

import dev.handson.todo.domain.Todo;
import dev.handson.todo.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByStatus(Status status);
}
