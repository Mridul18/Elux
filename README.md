# Country-Based Product API

A Kotlin (Ktor) service that manages products and discounts with country-based VAT calculation and concurrency-safe discount application.

## Quick Start

### Prerequisites

- Docker and Docker Compose

### Start the Application

```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080`

**To stop:**
```bash
docker-compose down
```

## API Endpoints

### GET /products?country={country}

Returns all products for the given country with calculated final prices.

**Example:**
```bash
curl "http://localhost:8080/products?country=SWEDEN"
```

### PUT /products/{id}/discount

Applies a discount to a product. The same discount cannot be applied twice (idempotent).

**Example:**
```bash
curl -X PUT "http://localhost:8080/products/laptop-1/discount" \
  -H "Content-Type: application/json" \
  -d '{"discountId": "black-friday", "percent": 15.0}'
```

### POST /products

Creates a new product.

**Example:**
```bash
curl -X POST "http://localhost:8080/products" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "laptop-1",
    "name": "MacBook Pro",
    "basePrice": 2000.0,
    "country": "SWEDEN"
  }'
```

## Example Usage

```bash
# 1. Create a product
curl -X POST "http://localhost:8080/products" \
  -H "Content-Type: application/json" \
  -d '{"id": "laptop-1", "name": "MacBook Pro", "basePrice": 2000.0, "country": "SWEDEN"}'

# 2. Apply discounts
curl -X PUT "http://localhost:8080/products/laptop-1/discount" \
  -H "Content-Type: application/json" \
  -d '{"discountId": "black-friday", "percent": 15.0}'

# 3. Get products with final prices
curl "http://localhost:8080/products?country=SWEDEN"
```

## Price Calculation

```
finalPrice = basePrice × (1 - totalDiscount%) × (1 + VAT%)
```

**Example:** Base price 2000, discounts 25%, VAT 25% → Final price: 1875.0

## Testing

Run the concurrency test to verify discount idempotency:

```bash
./gradlew test --tests ProductConcurrencyTest
```


