package dev.handson.todo.controller;

import dev.handson.todo.domain.Status;
import dev.handson.todo.domain.Todo;
import dev.handson.todo.repository.TodoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
public class TodoController {
    private final TodoRepository repo;

    public TodoController(TodoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("todos", repo.findAll());
        model.addAttribute("todoList", repo.findByStatus(Status.Todo));
        model.addAttribute("doingList", repo.findByStatus(Status.Doing));
        model.addAttribute("completedList", repo.findByStatus(Status.Completed));
        model.addAttribute("statuses", Status.values());
        return "index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("todo", new Todo());
        model.addAttribute("statuses", Status.values());
        return "form";
    }

    @PostMapping("/save")
    public String save(Todo todo) {
        repo.save(todo);
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Todo t = repo.findById(id).orElse(new Todo());
        model.addAttribute("todo", t);
        model.addAttribute("statuses", Status.values());
        return "form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/";
    }
}
