
package no.hvl.Lab.Web;


import java.util.UUID;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.*;

import no.hvl.Lab.Domain.User;
import no.hvl.Lab.Service.PollManager;

import java.util.Collection;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final PollManager manager;
    public UserController(PollManager manager) { this.manager = manager; }

    @PostMapping
    public User create(@RequestBody CreateUserRequest req) {
        return manager.registerUser(req.username, req.password, req.email);
    }

    @GetMapping
    public Collection<User> list() { return manager.allUsers(); }

    @GetMapping("/{userId}")
    public User get(@PathVariable UUID userId) {
        return manager.findUser(userId).orElseThrow();
    }

    @PutMapping("/{userId}")
    public User update(@PathVariable UUID userId, @RequestBody CreateUserRequest req) {
        // For demo: delete and re-create
        // (real app: update fields)
        return manager.registerUser(req.username, req.password, req.email);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable UUID userId) {
        // Not implemented: deleteUser
    }

    public static class CreateUserRequest {
        public String username;
        public String password;
        public String email;
    }
}
