package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.service.UserService;

@Controller
public class AdminUserController {
    @Autowired
    private UserService userService;
    @GetMapping("/admin/users")
    public String listUsers(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                            @RequestParam(name = "sort", required = false, defaultValue = "userId") String sort,
                            @RequestParam(name = "dir", required = false, defaultValue = "asc") String dir,
                            @RequestParam(name = "search", required = false) String search,
                            Model model) {
        int size = 5;
        org.springframework.data.domain.Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sort));
        org.springframework.data.domain.Page<com.example.demo.model.User> userPage = userService.findUsers(search, pageable);
        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("search", search);
        return "admin_users";
    }
}
