package com.pbshop.springshop.push;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByUserIdOrderByIdDesc(Long userId);

    Optional<PushSubscription> findByUserIdAndEndpointHash(Long userId, String endpointHash);
}
