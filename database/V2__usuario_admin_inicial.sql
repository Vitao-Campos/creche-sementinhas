-- V2: cria o usuário administrador inicial.
-- Senha provisória: Sementinhas@2026 (guardada como hash BCrypt).
-- TROCAR a senha antes de usar com dados reais.

INSERT INTO usuario (nome, login, senha_hash, cargo, setor, ativo, data_criacao)
VALUES (
    'Administrador',
    'admin',
    '$2a$10$fjfOP3DV94jTh2wvstz8i.KM9mimOn4NT0Vc3wz30AnqY3X/8eKpC',
    'Administrador',
    'Direção',
    TRUE,
    NOW()
);