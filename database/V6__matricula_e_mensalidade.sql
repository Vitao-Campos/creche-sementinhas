-- =====================================================================
-- V6: Frente B (Aluno e Matrícula)
-- Execute DEPOIS do V1 a V5.
--
-- 1) faixa_mensalidade: valor da mensalidade por faixa etária (RN-07).
--    O valor fica no banco (e não no código) para a Direção reajustar
--    sem precisar recompilar o sistema. Toda alteração é auditada (RNF-02).
--    A faixa é pela idade em ANOS COMPLETOS, contando o mês do aniversário
--    inteiro (cláusula do contrato: reajuste no mês do aniversário).
--
--    ATENÇÃO: os valores abaixo são EXEMPLOS. Confirme a tabela real
--    com a Direção da creche antes de usar em produção.
--
-- 2) Turmas iniciais. A idade mínima/máxima da turma é em MESES
--    (mesma unidade da CalculadoraIdade). Ajuste nomes, salas e
--    capacidades com a creche.
-- =====================================================================

CREATE TABLE faixa_mensalidade (
  id_faixa_mensalidade INT GENERATED ALWAYS AS IDENTITY NOT NULL,
  descricao VARCHAR(40) NOT NULL,
  idade_minima_anos SMALLINT NOT NULL,
  idade_maxima_anos SMALLINT,              -- NULL = sem limite superior
  valor NUMERIC(10,2) NOT NULL,
  CONSTRAINT pk_faixa_mensalidade PRIMARY KEY (id_faixa_mensalidade),
  CONSTRAINT uq_faixa_mensalidade_idade_minima UNIQUE (idade_minima_anos),
  CONSTRAINT ck_faixa_mensalidade_idades CHECK (
      idade_minima_anos >= 0
      AND (idade_maxima_anos IS NULL OR idade_maxima_anos >= idade_minima_anos)),
  CONSTRAINT ck_faixa_mensalidade_valor CHECK (valor > 0)
);

-- Valores de EXEMPLO (confirmar com a Direção)
INSERT INTO faixa_mensalidade (descricao, idade_minima_anos, idade_maxima_anos, valor) VALUES
  ('Berçário (0 a 11 meses)',   0, 0,    850.00),
  ('Maternal I (1 ano)',        1, 1,    780.00),
  ('Maternal II (2 anos)',      2, 2,    720.00),
  ('Jardim (3 anos ou mais)',   3, NULL, 680.00);

-- Turmas iniciais (idades em meses). Não duplica se já existir turma com o mesmo nome.
INSERT INTO turma (nome, idade_minima, idade_maxima, capacidade, periodo, sala)
SELECT v.nome, v.idade_minima, v.idade_maxima, v.capacidade, v.periodo, v.sala
FROM (VALUES
  ('Berçário',    0, 11, 12, 'INTEGRAL', 'Sala 01'),
  ('Maternal I', 12, 23, 14, 'INTEGRAL', 'Sala 02'),
  ('Maternal II',24, 35, 16, 'INTEGRAL', 'Sala 03'),
  ('Jardim',     36, 71, 20, 'INTEGRAL', 'Sala 04')
) AS v(nome, idade_minima, idade_maxima, capacidade, periodo, sala)
WHERE NOT EXISTS (SELECT 1 FROM turma t WHERE t.nome = v.nome);

-- Consultas da tela de matrícula
CREATE INDEX ix_matricula_aluno ON matricula (id_aluno);
CREATE INDEX ix_matricula_turma_status ON matricula (id_turma, status);

-- Uma criança só pode ter UMA matrícula ATIVA por vez.
CREATE UNIQUE INDEX uq_matricula_uma_ativa_por_aluno
  ON matricula (id_aluno) WHERE status = 'ATIVA';
