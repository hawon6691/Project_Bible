import { success } from "../utils/response.js";
import {
  autoRetryFailed,
  getFailedJobs,
  getQueueStats,
  getSupportedQueues,
  removeJob,
  retryFailedJobs,
  retryJob,
} from "./queue-admin.service.js";

export async function getSupportedQueuesController(_req, res) {
  res.status(200).json(success({ items: getSupportedQueues() }));
}

export async function getQueueStatsController(_req, res) {
  res.status(200).json(success(getQueueStats()));
}

export async function getFailedJobsController(req, res) {
  res.status(200).json(success(getFailedJobs(req.params.queueName, req.query)));
}

export async function retryFailedJobsController(req, res) {
  res.status(200).json(success(retryFailedJobs(req.params.queueName, req.query)));
}

export async function autoRetryFailedController(req, res) {
  res.status(200).json(success(autoRetryFailed(req.query)));
}

export async function retryJobController(req, res) {
  res.status(200).json(success(retryJob(req.params.queueName, req.params.jobId)));
}

export async function removeJobController(req, res) {
  res.status(200).json(success(removeJob(req.params.queueName, req.params.jobId)));
}
