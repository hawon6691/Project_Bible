from datetime import date
from decimal import Decimal

from django.contrib.auth import get_user_model
from django.db import IntegrityError, transaction
from django.test import TestCase
from django.utils import timezone

from apps.catalog.models import Category, Product
from apps.pricing.models import (
    PriceAlert,
    PriceHistory,
    PricePrediction,
    PriceRecommendation,
    PriceTrend,
)


class PriceAnalyticsEntityTests(TestCase):
    def setUp(self):
        self.category = Category.objects.create(name="Monitor")
        self.product = Product.objects.create(
            name="PBShop Monitor",
            description="Price analytics test product.",
            price=320000,
            category=self.category,
        )
        user_model = get_user_model()
        self.user = user_model.objects.create_user(
            email="price-user@example.com",
            password="pbshop-secret",
            name="Price User",
            phone="010-7777-8888",
            nickname="price-user",
        )

    def test_price_history_unique_constraint(self):
        PriceHistory.objects.create(
            product=self.product,
            date=date(2026, 4, 2),
            lowest_price=300000,
            average_price=315000,
            highest_price=330000,
        )

        with self.assertRaises(IntegrityError):
            with transaction.atomic():
                PriceHistory.objects.create(
                    product=self.product,
                    date=date(2026, 4, 2),
                    lowest_price=299000,
                    average_price=314000,
                    highest_price=329000,
                )

    def test_price_history_stores_price_fields(self):
        history = PriceHistory.objects.create(
            product=self.product,
            date=date(2026, 4, 3),
            lowest_price=301000,
            average_price=316000,
            highest_price=331000,
        )

        self.assertEqual(history.product, self.product)
        self.assertEqual(history.lowest_price, 301000)
        self.assertEqual(history.average_price, 316000)
        self.assertEqual(history.highest_price, 331000)

    def test_price_alert_unique_constraint(self):
        PriceAlert.objects.create(
            user=self.user,
            product=self.product,
            target_price=290000,
        )

        with self.assertRaises(IntegrityError):
            with transaction.atomic():
                PriceAlert.objects.create(
                    user=self.user,
                    product=self.product,
                    target_price=280000,
                )

    def test_price_alert_defaults_and_relationships(self):
        alert = PriceAlert.objects.create(
            user=self.user,
            product=self.product,
            target_price=295000,
        )

        self.assertEqual(alert.user, self.user)
        self.assertEqual(alert.product, self.product)
        self.assertFalse(alert.is_triggered)
        self.assertTrue(alert.is_active)

    def test_price_prediction_unique_constraint(self):
        PricePrediction.objects.create(
            product=self.product,
            prediction_date=date(2026, 4, 10),
            predicted_price=298000,
            confidence=Decimal("0.83"),
            trend=PriceTrend.FALLING,
            trend_strength=Decimal("0.61"),
            recommendation=PriceRecommendation.BUY_SOON,
            calculated_at=timezone.now(),
        )

        with self.assertRaises(IntegrityError):
            with transaction.atomic():
                PricePrediction.objects.create(
                    product=self.product,
                    prediction_date=date(2026, 4, 10),
                    predicted_price=297000,
                    confidence=Decimal("0.84"),
                    trend=PriceTrend.FALLING,
                    trend_strength=Decimal("0.62"),
                    recommendation=PriceRecommendation.BUY_NOW,
                    calculated_at=timezone.now(),
                )

    def test_price_prediction_stores_trend_and_metrics(self):
        prediction = PricePrediction.objects.create(
            product=self.product,
            prediction_date=date(2026, 4, 11),
            predicted_price=305000,
            confidence=Decimal("0.77"),
            trend=PriceTrend.STABLE,
            trend_strength=Decimal("0.20"),
            moving_avg_7d=310000,
            moving_avg_30d=325000,
            recommendation=PriceRecommendation.WAIT,
            seasonality_note="Display sale season is ending.",
            calculated_at=timezone.now(),
        )

        self.assertEqual(prediction.product, self.product)
        self.assertEqual(prediction.trend, PriceTrend.STABLE)
        self.assertEqual(prediction.recommendation, PriceRecommendation.WAIT)
        self.assertEqual(prediction.confidence, Decimal("0.77"))
        self.assertEqual(prediction.trend_strength, Decimal("0.20"))
