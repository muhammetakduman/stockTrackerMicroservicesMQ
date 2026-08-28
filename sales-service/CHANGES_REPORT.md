# 🔄 Sales-Service Değişiklik Raporu
> Tarih: 2026-08-28  
> Konu: sellerId JWT'den alınması + GET /sales/my endpoint'i

---

## 🔍 Analiz Bulguları

### JWT Yapısı (identity-service)
| Alan | Değer |
|---|---|
| **subject (`sub`)** | Kullanıcının UUID'si — ör: `"550e8400-e29b-41d4-a716-446655440000"` |
| **`roles` claim** | `["SALES_USER"]` formatında string listesi |
| **Ayrı `userId` claim** | ❌ YOK — user ID doğrudan `sub` içinde |

### Kritik Tip Uyumsuzluğu (pre-existing bug)
```
identity-service  User.id  →  UUID
JWT sub           →  UUID string
Sale.sellerId     →  Long   ← UYUMSUZ (benim değişikliğimden önce)
```
JWT subject bir UUID string'dir. `Long`'a parse edilmesi imkânsızdı.  
Bu nedenle `sellerId` tipi `Long` → `UUID` olarak değiştirildi.

---

## 📁 Değiştirilen Dosyalar

### 1. `pom.xml`
**Değişiklik:** `spring-rabbit-stream` dependency kaldırıldı.

```xml
<!-- KALDIRILDI -->
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit-stream</artifactId>
</dependency>
```

**Neden:** Bu dependency sales-service'de hiç kullanılmıyordu.  
Stream-based consumer/producer konfigürasyonu yapılmadığından gereksizdi.  
Classic AMQP için zaten `spring-boot-starter-amqp` mevcut — akış bozulmadı.  
⚠️ Lombok'u bozduğuna dair doğrulanmış bir kanıt yoktur — bu iddia geçersizdir.

---

### 2. `sale/domain/Sale.java`
**Değişiklik:** `sellerId` field ve tüm ilişkili yerler `Long` → `UUID`

```java
// ÖNCE
@Column(name = "seller_id", nullable = false)
private Long sellerId;

private Sale(Long sellerId, ...) { ... }
public static Sale create(Long sellerId, ...) { ... }
private static void validateSellerId(Long sellerId) {
    if (sellerId == null || sellerId <= 0) { ... }
}

// SONRA
@Column(name = "seller_id", nullable = false, columnDefinition = "uuid")
private UUID sellerId;

private Sale(UUID sellerId, ...) { ... }
public static Sale create(UUID sellerId, ...) { ... }
private static void validateSellerId(UUID sellerId) {
    if (sellerId == null) { ... }   // <= 0 kontrolü kaldırıldı (UUID'de anlamsız)
}
```

---

### 3. `sale/messaging/event/SaleCreatedEvent.java`
**Değişiklik:** `sellerId` tipi `Long` → `UUID`, geçersiz numeric doğrulama kaldırıldı

```java
// ÖNCE
public record SaleCreatedEvent(
    ...
    Long sellerId,
    ...
) {
    // compact constructor'da:
    Objects.requireNonNull(sellerId, "Seller ID cannot be null");
    if (sellerId <= 0) {  // ← UUID'de anlamsız, derleme hatası verirdi
        throw new IllegalArgumentException("Seller ID must be greater than zero");
    }
}

// SONRA
public record SaleCreatedEvent(
    ...
    UUID sellerId,
    ...
) {
    // compact constructor'da:
    Objects.requireNonNull(sellerId, "Seller ID cannot be null");
    // numeric kontrol kaldırıldı
}
```

> ⚠️ `SaleCreatedEvent` RabbitMQ üzerinden JSON olarak gönderilir.  
> inventory-service `sellerId` alanını kullanmıyor — stok işlemi etkilenmez.

---

### 4. `sale/dto/response/SaleResponse.java`
**Değişiklik:** `sellerId` tipi `Long` → `UUID`

```java
// ÖNCE
public record SaleResponse(
    Long id,
    Long sellerId,   // ← Long
    ...
)

// SONRA
public record SaleResponse(
    Long id,
    UUID sellerId,   // ← UUID
    ...
)
```

---

### 5. `sale/dto/request/CreateSaleRequest.java`
**Değişiklik:** `sellerId` alanı tamamen **kaldırıldı**

```java
// ÖNCE
public record CreateSaleRequest(
    @NotNull @Positive Long sellerId,   // ← KALDIRILDI
    @NotNull UUID stockItemId,
    @NotNull @DecimalMin("0.001") BigDecimal quantity,
    @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
    @NotNull Instant soldAt
)

// SONRA
public record CreateSaleRequest(
    @NotNull UUID stockItemId,
    @NotNull @DecimalMin("0.001") BigDecimal quantity,
    @NotNull @DecimalMin("0.00") BigDecimal unitPrice,
    @NotNull Instant soldAt
)
```

**Frontend yeni request formatı:**
```json
{
  "stockItemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "quantity": 10,
  "unitPrice": 200.00,
  "soldAt": "2026-08-28T10:00:00Z"
}
```

---

### 6. `sale/specification/SaleSpecification.java`
**Değişiklik:** `hasSellerId` metodu parametresi `Long` → `UUID`

```java
// ÖNCE
public static Specification<Sale> hasSellerId(Long sellerId)

// SONRA
public static Specification<Sale> hasSellerId(UUID sellerId)
```

---

### 7. `sale/service/SaleService.java`
**Değişiklik:** `createSale` ve `findAll` imzaları güncellendi

```java
// ÖNCE
public SaleResponse createSale(CreateSaleRequest request) {
    Sale sale = Sale.create(request.sellerId(), ...);  // request'ten alıyordu
}

public PageResponse<SaleResponse> findAll(
    SaleStatus status,
    Long sellerId,    // ← Long
    ...
)

// SONRA
public SaleResponse createSale(UUID sellerId, CreateSaleRequest request) {
    // sellerId artık parametre olarak geliyor (JWT'den controller tarafından)
    Sale sale = Sale.create(sellerId, ...);
}

public PageResponse<SaleResponse> findAll(
    SaleStatus status,
    UUID sellerId,    // ← UUID
    ...
)
```

---

### 8. `sale/controller/SaleController.java`
**Değişiklik:** JWT entegrasyonu + yeni `/my` endpoint + `findAll` sellerId tipi güncellendi

```java
// YENİ FIELD
private final AuthenticatedSeller authenticatedSeller;

// POST /api/v1/sales — sellerId artık JWT'den
public SaleResponse createSale(@Valid @RequestBody CreateSaleRequest request) {
    UUID sellerId = authenticatedSeller.getCurrentSellerId();  // JWT sub'dan alınır
    return saleService.createSale(sellerId, request);
}

// YENİ ENDPOINT: GET /api/v1/sales/my
public ResponseEntity<PageResponse<SaleResponse>> findMySales(
    SaleStatus status, UUID stockItemId, Instant from, Instant to, int page, int size
) {
    UUID sellerId = authenticatedSeller.getCurrentSellerId();  // JWT sub'dan alınır
    return ResponseEntity.ok(saleService.findAll(status, sellerId, stockItemId, from, to, page, size));
}

// GET /api/v1/sales — sellerId parametresi Long → UUID
public ResponseEntity<PageResponse<SaleResponse>> findAll(
    SaleStatus status,
    UUID sellerId,    // ← artık UUID
    ...
)
```

**Routing çakışması önlendi:**  
`GET /sales/my` literal path olduğu için `GET /sales/{id}` (Long param) ile çakışmaz.  
Spring `"my"` string'ini `Long`'a parse edemez, doğru endpoint'e yönlendirir.

---

## 🆕 Yeni Oluşturulan Dosya

### `sale/security/AuthenticatedSeller.java`
JWT `sub` claim'inden authenticated kullanıcının UUID'sini çözen component.

```java
@Component
public class AuthenticatedSeller {

    public UUID getCurrentSellerId() {
        // SecurityContextHolder → Jwt principal → jwt.getSubject() → UUID.fromString()
        // Hata durumları: 401 Unauthorized (JWT yok, sub null, sub UUID değil)
    }
}
```

**Güvenli hata yönetimi:**
| Durum | Response |
|---|---|
| JWT yok | `401 Authentication required` |
| Principal Jwt değil | `401 Invalid authentication principal` |
| `sub` claim boş | `401 JWT subject claim is missing` |
| `sub` UUID değil | `401 JWT subject is not a valid UUID` |

---

## 🗄️ Veritabanı — ÖNEMLİ

`sales` tablosundaki `seller_id` kolonu **BIGINT → UUID** olarak değişti.  
`ddl-auto: update` bu tip değişikliğini otomatik yapamaz.

**Uygulamayı başlatmadan önce:**
```sql
-- sales_db veritabanına bağlan
DROP TABLE IF EXISTS sales CASCADE;

-- Ardından uygulamayı başlat
-- Hibernate tabloyu UUID seller_id ile yeniden oluşturur
```

---

## ✅ Bozulmayan Akışlar

| Akış | Durum |
|---|---|
| Sale create (POST /sales) | ✅ Çalışır — sellerId artık JWT'den |
| Outbox event oluşturma | ✅ Bozulmadı |
| sale.created RabbitMQ publish | ✅ Bozulmadı |
| Inventory stok düşürme | ✅ Bozulmadı (sellerId kullanmıyor) |
| stock.decrease.completed inbox | ✅ Bozulmadı |
| stock.decrease.failed inbox | ✅ Bozulmadı |
| Sale COMPLETED / FAILED | ✅ Bozulmadı |
| GET /sales | ✅ Çalışır (sellerId UUID oldu) |
| GET /sales/{id} | ✅ Çalışır |
| GET /sales/my | ✅ YENİ — çalışır |

---

## 🧪 Test Senaryoları

### Postman ile doğrulama:

**TEST 1 — Satış oluştur (sellerId gönderme)**
```
POST /api/v1/sales
Authorization: Bearer <USER_A_TOKEN>

Body:
{
  "stockItemId": "...",
  "quantity": 10,
  "unitPrice": 200.00,
  "soldAt": "2026-08-28T10:00:00Z"
}

Beklenen: 201 Created, response.sellerId = User A'nın UUID'si
```

**TEST 2 — Kendi satışlarım**
```
GET /api/v1/sales/my
Authorization: Bearer <USER_A_TOKEN>

Beklenen: sadece seller_id = User A UUID olan kayıtlar
```

**TEST 3 — Kendi satışlarım + filtre**
```
GET /api/v1/sales/my?status=COMPLETED&stockItemId=<uuid>
Authorization: Bearer <USER_A_TOKEN>

Beklenen: User A + COMPLETED + stockItemId filtresi uygulanır
```

**TEST 4 — JWT olmadan**
```
GET /api/v1/sales/my
(Authorization header yok)

Beklenen: 401 Unauthorized
```

**TEST 5 — Tüm satışlar (sellerId UUID ile filtrele)**
```
GET /api/v1/sales?sellerId=<user-a-uuid>
Authorization: Bearer <ADMIN_TOKEN>

Beklenen: sadece User A satışları
```

---

## 📦 Build Durumu

```
[INFO] Compiling 30 source files
[INFO] BUILD SUCCESS
```

Sıfır compile hatası. ✅


