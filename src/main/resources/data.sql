-- Demo data for Turkcell Fatura Asistanı

-- Users
INSERT INTO turkcell.users (user_id, name, current_plan_id, type, msisdn, created_at, updated_at) VALUES
(1001, 'Ahmet Yılmaz', 1, 'INDIVIDUAL', '05321234567', NOW(), NOW()),
(1002, 'Ayşe Demir', 2, 'INDIVIDUAL', '05339876543', NOW(), NOW()),
(1003, 'Mehmet Kaya', 3, 'CORPORATE', '05335556677', NOW(), NOW());

-- Plans
INSERT INTO turkcell.plans (plan_id, plan_name, type, quota_gb, quota_min, quota_sms, monthly_price, overage_gb, overage_min, overage_sms, created_at, updated_at) VALUES
(1, 'Başlangıç Paketi', 'BASIC', 5.0, 500, 100, 49.90, 0.50, 0.25, 0.15, NOW(), NOW()),
(2, 'Orta Paket', 'STANDARD', 15.0, 1000, 500, 89.90, 0.40, 0.20, 0.10, NOW(), NOW()),
(3, 'Premium Paket', 'PREMIUM', 50.0, 2000, 1000, 149.90, 0.30, 0.15, 0.08, NOW(), NOW());

-- Add-on packs
INSERT INTO turkcell.add_on_packs (addon_id, name, type, extra_gb, extra_min, extra_sms, price, created_at, updated_at) VALUES
(101, 'Sosyal Medya 5GB', 'data', 5.0, 0, 0, 19.90, NOW(), NOW()),
(102, 'Ek Arama 500dk', 'voice', 0, 500, 0, 29.90, NOW(), NOW()),
(103, 'SMS Paketi 1000', 'sms', 0, 0, 1000, 9.90, NOW(), NOW());

-- VAS catalog
INSERT INTO turkcell.vas_catalog (vas_id, name, monthly_fee, provider, created_at, updated_at) VALUES
('vas_tone', 'Zil Sesi', 4.90, 'Turkcell', NOW(), NOW()),
('vas_news', 'Haber Servisi', 2.90, 'Turkcell', NOW(), NOW()),
('vas_games', 'Oyun Paketi', 9.90, 'Third Party', NOW(), NOW());

-- Premium SMS catalog
INSERT INTO turkcell.premium_sms_catalog (shortcode, provider, unit_price, created_at, updated_at) VALUES
('5555', 'Spor Servisi', 2.00, NOW(), NOW()),
('6666', 'Haber Servisi', 1.50, NOW(), NOW()),
('7777', 'Eğlence Servisi', 3.00, NOW(), NOW());

-- Bill headers
INSERT INTO turkcell.bill_headers (bill_id, user_id, period_start, period_end, issue_date, total_amount, currency, created_at, updated_at) VALUES
(700101, 1001, '2025-01-01', '2025-01-31', '2025-02-01', 89.85, 'TRY', NOW(), NOW()),
(700102, 1001, '2025-02-01', '2025-02-28', '2025-03-01', 156.70, 'TRY', NOW(), NOW()),
(700103, 1001, '2025-03-01', '2025-03-31', '2025-04-01', 134.90, 'TRY', NOW(), NOW()),
(700201, 1002, '2025-01-01', '2025-01-31', '2025-02-01', 129.90, 'TRY', NOW(), NOW()),
(700202, 1002, '2025-02-01', '2025-02-28', '2025-03-01', 145.60, 'TRY', NOW(), NOW());

-- Bill items for user 1001, January 2025
INSERT INTO turkcell.bill_items (bill_id, item_id, category, subtype, description, amount, unit_price, quantity, tax_rate, created_at) VALUES
(700101, 1, 'VAS', 'plan_fee', 'Başlangıç Paketi', 49.90, 49.90, 1, 0.18, NOW()),
(700101, 2, 'DATA', 'data_overage', 'Data Aşımı', 15.00, 0.50, 30.0, 0.18, NOW()),
(700101, 3, 'PREMIUM_SMS', 'premium_3rdparty', 'Premium SMS', 12.00, 2.00, 6, 0.18, NOW()),
(700101, 4, 'VAS', 'vas_tone', 'Zil Sesi', 4.90, 4.90, 1, 0.18, NOW()),
(700101, 5, 'TAX', 'kdv', 'KDV', 8.05, 0.18, 1, 0.00, NOW());

-- Bill items for user 1001, February 2025 (anomaly month)
INSERT INTO turkcell.bill_items (bill_id, item_id, category, subtype, description, amount, unit_price, quantity, tax_rate, created_at) VALUES
(700102, 6, 'VAS', 'plan_fee', 'Başlangıç Paketi', 49.90, 49.90, 1, 0.18, NOW()),
(700102, 7, 'DATA', 'data_overage', 'Data Aşımı', 25.00, 0.50, 50.0, 0.18, NOW()),
(700102, 8, 'PREMIUM_SMS', 'premium_3rdparty', 'Premium SMS', 48.00, 2.00, 24, 0.18, NOW()),
(700102, 9, 'ROAMING', 'roaming_data', 'Yurt Dışı Data', 15.00, 0.10, 150.0, 0.18, NOW()),
(700102, 10, 'VAS', 'vas_tone', 'Zil Sesi', 4.90, 4.90, 1, 0.18, NOW()),
(700102, 11, 'VAS', 'vas_games', 'Oyun Paketi', 9.90, 9.90, 1, 0.18, NOW()),
(700102, 12, 'TAX', 'kdv', 'KDV', 13.00, 0.18, 1, 0.00, NOW());

-- Bill items for user 1001, March 2025
INSERT INTO turkcell.bill_items (bill_id, item_id, category, subtype, description, amount, unit_price, quantity, tax_rate, created_at) VALUES
(700103, 13, 'VAS', 'plan_fee', 'Başlangıç Paketi', 49.90, 49.90, 1, 0.18, NOW()),
(700103, 14, 'DATA', 'data_overage', 'Data Aşımı', 20.00, 0.50, 40.0, 0.18, NOW()),
(700103, 15, 'PREMIUM_SMS', 'premium_3rdparty', 'Premium SMS', 18.00, 2.00, 9, 0.18, NOW()),
(700103, 16, 'VAS', 'vas_tone', 'Zil Sesi', 4.90, 4.90, 1, 0.18, NOW()),
(700103, 17, 'VAS', 'vas_news', 'Haber Servisi', 2.90, 2.90, 1, 0.18, NOW()),
(700103, 18, 'TAX', 'kdv', 'KDV', 12.20, 0.18, 1, 0.00, NOW());

-- Bill items for user 1002, January 2025
INSERT INTO turkcell.bill_items (bill_id, item_id, category, subtype, description, amount, unit_price, quantity, tax_rate, created_at) VALUES
(700201, 19, 'VAS', 'plan_fee', 'Orta Paket', 89.90, 89.90, 1, 0.18, NOW()),
(700201, 20, 'DATA', 'data_overage', 'Data Aşımı', 25.00, 0.40, 62.5, 0.18, NOW()),
(700201, 21, 'VAS', 'vas_tone', 'Zil Sesi', 4.90, 4.90, 1, 0.18, NOW()),
(700201, 22, 'TAX', 'kdv', 'KDV', 9.10, 0.18, 1, 0.00, NOW());

-- Bill items for user 1002, February 2025
INSERT INTO turkcell.bill_items (bill_id, item_id, category, subtype, description, amount, unit_price, quantity, tax_rate, created_at) VALUES
(700202, 23, 'VAS', 'plan_fee', 'Orta Paket', 89.90, 89.90, 1, 0.18, NOW()),
(700202, 24, 'DATA', 'data_overage', 'Data Aşımı', 30.00, 0.40, 75.0, 0.18, NOW()),
(700202, 25, 'PREMIUM_SMS', 'premium_3rdparty', 'Premium SMS', 15.00, 2.00, 7.5, 0.18, NOW()),
(700202, 26, 'VAS', 'vas_tone', 'Zil Sesi', 4.90, 4.90, 1, 0.18, NOW()),
(700202, 27, 'TAX', 'kdv', 'KDV', 10.90, 0.18, 1, 0.00, NOW());

-- Usage daily data (mock)
INSERT INTO turkcell.usage_daily (user_id, date, mb_used, minutes_used, sms_used, roaming_mb, created_at, updated_at) VALUES
(1001, '2025-01-15', 2048, 45, 12, 0, NOW(), NOW()),
(1001, '2025-01-20', 3072, 67, 8, 0, NOW(), NOW()),
(1001, '2025-02-10', 4096, 89, 15, 0, NOW(), NOW()),
(1001, '2025-02-15', 5120, 120, 22, 150, NOW(), NOW()),
(1001, '2025-03-05', 3584, 78, 18, 0, NOW(), NOW()),
(1002, '2025-01-10', 8192, 156, 45, 0, NOW(), NOW()),
(1002, '2025-02-12', 10240, 189, 52, 0, NOW(), NOW());
