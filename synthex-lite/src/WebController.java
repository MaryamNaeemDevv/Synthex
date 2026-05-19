package com.synthex;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        if (Main.currentUser.isEmpty()) {
            return "redirect:/login";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, RedirectAttributes ra) {
        FirebaseAuthService.AuthResult result = Main.auth.login(email, password);
        if (result.success) {
            Main.currentUser = result.username;
            Main.local.setLocalUsername(Main.currentUser);
            return "redirect:/dashboard";
        } else {
            ra.addFlashAttribute("error", result.message);
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String password, @RequestParam String username, RedirectAttributes ra) {
        FirebaseAuthService.AuthResult result = Main.auth.register(email, password, username);
        if (result.success) {
            Main.currentUser = result.username;
            Main.local.setLocalUsername(Main.currentUser);
            return "redirect:/dashboard";
        } else {
            ra.addFlashAttribute("error", result.message);
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        Main.currentUser = "";
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        if (Main.currentUser.isEmpty()) return "redirect:/login";
        
        model.addAttribute("username", Main.currentUser);
        model.addAttribute("leaderboard", Main.local.getFriendLeaderboard(Main.currentUser));
        model.addAttribute("history", Main.local.getPerformanceHistory(Main.currentUser));
        return "index";
    }

    @PostMapping("/sync")
    public String sync(RedirectAttributes ra) {
        if (!Main.currentUser.isEmpty() && Main.cloud.verifyIntegrity()) {
            List<Map<String, Object>> friends = Main.cloud.fetchFriendList(Main.currentUser);
            List<Map<String, Object>> global = Main.cloud.fetchLeaderboard();
            for (Map<String, Object> f : friends) {
                String fName = (String) f.get("username");
                for (Map<String, Object> entry : global) {
                    if (fName.equalsIgnoreCase((String) entry.get("username"))) f.put("highscore", entry.get("score"));
                }
            }
            Main.local.syncFriends(Main.currentUser, friends);
            ra.addFlashAttribute("message", "Sync complete.");
        }
        return "redirect:/dashboard";
    }
}
