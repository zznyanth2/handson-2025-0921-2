package dev.handson.todo;

import dev.handson.todo.domain.Status;
import dev.handson.todo.domain.Todo;
import dev.handson.todo.repository.TodoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner init(TodoRepository repo) {
        return args -> {
            repo.save(new Todo("Write samples","Create sample todos", Status.Todo));
            repo.save(new Todo("Implement feature","Work on doing items", Status.Doing));
            repo.save(new Todo("Release","Mark as completed", Status.Completed));
        };
    }
}
