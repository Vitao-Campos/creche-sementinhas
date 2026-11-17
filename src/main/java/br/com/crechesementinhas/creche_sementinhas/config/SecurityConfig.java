package br.com.crechesementinhas.creche_sementinhas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança do sistema.
 *
 * TEMPORÁRIO: libera todas as páginas enquanto o login não existe.
 * Será substituída pela configuração real no commit do login (RNF-01).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define as regras do "porteiro": quem pode acessar o quê.
     *
     * @param http construtor das regras de segurança
     * @return a cadeia de filtros que o Spring Security vai aplicar
     * @throws Exception se a configuração falhar
     */
    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(regras -> regras
                .anyRequest().permitAll());
        return http.build();
    }
}