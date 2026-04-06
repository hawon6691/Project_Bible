from django.test import SimpleTestCase


class DocsTests(SimpleTestCase):
    def test_openapi_endpoint_returns_document(self):
        response = self.client.get("/docs/openapi")

        self.assertEqual(response.status_code, 200)
        payload = response.json()
        self.assertEqual(payload["openapi"], "3.1.0")
        self.assertEqual(payload["info"]["title"], "PBShop Python Django ORM API")
        self.assertIn("/api/v1/auth/login", payload["paths"])
        self.assertIn("/api/v1/categories", payload["paths"])
        self.assertIn("/api/v1/products", payload["paths"])
        self.assertIn("/api/v1/products/{product_id}/specs", payload["paths"])
        self.assertIn("/api/v1/specs/definitions", payload["paths"])
        self.assertIn("/api/v1/specs/compare", payload["paths"])
        self.assertIn("/api/v1/users/me", payload["paths"])
        self.assertIn("bearerAuth", payload["components"]["securitySchemes"])
        self.assertIn("CategoryTreeNode", payload["components"]["schemas"])
        self.assertIn("ProductDetail", payload["components"]["schemas"])
        self.assertIn("SpecDefinition", payload["components"]["schemas"])
        self.assertIn("CompareSpecsResponse", payload["components"]["schemas"])
        self.assertIn("ApiErrorEnvelope", payload["components"]["schemas"])

    def test_swagger_redirect_and_ui(self):
        redirect = self.client.get("/docs/swagger")

        self.assertEqual(redirect.status_code, 302)
        self.assertEqual(redirect["Location"], "/docs/swagger-ui/index.html")

        ui = self.client.get("/docs/swagger-ui/index.html")
        self.assertEqual(ui.status_code, 200)
        html = ui.content.decode("utf-8")
        self.assertIn("/docs/openapi", html)
        self.assertIn("SwaggerUIBundle", html)
