# Stripe Payment Testing Guide (Sandbox/Test Mode)

This project integrates Stripe using PaymentIntents and a webhook.

Notes
- Do not commit secrets. Keep `application.yml` local-only (it is ignored by git).
- In Stripe test mode, your secret key starts with `sk_test_...` and your webhook signing secret starts with `whsec_...`.

## What Was Implemented

- Create PaymentIntent for an existing order:
  - `POST /api/payments/stripe/payment-intents` (JWT required)
- Receive Stripe webhooks:
  - `POST /api/payments/stripe/webhook` (public; signature verified)
- Persistence:
  - `payments` table stores `order_id`, `payment_intent_id`, `amount`, `currency`, `status`.

Webhook behavior
- `payment_intent.succeeded`:
  - marks `Payment.status = SUCCEEDED`
  - sets `Order.status = PAID`
- `payment_intent.payment_failed`:
  - marks `Payment.status = FAILED`
  - cancels the order (restores stock via `OrderService.updateOrderStatus(..., CANCELED)`)

## Local Config

Your local (ignored) `application.yml` should include:

```yml
stripe:
  secret-key: sk_test_...
  webhook-secret: whsec_...
  currency: aed
```

## End-to-End Test (Recommended): Stripe CLI Forwarding

This is the simplest way to test the webhook locally.

1. Start the app
- Run the Spring Boot app (port is `8081` by default).

2. Start Stripe CLI webhook forwarding
- Forward Stripe events to your local webhook endpoint:
  - Forward to: `http://localhost:8081/api/payments/stripe/webhook`
- Stripe CLI will print a webhook signing secret (`whsec_...`).
  - Put that value into `stripe.webhook-secret` (or restart with the updated value).

3. Create an order
- Use your existing order endpoints to create an order and get an `orderId`.

4. Create a PaymentIntent (your API)
Request:
```http
POST http://localhost:8081/api/payments/stripe/payment-intents
Authorization: Bearer <YOUR_JWT>
Content-Type: application/json

{
  "orderId": "<ORDER_UUID>"
}
```
Response contains:
- `paymentIntentId` (looks like `pi_...`)
- `clientSecret` (used by frontend; also useful for debugging)

5. Trigger a success webhook (Stripe CLI)
- Use Stripe CLI to trigger a payment intent success event.
- Confirm in your DB that:
  - `payments.status` becomes `SUCCEEDED`
  - `orders.status` becomes `PAID`

## Testing Using Postman

You can test the full flow with Postman by:
1) calling your API to create the PaymentIntent
2) calling Stripe’s API from Postman to confirm the PaymentIntent
3) letting Stripe deliver the webhook (via Stripe CLI forwarding)

### A) Get JWT (your API)
1. Register/login with `/api/auth/**` to get a JWT.
2. Use the JWT as:
   - `Authorization: Bearer <JWT>`

### B) Create an order (your API)
Create an order and copy the `orderId`.

### C) Create PaymentIntent (your API)
Same request as above:
- `POST /api/payments/stripe/payment-intents`

Copy the `paymentIntentId` from the response.

### D) Confirm the PaymentIntent (Stripe API via Postman)

Stripe’s REST API uses HTTP Basic Auth:
- Username: your Stripe secret key (`sk_test_...`)
- Password: (empty)

1) Create a PaymentMethod (test card)
```http
POST https://api.stripe.com/v1/payment_methods
Content-Type: application/x-www-form-urlencoded

type=card&
card[number]=4242424242424242&
card[exp_month]=12&
card[exp_year]=2030&
card[cvc]=123
```

Response includes `id` like `pm_...`.

2) Confirm the PaymentIntent
```http
POST https://api.stripe.com/v1/payment_intents/{PAYMENT_INTENT_ID}/confirm
Content-Type: application/x-www-form-urlencoded

payment_method={PAYMENT_METHOD_ID}
```

If Stripe CLI forwarding is running, Stripe will deliver the webhook to:
- `http://localhost:8081/api/payments/stripe/webhook`

Then verify:
- `payments.status = SUCCEEDED`
- `orders.status = PAID`

## Troubleshooting

- 401 on `/api/payments/stripe/payment-intents`:
  - You need `Authorization: Bearer <JWT>`.
- Webhook returns 4xx / signature errors:
  - Ensure `stripe.webhook-secret` matches the source sending the webhook.
  - If using Stripe CLI `listen`, use the CLI-provided `whsec_...`.
- Currency/amount issues:
  - Amount is computed from `Order.subtotal` and converted to minor units (2-decimal currencies like AED are supported).

