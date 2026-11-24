-- V5: responsável financeiro e pessoas autorizadas ligadas à criança.
-- 1) aluno_responsavel ganha "responsavel_financeiro": quem paga a mensalidade daquela criança
--    (pode ser o responsável legal ou outra pessoa: avô, padrinho, tio...). RF-04.
-- 2) aluno_pessoa_autorizada: quem pode buscar a criança, ligado ao ALUNO, para aparecer
--    na ficha dela (RF-12, RF-13). A tabela matricula_pessoa_autorizada continua existindo
--    para a frente de matrícula usar, se o grupo preferir ligar à matrícula depois.

ALTER TABLE aluno_responsavel
  ADD COLUMN responsavel_financeiro BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE aluno_pessoa_autorizada (
  id_aluno_pessoa_autorizada INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_aluno INT NOT NULL,
  id_pessoa_autorizada INT NOT NULL,
  parentesco VARCHAR(30),
  tipo_autorizacao VARCHAR(10) NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT pk_aluno_pessoa_autorizada PRIMARY KEY (id_aluno_pessoa_autorizada),
  CONSTRAINT fk_aluno_pessoa_autorizada_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_aluno),
  CONSTRAINT fk_aluno_pessoa_autorizada_id_pessoa FOREIGN KEY (id_pessoa_autorizada) REFERENCES pessoa_autorizada (id_pessoa_autorizada),
  CONSTRAINT ck_aluno_pessoa_autorizada_tipo CHECK (tipo_autorizacao IN ('RETIRADA','EMERGENCIA','AMBOS'))
);

-- Uma pessoa não pode estar ATIVA duas vezes para a mesma criança.
CREATE UNIQUE INDEX uq_aluno_pessoa_autorizada_ativa
  ON aluno_pessoa_autorizada (id_aluno, id_pessoa_autorizada) WHERE ativo;

-- Consulta da ficha do aluno (RNF-04).
CREATE INDEX ix_aluno_pessoa_autorizada_aluno ON aluno_pessoa_autorizada (id_aluno);
CREATE INDEX ix_aluno_responsavel_aluno ON aluno_responsavel (id_aluno);
