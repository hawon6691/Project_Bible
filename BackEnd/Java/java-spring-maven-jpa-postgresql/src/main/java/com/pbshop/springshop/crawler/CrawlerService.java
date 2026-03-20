package com.pbshop.springshop.crawler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.crawler.dto.CrawlerDtos.CreateCrawlerJobRequest;
import com.pbshop.springshop.crawler.dto.CrawlerDtos.TriggerCrawlerRequest;
import com.pbshop.springshop.crawler.dto.CrawlerDtos.UpdateCrawlerJobRequest;

@Service
@Transactional
public class CrawlerService {

    private final CrawlerJobRepository crawlerJobRepository;
    private final CrawlerRunRepository crawlerRunRepository;
    private final ObjectMapper objectMapper;

    public CrawlerService(
            CrawlerJobRepository crawlerJobRepository,
            CrawlerRunRepository crawlerRunRepository,
            ObjectMapper objectMapper
    ) {
        this.crawlerJobRepository = crawlerJobRepository;
        this.crawlerRunRepository = crawlerRunRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> jobs(AuthenticatedUserPrincipal principal, String status, int page, int limit) {
        requireAdmin(principal);
        Page<CrawlerJob> result = status == null || status.isBlank()
                ? crawlerJobRepository.findAllByOrderByIdDesc(PageRequest.of(Math.max(page - 1, 0), limit))
                : crawlerJobRepository.findByStatusOrderByIdDesc(status, PageRequest.of(Math.max(page - 1, 0), limit));
        return Map.of("items", result.getContent().stream().map(this::serializeJob).toList(), "pagination", pagination(result));
    }

    public Map<String, Object> createJob(AuthenticatedUserPrincipal principal, CreateCrawlerJobRequest request) {
        requireAdmin(principal);
        CrawlerJob job = new CrawlerJob();
        job.setName(request.name());
        job.setJobType(request.jobType());
        job.setStatus(request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status());
        job.setPayloadJson(writeJson(request.payload()));
        return serializeJob(crawlerJobRepository.save(job));
    }

    public Map<String, Object> updateJob(AuthenticatedUserPrincipal principal, Long id, UpdateCrawlerJobRequest request) {
        requireAdmin(principal);
        CrawlerJob job = crawlerJobRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "크롤러 작업을 찾을 수 없습니다."));
        if (request.name() != null) {
            job.setName(request.name());
        }
        if (request.jobType() != null) {
            job.setJobType(request.jobType());
        }
        if (request.status() != null) {
            job.setStatus(request.status());
        }
        if (request.payload() != null) {
            job.setPayloadJson(writeJson(request.payload()));
        }
        return serializeJob(crawlerJobRepository.save(job));
    }

    public Map<String, Object> deleteJob(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);
        crawlerJobRepository.deleteById(id);
        return Map.of("message", "크롤러 작업이 삭제되었습니다.");
    }

    public Map<String, Object> runJob(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);
        CrawlerJob job = crawlerJobRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "크롤러 작업을 찾을 수 없습니다."));
        CrawlerRun run = new CrawlerRun();
        run.setCrawlerJob(job);
        run.setStatus("QUEUED");
        run.setTriggerType("MANUAL");
        crawlerRunRepository.save(run);
        return Map.of("message", "크롤러 작업이 실행되었습니다.", "runId", run.getId());
    }

    public Map<String, Object> trigger(AuthenticatedUserPrincipal principal, TriggerCrawlerRequest request) {
        requireAdmin(principal);
        CrawlerRun run = new CrawlerRun();
        if (request.jobId() != null) {
            crawlerJobRepository.findById(request.jobId()).ifPresent(run::setCrawlerJob);
        }
        run.setStatus("QUEUED");
        run.setTriggerType(request.targetType() == null || request.targetType().isBlank() ? "MANUAL" : request.targetType());
        crawlerRunRepository.save(run);
        return Map.of("message", "크롤러 트리거가 등록되었습니다.", "runId", run.getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> runs(AuthenticatedUserPrincipal principal, String status, Long jobId, int page, int limit) {
        requireAdmin(principal);
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), limit);
        Page<CrawlerRun> result;
        if (jobId != null && status != null && !status.isBlank()) {
            result = crawlerRunRepository.findByCrawlerJobIdAndStatusOrderByIdDesc(jobId, status, pageable);
        } else if (jobId != null) {
            result = crawlerRunRepository.findByCrawlerJobIdOrderByIdDesc(jobId, pageable);
        } else if (status != null && !status.isBlank()) {
            result = crawlerRunRepository.findByStatusOrderByIdDesc(status, pageable);
        } else {
            result = crawlerRunRepository.findAllByOrderByIdDesc(pageable);
        }
        return Map.of("items", result.getContent().stream().map(this::serializeRun).toList(), "pagination", pagination(result));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> monitoring(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        Long latestRunId = crawlerRunRepository.findAllByOrderByIdDesc(PageRequest.of(0, 1)).stream()
                .findFirst()
                .map(CrawlerRun::getId)
                .orElse(null);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("jobCount", crawlerJobRepository.count());
        payload.put("queuedRunCount", crawlerRunRepository.countByStatus("QUEUED"));
        payload.put("latestRunId", latestRunId);
        return payload;
    }

    private Map<String, Object> serializeJob(CrawlerJob job) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", job.getId());
        payload.put("name", job.getName());
        payload.put("jobType", job.getJobType());
        payload.put("status", job.getStatus());
        payload.put("payload", readJson(job.getPayloadJson()));
        return payload;
    }

    private Map<String, Object> serializeRun(CrawlerRun run) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", run.getId());
        payload.put("jobId", run.getCrawlerJob() == null ? null : run.getCrawlerJob().getId());
        payload.put("status", run.getStatus());
        payload.put("triggerType", run.getTriggerType());
        return payload;
    }

    private Map<String, Object> pagination(Page<?> page) {
        return Map.of(
                "page", page.getNumber() + 1,
                "limit", page.getSize(),
                "total", page.getTotalElements(),
                "totalPages", page.getTotalPages()
        );
    }

    private String writeJson(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            return "{}";
        }
    }

    private Map<String, Object> readJson(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() { });
        } catch (Exception exception) {
            return null;
        }
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }
}
