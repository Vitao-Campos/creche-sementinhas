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