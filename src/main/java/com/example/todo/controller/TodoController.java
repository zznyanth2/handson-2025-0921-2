package com.example.todo.controller;

import com.example.todo.model.Status;
import com.example.todo.model.Todo;
import com.example.todo.service.TodoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Controller
public class TodoController {
    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    // Thymeleaf main page
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("todosTodo", service.listByStatus(Status.TODO, PageRequest.of(0, 100)).getContent());
        model.addAttribute("todosDoing", service.listByStatus(Status.DOING, PageRequest.of(0, 100)).getContent());
        model.addAttribute("todosCompleted", service.listByStatus(Status.COMPLETED, PageRequest.of(0, 100)).getContent());
        model.addAttribute("newTodo", new Todo());
        return "index";
    }

    // REST endpoints
    @GetMapping("/api/todos")
    @ResponseBody
    public ResponseEntity<?> list(@RequestParam Optional<Status> status) {
        if (status.isPresent()) {
            return ResponseEntity.ok(service.listByStatus(status.get(), PageRequest.of(0, 100)).getContent());
        }
        return ResponseEntity.ok(service.listAll(PageRequest.of(0, 100)).getContent());
    }

    @PostMapping("/api/todos")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody Todo todo) {
        Todo created = service.create(todo);
        return ResponseEntity.created(URI.create("/api/todos/" + created.getId())).body(created);
    }

    @GetMapping("/api/todos/{id}")
    @ResponseBody
    public ResponseEntity<?> get(@PathVariable UUID id) {
        return service.find(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/todos/{id}")
    @ResponseBody
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Todo todo) {
        return service.update(id, todo).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/api/todos/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable UUID id, @RequestBody StatusWrapper wrapper) {
        return service.updateStatus(id, wrapper.getStatus()).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/todos/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        boolean ok = service.softDelete(id);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    public static class StatusWrapper { private Status status; public Status getStatus() { return status; } public void setStatus(Status s) { this.status = s; } }
}
