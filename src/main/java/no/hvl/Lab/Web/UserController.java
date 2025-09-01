package no.hvl.Lab.Web;

import org.springframework.web.bind.annotation.*;

import no.hvl.Lab.Domain.User;
import no.hvl.Lab.Service.PollManager;

import java.util.Collection;

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

    public static class CreateUserRequest {
        public String username;
        public String password;
        public String email;
    }
}
