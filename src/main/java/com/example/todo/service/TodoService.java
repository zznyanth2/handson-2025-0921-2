package com.example.todo.service;

import com.example.todo.model.Status;
import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TodoService {
    private final TodoRepository repo;

    public TodoService(TodoRepository repo) {
        this.repo = repo;
    }

    public Page<Todo> listAll(Pageable p) {
        return repo.findByDeletedAtIsNull(p);
    }

    public Page<Todo> listByStatus(Status s, Pageable p) {
        return repo.findByStatusAndDeletedAtIsNull(s, p);
    }

    public Optional<Todo> find(UUID id) {
        return repo.findById(id).filter(t -> t.getDeletedAt() == null);
    }

    @Transactional
    public Todo create(Todo t) {
        t.setCreatedAt(Instant.now());
        t.setUpdatedAt(Instant.now());
        return repo.save(t);
    }

    @Transactional
    public Optional<Todo> update(UUID id, Todo update) {
        return repo.findById(id).map(existing -> {
            existing.setTitle(update.getTitle());
            existing.setDescription(update.getDescription());
            existing.setDueDate(update.getDueDate());
            existing.setStatus(update.getStatus());
            existing.setUpdatedAt(Instant.now());
            return repo.save(existing);
        }).filter(t -> t.getDeletedAt() == null);
    }

    @Transactional
    public boolean softDelete(UUID id) {
        return repo.findById(id).map(t -> {
            t.setDeletedAt(Instant.now());
            repo.save(t);
            return true;
        }).orElse(false);
    }

    @Transactional
    public Optional<Todo> updateStatus(UUID id, Status status) {
        return repo.findById(id).map(t -> {
            t.setStatus(status);
            t.setUpdatedAt(Instant.now());
            return repo.save(t);
        }).filter(t -> t.getDeletedAt() == null);
    }
}
