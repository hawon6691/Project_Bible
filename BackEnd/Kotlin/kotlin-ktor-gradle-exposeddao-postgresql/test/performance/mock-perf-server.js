const http = require("http");

const server = http.createServer((_, res) => {
  res.writeHead(200, { "Content-Type": "application/json" });
  res.end(JSON.stringify({ status: "ok", service: "pbshop-kotlin-perf-mock" }));
});

const port = Number(process.env.PORT || 19090);
server.listen(port, () => {
  console.log(`PBShop perf mock server listening on ${port}`);
});
