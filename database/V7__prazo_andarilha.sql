-- =====================================================================
-- V7: prazo previsto da matrícula andarilha (RF-11: "justificativa e
-- tempo previsto de permanência"). Execute DEPOIS do V6.
--
-- andarilha_ate: até quando a criança fica, como exceção, numa turma
-- fora da sua faixa etária (RN-08). Vencido o prazo, a tela de turmas
-- volta a alertar.
--
-- O CHECK é NOT VALID para não falhar em matrículas andarilhas antigas
-- (sem prazo): ele vale para tudo que for gravado daqui em diante.
-- =====================================================================

ALTER TABLE matricula ADD COLUMN andarilha_ate DATE;

ALTER TABLE matricula ADD CONSTRAINT ck_matricula_andarilha_prazo CHECK (
    (andarilha = TRUE  AND andarilha_ate IS NOT NULL)
 OR (andarilha = FALSE AND andarilha_ate IS NULL)
) NOT VALID;
