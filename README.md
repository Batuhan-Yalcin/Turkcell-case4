## Turkcell Fatura Asistanı – Şeffaf Fatura Açıklayıcı, Anomali Avcısı ve What-If Simülatörü

Bu proje, Turkcell müşterilerinin aylık faturalarını veri odaklı şekilde açıklayan, beklenmedik ücretleri (anomali) tespit eden ve alternatif plan/ek paket senaryolarını simüle ederek en düşük maliyetli seçenekleri öneren bir backend uygulamasıdır.

### Neler Sunar?
- Açıklanabilir fatura: Kalem kalem kategori, kısa açıklama ve tarih bazlı detay satırları
- Doğal dil özeti: Kullanım ve maliyet sürükleyicilerini yalın dille özetler
- Anomali tespiti: İstatistik (z-score, yüzde fark), yeni kalem ve Roaming/Premium SMS/VAS artışı
- What-If simülasyonu: Plan değişimi, ek paket ekleme, VAS/Premium SMS kapama senaryoları ve tasarruf hesapları
- Bonuslar: LLM destekli açıklamalar, kohort kıyası, vergi ayrıştırma, otomatik “autofix” önerisi


## Hızlı Başlangıç

### Gereksinimler
- JDK 17+
- Maven 3.9+ (veya repo içindeki `mvnw`)
- PostgreSQL 14+

### Kurulum
1) Veritabanını ayarla (varsayılan değerler `src/main/resources/application.properties`):
```
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.default_schema=turkcell
server.servlet.context-path=/api
server.port=8080
```

2) Demo verisini otomatik yükle: Uygulama ilk açılışta `src/main/resources/data.sql` dosyasını çalıştırır ve aşağıdaki tabloları örnek verilerle doldurur:
- `users`, `plans`, `add_on_packs`, `vas_catalog`, `premium_sms_catalog`
- `bill_headers`, `bill_items`, `usage_daily`

3) Çalıştır
```
mvn clean spring-boot:run
```
Uygulama: `http://localhost:8080/api`


## Veri Modeli (Özet)
- users: user_id, name, current_plan_id, type (INDIVIDUAL|CORPORATE), msisdn
- plans: plan_id, plan_name, type, quota_gb, quota_min, quota_sms, monthly_price, overage_gb/min/sms
- bill_headers: bill_id, user_id, period_start, period_end, issue_date, total_amount, currency
- bill_items: bill_id, item_id, category (data|voice|sms|roaming|premium_sms|vas|one_off|discount|tax), subtype, description, amount, unit_price, quantity, tax_rate, created_at
- usage_daily: user_id, date, mb_used, minutes_used, sms_used, roaming_mb
- vas_catalog / premium_sms_catalog / add_on_packs: referans kataloglar


## API Özeti

### Kullanıcılar
- GET `/api/users/{id}` → Kullanıcı detayı
- GET `/api/users` → Kullanıcı listesi
- GET `/api/users/msisdn/{msisdn}` → MSISDN ile kullanıcı
- GET `/api/users/type/{userType}` → Tür ile kullanıcılar

### Fatura
- GET `/api/bills/{billId}` → Fatura başlığı + kalemleri
- GET `/api/bills/{userId}?period=YYYY-MM` → Kullanıcı + dönem faturası
- GET `/api/bills/{billId}/items` → Fatura kalemleri
- GET `/api/bills/{billId}/summary` → Özet (Toplam, Vergiler, Kullanım Bazlı, Tek Seferlik)
- GET `/api/bills/{userId}/recent` → Son faturalar
- GET `/api/bills/{userId}/periods` → Mevcut dönemler
- GET `/api/bills/{userId}/range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` → Tarih aralığı faturaları

### Açıklanabilir Fatura
- POST `/api/explain` Body: `{"billId":700101}` →
  - `summary`: total, taxes, usageBasedCharges, oneTimeCharges, savingsHint
  - `breakdown`: kategori toplamları ve tarihli satır açıklamaları
  - `naturalLanguageSummary`: doğal dil özeti
- GET `/api/explain/{billId}/summary`
- GET `/api/explain/{billId}/breakdown?category=DATA|VOICE|SMS|ROAMING|PREMIUM_SMS|VAS|TAX|DISCOUNT|ONE_OFF`

### Anomali
- POST `/api/anomalies` Body: `{"userId":1001,"period":"2025-02"}` →
  - z-score / % fark, yeni kalem, roaming/premium/VAS artışı ve gerekçeler
- GET `/api/anomalies/{userId}/history`
- GET `/api/anomalies/{userId}/summary`

### What-If Simülasyonu
- POST `/api/whatif` Body:
```
{
  "userId": 1001,
  "period": "2025-02",
  "scenario": {
    "planId": 3,
    "addons": [101],
    "disableVas": true,
    "blockPremiumSms": true
  }
}
```
→ `newTotal`, `saving`, `details`, `recommendations`
- GET `/api/whatif/{userId}/scenarios`
- POST `/api/whatif/compare`
- GET `/api/whatif/{userId}/analysis?period=YYYY-MM` → en iyi 3-5 senaryo ve özet

### Catalog
- GET `/api/catalog` (plans, addons, vas, premium_sms)
- GET `/api/catalog/plans`
- GET `/api/catalog/addons`
- GET `/api/catalog/vas`
- GET `/api/catalog/premium-sms`

### Checkout (Mock)
- POST `/api/checkout` Body: `{ userId, actions:[{ type:"CHANGE_PLAN"|"ADD_ADDON"|"CANCEL_VAS"|"BLOCK_PREMIUM_SMS", payload:{} }] }`
→ 201: `{ status:"ok", orderId:"MOCK-FT-..." }`
- GET `/api/checkout/{orderId}/status`
- POST `/api/checkout/validate`
- POST `/api/checkout/preview`

### Bonus (LLM, Kohort, Vergi, Autofix)
- POST `/api/bonus/llm/anomaly` → LLM ile anomali açıklaması
- POST `/api/bonus/llm/cohort` → LLM ile kohort analizi metni
- POST `/api/bonus/llm/tax` → LLM ile vergi özeti
- GET `/api/bonus/cohort/{userId}?period=YYYY-MM`
- GET `/api/bonus/cohort/{userId}/similar?period=YYYY-MM`
- GET `/api/bonus/tax/{billId}`
- GET `/api/bonus/tax/{userId}/trend?months=3`
- GET `/api/bonus/autofix/{userId}/best?period=YYYY-MM`
- GET `/api/bonus/autofix/{userId}/scenarios?period=YYYY-MM`
- POST `/api/bonus/autofix/{userId}/apply?autofixId=...`
- GET `/api/bonus/{userId}/analysis?period=YYYY-MM` → Bonusların tamamı tek uçtan


## cURL Hızlı Örnekler
```
curl -s "http://localhost:8080/api/bills/1001?period=2025-02"
curl -s -X POST http://localhost:8080/api/explain -H "Content-Type: application/json" -d '{"billId":700101}'
curl -s -X POST http://localhost:8080/api/whatif -H "Content-Type: application/json" -d '{"userId":1001,"period":"2025-02","scenario":{"planId":3,"addons":[101],"disableVas":true}}'
```


## Açıklanabilir Fatura Mantığı
- Gruplama: `bill_items` → kategori bazlı toplamlar
- Satır üretimi örnekleri:
  - Data aşımı: `Ay içinde {x} GB aşım → {x}×{overage_gb} TL`
  - Premium SMS: `{shortcode} numarasına {n} SMS → {n}×{unit_price} TL (sağlayıcı: {provider})`
  - VAS: `{name} servisi aylık ücret {monthly_fee} TL`
- Doğal dil özeti: Premium SMS payı, roaming toplamı, VAS toplamı, tasarruf ipuçları


## Anomali Kuralları
- İstatistik: `this_month > mean(last_3) + 2σ` veya `%Δ > 80%`
- Yeni Kalem: Son 3 ayda görülmeyen alt tip ilk kez görünürse
- Roaming/Premium SMS/VAS artışı: Ani aktivasyon veya keskin artışta uyarı
- Her anomali için gerekçe: "Önceki ortalama 12 TL iken bu ay 58 TL" vb.


## What-If Hesaplama Mantığı
- Plan değişikliği: `new_total = new_plan.monthly_price + max(0, usage_gb - quota_gb)*overage_gb + ...`
- Ek paket: `effective_quota_gb = quota_gb + Σ(addons.extra_gb)`
- VAS/Premium SMS kapalı: ilgili kalemler yeni toplamdan çıkarılır
- Sıralama: En düşük `new_total` ilk; `saving = current_total - new_total`


## LLM Entegrasyonu (Opsiyonel)
- Ayarlar (`application.properties`):
```
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent
gemini.api.key=YOUR_KEY
```
- Servis ulaşılamazsa otomatik fallback metinleri döner (sistem çalışmaya devam eder).


## Güvenlik
- Projede JWT tabanlı güvenlik yapılandırması bulunur.
- Demo ve değerlendirme için isterseniz güvenliği devre dışı bırakabilir veya test token’ı kullanabilirsiniz.


## Proje Yapısı
```
src/main/java/com/turkcellcase4
  ├─ billing (controller/dto/mapper/model/repository/service)
  ├─ catalog (plan/add-on/VAS/premium SMS katalogu)
  ├─ checkout (mock checkout akışları)
  ├─ simulation (what-if senaryoları)
  ├─ security (JWT)
  ├─ user (kullanıcı domaini)
  └─ starter (Spring Boot giriş noktası)
```


## Teknolojiler
- Spring Boot (Web, Data JPA, Validation)
- PostgreSQL + HikariCP
- MapStruct (DTO ↔ Entity dönüştürme)
- Lombok
- WebClient (LLM çağrıları)


## Geliştirme ve Test
```
mvn clean compile
mvn test
mvn spring-boot:run
```


## 3 Dakikalık Demo Akışı (Öneri)
1) `GET /api/users` ile kullanıcıyı seçin → `GET /api/bills/{userId}?period=YYYY-MM`
2) `POST /api/explain` ile faturayı açıklayın → breakdown ve doğal dil özetini gösterin
3) `POST /api/anomalies` ile anomalileri gösterin (gerekçelerle)
4) `POST /api/whatif` ile farklı senaryoları deneyin, tasarrufu gösterin
5) `POST /api/checkout` ile (mock) en iyi senaryoyu “uygulayın”


## Bilinen Sınırlamalar
- LLM cevapları gerçek API anahtarına bağlıdır; anahtar yoksa fallback metinleri üretilir.
- Vergi ayrıştırma basitleştirilmiştir; örnek veride KDV ağırlıklıdır.


## Lisans
Bu proje Codenight Case sunumu için hazırlanmıştır.


