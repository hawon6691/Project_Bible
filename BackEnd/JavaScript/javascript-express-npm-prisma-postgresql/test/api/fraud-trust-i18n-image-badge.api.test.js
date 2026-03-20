import assert from "node:assert/strict";
import test, { after, before } from "node:test";

import {
  bootTestApp,
  requestJson,
  shutdownTestApp,
  withBearer,
} from "../e2e/_support/harness.js";

let context;

before(async () => {
  context = await bootTestApp();
});

after(async () => {
  await shutdownTestApp(context);
});

test("Fraud and trust APIs return current pricing and trust details", async () => {
  const realPriceResult = await requestJson(
    context.baseUrl,
    "/api/v1/products/1/real-price",
  );
  const trustResult = await requestJson(
    context.baseUrl,
    "/api/v1/sellers/1/trust",
  );

  assert.equal(realPriceResult.status, 200);
  assert.equal(realPriceResult.body.success, true);
  assert.equal(trustResult.status, 200);
  assert.equal(trustResult.body.success, true);
  assert.equal(trustResult.body.data.sellerId, 1);
});

test("Fraud admin alerts API is available to admin", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/fraud/alerts",
    withBearer(context.adminToken),
  );

  assert.equal(result.status, 200);
  assert.equal(result.body.success, true);
  assert.ok(Array.isArray(result.body.data));
});

test("I18n and badge APIs return public data", async () => {
  const exchangeRatesResult = await requestJson(
    context.baseUrl,
    "/api/v1/i18n/exchange-rates",
  );
  const badgesResult = await requestJson(
    context.baseUrl,
    "/api/v1/badges",
  );

  assert.equal(exchangeRatesResult.status, 200);
  assert.equal(exchangeRatesResult.body.success, true);
  assert.ok(Array.isArray(exchangeRatesResult.body.data));
  assert.equal(badgesResult.status, 200);
  assert.equal(badgesResult.body.success, true);
  assert.ok(Array.isArray(badgesResult.body.data));
});

test("Image variants API returns 404 for missing image", async () => {
  const result = await requestJson(
    context.baseUrl,
    "/api/v1/images/999999/variants",
  );

  assert.equal(result.status, 404);
  assert.equal(result.body.success, false);
});
