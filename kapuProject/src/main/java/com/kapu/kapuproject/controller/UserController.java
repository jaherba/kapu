package com.kapu.kapuproject.controller;

import com.kapu.kapuproject.model.User;
import com.kapu.kapuproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

	
	    private final UserService userService;

	    @Autowired
	    public UserController(UserService userService) {
	        this.userService = userService;
	    }

	    // list of users page
	    @GetMapping
	    public String getAllUsers(Model model) {
	        List<User> users = userService.getAllUsers();
	        model.addAttribute("users", users);
	        return "users"; // Renderiza users.html
	    }

	    // form to add a user
	    @GetMapping("/new")
	    public String showCreateUserForm(Model model) {
	        model.addAttribute("user", new User());
	        return "create-user"; // Renderiza create-user.html
	    }

	    // save a user
	    @PostMapping
	    public String saveUser(@ModelAttribute User user) {
	        userService.saveUser(user);
	        return "redirect:/users"; // Redirige a la lista de usuarios
	    }

	    // delete user
	    @GetMapping("/delete/{id}")
	    public String deleteUser(@PathVariable Long id) {
	        userService.deleteUser(id);
	        return "redirect:/users"; // Redirige a la lista de usuarios
	    }

}
