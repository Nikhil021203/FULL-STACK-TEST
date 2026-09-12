package com.example.fullstacktest.item;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class LabItemController {
    private final LabItemRepository repository;

    public LabItemController(LabItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<LabItem> all() {
        return repository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LabItem create(@Valid @RequestBody ItemRequest request) {
        return repository.save(new LabItem(request.title(), request.note()));
    }

    @PutMapping("/{id}")
    public LabItem update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        LabItem item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.update(request.title(), request.note());
        return repository.save(item);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ItemNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
