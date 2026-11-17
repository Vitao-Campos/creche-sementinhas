package br.com.crechesementinhas.creche_sementinhas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança do sistema (RNF-01).
 * Exige login para qualquer página, exceto a tela de login e os estilos.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Codificador de senhas. O BCrypt transforma a senha em um hash
     * que não pode ser desfeito.
     *
     * @return o codificador BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define as regras do porteiro: quem pode acessar o quê.
     *
     * @param http construtor das regras de segurança
     * @return a cadeia de filtros do Spring Security
     * @throws Exception se a configuração falhar
     */
    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers("/login", "/css/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(saida -> saida
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }
}