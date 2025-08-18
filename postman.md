# 🚀 Turkcell Case 4: Fatura Asistanı - Postman Test Dokümanı

## 📋 Genel Bilgiler
- **Base URL:** `http://localhost:8080/api`
- **Port:** 8080
- **Context Path:** /api
- **Database:** PostgreSQL (turkcell şeması)

## 🔐 Authentication
Şu anda tüm endpoint'ler public olarak ayarlandı. JWT implementasyonu hazır ama henüz aktif değil.

## 📊 API Endpoint'leri

### 1. 👤 User Management

#### 1.1 Tüm Kullanıcıları Listele
```
GET /api/users
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/users`
- Headers: `Content-Type: application/json`

**Expected Response:**
```json
{
  "users": [
    {
      "userId": 1,
      "name": "Ahmet Yılmaz",
      "currentPlanId": 1,
      "type": "INDIVIDUAL",
      "msisdn": "5551234567"
    }
  ]
}
```

#### 1.2 Kullanıcı ID ile Getir
```
GET /api/users/{id}
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/users/1`
- Headers: `Content-Type: application/json`

#### 1.3 MSISDN ile Kullanıcı Getir
```
GET /api/users/msisdn/{msisdn}
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/users/msisdn/5551234567`
- Headers: `Content-Type: application/json`

#### 1.4 Kullanıcı Tipine Göre Listele
```
GET /api/users/type/{userType}
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/users/type/INDIVIDUAL`
- Headers: `Content-Type: application/json`

**User Types:** `INDIVIDUAL`, `CORPORATE`, `PREPAID`

#### 1.5 Yeni Kullanıcı Oluştur
```
POST /api/users
```
**Postman Setup:**
- Method: `POST`
- URL: `http://localhost:8080/api/users`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "name": "Mehmet Demir",
  "currentPlanId": 2,
  "type": "INDIVIDUAL",
  "msisdn": "5559876543"
}
```

#### 1.6 Kullanıcı Güncelle
```
PUT /api/users/{id}
```
**Postman Setup:**
- Method: `PUT`
- URL: `http://localhost:8080/api/users/1`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "name": "Ahmet Yılmaz Güncellendi",
  "currentPlanId": 3,
  "type": "INDIVIDUAL",
  "msisdn": "5551234567"
}
```

#### 1.7 Kullanıcı Sil
```
DELETE /api/users/{id}
```
**Postman Setup:**
- Method: `DELETE`
- URL: `http://localhost:8080/api/users/1`
- Headers: `Content-Type: application/json`

### 2. 💰 Billing Management

#### 2.1 Fatura ID ile Getir
```
GET /api/bills/{billId}
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/bills/1`
- Headers: `Content-Type: application/json`

#### 2.2 Kullanıcı ve Dönem ile Fatura Getir
```
GET /api/bills/{userId}?period=YYYY-MM
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/bills/1?period=2025-07`
- Headers: `Content-Type: application/json`

**Expected Response:**
```json
{
  "billId": 1,
  "userId": 1,
  "periodStart": "2025-07-01",
  "periodEnd": "2025-07-31",
  "issueDate": "2025-08-05",
  "totalAmount": 87.45,
  "currency": "TRY",
  "items": [
    {
      "itemId": 1,
      "category": "DATA",
      "subtype": "data_overage",
      "description": "5GB aşım ücreti",
      "amount": 12.50,
      "unitPrice": 2.50,
      "quantity": 5,
      "taxRate": 0.18
    }
  ]
}
```

#### 2.3 Kullanıcının Son Faturalarını Getir
```
GET /api/bills/{userId}/recent
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/bills/1/recent`
- Headers: `Content-Type: application/json`

#### 2.4 Fatura Kalemlerini Getir
```
GET /api/bills/{billId}/items
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/bills/1/items`
- Headers: `Content-Type: application/json`

#### 2.5 Tarih Aralığında Faturaları Getir
```
GET /api/bills/{userId}/range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/bills/1/range?startDate=2025-01-01&endDate=2025-12-31`
- Headers: `Content-Type: application/json`

### 3. 📚 Catalog Management

#### 3.1 Tüm Katalog Bilgilerini Getir
```
GET /api/catalog
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/catalog`
- Headers: `Content-Type: application/json`

#### 3.2 Plan Listesini Getir
```
GET /api/catalog/plans
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/catalog/plans`
- Headers: `Content-Type: application/json`

#### 3.3 Ek Paket Listesini Getir
```
GET /api/catalog/addons
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/catalog/addons`
- Headers: `Content-Type: application/json`

#### 3.4 VAS Listesini Getir
```
GET /api/catalog/vas
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/catalog/vas`
- Headers: `Content-Type: application/json`

#### 3.5 Premium SMS Listesini Getir
```
GET /api/catalog/premium-sms
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/catalog/premium-sms`
- Headers: `Content-Type: application/json`

### 4. 🔍 Explain (Fatura Açıklama)

#### 4.1 Fatura Açıklaması
```
POST /api/explain
```
**Postman Setup:**
- Method: `POST`
- URL: `http://localhost:8080/api/explain`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "billId": 1
}
```

#### 4.2 Fatura Özeti
```
GET /api/explain/{billId}/summary
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/explain/1/summary`
- Headers: `Content-Type: application/json`

#### 4.3 Kategori Bazlı Dağılım
```
GET /api/explain/{billId}/breakdown
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/explain/1/breakdown`
- Headers: `Content-Type: application/json`

### 5. 🚨 Anomaly Detection

#### 5.1 Anomali Tespiti
```
POST /api/anomalies
```
**Postman Setup:**
- Method: `POST`
- URL: `http://localhost:8080/api/anomalies`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "userId": 1,
  "period": "2025-07"
}
```

#### 5.2 Anomali Geçmişi
```
GET /api/anomalies/{userId}/history
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/anomalies/1/history`
- Headers: `Content-Type: application/json`

#### 5.3 Anomali Özeti
```
GET /api/anomalies/{userId}/summary
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/anomalies/1/summary`
- Headers: `Content-Type: application/json`

### 6. 🎯 What-If Simulation

#### 6.1 Senaryo Simülasyonu
```
POST /api/whatif
```
**Postman Setup:**
- Method: `POST`
- URL: `http://localhost:8080/api/whatif`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "userId": 1,
  "period": "2025-07",
  "scenario": {
    "planId": 3,
    "addons": [101],
    "disableVas": true,
    "blockPremiumSms": false
  }
}
```

#### 6.2 Mevcut Senaryolar
```
GET /api/whatif/{userId}/scenarios
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/whatif/1/scenarios`
- Headers: `Content-Type: application/json`

#### 6.3 Senaryo Karşılaştırması
```
POST /api/whatif/compare
```
**Postman Setup:**
- Method: `POST`
- URL: `http://localhost:8080/api/whatif/compare`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "userId": 1,
  "period": "2025-07",
  "scenarios": [
    {
      "planId": 2,
      "addons": []
    },
    {
      "planId": 3,
      "addons": [101]
    }
  ]
}
```

### 7. 📊 Usage Analysis

#### 7.1 Günlük Kullanım Verileri
```
GET /api/usage/{userId}/daily?period=YYYY-MM
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/usage/1001/daily?period=2025-01`
- Headers: `Content-Type: application/json`

#### 7.2 Kullanım Özeti
```
GET /api/usage/{userId}/summary?period=YYYY-MM
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/usage/1001/summary?period=2025-01`
- Headers: `Content-Type: application/json`

#### 7.3 Data Kullanım Analizi
```
GET /api/usage/{userId}/analysis/data?period=YYYY-MM
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/usage/1001/analysis/data?period=2025-01`
- Headers: `Content-Type: application/json`

#### 7.4 Ses Kullanım Analizi
```
GET /api/usage/{userId}/analysis/voice?period=YYYY-MM
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/usage/1001/analysis/voice?period=2025-01`
- Headers: `Content-Type: application/json`

#### 7.5 SMS Kullanım Analizi
```
GET /api/usage/{userId}/analysis/sms?period=YYYY-MM
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/usage/1001/analysis/sms?period=2025-01`
- Headers: `Content-Type: application/json`

#### 7.6 Roaming Kullanım Analizi
```
GET /api/usage/{userId}/analysis/roaming?period=YYYY-MM
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/usage/1001/analysis/roaming?period=2025-01`
- Headers: `Content-Type: application/json`

### 8. 🛒 Checkout (Mock)

#### 8.1 Mock İşlem
```
POST /api/checkout
```
**Postman Setup:**
- Method: `POST`
- URL: `http://localhost:8080/api/checkout`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "userId": 1,
  "actions": [
    {
      "type": "CHANGE_PLAN",
      "payload": {
        "planId": 3
      }
    }
  ]
}
```

#### 7.2 İşlem Durumu
```
GET /api/checkout/{orderId}/status
```
**Postman Setup:**
- Method: `GET`
- URL: `http://localhost:8080/api/checkout/MOCK-FT-123/status`
- Headers: `Content-Type: application/json`

#### 7.3 Senaryo Validasyonu
```
POST /api/checkout/validate
```
**Postman Setup:**
- Method: `POST`
- URL: `http://localhost:8080/api/checkout/validate`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "userId": 1,
  "scenario": {
    "planId": 3,
    "addons": [101]
  }
}
```

## 🧪 Test Senaryoları

### Senaryo 1: Temel Kullanıcı İşlemleri
1. Yeni kullanıcı oluştur
2. Kullanıcı bilgilerini getir
3. Kullanıcı bilgilerini güncelle
4. Kullanıcıyı sil

### Senaryo 2: Fatura İşlemleri
1. Kullanıcı için fatura oluştur
2. Fatura detaylarını getir
3. Fatura kalemlerini listele
4. Fatura açıklaması al

### Senaryo 3: Anomali Tespiti
1. Kullanıcı için anomali analizi yap
2. Anomali geçmişini getir
3. Anomali özetini al

### Senaryo 4: What-If Simülasyonu
1. Farklı plan senaryoları oluştur
2. Simülasyon sonuçlarını karşılaştır
3. En iyi senaryoyu seç

### Senaryo 5: Kullanım Analizi
1. Kullanıcının günlük kullanım verilerini getir
2. Kullanım özetini al
3. Data, ses, SMS ve roaming analizlerini yap
4. Kullanım trendini incele

### Senaryo 6: Checkout İşlemi
1. Senaryo validasyonu yap
2. Mock checkout işlemi gerçekleştir
3. İşlem durumunu kontrol et

## 📝 Postman Collection Import

Postman'de "Import" butonuna tıklayıp aşağıdaki JSON'u import edebilirsiniz:

```json
{
  "info": {
    "name": "Turkcell Case 4 - Fatura Asistanı",
    "description": "Turkcell Case 4 API test collection",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Users",
      "item": [
        {
          "name": "Get All Users",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/users",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "users"]
            }
          }
        },
        {
          "name": "Get User by ID",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/users/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "users", "1"]
            }
          }
        }
      ]
    },
    {
      "name": "Bills",
      "item": [
        {
          "name": "Get Bill by ID",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "http://localhost:8080/api/bills/1",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "bills", "1"]
            }
          }
        }
      ]
    }
  ]
}
```

## 🚀 Hızlı Test

### 1. Uygulama Çalışıyor mu?
```
GET http://localhost:8080/api/users
```

### 2. Veritabanı Bağlantısı
```
GET http://localhost:8080/api/catalog
```

### 3. Fatura Sistemi
```
GET http://localhost:8080/api/bills/1
```

### 4. Kullanım Analizi
```
GET http://localhost:8080/api/usage/1001/summary?period=2025-01
```

## ⚠️ Önemli Notlar

1. **Port:** Uygulama 8080 portunda çalışıyor
2. **Context Path:** Tüm endpoint'ler `/api` ile başlıyor
3. **Database:** PostgreSQL'de `turkcell` şeması kullanılıyor
4. **CORS:** Tüm origin'lere izin veriliyor
5. **Validation:** Request body'lerde validation aktif

## 🔧 Hata Durumları

### 404 Not Found
- Endpoint yanlış yazıldı
- ID bulunamadı

### 400 Bad Request
- Request body formatı yanlış
- Validation hatası

### 500 Internal Server Error
- Database bağlantı hatası
- Service katmanında hata

## 📊 Test Sonuçları

Her test sonrasında aşağıdaki bilgileri not edin:
- ✅ Başarılı endpoint'ler
- ❌ Hatalı endpoint'ler
- ⏱️ Response süreleri
- 📊 Response boyutları
- 🔍 Beklenen vs gerçek sonuçlar

Bu doküman ile tüm API'leri test edebilir ve Case 4 gereksinimlerini karşıladığınızı doğrulayabilirsiniz! 🎯
