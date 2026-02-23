export const QUEUE_NAMES = {
  ACTIVITY_LOG: 'activity-log',
  VIDEO_TRANSCODE: 'video-transcode',
  CRAWLER_COLLECT: 'crawler-collect',
  SEARCH_INDEX_SYNC: 'search-index-sync',
} as const;

export const MANAGED_QUEUE_NAMES = [
  QUEUE_NAMES.ACTIVITY_LOG,
  QUEUE_NAMES.VIDEO_TRANSCODE,
  QUEUE_NAMES.CRAWLER_COLLECT,
  QUEUE_NAMES.SEARCH_INDEX_SYNC,
] as const;

export type ManagedQueueName = (typeof MANAGED_QUEUE_NAMES)[number];
