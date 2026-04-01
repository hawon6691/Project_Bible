from django.test import SimpleTestCase


class BootstrapSmokeTests(SimpleTestCase):
    def test_health_endpoint(self):
        response = self.client.get("/health")

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["success"], True)
        self.assertEqual(response.json()["data"]["status"], "ok")

    def test_api_root_endpoint(self):
        response = self.client.get("/api/v1/")

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()["success"], True)
        self.assertEqual(response.json()["data"]["version"], "bootstrap")
