-- V3: segurança do sistema.
-- 1) Tabela de log de acesso (EX-03): registra tentativas de login.
-- 2) Perfil do administrador passa a ser "Suporte" (RNF-03, decisão do grupo).

CREATE TABLE log_acesso (
  id_log INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  data_hora TIMESTAMP NOT NULL DEFAULT NOW(),
  login_informado VARCHAR(40) NOT NULL,
  id_usuario INT,
  sucesso BOOLEAN NOT NULL,
  motivo VARCHAR(20),
  computador VARCHAR(100),
  CONSTRAINT pk_log_acesso PRIMARY KEY (id_log),
  CONSTRAINT fk_log_acesso_id_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT ck_log_acesso_motivo
    CHECK (motivo IN ('USUARIO_INEXISTENTE', 'SENHA_INCORRETA', 'USUARIO_INATIVO')),
  CONSTRAINT ck_log_acesso_sucesso_motivo
    CHECK ((sucesso AND motivo IS NULL) OR (NOT sucesso AND motivo IS NOT NULL))
);

-- Índice para consultas por período (ex.: "tentativas de hoje").
CREATE INDEX idx_log_acesso_data_hora ON log_acesso (data_hora);

UPDATE usuario
   SET cargo = 'Suporte'
 WHERE login = 'admin';