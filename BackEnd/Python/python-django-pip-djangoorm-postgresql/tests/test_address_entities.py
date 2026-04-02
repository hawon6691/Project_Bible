from django.contrib.auth import get_user_model
from django.test import TestCase

from apps.address.models import Address


class AddressEntityTests(TestCase):
    def setUp(self):
        user_model = get_user_model()
        self.user = user_model.objects.create_user(
            email="address-user@example.com",
            password="pbshop-secret",
            name="Address User",
            phone="010-2222-3333",
            nickname="address-user",
        )

    def test_address_belongs_to_user(self):
        address = Address.objects.create(
            user=self.user,
            label="Home",
            recipient_name="PB User",
            phone="010-2222-3333",
            zip_code="12345",
            address="Seoul Street 1",
            address_detail="101",
            is_default=True,
        )

        self.assertEqual(address.user, self.user)
        self.assertTrue(address.is_default)

