-- =====================================================================
-- V3 - Ajustes da Frente F (Contrato e documentos)
-- Execute DEPOIS do V1 e do V2, no banco "creche" (pgAdmin > Query Tool > F5).
-- =====================================================================

-- 1) Valor da mensalidade gravado no contrato.
--    Cada versão do contrato guarda o valor combinado naquela época,
--    assim o histórico não muda quando a mensalidade for reajustada (RF-08).
ALTER TABLE contrato
    ADD COLUMN valor_mensalidade NUMERIC(10,2);

ALTER TABLE contrato
    ADD CONSTRAINT ck_contrato_valor_mensalidade CHECK (valor_mensalidade > 0);

-- 2) RN-05: o contrato só vale depois de assinado pelo responsável legal.
--    Então a data de assinatura começa VAZIA e é preenchida quando ele assina.
ALTER TABLE contrato
    ALTER COLUMN data_assinatura DROP NOT NULL;
    
-- 3) RF-08: não pode existir duas "versão 1" para a mesma matrícula.
ALTER TABLE contrato
    ADD CONSTRAINT uq_contrato_matricula_versao UNIQUE (id_matricula, versao);

-- 4) RN-06 / EX-04: só UM contrato VIGENTE por matrícula
--    (índice que já estava comentado no V1).
CREATE UNIQUE INDEX uq_contrato_um_vigente
    ON contrato (id_matricula) WHERE status = 'VIGENTE';
    