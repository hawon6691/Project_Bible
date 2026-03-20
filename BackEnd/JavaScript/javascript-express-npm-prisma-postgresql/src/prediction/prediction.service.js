import { badRequest, notFound } from "../utils/http-error.js";
import {
  findLatestPricePrediction,
  findPricePredictions,
  findProductById,
  findRecentPriceHistory,
} from "./prediction.repository.js";

function getCurrentPrice(product) {
  return product.lowestPrice ?? product.discountPrice ?? product.price;
}

function normalizeDays(days) {
  if (days === undefined) {
    return 30;
  }

  const parsed = Number(days);
  if (!Number.isInteger(parsed) || parsed < 1 || parsed > 90) {
    throw badRequest("days must be between 1 and 90");
  }

  return parsed;
}

function recommendationReason(recommendation) {
  switch (recommendation) {
    case "BUY_SOON":
      return "추가 하락 가능성이 있어 단기 추이를 지켜본 뒤 구매를 권장합니다.";
    case "BUY_NOW":
      return "상승 추세가 감지되어 빠른 구매가 유리합니다.";
    case "WAIT":
      return "가격 안정 구간이라 조금 더 관찰하는 편이 좋습니다.";
    case "HOLD":
    default:
      return "현재 가격대가 안정적입니다.";
  }
}

function buildFallbackPredictions(currentPrice, trend, days) {
  const count = Math.min(days, 7);
  const step = Math.max(Math.round(currentPrice * 0.01), 1);

  return Array.from({ length: count }, (_, index) => {
    const day = index + 1;
    let predictedPrice = currentPrice;

    if (trend === "FALLING") {
      predictedPrice = Math.max(0, currentPrice - step * day);
    } else if (trend === "RISING") {
      predictedPrice = currentPrice + step * day;
    }

    return {
      date: new Date(Date.now() + day * 24 * 60 * 60 * 1000).toISOString().slice(0, 10),
      predictedPrice,
      confidence: Number(Math.max(0.5, 0.9 - day * 0.04).toFixed(2)),
    };
  });
}

async function requireProduct(productId) {
  const product = await findProductById(productId);
  if (!product) {
    throw notFound("Product not found");
  }
  return product;
}

function toStoredPredictionDto(item) {
  return {
    date: item.predictionDate.toISOString().slice(0, 10),
    predictedPrice: item.predictedPrice,
    confidence: Number(item.confidence),
  };
}

export async function getPriceTrend(productId, query) {
  const days = normalizeDays(query?.days);
  const product = await requireProduct(productId);
  const currentPrice = getCurrentPrice(product);
  const endDate = new Date();
  endDate.setDate(endDate.getDate() + days);
  endDate.setHours(23, 59, 59, 999);

  const storedPredictions = await findPricePredictions(productId, endDate);
  const latestPrediction = storedPredictions.length > 0
    ? storedPredictions.reduce((latest, current) => (
      new Date(current.calculatedAt).getTime() > new Date(latest.calculatedAt).getTime() ? current : latest
    ))
    : await findLatestPricePrediction(productId);

  if (storedPredictions.length > 0 && latestPrediction) {
    return {
      productId: product.id,
      productName: product.name,
      currentPrice,
      predictions: storedPredictions.map(toStoredPredictionDto),
      trend: latestPrediction.trend,
      trendStrength: Number(latestPrediction.trendStrength),
      movingAverage7d: latestPrediction.movingAvg7d,
      movingAverage30d: latestPrediction.movingAvg30d,
      seasonalityNote: latestPrediction.seasonalityNote,
      recommendation: latestPrediction.recommendation,
      recommendationReason: recommendationReason(latestPrediction.recommendation),
      updatedAt: latestPrediction.calculatedAt,
    };
  }

  const recentHistory = await findRecentPriceHistory(productId, 30);

  if (recentHistory.length === 0) {
    return {
      productId: product.id,
      productName: product.name,
      currentPrice,
      predictions: [
        {
          date: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().slice(0, 10),
          predictedPrice: currentPrice,
          confidence: 0.5,
        },
      ],
      trend: "STABLE",
      trendStrength: 0,
      movingAverage7d: currentPrice,
      movingAverage30d: currentPrice,
      seasonalityNote: "예측 데이터가 부족하여 현재 가격 기준으로 반환합니다.",
      recommendation: "HOLD",
      recommendationReason: recommendationReason("HOLD"),
      updatedAt: new Date(),
    };
  }

  const history = [...recentHistory].reverse();
  const prices = history.map((item) => item.averagePrice);
  const firstPrice = prices[0];
  const lastPrice = prices[prices.length - 1];
  const delta = lastPrice - firstPrice;
  const trend = Math.abs(delta) < 1 ? "STABLE" : delta > 0 ? "RISING" : "FALLING";
  const trendStrength = firstPrice > 0 ? Number(Math.min(1, Math.abs(delta) / firstPrice).toFixed(2)) : 0;
  const last7 = history.slice(-Math.min(7, history.length));
  const movingAverage7d = Math.round(last7.reduce((sum, item) => sum + item.averagePrice, 0) / last7.length);
  const movingAverage30d = Math.round(history.reduce((sum, item) => sum + item.averagePrice, 0) / history.length);
  const recommendation = trend === "FALLING" ? "BUY_SOON" : trend === "RISING" ? "BUY_NOW" : "HOLD";

  return {
    productId: product.id,
    productName: product.name,
    currentPrice,
    predictions: buildFallbackPredictions(currentPrice, trend, days),
    trend,
    trendStrength,
    movingAverage7d,
    movingAverage30d,
    seasonalityNote: trend === "FALLING" ? "단기 하락 패턴이 감지되었습니다." : "가격 변동성이 크지 않습니다.",
    recommendation,
    recommendationReason: recommendationReason(recommendation),
    updatedAt: new Date(),
  };
}
