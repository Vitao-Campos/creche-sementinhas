-- =====================================================================
-- DADOS DE TESTE da Frente F (somente para desenvolvimento!)
-- Enquanto as telas de aluno/responsáveis (Frentes B e C) não ficam
-- prontas, este script cria uma criança matriculada para testarmos.
-- Execute DEPOIS do V1, V2 e V3. NÃO devemos executar no computador da creche.
-- =====================================================================

INSERT INTO turma (nome, idade_minima, idade_maxima, capacidade, periodo, sala)
VALUES ('Maternal 1', 0, 1, 14, 'INTEGRAL', 'Sala 02');

INSERT INTO aluno (nome, data_nascimento, cpf, sexo, naturalidade, nacionalidade)
VALUES ('Lucas Henrique Lima', '2026-08-15', '48912345601', 'M', 'Salto', 'Brasileira');

-- Mãe: responsável LEGAL
INSERT INTO responsavel (nome, cpf, email, telefone, cep, endereco, profissao)
VALUES ('Fernanda Lima', '35678912300', 'fernanda@email.com', '(11)99876-5432',
        '13320005', 'Rua Nove de Julho, 410, Centro, Salto/SP', 'Analista');

-- Avó: só responsável FINANCEIRA (RN-03: terceiro pagador, sem poder de assinar)
INSERT INTO responsavel (nome, cpf, email, telefone, cep, endereco, profissao)
VALUES ('Helena Souza Lima', '12345678909', NULL, '(11)98765-1122',
        '13320005', 'Rua Nove de Julho, 410, Centro, Salto/SP', 'Aposentada');

INSERT INTO aluno_responsavel (id_aluno, id_responsavel, grau_parentesco, responsavel_legal, data_inicio)
SELECT a.id_aluno, r.id_responsavel, 'Mãe', TRUE, '2026-02-01'
FROM aluno a, responsavel r
WHERE a.cpf = '48912345601' AND r.cpf = '35678912300';

INSERT INTO aluno_responsavel (id_aluno, id_responsavel, grau_parentesco, responsavel_legal, data_inicio)
SELECT a.id_aluno, r.id_responsavel, 'Avó', FALSE, '2026-02-01'
FROM aluno a, responsavel r
WHERE a.cpf = '48912345601' AND r.cpf = '12345678909';

INSERT INTO matricula (numero_matricula, id_aluno, id_turma, status, data_matricula, andarilha)
SELECT 'MAT-2026-001', a.id_aluno, t.id_turma, 'ATIVA', '2026-02-01', FALSE
FROM aluno a, turma t
WHERE a.cpf = '48912345601' AND t.nome = 'Maternal 1';