package br.com.crechesementinhas.creche_sementinhas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller da página inicial do sistema.
 */
@Controller
public class HomeController {

    /**
     * Atende o pedido da raiz do site ("/") e entrega a página "home".
     *
     * @return o nome do template Thymeleaf (arquivo templates/home.html)
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }
}