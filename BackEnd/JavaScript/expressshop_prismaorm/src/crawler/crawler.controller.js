import { success } from "../utils/response.js";
import {
  createAdminCrawlerJob,
  deleteAdminCrawlerJob,
  getCrawlerJobs,
  getCrawlerMonitoring,
  getCrawlerRuns,
  runCrawlerJob,
  triggerCrawler,
  updateAdminCrawlerJob,
} from "./crawler.service.js";

export async function getCrawlerJobsController(req, res) {
  const { items, meta } = await getCrawlerJobs(req.query);
  res.status(200).json(success(items, meta));
}

export async function createCrawlerJobController(req, res) {
  const data = await createAdminCrawlerJob(req.body);
  res.status(201).json(success(data));
}

export async function updateCrawlerJobController(req, res) {
  const data = await updateAdminCrawlerJob(req.params.id, req.body);
  res.status(200).json(success(data));
}

export async function deleteCrawlerJobController(req, res) {
  const data = await deleteAdminCrawlerJob(req.params.id);
  res.status(200).json(success(data));
}

export async function runCrawlerJobController(req, res) {
  const data = await runCrawlerJob(req.params.id);
  res.status(200).json(success(data));
}

export async function triggerCrawlerController(req, res) {
  const data = await triggerCrawler(req.body);
  res.status(200).json(success(data));
}

export async function getCrawlerRunsController(req, res) {
  const { items, meta } = await getCrawlerRuns(req.query);
  res.status(200).json(success(items, meta));
}

export async function getCrawlerMonitoringController(_req, res) {
  const data = await getCrawlerMonitoring();
  res.status(200).json(success(data));
}
