import { buildOpenApiSpec, renderSwaggerHtml } from "./docs.service.js";

export function openApiController(apiPrefix) {
  return (_req, res) => {
    res.status(200).json(buildOpenApiSpec(apiPrefix));
  };
}

export function swaggerRedirectController(_req, res) {
  res.redirect(302, "/docs/swagger-ui/index.html");
}

export function swaggerUiController(_req, res) {
  res.status(200).type("html").send(renderSwaggerHtml());
}
