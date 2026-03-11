# Payment Flow Documentation

## Overview

The Carbon Credit Marketplace uses an **event-driven microservice architecture** to process payments. Three microservices collaborate via **Apache Kafka** to complete a payment: the **Transaction Service** (orchestrator), the **Wallet Service** (payment processor), and the **Market Service** (listing manager).

Two payment methods are supported:
- **WALLET** — Direct wallet-to-wallet transfer (synchronous request-response over Kafka)
- **VN_PAY** — Redirect-based payment via the VNPay payment gateway (asynchronous callback)

---

## Architecture Diagram

```
┌──────────┐       REST        ┌─────────────────────┐
│  Buyer   │ ───────────────►  │  Transaction Service │
│ (Client) │ ◄───────────────  │     (Port 8087)      │
└──────────┘                   └──────────┬───────────┘
                                          │
                         Kafka            │           Kafka
              ┌───────────────────────────┼──────────────────────────┐
              │                           │                          │
              ▼                           ▼                          ▼
   ┌─────────────────┐    ┌──────────────────────┐    ┌─────────────────────┐
   │  Wallet Service  │    │    VNPay Gateway     │    │   Market Service    │
   │   (Port 8084)    │    │  (External Service)  │    │    (Port 8086)     │
   └─────────────────┘    └──────────────────────┘    └─────────────────────┘
```

---

## Kafka Topics

| Topic | Producer | Consumer | Payload | Purpose |
|---|---|---|---|---|
| `market-payment-request` | Transaction Service | Wallet Service | `PaymentRequest` | Request wallet transfer |
| `market-payment-response` | Wallet Service | Transaction Service | `PaymentResponse` | Return transfer result |
| `market-payment-event` | Transaction Service | Market Service | `String` (listingId) | Notify listing sold |

These topic constants are defined in [`common-dto/.../kafka/KafkaTopics.java`](../common-dto/src/main/java/com/example/commondto/kafka/KafkaTopics.java).

---

## Shared DTOs

### PaymentRequest

Sent from Transaction Service → Wallet Service via Kafka.

```java
// common-dto/.../dto/request/PaymentRequest.java
public class PaymentRequest extends BaseMessageKafka {
    String buyerId;
    String sellerId;
    PaymentMethod method;   // WALLET or VN_PAY
    Double amount;          // VND amount
    Double credit;          // Carbon credits quantity
    // Inherited: String correlationId (from BaseMessageKafka)
}
```

### PaymentResponse

Sent from Wallet Service → Transaction Service via Kafka.

```java
// common-dto/.../dto/response/PaymentResponse.java
public class PaymentResponse extends BaseMessageKafka {
    HttpStatus status;
    boolean success;
    String message;
    // Inherited: String correlationId (from BaseMessageKafka)
}
```

### TransactionStatus

```java
public enum TransactionStatus {
    PENDING_PAYMENT, SUCCESS, FAILED, CANCELED
}
```

---

## Payment Flow — WALLET Method

The wallet payment is a **synchronous request-response** pattern implemented over Kafka using `CompletableFuture` with a 10-second timeout.

### Sequence Diagram

```
 Buyer            Transaction Service          Kafka             Wallet Service          Market Service
   │                      │                      │                      │                      │
   │  POST /api/          │                      │                      │                      │
   │  transactions/{id}/  │                      │                      │                      │
   │  pay?method=WALLET   │                      │                      │                      │
   │─────────────────────►│                      │                      │                      │
   │                      │                      │                      │                      │
   │                      │  1. Build PaymentRequest                    │                      │
   │                      │     (correlationId=UUID)                    │                      │
   │                      │                      │                      │                      │
   │                      │  2. Register CompletableFuture              │                      │
   │                      │     in pendingRequests map                  │                      │
   │                      │                      │                      │                      │
   │                      │  ──── PaymentRequest ────►                  │                      │
   │                      │      (market-payment-request)               │                      │
   │                      │                      │  ──── PaymentRequest ────►                   │
   │                      │                      │                      │                      │
   │                      │                      │      3. Idempotency check                   │
   │                      │                      │      4. Validate buyer balance               │
   │                      │                      │      5. Deduct buyer wallet                  │
   │                      │                      │      6. Credit seller wallet                 │
   │                      │                      │      7. Transfer carbon credits              │
   │                      │                      │      8. Create 4 audit records               │
   │                      │                      │      9. Mark correlationId processed         │
   │                      │                      │                      │                      │
   │                      │                      │  ◄── PaymentResponse ────                   │
   │                      │  ◄── PaymentResponse ────                   │                      │
   │                      │      (market-payment-response)              │                      │
   │                      │                      │                      │                      │
   │                      │  10. Complete future,                       │                      │
   │                      │      set status=SUCCESS                     │                      │
   │                      │                      │                      │                      │
   │                      │  ──── listingId ─────────────────────────────────────────────────►  │
   │                      │      (market-payment-event)                                        │
   │                      │                      │                      │   11. Set listing     │
   │                      │                      │                      │       status=SOLD     │
   │  ◄─────── 200 OK ───│                      │                      │                      │
   │   MarketPaymentResponse                    │                      │                      │
   │   {status: SUCCESS}  │                      │                      │                      │
```

### Step-by-Step

1. **Buyer** sends `POST /api/transactions/{id}/pay?paymentMethod=WALLET`
2. **TransactionServiceImpl.initiatePayment()** sets the payment method on the transaction and calls `WalletIntegration.pay(tx)`
3. **WalletIntegration.pay()** builds a `PaymentRequest` with a unique `correlationId`, registers a `CompletableFuture` in a `ConcurrentHashMap`, and sends the request to Kafka topic `market-payment-request`
4. **WalletIntegration** blocks on `future.get(10, TimeUnit.SECONDS)` waiting for the response
5. **MarketPaymentConsumer** (Wallet Service) receives the `PaymentRequest` and processes it inside a `@Transactional` method:
   - **Idempotency check**: If `correlationId` was already processed, sends a success response and returns
   - **Validation**: Ensures buyer wallet has sufficient balance; checks that seller wallet and carbon credit records exist
   - **Deduct buyer wallet**: `buyerWallet.balance -= amount`
   - **Credit seller wallet**: `sellerWallet.balance += amount`
   - **Transfer carbon credits**: `buyerCredit.totalCredit += credit`
   - **Audit records**: Creates 4 audit entries (buyer WITHDRAW, seller DEPOSIT, buyer CREDIT_BUY, seller CREDIT_TRADE)
   - **Mark processed**: Saves `correlationId` to `ProcessedPayment` table
6. **MarketPaymentConsumer** sends a `PaymentResponse` to Kafka topic `market-payment-response`
7. **WalletConsumer** (Transaction Service) receives the response and calls `WalletIntegration.completeResponse()`, which resolves the `CompletableFuture`
8. **TransactionServiceImpl** sets `status=SUCCESS`, `paidAt=now()`, saves the transaction
9. **TransactionProducer** sends the `listingId` to Kafka topic `market-payment-event`
10. **PaymentEventConsumer** (Market Service) receives the listing ID and updates the listing status to `SOLD`
11. The buyer receives a `MarketPaymentResponse` with `status=SUCCESS`

### Error Handling

| Scenario | Response |
|---|---|
| Buyer wallet balance insufficient | `PaymentResponse(success=false, status=402 PAYMENT_REQUIRED)` |
| Missing wallet or carbon credit record | `PaymentResponse(success=false, status=404 NOT_FOUND)` |
| Kafka response timeout (>10 seconds) | `CustomException(status=504 GATEWAY_TIMEOUT)` |
| Internal processing error | `PaymentResponse(success=false, status=500 INTERNAL_SERVER_ERROR)` + `@Transactional` rollback |

### Key Source Files

| File | Service | Role |
|---|---|---|
| [`TransactionServiceImpl.java`](../transaction-service/src/main/java/com/example/transactionservice/transaction/TransactionServiceImpl.java) | Transaction | Orchestrates the payment flow |
| [`WalletIntegration.java`](../transaction-service/src/main/java/com/example/transactionservice/intergration/WalletIntegration.java) | Transaction | Sends Kafka request, waits for response with CompletableFuture |
| [`WalletProducer.java`](../transaction-service/src/main/java/com/example/transactionservice/kafka/producer/WalletProducer.java) | Transaction | Produces `PaymentRequest` to Kafka |
| [`WalletConsumer.java`](../transaction-service/src/main/java/com/example/transactionservice/kafka/consumer/WalletConsumer.java) | Transaction | Consumes `PaymentResponse` from Kafka |
| [`MarketPaymentConsumer.java`](../wallet-service/src/main/java/com/example/walletservice/kafka/consumer/MarketPaymentConsumer.java) | Wallet | Core payment processor — transfers money and credits |
| [`PaymentEventConsumer.java`](../market-service/src/main/java/com/example/marketservice/kafka/consumer/PaymentEventConsumer.java) | Market | Updates listing status to SOLD |

---

## Payment Flow — VN_PAY Method

VNPay is a redirect-based payment gateway. The buyer is redirected to VNPay's hosted payment page, completes the bank transfer, and VNPay calls back the Transaction Service with the result.

### Sequence Diagram

```
 Buyer            Transaction Service           VNPay Gateway         Wallet Service        Market Service
   │                      │                          │                      │                      │
   │  POST /api/          │                          │                      │                      │
   │  transactions/{id}/  │                          │                      │                      │
   │  pay?method=VN_PAY   │                          │                      │                      │
   │─────────────────────►│                          │                      │                      │
   │                      │                          │                      │                      │
   │                      │  1. Generate payment URL │                      │                      │
   │                      │     (HmacSHA512 signed)  │                      │                      │
   │                      │     15-min expiration     │                      │                      │
   │                      │                          │                      │                      │
   │  ◄── paymentUrl ─────│                          │                      │                      │
   │                      │                          │                      │                      │
   │  ─── Redirect ───────────────────────────────►  │                      │                      │
   │                      │                          │                      │                      │
   │         (Buyer completes bank transfer)         │                      │                      │
   │                      │                          │                      │                      │
   │                      │  ◄── GET /api/payments/  │                      │                      │
   │                      │      vnpay-return?       │                      │                      │
   │                      │      vnp_ResponseCode=00 │                      │                      │
   │                      │      &vnp_SecureHash=... │                      │                      │
   │                      │                          │                      │                      │
   │                      │  2. Verify HmacSHA512    │                      │                      │
   │                      │     signature            │                      │                      │
   │                      │                          │                      │                      │
   │                      │  3. Update transaction   │                      │                      │
   │                      │     status=SUCCESS       │                      │                      │
   │                      │                          │                      │                      │
   │                      │  4. Call WalletIntegration.pay()                │                      │
   │                      │                          │                      │                      │
   │                      │  ──── PaymentRequest ──────────────────────────►│                      │
   │                      │      (market-payment-request)                   │                      │
   │                      │                          │                      │                      │
   │                      │                          │      5. Credit seller│                      │
   │                      │                          │         wallet       │                      │
   │                      │                          │      6. Transfer     │                      │
   │                      │                          │         carbon creds │                      │
   │                      │                          │      7. Audit        │                      │
   │                      │                          │                      │                      │
   │                      │  ──── listingId ──────────────────────────────────────────────────────►│
   │                      │      (market-payment-event)                     │                      │
   │                      │                          │                      │       8. Set listing  │
   │                      │                          │                      │          status=SOLD  │
   │                      │                          │                      │                      │
   │  ◄── Redirect ───────│                          │                      │                      │
   │      to frontend     │                          │                      │                      │
   │      /buyer/payment/ │                          │                      │                      │
   │      callback?       │                          │                      │                      │
   │      status=success  │                          │                      │                      │
```

### Step-by-Step

1. **Buyer** sends `POST /api/transactions/{id}/pay?paymentMethod=VN_PAY`
2. **TransactionServiceImpl.initiatePayment()** calls `VNPayService.pay(tx, clientIp)` which generates a signed payment URL:
   - Builds VNPay parameters: `vnp_Amount` (amount × 100), `vnp_TxnRef` (transactionId), `vnp_CreateDate`, `vnp_ExpireDate` (15 min), etc.
   - Sorts parameters alphabetically
   - Creates `hashData` string from sorted key-value pairs
   - Signs with **HmacSHA512** using the VNPay secret key
   - Returns the complete payment URL
3. Transaction status remains `PENDING_PAYMENT` with the `paymentUrl` stored
4. **Buyer** is redirected to VNPay's payment page and completes the bank transfer
5. **VNPay** redirects to `GET /api/payments/vnpay-return` with callback parameters including `vnp_SecureHash`
6. **PaymentController.handleVNPayReturn()** processes the callback:
   - Extracts `vnp_SecureHash` from the callback params
   - Recalculates the hash from the remaining parameters
   - **Verifies the signature** — rejects if mismatch
   - Checks `vnp_ResponseCode`:
     - `"00"` → Payment successful
     - Other → Payment failed (maps to specific error messages)
7. On success (`"00"`):
   - Calls `TransactionServiceImpl.update(id, SUCCESS)` which triggers `WalletIntegration.pay()` (same Kafka-based flow as wallet payment, but **without deducting from buyer wallet** since payment was via bank)
   - Wallet Service credits the seller's wallet and transfers carbon credits to the buyer
   - Transaction Service sends `market-payment-event` → Market Service marks listing as `SOLD`
8. On failure:
   - Calls `TransactionServiceImpl.update(id, FAILED)`
9. **Redirects** the buyer to the frontend callback URL: `http://localhost:5173/buyer/payment/callback?status=success|failed&...`

### VNPay Error Codes

| Code | Description |
|---|---|
| `00` | Success |
| `07` | Suspected fraud |
| `09` | Card/Account not registered for InternetBanking |
| `10` | Authentication failed 3+ times |
| `11` | Payment timeout |
| `12` | Card/Account locked |
| `13` | Wrong OTP |
| `24` | Customer cancelled |
| `51` | Insufficient balance |
| `65` | Exceeded daily transaction limit |
| `75` | Bank under maintenance |
| `79` | Wrong payment password too many times |

### Key Difference from WALLET Method

In the VNPay flow, the buyer's **bank account** is debited by VNPay (not the in-app wallet). The Wallet Service still processes the seller's credit and carbon transfer, but **skips the buyer wallet deduction** since `PaymentMethod` is `VN_PAY`:

```java
// MarketPaymentConsumer.java — only deducts buyer wallet for WALLET method
if (request.getMethod() == PaymentMethod.WALLET) {
    buyerWallet.setBalance(buyerWallet.getBalance() - request.getAmount());
    // ... audit
}
// Seller wallet credit and carbon transfer happen for BOTH methods
```

### Key Source Files

| File | Service | Role |
|---|---|---|
| [`VNPayService.java`](../transaction-service/src/main/java/com/example/transactionservice/vnpay/VNPayService.java) | Transaction | Generates signed VNPay payment URL |
| [`VNPayConfig.java`](../transaction-service/src/main/java/com/example/transactionservice/vnpay/VNPayConfig.java) | Transaction | VNPay credentials and configuration |
| [`PaymentController.java`](../transaction-service/src/main/java/com/example/transactionservice/payment/PaymentController.java) | Transaction | Handles VNPay callback, verifies signature, redirects to frontend |

---

## Transaction Lifecycle

```
                        ┌───────────────────┐
                        │  PENDING_PAYMENT  │
                        └─────────┬─────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │                           │
              WALLET pay                   VN_PAY pay
              succeeds                     URL generated
                    │                           │
                    │                   User redirected
                    │                    to VNPay...
                    │                           │
                    │                ┌──────────┼──────────┐
                    │                │                     │
                    │          Callback "00"          Callback != "00"
                    │           (success)              (failed/cancel)
                    │                │                     │
                    ▼                ▼                     ▼
              ┌───────────┐   ┌───────────┐         ┌──────────┐
              │  SUCCESS  │   │  SUCCESS  │         │  FAILED  │
              └───────────┘   └───────────┘         └──────────┘
```

### Transaction Entity

```java
@Entity @Table(name = "transactions")
public class Transaction extends BaseEntity {
    @Id String id;                         // UUID
    String listingId;                      // Market listing reference
    String buyerId;                        // Buyer user ID
    String sellerId;                       // Seller user ID
    Double amount;                         // VND amount
    Double credit;                         // Carbon credits quantity
    TransactionStatus status;              // PENDING_PAYMENT, SUCCESS, FAILED, CANCELED
    PaymentMethod paymentMethod;           // WALLET or VN_PAY
    String paymentUrl;                     // VNPay URL (VN_PAY only)
    LocalDateTime paidAt;                  // When payment completed
}
```

---

## Architectural Patterns

### 1. Request-Response over Kafka (Correlation ID Pattern)

The wallet payment uses a **synchronous request-response** pattern over asynchronous Kafka messaging:

```java
// WalletIntegration.java (Transaction Service)
ConcurrentHashMap<String, CompletableFuture<PaymentResponse>> pendingRequests;

public MarketPaymentResponse pay(Transaction tx) {
    PaymentRequest request = PaymentRequest.builder()...build();

    CompletableFuture<PaymentResponse> future = new CompletableFuture<>();
    pendingRequests.put(request.getCorrelationId(), future);

    walletProducer.sendPaymentEvent(request);                    // Send to Kafka
    PaymentResponse response = future.get(10, TimeUnit.SECONDS); // Wait for response
    ...
}

public void completeResponse(PaymentResponse response) {        // Called by WalletConsumer
    CompletableFuture<PaymentResponse> future = pendingRequests.remove(response.getCorrelationId());
    if (future != null) future.complete(response);
}
```

### 2. Idempotency (Processed Payment Table)

Duplicate Kafka messages are handled via a `ProcessedPayment` table keyed on `correlationId`:

```java
// MarketPaymentConsumer.java (Wallet Service)
if (processedPaymentService.isProcessed(request.getCorrelationId())) {
    // Return success without re-processing
    walletProducer.sendMarketPaymentResponse(successResponse);
    return;
}
// ... process payment ...
processedPaymentService.markAsProcessed(request.getCorrelationId());
```

### 3. Transactional Consistency

The `MarketPaymentConsumer.consumeMarketPaymentRequest()` method is annotated with `@Transactional`. If any step fails, all database changes (wallet balances, carbon credits, audit records) are rolled back automatically.

### 4. Audit Trail

Every payment produces **4 audit records**:

| # | User | Type | Action | Description |
|---|---|---|---|---|
| 1 | Buyer | WALLET | WITHDRAW | Buyer wallet debited (WALLET method only) |
| 2 | Seller | WALLET | DEPOSIT | Seller wallet credited |
| 3 | Buyer | CARBON_CREDIT | CREDIT_BUY | Buyer received carbon credits |
| 4 | Seller | CARBON_CREDIT | CREDIT_TRADE | Seller's credits traded |

Each audit record includes the `correlationId` for traceability.

---

## End-to-End Example

### Buyer purchases carbon credits using WALLET

1. Seller lists 10 carbon credits at 50,000 VND each → Market Service creates listing `L001`
2. Buyer clicks "Buy" → Transaction Service creates transaction `T001` with `status=PENDING_PAYMENT`, `amount=500000`, `credit=10`
3. Buyer selects WALLET payment → `POST /api/transactions/T001/pay?paymentMethod=WALLET`
4. Transaction Service sends `PaymentRequest(buyerId=B1, sellerId=S1, amount=500000, credit=10, correlationId=abc-123)` to Kafka
5. Wallet Service receives and processes:
   - B1 wallet: 1,000,000 → 500,000 VND
   - S1 wallet: 200,000 → 700,000 VND
   - B1 carbon: 0 → 10 credits
   - Creates 4 audit records
6. Wallet Service sends `PaymentResponse(success=true, correlationId=abc-123)` to Kafka
7. Transaction Service receives response, sets `T001.status=SUCCESS`, `T001.paidAt=now`
8. Transaction Service sends `L001` to `market-payment-event` Kafka topic
9. Market Service sets listing `L001` status to `SOLD`
10. Buyer receives `{ transactionId: "T001", status: "SUCCESS" }`
