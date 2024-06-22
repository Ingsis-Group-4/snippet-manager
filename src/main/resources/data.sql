-- Create formatting rules
INSERT INTO rule (id, created_at, updated_at, default_value, name, rule_type, value_type)
VALUES ('ddade5c7-76ef-4c24-a8eb-c78993f9f740', '2024-05-30 19:40:08.000000', '2024-05-30 19:40:08.000000', 'false',
        'hasSpaceBetweenColon',
        'FORMATTING', 'BOOLEAN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule (id, created_at, updated_at, default_value, name, rule_type, value_type)
VALUES ('6606ad48-2ecd-4ad8-95b0-bdccea7660ee', '2024-05-30 19:40:08.000000', '2024-05-30 19:40:08.000000', 'false',
        'hasSpaceBetweenEqualSign',
        'FORMATTING', 'BOOLEAN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule (id, created_at, updated_at, default_value, name, rule_type, value_type)
VALUES ('02e42f09-a6d3-4853-b1cb-d60533de18a3', '2024-05-30 19:40:08.000000', '2024-05-30 19:40:08.000000', '1',
        'lineBreakBeforePrintLn',
        'FORMATTING', 'INTEGER')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule (id, created_at, updated_at, default_value, name, rule_type, value_type)
VALUES ('ca7af7f6-1746-474e-8ee8-ccd66d4819e3', '2024-05-30 19:40:08.000000', '2024-05-30 19:40:08.000000', '2',
        'ifBlockIndent',
        'FORMATTING', 'INTEGER')
ON CONFLICT (id) DO NOTHING;

-- Create linting rules
INSERT INTO rule (id, created_at, updated_at, default_value, name, rule_type, value_type)
VALUES ('06e7e58c-d81e-4b68-b7d3-b0821f130328', '2024-05-30 19:40:08.000000', '2024-05-30 19:40:08.000000',
        '^[a-z]+(?:[A-Z][a-z]*)*$', 'variableNamingRule',
        'LINTING', 'STRING')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule (id, created_at, updated_at, default_value, name, rule_type, value_type)
VALUES ('64d199da-a609-49b4-9767-5990c3e1f3bd', '2024-05-30 19:40:08.000000', '2024-05-30 19:40:08.000000', 'false',
        'printLnArgumentNonExpressionRule',
        'LINTING', 'BOOLEAN')
ON CONFLICT (id) DO NOTHING;