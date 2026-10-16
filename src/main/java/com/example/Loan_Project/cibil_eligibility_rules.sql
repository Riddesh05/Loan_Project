
USE loan_project;

START TRANSACTION;

-- Update the existing CIBIL rules.

UPDATE cibil_eligibility_rules
SET max_cibil_score = NULL,
    status = 'EXCELLENT',
    eligible_amount = 10000000.00,
    active = 1
WHERE min_cibil_score = 900;

UPDATE cibil_eligibility_rules
SET max_cibil_score = 899,
    status = 'VERY_GOOD',
    eligible_amount = 10000000.00,
    active = 1
WHERE min_cibil_score = 800;

UPDATE cibil_eligibility_rules
SET max_cibil_score = 799,
    status = 'GOOD',
    eligible_amount = 7500000.00,
    active = 1
WHERE min_cibil_score = 750;

UPDATE cibil_eligibility_rules
SET max_cibil_score = 749,
    status = 'AVERAGE',
    eligible_amount = 5000000.00,
    active = 1
WHERE min_cibil_score = 700;

UPDATE cibil_eligibility_rules
SET max_cibil_score = 699,
    status = 'RISKY',
    eligible_amount = 2500000.00,
    active = 1
WHERE min_cibil_score = 650;

UPDATE cibil_eligibility_rules
SET max_cibil_score = 649,
    status = 'REJECT',
    eligible_amount = 0.00,
    active = 1
WHERE min_cibil_score = 0;

-- Insert missing rules without duplicating existing bands.

INSERT INTO cibil_eligibility_rules
(min_cibil_score, max_cibil_score, status, eligible_amount, active)
SELECT 900, NULL, 'EXCELLENT', 10000000.00, 1
    WHERE NOT EXISTS (
    SELECT 1 FROM cibil_eligibility_rules
    WHERE min_cibil_score = 900
      AND max_cibil_score IS NULL
);

INSERT INTO cibil_eligibility_rules
(min_cibil_score, max_cibil_score, status, eligible_amount, active)
SELECT 800, 899, 'VERY_GOOD', 10000000.00, 1
    WHERE NOT EXISTS (
    SELECT 1 FROM cibil_eligibility_rules
    WHERE min_cibil_score = 800 AND max_cibil_score = 899
);

INSERT INTO cibil_eligibility_rules
(min_cibil_score, max_cibil_score, status, eligible_amount, active)
SELECT 750, 799, 'GOOD', 7500000.00, 1
    WHERE NOT EXISTS (
    SELECT 1 FROM cibil_eligibility_rules
    WHERE min_cibil_score = 750 AND max_cibil_score = 799
);

INSERT INTO cibil_eligibility_rules
(min_cibil_score, max_cibil_score, status, eligible_amount, active)
SELECT 700, 749, 'AVERAGE', 5000000.00, 1
    WHERE NOT EXISTS (
    SELECT 1 FROM cibil_eligibility_rules
    WHERE min_cibil_score = 700 AND max_cibil_score = 749
);

INSERT INTO cibil_eligibility_rules
(min_cibil_score, max_cibil_score, status, eligible_amount, active)
SELECT 650, 699, 'RISKY', 2500000.00, 1
    WHERE NOT EXISTS (
    SELECT 1 FROM cibil_eligibility_rules
    WHERE min_cibil_score = 650 AND max_cibil_score = 699
);

INSERT INTO cibil_eligibility_rules
(min_cibil_score, max_cibil_score, status, eligible_amount, active)
SELECT 0, 649, 'REJECT', 0.00, 1
    WHERE NOT EXISTS (
    SELECT 1 FROM cibil_eligibility_rules
    WHERE min_cibil_score = 0 AND max_cibil_score = 649
);

COMMIT;

-- Verify all rules.
SELECT *
FROM cibil_eligibility_rules
ORDER BY min_cibil_score DESC;
