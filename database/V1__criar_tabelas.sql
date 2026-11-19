-- =====================================================================
-- Creche - DDL (PostgreSQL)
-- Tradução fiel do DDL MySQL do E1 (12 tabelas), sem mudar o modelo.
--
-- Como usar no pgAdmin 4:
--   1. Crie o banco "creche" (encoding UTF8).
--   2. Clique com o botão direito no banco > Query Tool.
--   3. Cole este arquivo inteiro e execute (F5).
--
-- Diferenças em relação ao MySQL:
--   AUTO_INCREMENT      -> GENERATED ALWAYS AS IDENTITY
--   TINYINT UNSIGNED    -> SMALLINT + CHECK (faixa de valores)
--   ENUM(...)           -> VARCHAR + CHECK (... IN (...))
--   DATETIME            -> TIMESTAMP
--   ENGINE / CHARSET    -> não existem no PostgreSQL (o encoding é do banco)
--   CREATE DATABASE / USE -> feitos pelo pgAdmin, não por este script
-- =====================================================================

CREATE TABLE aluno (
  id_aluno INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  nome VARCHAR(100) NOT NULL,
  data_nascimento DATE NOT NULL,
  cpf CHAR(11),
  sexo VARCHAR(1) NOT NULL,
  naturalidade VARCHAR(60),
  nacionalidade VARCHAR(40),
  observacao TEXT,
  CONSTRAINT pk_aluno PRIMARY KEY (id_aluno),
  CONSTRAINT uq_aluno_cpf UNIQUE (cpf),
  CONSTRAINT ck_aluno_sexo CHECK (sexo IN ('F','M'))
);

CREATE TABLE responsavel (
  id_responsavel INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  nome VARCHAR(100) NOT NULL,
  cpf CHAR(11) NOT NULL,
  email VARCHAR(100),
  telefone VARCHAR(15) NOT NULL,
  cep CHAR(8),
  endereco VARCHAR(150),
  profissao VARCHAR(60),
  CONSTRAINT pk_responsavel PRIMARY KEY (id_responsavel),
  CONSTRAINT uq_responsavel_cpf UNIQUE (cpf)
);

CREATE TABLE aluno_responsavel (
  id_aluno_responsavel INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_aluno INT NOT NULL,
  id_responsavel INT NOT NULL,
  grau_parentesco VARCHAR(30) NOT NULL,
  responsavel_legal BOOLEAN NOT NULL,
  data_inicio DATE NOT NULL,
  data_fim DATE,
  CONSTRAINT pk_aluno_responsavel PRIMARY KEY (id_aluno_responsavel),
  CONSTRAINT fk_aluno_responsavel_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_aluno),
  CONSTRAINT fk_aluno_responsavel_id_responsavel FOREIGN KEY (id_responsavel) REFERENCES responsavel (id_responsavel)
);

CREATE TABLE turma (
  id_turma INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  nome VARCHAR(50) NOT NULL,
  idade_minima SMALLINT NOT NULL,
  idade_maxima SMALLINT NOT NULL,
  capacidade SMALLINT NOT NULL,
  periodo VARCHAR(10) NOT NULL,
  responsavel_pedagogico VARCHAR(100),
  sala VARCHAR(20),
  CONSTRAINT pk_turma PRIMARY KEY (id_turma),
  CONSTRAINT ck_turma_idade_minima CHECK (idade_minima BETWEEN 0 AND 255),
  CONSTRAINT ck_turma_idade_maxima CHECK (idade_maxima BETWEEN 0 AND 255),
  CONSTRAINT ck_turma_capacidade CHECK (capacidade BETWEEN 0 AND 255),
  CONSTRAINT ck_turma_periodo CHECK (periodo IN ('MANHA','TARDE','INTEGRAL'))
);

CREATE TABLE matricula (
  id_matricula INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  numero_matricula VARCHAR(20) NOT NULL,
  id_aluno INT NOT NULL,
  id_turma INT NOT NULL,
  status VARCHAR(10) NOT NULL,
  data_matricula DATE NOT NULL,
  data_encerramento DATE,
  andarilha BOOLEAN NOT NULL,
  motivo_andarilha VARCHAR(200),
  CONSTRAINT pk_matricula PRIMARY KEY (id_matricula),
  CONSTRAINT uq_matricula_numero_matricula UNIQUE (numero_matricula),
  CONSTRAINT fk_matricula_id_aluno FOREIGN KEY (id_aluno) REFERENCES aluno (id_aluno),
  CONSTRAINT fk_matricula_id_turma FOREIGN KEY (id_turma) REFERENCES turma (id_turma),
  CONSTRAINT ck_matricula_status CHECK (status IN ('ATIVA','TRANCADA','ENCERRADA'))
);

CREATE TABLE ficha_anamnese (
  id_ficha_anamnese INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_matricula INT NOT NULL,
  autorizacao_imagem BOOLEAN NOT NULL,
  possui_alergia BOOLEAN NOT NULL,
  descricao_alergia VARCHAR(255),
  restricoes_alimentares BOOLEAN NOT NULL,
  descricao_restricao VARCHAR(255),
  receita_antitermico VARCHAR(255),
  possui_cirurgia BOOLEAN NOT NULL,
  descricao_cirurgia VARCHAR(255),
  possui_problemas_saude BOOLEAN NOT NULL,
  descricao_problemas_saude VARCHAR(255),
  usa_medicacao_continua BOOLEAN NOT NULL,
  descricao_medicacao VARCHAR(255),
  possui_fraturas BOOLEAN NOT NULL,
  descricao_fratura VARCHAR(255),
  usa_mamadeira BOOLEAN NOT NULL,
  usa_formula BOOLEAN NOT NULL,
  descricao_formula VARCHAR(255),
  usa_chupeta BOOLEAN NOT NULL,
  usa_fralda BOOLEAN NOT NULL,
  CONSTRAINT pk_ficha_anamnese PRIMARY KEY (id_ficha_anamnese),
  CONSTRAINT uq_ficha_anamnese_id_matricula UNIQUE (id_matricula),
  CONSTRAINT fk_ficha_anamnese_id_matricula FOREIGN KEY (id_matricula) REFERENCES matricula (id_matricula)
);

CREATE TABLE documento (
  id_documento INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_matricula INT NOT NULL,
  nome VARCHAR(100) NOT NULL,
  tipo VARCHAR(40) NOT NULL,
  arquivo VARCHAR(255) NOT NULL,
  data DATE NOT NULL,
  status VARCHAR(10) NOT NULL,
  CONSTRAINT pk_documento PRIMARY KEY (id_documento),
  CONSTRAINT fk_documento_id_matricula FOREIGN KEY (id_matricula) REFERENCES matricula (id_matricula),
  CONSTRAINT ck_documento_status CHECK (status IN ('PENDENTE','ENTREGUE','VALIDADO'))
);

CREATE TABLE contrato (
  id_contrato INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_matricula INT NOT NULL,
  id_responsavel_financeiro INT NOT NULL,
  plano VARCHAR(40) NOT NULL,
  forma_pagamento VARCHAR(8) NOT NULL,
  data_vencimento_mensalidade SMALLINT NOT NULL,
  vigencia_contrato DATE NOT NULL,
  data_assinatura DATE NOT NULL,
  data_saida DATE,
  versao SMALLINT NOT NULL,
  status VARCHAR(9) NOT NULL,
  CONSTRAINT pk_contrato PRIMARY KEY (id_contrato),
  CONSTRAINT fk_contrato_id_matricula FOREIGN KEY (id_matricula) REFERENCES matricula (id_matricula),
  CONSTRAINT fk_contrato_id_responsavel_financeiro FOREIGN KEY (id_responsavel_financeiro) REFERENCES responsavel (id_responsavel),
  CONSTRAINT ck_contrato_forma_pagamento CHECK (forma_pagamento IN ('BOLETO','PIX','CARTAO','DINHEIRO')),
  CONSTRAINT ck_contrato_dia_vencimento CHECK (data_vencimento_mensalidade BETWEEN 1 AND 31),
  CONSTRAINT ck_contrato_versao CHECK (versao >= 1),
  CONSTRAINT ck_contrato_status CHECK (status IN ('VIGENTE','ENCERRADO','CANCELADO'))
);

CREATE TABLE pessoa_autorizada (
  id_pessoa_autorizada INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  nome VARCHAR(100) NOT NULL,
  rg VARCHAR(14) NOT NULL,
  telefone VARCHAR(15) NOT NULL,
  CONSTRAINT pk_pessoa_autorizada PRIMARY KEY (id_pessoa_autorizada)
);

CREATE TABLE matricula_pessoa_autorizada (
  id_matricula_pessoa_autorizada INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_matricula INT NOT NULL,
  id_pessoa_autorizada INT NOT NULL,
  parentesco VARCHAR(30),
  tipo_autorizacao VARCHAR(10) NOT NULL,
  ativo BOOLEAN NOT NULL,
  CONSTRAINT pk_matricula_pessoa_autorizada PRIMARY KEY (id_matricula_pessoa_autorizada),
  CONSTRAINT fk_matricula_pessoa_autorizada_id_matricula FOREIGN KEY (id_matricula) REFERENCES matricula (id_matricula),
  CONSTRAINT fk_matricula_pessoa_autorizada_id_pessoa_autorizada FOREIGN KEY (id_pessoa_autorizada) REFERENCES pessoa_autorizada (id_pessoa_autorizada),
  CONSTRAINT ck_matricula_pessoa_autorizada_tipo CHECK (tipo_autorizacao IN ('RETIRADA','EMERGENCIA','AMBOS'))
);

CREATE TABLE usuario (
  id_usuario INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  nome VARCHAR(100) NOT NULL,
  login VARCHAR(40) NOT NULL,
  senha_hash CHAR(60) NOT NULL,
  cargo VARCHAR(40),
  setor VARCHAR(40),
  ativo BOOLEAN NOT NULL,
  ultimo_login TIMESTAMP,
  data_criacao TIMESTAMP NOT NULL,
  CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
  CONSTRAINT uq_usuario_login UNIQUE (login)
);

CREATE TABLE historico_alteracao (
  id_historico INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_usuario INT NOT NULL,
  data_hora TIMESTAMP NOT NULL,
  entidade VARCHAR(40) NOT NULL,
  id_entidade INT NOT NULL,
  tipo_alteracao VARCHAR(9) NOT NULL,
  campo_alterado VARCHAR(60),
  valor_anterior TEXT,
  valor_novo TEXT,
  justificativa VARCHAR(255),
  CONSTRAINT pk_historico_alteracao PRIMARY KEY (id_historico),
  CONSTRAINT fk_historico_alteracao_id_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
  CONSTRAINT ck_historico_tipo_alteracao CHECK (tipo_alteracao IN ('INSERCAO','ALTERACAO','EXCLUSAO'))
);

-- =====================================================================
-- OPCIONAL (regra RN-06: só um contrato VIGENTE por matrícula)
-- No PostgreSQL basta um índice único parcial. Descomente para usar.
-- =====================================================================
-- CREATE UNIQUE INDEX uq_contrato_um_vigente
--   ON contrato (id_matricula) WHERE status = 'VIGENTE';