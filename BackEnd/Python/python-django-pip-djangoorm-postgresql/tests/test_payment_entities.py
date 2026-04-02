from django.contrib.auth import get_user_model
from django.test import TestCase

from apps.order.models import Order
from apps.payment.models import Payment, PaymentMethod, PaymentStatus


class PaymentEntityTests(TestCase):
    def setUp(self):
        user_model = get_user_model()
        self.user = user_model.objects.create_user(
            email="payment-user@example.com",
            password="pbshop-secret",
            name="Payment User",
            phone="010-2222-3333",
            nickname="payment-user",
        )

    def test_payment_defaults_and_relationships_work(self):
        order = Order.objects.create(
            user=self.user,
            order_number="ORD-20260402-000004",
            total_amount=550000,
            final_amount=550000,
            recipient_name="PB User",
            recipient_phone="010-2222-3333",
            zip_code="12345",
            address="Seoul Street 1",
        )
        payment = Payment.objects.create(
            order=order,
            method=PaymentMethod.CARD,
            amount=550000,
        )

        self.assertEqual(payment.order, order)
        self.assertEqual(payment.status, PaymentStatus.PENDING)
        self.assertEqual(payment.amount, 550000)
