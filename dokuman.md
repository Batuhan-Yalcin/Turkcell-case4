# 🚀 Turkcell Case 4: Fatura Asistanı - Teknik Doküman

## 📋 Proje Genel Bakış
**Amaç:** 10 saat içinde fatura açıklayıcı, anomali tespit edici ve "what-if" simülatörü olan web uygulaması

**Teknolojiler:** Spring Boot 3.5.4, Java 17, PostgreSQL, JPA, Spring Security, Lombok

## 🏗️ Proje Mimarisi

```
src/main/java/com/turkcellcase4/
├── user/               # User Service modülü
├── billing/            # Billing Service modülü (Ana modül)
├── catalog/            # Catalog Service modülü
├── simulation/         # What-If Simulation modülü
├── security/           # Auth / JWT / Spring Security Config
└── common/             # Ortak kullanılan sınıflar
```

## 📊 Veri Modeli (Entity Sınıfları)

### 1. User Entity
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "current_plan_id")
    private Long currentPlanId;
    
    @Enumerated(EnumType.STRING)
    private UserType type;
    
    @Column(unique = true)
    private String msisdn;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Bill> bills;
}

public enum UserType {
    INDIVIDUAL, CORPORATE, PREPAID
}
```

### 2. Plan Entity
```java
@Entity
@Table(name = "plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;
    
    @Column(name = "plan_name", nullable = false)
    private String planName;
    
    @Enumerated(EnumType.STRING)
    private PlanType type;
    
    @Column(name = "quota_gb")
    private Double quotaGb;
    
    @Column(name = "quota_min")
    private Integer quotaMin;
    
    @Column(name = "quota_sms")
    private Integer quotaSms;
    
    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;
    
    @Column(name = "overage_gb")
    private BigDecimal overageGb;
    
    @Column(name = "overage_min")
    private BigDecimal overageMin;
    
    @Column(name = "overage_sms")
    private BigDecimal overageSms;
}

public enum PlanType {
    BASIC, PREMIUM, UNLIMITED, CORPORATE
}
```

### 3. Bill Entity
```java
@Entity
@Table(name = "bill_headers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private String currency;
    
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BillItem> items;
}
```

### 4. BillItem Entity
```java
@Entity
@Table(name = "bill_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;
    
    @Column(nullable = false)
    private String subtype;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "tax_rate")
    private BigDecimal taxRate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

public enum ItemCategory {
    DATA, VOICE, SMS, ROAMING, PREMIUM_SMS, VAS, ONE_OFF, DISCOUNT, TAX
}
```

### 5. UsageDaily Entity
```java
@Entity
@Table(name = "usage_daily")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "mb_used")
    private Double mbUsed;
    
    @Column(name = "minutes_used")
    private Integer minutesUsed;
    
    @Column(name = "sms_used")
    private Integer smsUsed;
    
    @Column(name = "roaming_mb")
    private Double roamingMb;
}
```

### 6. VAS Entity
```java
@Entity
@Table(name = "vas_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VAS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vasId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "monthly_fee", nullable = false)
    private BigDecimal monthlyFee;
    
    @Column(nullable = false)
    private String provider;
}
```

### 7. PremiumSMS Entity
```java
@Entity
@Table(name = "premium_sms_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumSMS {
    @Id
    @Column(name = "shortcode", nullable = false)
    private String shortcode;
    
    @Column(nullable = false)
    private String provider;
    
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
}
```

### 8. AddOnPack Entity
```java
@Entity
@Table(name = "add_on_packs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddOnPack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addonId;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    private AddOnType type;
    
    @Column(name = "extra_gb")
    private Double extraGb;
    
    @Column(name = "extra_min")
    private Integer extraMin;
    
    @Column(name = "extra_sms")
    private Integer extraSms;
    
    @Column(nullable = false)
    private BigDecimal price;
}

public enum AddOnType {
    DATA, VOICE, SMS, SOCIAL, INTERNATIONAL
}
```

## 🔧 Gerekli Dependencies (pom.xml'e eklenecek)

```xml
<!-- MapStruct için -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>

<!-- Validation için -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## 📝 DTO Sınıfları

### 1. User DTO'ları
```java
// UserResponseDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long userId;
    private String name;
    private Long currentPlanId;
    private UserType type;
    private String msisdn;
}

// UserListDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListDTO {
    private List<UserResponseDTO> users;
}
```

### 2. Bill DTO'ları
```java
// BillResponseDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponseDTO {
    private Long billId;
    private Long userId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private String currency;
    private List<BillItemDTO> items;
}

// BillItemDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemDTO {
    private Long itemId;
    private ItemCategory category;
    private String subtype;
    private String description;
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal taxRate;
}

// BillListDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillListDTO {
    private List<BillResponseDTO> bills;
}
```

### 3. Explain DTO'ları
```java
// ExplainRequestDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplainRequestDTO {
    @NotNull(message = "Bill ID is required")
    private Long billId;
}

// ExplainResponseDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplainResponseDTO {
    private BillSummaryDTO summary;
    private List<CategoryBreakdownDTO> breakdown;
    private String naturalLanguageSummary;
}

// BillSummaryDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillSummaryDTO {
    private BigDecimal totalAmount;
    private BigDecimal taxes;
    private BigDecimal usageBasedCharges;
    private BigDecimal oneTimeCharges;
    private String savingsHint;
}

// CategoryBreakdownDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBreakdownDTO {
    private ItemCategory category;
    private BigDecimal total;
    private List<BillItemDTO> lines;
    private String explanation;
}
```

### 4. Anomaly DTO'ları
```java
// AnomalyRequestDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Period is required")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "Period must be in YYYY-MM format")
    private String period;
}

// AnomalyResponseDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyResponseDTO {
    private List<AnomalyDTO> anomalies;
}

// AnomalyDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDTO {
    private ItemCategory category;
    private String subtype;
    private BigDecimal delta;
    private BigDecimal percentageChange;
    private String reason;
    private String suggestedAction;
    private AnomalyType type;
}

public enum AnomalyType {
    SPIKE, NEW_ITEM, ROAMING_ACTIVATION, PREMIUM_SMS_INCREASE
}
```

### 5. What-If Simulation DTO'ları
```java
// SimulationRequestDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Period is required")
    private String period;
    
    @Valid
    private SimulationScenarioDTO scenario;
}

// SimulationScenarioDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationScenarioDTO {
    private Long planId;
    private List<Long> addons;
    private Boolean disableVas;
    private Boolean blockPremiumSms;
}

// SimulationResponseDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponseDTO {
    private BigDecimal newTotal;
    private BigDecimal currentTotal;
    private BigDecimal saving;
    private String details;
    private List<ScenarioComparisonDTO> topScenarios;
}

// ScenarioComparisonDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioComparisonDTO {
    private String scenarioName;
    private BigDecimal totalCost;
    private BigDecimal saving;
    private String description;
}
```

### 6. Catalog DTO'ları
```java
// CatalogResponseDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResponseDTO {
    private List<PlanDTO> plans;
    private List<AddOnDTO> addons;
    private List<VASDTO> vas;
    private List<PremiumSMSDTO> premiumSms;
}

// PlanDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDTO {
    private Long planId;
    private String planName;
    private PlanType type;
    private Double quotaGb;
    private Integer quotaMin;
    private Integer quotaSms;
    private BigDecimal monthlyPrice;
    private BigDecimal overageGb;
    private BigDecimal overageMin;
    private BigDecimal overageSms;
}

// AddOnDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddOnDTO {
    private Long addonId;
    private String name;
    private AddOnType type;
    private Double extraGb;
    private Integer extraMin;
    private Integer extraSms;
    private BigDecimal price;
}

// VASDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VASDTO {
    private Long vasId;
    private String name;
    private BigDecimal monthlyFee;
    private String provider;
}

// PremiumSMSDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumSMSDTO {
    private String shortcode;
    private String provider;
    private BigDecimal unitPrice;
}
```

### 7. Checkout DTO'ları
```java
// CheckoutRequestDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotEmpty(message = "Actions list cannot be empty")
    private List<CheckoutActionDTO> actions;
}

// CheckoutActionDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutActionDTO {
    @NotNull(message = "Action type is required")
    private ActionType type;
    
    private Map<String, Object> payload;
}

public enum ActionType {
    CHANGE_PLAN, ADD_ADDON, CANCEL_VAS, BLOCK_PREMIUM_SMS
}

// CheckoutResponseDTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDTO {
    private String status;
    private String orderId;
    private String message;
}
```

## 🗄️ Repository Interfaces

### 1. User Repository
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMsisdn(String msisdn);
    List<User> findByType(UserType type);
}
```

### 2. Bill Repository
```java
@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByUserIdAndPeriodStartBetween(Long userId, LocalDate start, LocalDate end);
    
    @Query("SELECT b FROM Bill b WHERE b.user.userId = :userId AND b.periodStart >= :startDate")
    List<Bill> findRecentBillsByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT b FROM Bill b WHERE b.user.userId = :userId AND FUNCTION('YEAR', b.periodStart) = :year AND FUNCTION('MONTH', b.periodStart) = :month")
    Optional<Bill> findByUserIdAndPeriod(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
}
```

### 3. BillItem Repository
```java
@Repository
public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    List<BillItem> findByBillId(Long billId);
    
    @Query("SELECT bi FROM BillItem bi WHERE bi.bill.user.userId = :userId AND bi.category = :category AND bi.bill.periodStart >= :startDate")
    List<BillItem> findByUserIdAndCategoryAndPeriod(@Param("userId") Long userId, @Param("category") ItemCategory category, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT bi.category, SUM(bi.amount) FROM BillItem bi WHERE bi.bill.billId = :billId GROUP BY bi.category")
    List<Object[]> getCategoryTotalsByBillId(@Param("billId") Long billId);
}
```

### 4. Plan Repository
```java
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByType(PlanType type);
    
    @Query("SELECT p FROM Plan p WHERE p.monthlyPrice BETWEEN :minPrice AND :maxPrice")
    List<Plan> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
}
```

### 5. UsageDaily Repository
```java
@Repository
public interface UsageDailyRepository extends JpaRepository<UsageDaily, Long> {
    List<UsageDaily> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT AVG(ud.mbUsed) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Double getAverageDataUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT AVG(ud.minutesUsed) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Double getAverageVoiceUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT SUM(ud.roamingMb) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Double getTotalRoamingUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}
```

### 6. VAS Repository
```java
@Repository
public interface VASRepository extends JpaRepository<VAS, Long> {
    List<VAS> findByProvider(String provider);
    
    @Query("SELECT v FROM VAS v WHERE v.monthlyFee <= :maxFee")
    List<VAS> findByMaxMonthlyFee(@Param("maxFee") BigDecimal maxFee);
}
```

### 7. AddOnPack Repository
```java
@Repository
public interface AddOnPackRepository extends JpaRepository<AddOnPack, Long> {
    List<AddOnPack> findByType(AddOnType type);
    
    @Query("SELECT a FROM AddOnPack a WHERE a.price <= :maxPrice")
    List<AddOnPack> findByMaxPrice(@Param("maxPrice") BigDecimal maxPrice);
}
```

## 🌐 API Endpoints

### 1. User Controller
```
GET    /api/users/{id}                    # Kullanıcı bilgilerini getir
GET    /api/users                         # Tüm kullanıcıları listele
GET    /api/users/{id}/bills              # Kullanıcının faturalarını getir
```

### 2. Bill Controller
```
GET    /api/bills/{user_id}?period=YYYY-MM    # Belirli dönem faturasını getir
GET    /api/bills/{user_id}/recent            # Son faturaları getir
GET    /api/bills/{bill_id}/items             # Fatura kalemlerini getir
```

### 3. Explain Controller
```
POST   /api/explain                           # Fatura açıklaması
GET    /api/explain/{bill_id}/summary         # Fatura özeti
GET    /api/explain/{bill_id}/breakdown       # Kategori bazlı dağılım
```

### 4. Anomaly Controller
```
POST   /api/anomalies                         # Anomali tespiti
GET    /api/anomalies/{user_id}/history       # Anomali geçmişi
GET    /api/anomalies/{user_id}/summary       # Anomali özeti
```

### 5. What-If Simulation Controller
```
POST   /api/whatif                            # Senaryo simülasyonu
GET    /api/whatif/{user_id}/scenarios        # Mevcut senaryolar
POST   /api/whatif/compare                    # Senaryo karşılaştırması
```

### 6. Catalog Controller
```
GET    /api/catalog                           # Tüm katalog bilgileri
GET    /api/catalog/plans                     # Plan listesi
GET    /api/catalog/addons                    # Ek paket listesi
GET    /api/catalog/vas                       # VAS listesi
GET    /api/catalog/premium-sms               # Premium SMS listesi
```

### 7. Checkout Controller
```
POST   /api/checkout                          # Mock işlem
GET    /api/checkout/{order_id}/status        # İşlem durumu
POST   /api/checkout/validate                 # Senaryo validasyonu
```

## 🔄 Mapper Interfaces

### 1. User Mapper
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toUserResponseDTO(User user);
    List<UserResponseDTO> toUserResponseDTOList(List<User> users);
    UserListDTO toUserListDTO(List<User> users);
}
```

### 2. Bill Mapper
```java
@Mapper(componentModel = "spring")
public interface BillMapper {
    BillResponseDTO toBillResponseDTO(Bill bill);
    BillItemDTO toBillItemDTO(BillItem billItem);
    List<BillResponseDTO> toBillResponseDTOList(List<Bill> bills);
    BillListDTO toBillListDTO(List<Bill> bills);
}
```

### 3. Explain Mapper
```java
@Mapper(componentModel = "spring")
public interface ExplainMapper {
    BillSummaryDTO toBillSummaryDTO(Bill bill);
    CategoryBreakdownDTO toCategoryBreakdownDTO(ItemCategory category, BigDecimal total, List<BillItem> items);
    ExplainResponseDTO toExplainResponseDTO(Bill bill, BillSummaryDTO summary, List<CategoryBreakdownDTO> breakdown, String naturalLanguageSummary);
}
```

### 4. Anomaly Mapper
```java
@Mapper(componentModel = "spring")
public interface AnomalyMapper {
    AnomalyDTO toAnomalyDTO(ItemCategory category, String subtype, BigDecimal delta, BigDecimal percentageChange, String reason, String suggestedAction, AnomalyType type);
    AnomalyResponseDTO toAnomalyResponseDTO(List<AnomalyDTO> anomalies);
}
```

### 5. Simulation Mapper
```java
@Mapper(componentModel = "spring")
public interface SimulationMapper {
    SimulationResponseDTO toSimulationResponseDTO(BigDecimal newTotal, BigDecimal currentTotal, BigDecimal saving, String details, List<ScenarioComparisonDTO> topScenarios);
    ScenarioComparisonDTO toScenarioComparisonDTO(String scenarioName, BigDecimal totalCost, BigDecimal saving, String description);
}
```

### 6. Catalog Mapper
```java
@Mapper(componentModel = "spring")
public interface CatalogMapper {
    PlanDTO toPlanDTO(Plan plan);
    AddOnDTO toAddOnDTO(AddOnPack addOnPack);
    VASDTO toVASDTO(VAS vas);
    PremiumSMSDTO toPremiumSMSDTO(PremiumSMS premiumSMS);
    CatalogResponseDTO toCatalogResponseDTO(List<Plan> plans, List<AddOnPack> addOnPacks, List<VAS> vasList, List<PremiumSMS> premiumSMSList);
}
```

## 📊 Mock Data Yapısı

### 1. users.json
```json
[
  {
    "userId": 1001,
    "name": "Ahmet Yılmaz",
    "currentPlanId": 1,
    "type": "INDIVIDUAL",
    "msisdn": "5551234567"
  }
]
```

### 2. plans.json
```json
[
  {
    "planId": 1,
    "planName": "Temel 5GB",
    "type": "BASIC",
    "quotaGb": 5.0,
    "quotaMin": 100,
    "quotaSms": 100,
    "monthlyPrice": 49.90,
    "overageGb": 2.50,
    "overageMin": 0.25,
    "overageSms": 0.10
  }
]
```

### 3. bill_headers.json
```json
[
  {
    "billId": 700101,
    "userId": 1001,
    "periodStart": "2025-07-01",
    "periodEnd": "2025-07-31",
    "issueDate": "2025-08-05",
    "totalAmount": 87.45,
    "currency": "TRY"
  }
]
```

### 4. bill_items.json
```json
[
  {
    "itemId": 1,
    "billId": 700101,
    "category": "DATA",
    "subtype": "data_overage",
    "description": "5GB aşım ücreti",
    "amount": 12.50,
    "unitPrice": 2.50,
    "quantity": 5,
    "taxRate": 0.18,
    "createdAt": "2025-08-05T10:00:00"
  }
]
```

## 🎯 Implementasyon Sırası

### 1. Saat 1-2: Proje Yapısı + Entity'ler
- Proje klasör yapısını oluştur
- Entity sınıflarını yaz
- Enum'ları tanımla

### 2. Saat 3-4: Repository + DTO'lar
- Repository interface'lerini yaz
- DTO sınıflarını oluştur
- Mapper interface'lerini tanımla

### 3. Saat 5-6: Service + Business Logic
- Service katmanını implement et
- Anomali tespit algoritmalarını yaz
- What-if simülasyon mantığını geliştir

### 4. Saat 7-8: Controller + API
- REST endpoint'lerini implement et
- Validation ekle
- Error handling yap

### 5. Saat 9-10: Mock Data + Test + Demo
- Mock data dosyalarını hazırla
- API'leri test et
- Demo senaryolarını hazırla

## 🏆 Puanlama Kriterleri

- **Kod Kalitesi (30%):** ✅ Katmanlı mimari, temiz kod
- **Doğruluk (20%):** ✅ Kalem eşleşmesi, açıklamalar
- **Analitik (15%):** ✅ Anomali kuralları, z-score
- **UI/UX (20%):** ✅ Hızlı API, kullanıcı deneyimi
- **Özellik Tamamlığı (10%):** ✅ Tüm MVP özellikleri
- **Bonus (5%):** ✅ Modüler yapı, profesyonel mimari
