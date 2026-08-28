# 📋 Endpoint Yol Haritası — stockTrackerMicroservicesMQ

> Tarih: 2026-08-25  
> Durum: Mevcut altyapı analiz edildi, eksik ve önerilen endpoint'ler listelendi.

---

## 🗂️ Mevcut Altyapı Özeti

### Tablolar ve Veriler

| Servis | Tablo | Alan Özeti |
|---|---|---|
| **identity-service** | `users` | id, first_name, last_name, email, password_hash, enabled, account_locked, created_at, updated_at |
| **identity-service** | `roles` | id, name (ADMIN / STOCK_MANAGER / PRODUCTION_USER / SALES_USER) |
| **identity-service** | `user_roles` | user_id, role_id |
| **identity-service** | `refresh_tokens` | id, user_id, token_hash, expires_at, revoked, created_at, replaced_by_token_id |
| **inventory-service** | `stock_items` | id (UUID), name, sku, description, item_type, unit, active, created_at, updated_at |
| **inventory-service** | `stock_balances` | id, stock_item_id, on_hand_quantity, reserved_quantity, version, updated_at |
| **inventory-service** | `stock_movements` | id, source_event_id, stock_item_id, movement_type, quantity_change, previous_on_hand_quantity, new_on_hand_quantity, reference_type, reference_id, source_occurred_at, created_at |
| **purchase-service** | `purchases` | id (Long), stock_item_id, quantity, unit_price, supplier_name, status, purchased_at, failure_reason, created_at, updated_at, version |
| **sales-service** | `sales` | id (Long), seller_id, stock_item_id, quantity, unit_price, total_price, status, failure_reason, sold_at, created_at, updated_at |

### Enum'lar

| Enum | Değerler |
|---|---|
| `StockItemType` | ESSENCE, FINISHED_PRODUCT, PACKAGING |
| `StockUnit` | GRAM, MILLILITER, PIECE |
| `StockMovementType` | PURCHASE_RECEIPT, SALE |
| `PurchaseStatus` | PENDING_STOCK_UPDATE, COMPLETED, FAILED, CANCELLED |
| `SaleStatus` | PENDING_STOCK_UPDATE, COMPLETED, FAILED |
| `RoleName` | ADMIN, STOCK_MANAGER, PRODUCTION_USER, SALES_USER |

### Mevcut Endpoint'ler (Şu An Çalışıyor)

| Servis | Metod | URL | Açıklama |
|---|---|---|---|
| identity | POST | `/api/v1/auth/register` | Kayıt |
| identity | POST | `/api/v1/auth/login` | Giriş |
| identity | POST | `/api/v1/auth/refresh-token` | Token yenileme |
| identity | POST | `/api/v1/auth/logout` | Çıkış |
| identity | GET | `/api/v1/users/me` | Kendi bilgileri |
| identity | GET | `/api/v1/admin/users` | Tüm kullanıcılar (sayfalı) |
| identity | GET | `/api/v1/admin/users/{userId}` | Kullanıcı detayı |
| identity | PATCH | `/api/v1/admin/users/{userId}/status` | Aktif/pasif |
| identity | PUT | `/api/v1/admin/users/{userId}/roles` | Rol atama |
| inventory | POST | `/api/v1/stock-items` | Stok kalemi oluştur |
| inventory | GET | `/api/v1/stock-items` | Tüm stok kalemleri |
| inventory | GET | `/api/v1/stock-items/{id}` | Stok kalemi detayı |
| purchase | POST | `/api/v1/purchases` | Satın alma oluştur |
| purchase | GET | `/api/v1/purchases/{id}` | Satın alma detayı |
| sales | POST | `/api/v1/sales` | Satış oluştur |

---

## 🚀 Önerilen Yeni Endpoint'ler

### Öncelik Sırası
- 🔴 **P1 — Kritik** (frontend olmadan çalışmaz)
- 🟡 **P2 — Önemli** (dashboard için gerekli)
- 🟢 **P3 — Nice-to-have** (raporlama, gelişmiş özellikler)

---

## 🟦 INVENTORY-SERVICE — Yeni Endpoint'ler

### P1 — Kritik

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `PATCH` | `/api/v1/stock-items/{id}` | STOCK_MANAGER, ADMIN | Stok kalemi güncelle (name, description, active) |
| `PATCH` | `/api/v1/stock-items/{id}/status` | STOCK_MANAGER, ADMIN | Aktif/pasif yap |
| `GET` | `/api/v1/stock-items?active=true&type=ESSENCE` | Hepsi | Filtrelemeli liste (type, active, unit) |
| `GET` | `/api/v1/stock-balances` | STOCK_MANAGER, ADMIN, PRODUCTION_USER | Tüm stok bakiyeleri listesi |
| `GET` | `/api/v1/stock-balances/low-stock?threshold=10` | STOCK_MANAGER, ADMIN | Kritik seviye altındaki stoklar |

### P2 — Önemli

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `GET` | `/api/v1/stock-movements` | STOCK_MANAGER, ADMIN | Tüm hareketler (sayfalı, filtrelenebilir) |
| `GET` | `/api/v1/stock-movements/{id}` | STOCK_MANAGER, ADMIN | Hareket detayı |
| `GET` | `/api/v1/stock-items/{id}/movements` | STOCK_MANAGER, ADMIN | Bir kalemin hareket geçmişi |
| `GET` | `/api/v1/stock-movements?type=PURCHASE_RECEIPT&from=2026-01-01&to=2026-12-31` | STOCK_MANAGER, ADMIN | Tarih/tür bazlı filtre |

### P3 — Nice-to-have

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `GET` | `/api/v1/stock-items/search?q=rose` | Hepsi | İsim/SKU arama |
| `GET` | `/api/v1/stock-items/by-sku/{sku}` | Hepsi | SKU ile getir |
| `GET` | `/api/v1/stock-summary` | ADMIN, STOCK_MANAGER | Özet: toplam kalem, toplam bakiye, kritik stok sayısı |

---

## 🟧 PURCHASE-SERVICE — Yeni Endpoint'ler

### P1 — Kritik

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `GET` | `/api/v1/purchases` | STOCK_MANAGER, ADMIN | Tüm satın almalar (sayfalı) |
| `GET` | `/api/v1/purchases?status=PENDING_STOCK_UPDATE` | STOCK_MANAGER, ADMIN | Duruma göre filtrele |
| `GET` | `/api/v1/purchases?stockItemId={uuid}` | STOCK_MANAGER, ADMIN | Kaleme göre filtrele |
| `POST` | `/api/v1/purchases/{id}/cancel` | STOCK_MANAGER, ADMIN | İptal et (sadece PENDING durumunda) |

### P2 — Önemli

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `GET` | `/api/v1/purchases?from=2026-01-01&to=2026-12-31` | STOCK_MANAGER, ADMIN | Tarih aralığı filtresi |
| `GET` | `/api/v1/purchases/summary` | ADMIN | Özet: toplam tutar, tamamlanan/bekleyen sayısı |
| `GET` | `/api/v1/purchases?supplierName=ABC` | STOCK_MANAGER, ADMIN | Tedarikçiye göre filtrele |

### P3 — Nice-to-have

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `GET` | `/api/v1/purchases/suppliers` | STOCK_MANAGER, ADMIN | Unique tedarikçi listesi |
| `GET` | `/api/v1/purchases/export?format=csv` | ADMIN | CSV dışa aktarım |

---

## 🟩 SALES-SERVICE — Yeni Endpoint'ler

### P1 — Kritik

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `GET` | `/api/v1/sales` | SALES_USER, ADMIN | Kendi satışlarını görür / ADMIN hepsini görür |
| `GET` | `/api/v1/sales/{id}` | SALES_USER, ADMIN | Satış detayı |
| `GET` | `/api/v1/sales?status=COMPLETED` | SALES_USER, ADMIN | Duruma göre filtrele |
| `GET` | `/api/v1/sales?sellerId={id}` | ADMIN | Satıcıya göre filtrele |

### P2 — Önemli

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `GET` | `/api/v1/sales?from=2026-01-01&to=2026-12-31` | SALES_USER, ADMIN | Tarih aralığı filtresi |
| `GET` | `/api/v1/sales/my` | SALES_USER | JWT'den seller_id çekip kendi satışlarını getir |
| `GET` | `/api/v1/sales/summary` | ADMIN, SALES_USER | Özet: toplam gelir, satış adedi, ortalama sepet |
| `POST` | `/api/v1/sales/{id}/cancel` | SALES_USER, ADMIN | Satış iptali (sadece PENDING durumunda) |

### P3 — Nice-to-have

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `GET` | `/api/v1/sales/top-items?limit=10` | ADMIN | En çok satan ürünler |
| `GET` | `/api/v1/sales/export?format=csv` | ADMIN | CSV dışa aktarım |

---

## 🟪 IDENTITY-SERVICE — Yeni Endpoint'ler

### P1 — Kritik

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `PUT` | `/api/v1/users/me/password` | Hepsi (authenticated) | Şifre değiştir |
| `PUT` | `/api/v1/users/me/profile` | Hepsi (authenticated) | Ad/soyad güncelle |

### P2 — Önemli

| Metod | URL | Rol | Açıklama |
|---|---|---|---|
| `GET` | `/api/v1/admin/users?role=SALES_USER` | ADMIN | Role göre kullanıcı filtrele |
| `GET` | `/api/v1/admin/users?enabled=true` | ADMIN | Aktif/pasif filtresi |
| `DELETE` | `/api/v1/admin/users/{userId}` | ADMIN | Soft delete (enabled=false) |

---

## 📊 Frontend Dashboard İçin Özet Endpoint'ler (Cross-Service)

> Bu endpoint'ler tek bir serviste aggregation yapılarak oluşturulabilir ya da API Gateway eklenince orada birleştirilebilir.

| Servis | URL | Açıklama |
|---|---|---|
| inventory | `GET /api/v1/stock-summary` | Toplam stok kalemi, kritik stok sayısı |
| purchase | `GET /api/v1/purchases/summary` | Toplam alım tutarı, bekleyen alımlar |
| sales | `GET /api/v1/sales/summary` | Toplam gelir, günlük/aylık satış |

---

## 🔐 Rol Yetki Matrisi

| Endpoint Grubu | ADMIN | STOCK_MANAGER | PRODUCTION_USER | SALES_USER |
|---|---|---|---|---|
| Auth (register/login) | ✅ | ✅ | ✅ | ✅ |
| `/users/me` | ✅ | ✅ | ✅ | ✅ |
| `/admin/**` | ✅ | ❌ | ❌ | ❌ |
| Stok kalemi oluştur/güncelle | ✅ | ✅ | ❌ | ❌ |
| Stok kalemi listele | ✅ | ✅ | ✅ | ✅ |
| Stok hareketleri | ✅ | ✅ | ✅ | ❌ |
| Satın alma oluştur | ✅ | ✅ | ❌ | ❌ |
| Satın alma listele | ✅ | ✅ | ❌ | ❌ |
| Satış oluştur | ✅ | ❌ | ❌ | ✅ |
| Satış listele (kendi) | ✅ | ❌ | ❌ | ✅ |
| Satış listele (hepsi) | ✅ | ❌ | ❌ | ❌ |
| Özet/Dashboard | ✅ | ✅ | ❌ | ✅ (sadece satış) |

---

## 🛠️ Öneri: Uygulama Sırası

### Sprint 1 — P1 endpoint'ler (Frontend için şart)
1. `GET /api/v1/purchases` — filtreli liste
2. `GET /api/v1/sales` — filtreli liste
3. `GET /api/v1/sales/{id}` — detay
4. `GET /api/v1/stock-items` — filtreli (zaten var, filtre ekle)
5. `PATCH /api/v1/stock-items/{id}/status` — aktif/pasif
6. `GET /api/v1/stock-balances` — bakiye listesi

### Sprint 2 — P2 endpoint'ler (Dashboard)
1. `GET /api/v1/stock-movements` — hareket geçmişi
2. `GET /api/v1/sales/my` — satıcının kendi satışları
3. `GET /api/v1/purchases/summary` — özet
4. `GET /api/v1/sales/summary` — özet
5. `PUT /api/v1/users/me/password` — şifre değiştir

### Sprint 3 — P3 endpoint'ler (Gelişmiş)
1. Export, arama, tedarikçi listesi
2. Top ürünler raporu

---

## 💬 Swagger Notları

Mevcut `inventory-service`'de Swagger (`springdoc-openapi`) zaten kullanılıyor.

`purchase-service` ve `sales-service`'e aşağıdaki dependency eklenmeli:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.9</version>
</dependency>
```

Swagger UI adresleri:
- inventory: `http://localhost:8081/swagger-ui.html`
- purchase:  `http://localhost:8082/swagger-ui.html`
- sales:     `http://localhost:8083/swagger-ui.html`
- identity:  `http://localhost:8080/swagger-ui.html`

---

> **Not:** Bu dosya yol haritasıdır. Her endpoint eklendikçe "✅ Tamamlandı" olarak güncellenebilir.

