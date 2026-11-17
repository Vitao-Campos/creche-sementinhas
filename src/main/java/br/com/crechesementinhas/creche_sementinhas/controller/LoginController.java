package br.com.crechesementinhas.creche_sementinhas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller da tela de login.
 */
@Controller
public class LoginController {

    /**
     * Entrega a página de login. A conferência da senha
     * é feita pelo Spring Security, não por este método.
     *
     * @return o nome do template (templates/login.html)
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}