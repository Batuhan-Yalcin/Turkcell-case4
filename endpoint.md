# TRKCEL API Endpoint'leri

Bu dokümanda TRKCEL projesinin tüm API endpoint'leri ve açıklamaları bulunmaktadır.

## 🔐 Kimlik Doğrulama (Authentication)

### `/auth`
- **POST** `/auth/register` - Yeni kullanıcı kaydı
- **POST** `/auth/login` - Kullanıcı girişi
- **POST** `/auth/refresh` - Access token yenileme
- **GET** `/auth/me` - Mevcut kullanıcı bilgileri

## 👥 Kullanıcı Yönetimi (User Management)

### `/users`
- **GET** `/users` - Tüm kullanıcıları listele
- **GET** `/users/{id}` - ID ile kullanıcı getir
- **GET** `/users/msisdn/{msisdn}` - MSISDN ile kullanıcı getir
- **GET** `/users/type/{userType}` - Kullanıcı tipine göre listele
- **GET** `/users/{id}/bills` - Kullanıcının faturalarını getir
- **POST** `/users` - Yeni kullanıcı oluştur
- **PUT** `/users/{id}` - Kullanıcı bilgilerini güncelle
- **DELETE** `/users/{id}` - Kullanıcı sil

## 📊 Fatura Yönetimi (Billing)

### `/bills`
- **GET** `/bills/{billId}` - ID ile fatura getir
- **GET** `/bills/{userId}` - Kullanıcı ve dönem bazında fatura getir
- **GET** `/bills/{userId}/recent` - Kullanıcının son faturalarını getir
- **GET** `/bills/{billId}/items` - Fatura kalemlerini getir
- **GET** `/bills/{userId}/range` - Tarih aralığında faturaları getir

### `/anomalies`
- **POST** `/anomalies` - Anomali tespiti yap
- **GET** `/anomalies/{userId}/history` - Kullanıcının anomali geçmişi
- **GET** `/anomalies/{userId}/summary` - Kullanıcının anomali özeti

### `/usage`
- **GET** `/usage/{userId}/daily` - Günlük kullanım verileri
- **GET** `/usage/{userId}/daily/range` - Tarih aralığında günlük kullanım
- **GET** `/usage/{userId}/summary` - Kullanım özeti
- **GET** `/usage/{userId}/summary/range` - Tarih aralığında kullanım özeti
- **GET** `/usage/{userId}/trend` - Kullanım trendi analizi
- **GET** `/usage/{userId}/analysis/data` - Data kullanım analizi
- **GET** `/usage/{userId}/analysis/voice` - Ses kullanım analizi
- **GET** `/usage/{userId}/analysis/sms` - SMS kullanım analizi
- **GET** `/usage/{userId}/analysis/roaming` - Roaming kullanım analizi
- **GET** `/usage/{userId}/analysis/all` - Tüm kullanım analizleri

### `/explain`
- **POST** `/explain` - Fatura açıklaması
- **GET** `/explain/{billId}/summary` - Fatura özeti
- **GET** `/explain/{billId}/breakdown` - Kategori bazında ayrıştırma

## 🎁 Bonus ve Analiz (Bonus & Analytics)

### `/bonus`
- **POST** `/bonus/llm/anomaly` - Anomali için AI açıklaması
- **POST** `/bonus/llm/cohort` - Kohort analizi için AI açıklaması
- **POST** `/bonus/llm/tax` - Vergi analizi için AI açıklaması
- **GET** `/bonus/cohort/{userId}` - Kullanıcı kohort analizi
- **GET** `/bonus/cohort/{userId}/similar` - Benzer kullanıcıları bul
- **GET** `/bonus/tax/{billId}` - Fatura vergi ayrıştırması
- **GET** `/bonus/tax/{userId}/trend` - Kullanıcı vergi trendi
- **GET** `/bonus/autofix/{userId}/best` - En iyi autofix önerisi
- **GET** `/bonus/autofix/{userId}/scenarios` - Tüm autofix senaryoları
- **POST** `/bonus/autofix/{userId}/apply` - Autofix senaryosunu uygula
- **GET** `/bonus/{userId}/analysis` - Tüm bonus analizleri

## 📋 Katalog (Catalog)

### `/catalog`
- **GET** `/catalog` - Tam katalog
- **GET** `/catalog/plans` - Tarife planları
- **GET** `/catalog/addons` - Ek paketler
- **GET** `/catalog/vas` - Değer katkılı servisler
- **GET** `/catalog/premium-sms` - Premium SMS servisleri

## 🛒 Ödeme (Checkout)

### `/checkout`
- **POST** `/checkout` - Ödeme işlemi
- **GET** `/checkout/{orderId}/status` - Sipariş durumu
- **POST** `/checkout/validate` - Senaryo doğrulama

## 🔮 Simülasyon (Simulation)

### `/whatif`
- **POST** `/whatif` - Senaryo simülasyonu
- **GET** `/whatif/{userId}/scenarios` - Kullanıcı senaryoları
- **POST** `/whatif/compare` - Senaryo karşılaştırması

---

## 📝 Notlar

- Tüm endpoint'ler CORS desteği ile gelir
- Kimlik doğrulama gerektiren endpoint'ler JWT token kullanır
- Tarih parametreleri ISO formatında (YYYY-MM-DD) kabul edilir
- Dönem parametreleri string olarak (örn: "2024-01", "last-month") kabul edilir
- Kullanıcı ID'leri Long tipinde, MSISDN'ler String tipinde kabul edilir
