const http = require('http');

const port = Number(process.env.PERF_PORT || 3310);

const server = http.createServer((req, res) => {
  if (req.url === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'UP' }));
    return;
  }

  if (req.url.startsWith('/api/v1/products') || req.url.startsWith('/api/v1/rankings') || req.url.startsWith('/api/v1/compare')) {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ success: true, data: { items: [] } }));
    return;
  }

  res.writeHead(404, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ success: false }));
});

server.listen(port, '127.0.0.1', () => {
  console.log(`mock perf server listening on ${port}`);
});
